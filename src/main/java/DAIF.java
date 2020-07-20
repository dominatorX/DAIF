import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class DAIF {
    public static void main(String[] args) throws IOException {
        String mode = "DP";
        final int work_num;
        if (args.length>1){
             work_num = Integer.parseInt(args[1]);
        }else {
            work_num = 3000;
        }
        final double detour_factor;
        if (args.length>2){
            detour_factor = Double.parseDouble(args[2]);
        }else {
            detour_factor = 1.3;
        }
        final int work_cap;
        if (args.length>4){
            work_cap = Integer.parseInt(args[3]);
        }else {
            work_cap = 3;
        }
        final int penalty_weight = 30;

        String save;
        String graph_file = "./NYC/ny";
        String data_file = "./NYC/ny";
        String demand_file = data_file+"_DNM02.json";
        ArrayList<String> date_list = new ArrayList<>();
        date_list.add("02");

        ShortestPathLRU SPC = new ShortestPathLRU();
        SPC.init(graph_file, data_file);
        GridPrune Grid = new GridPrune();
        Grid.init(graph_file);

        Gson gson = new Gson();
        Random rand = new Random();
        InputStreamReader in = new InputStreamReader(new FileInputStream(graph_file+"_regions_j.json"));
        HashSet<Integer> regions  = gson.fromJson(in,
                new TypeToken<HashSet<Integer>>(){ }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(graph_file+"_locations_j.json"));
        ArrayList<Double[]> locations  = gson.fromJson(in,
                new TypeToken<ArrayList<Double[]>>(){ }.getType());
        in.close();

        if(args.length>0){
            mode = args[0];
        }

        final int len_time_span = 900;  // 15 minute per span
        int num_time_span = 6;  // check 1.5 hour
        ArrayList<Integer> time_spans = new ArrayList<>();
        int num;
        for (num=0;num<num_time_span;num++) {
            time_spans.add(len_time_span * num);
        }

        int counter = 0;
        while (counter < date_list.size()) {
            in = new InputStreamReader(new FileInputStream(data_file+"_Task" + date_list.get(counter) + ".json"));
            ArrayList<int[]> worker_location = gson.fromJson(in,
                    new TypeToken<ArrayList<int[]>>() {
                    }.getType());
            in.close();

            ArrayList<Route> routes = new ArrayList<>(work_num);
            int work_idx;
            int start = rand.nextInt(worker_location.size()-work_num);
            for (work_idx = start; work_idx < start+work_num; work_idx++) {
                Route temp = new Route();
                ArrayList<int[]> temp_route = new ArrayList<>();
                int[] temp_loc = {worker_location.get(work_idx)[1], -10, -10, 0, 6666, 0};
                temp_route.add(temp_loc);
                temp.init(-10, work_cap, temp_route);

                routes.add(temp);
            }
            System.out.println("we have" + routes.size() + "routes");
            DemandNumberMap DNM = new DemandNumberMap();
            DNM.init(demand_file, regions, time_spans, date_list);
            SupplyNumberMap SNM = new SupplyNumberMap();
            SNM.init(graph_file, routes, time_spans, DNM.DN_m);
            Insertion insertor = new Insertion();

            long time_all = 0;
            long penalty = 0;
            int time_idx = 0;
            int test_num = 500000;
            int current_num = 0;
            int served = 0;
            //int num_can = 0;
            //int num_check = 0;

            in = new InputStreamReader(new FileInputStream(data_file+"_Task" + date_list.get(counter) + ".json"));
            ArrayList<int[]> request_list = gson.fromJson(in,
                    new TypeToken<ArrayList<int[]>>() {
                    }.getType());
            in.close();
            for (int[] request_info : request_list) {
                int dist = SPC.dis(request_info[1], request_info[2]);
                if (dist == -1) {
                    continue;
                }
                Request request = new Request();
                request.init(request_info[1], request_info[2], request_info[0], (int) (request_info[0] + dist * detour_factor),
                        penalty_weight * dist, request_info[3]);
                int time_left = request.td - dist;
                if (current_num == test_num) {
                    break;
                }
                if (current_num % 15000 == 0) {
                    save = current_num + " arrived, served " + served + ", penalty is " + penalty+"\n";
                    System.out.print(save);
                }
                if (request.tr >= len_time_span * time_idx) {
                    System.out.println("now time is " + request.tr);
                    time_idx += 1;
                    SNM.update(routes, SPC, DNM.DN_m);
                    System.out.println(SNM.time_spans);
                    DNM.update();
                    System.out.println(DNM.time_spans);
                }
                long start_assign=System.currentTimeMillis();
                Comparator<Pair<Integer,Integer>> comp = new ShortestPathLRU.pair_com();
                ArrayList<Pair<Integer, Integer>> candidate = new ArrayList<>(routes.size());

                double cost_final = Integer.MAX_VALUE-1;
                int route_idx = -1;
                int[] insert_idx = {-1, -1};
                int rou_idx;
                for (rou_idx = 0; rou_idx < routes.size(); rou_idx++) {
                    Route route_ = routes.get(rou_idx);
                    if (Grid.reachable(route_.route.get(0)[0], request.ls, time_left - route_.route.get(0)[2])) {
                        if(mode.equals("DP")) route_.update(request.tr, SPC, true);
                        else if(mode.equals("Basic"))route_.update(request.tr, SPC, false);
                        else {
                            System.out.println("method can only be 'DP|Basic'");
                            return;
                        }
                        int bound = route_.bound(request, SPC.dis(request.ls, request.le), locations, SNM);
                        if(bound==Integer.MIN_VALUE){
                            Pair<Pair<Integer,Integer>,Double> info;
                            if(mode.equals("DP")) {
                                info = insertor.DPinsertion(route_, request, DNM.DN, SNM, SPC, dist);
                            }else if(mode.equals("Basic")){
                                info = insertor.Bainsertion(route_, request, DNM.DN, SNM, SPC, dist);
                            }else {
                                System.out.println("method can only be 'DP|Basic'");
                                return;
                            }
                            if (info != null) {
                                double cost = info.getValue();
                                if (cost < cost_final) {
                                    cost_final = cost;
                                    route_idx = rou_idx;
                                    insert_idx[0] = info.getKey().getKey();
                                    insert_idx[1] = info.getKey().getValue();
                                }
                            }
                        }
                        else if (bound < cost_final) candidate.add(new Pair<>(-bound, rou_idx));
                    }
                }
                //num_can += candidate.size();

                candidate.sort(comp);

                for (Pair<Integer, Integer>x:candidate){
                    if(-x.getKey()>=cost_final) break;
                    else {
                        rou_idx = x.getValue();
                        Route route_ = routes.get(rou_idx);
                        //num_check ++;
                        Pair<Pair<Integer,Integer>,Double> info;
                        if(mode.equals("DP")) {
                            info = insertor.DPinsertion(route_, request, DNM.DN, SNM, SPC, dist);
                        }else if(mode.equals("Basic")){
                            info = insertor.Bainsertion(route_, request, DNM.DN, SNM, SPC, dist);
                        }else {
                            System.out.println("method can only be 'DP|Basic'");
                            return;
                        }
                        if (info != null) {
                            double cost = info.getValue();
                            if (cost < cost_final) {
                                cost_final = cost;
                                route_idx = rou_idx;
                                insert_idx[0] = info.getKey().getKey();
                                insert_idx[1] = info.getKey().getValue();
                            }
                        }
                    }
                }
                time_all += System.currentTimeMillis()-start_assign;
                if (route_idx != -1) {
                    served += 1;
                    //uncomment codes belong if you want to see detours about how routes are changed after insertion
                    //System.out.println("new insertion:");
                    //routes.get(route_idx).print_tour();
                    int last_time = routes.get(route_idx).route.get(routes.get(route_idx).size - 1)[2];
                    if(mode.equals("DP")){
                        routes.get(route_idx).insert(insert_idx[0], insert_idx[1], request, SNM, SPC, dist);
                    }else if(mode.equals("Basic")) {
                        routes.get(route_idx).bainsert(insert_idx[0], insert_idx[1], request, SNM, SPC, dist);
                    }
                    SNM.update_min_max(DNM.DN_m);
                    //routes.get(route_idx).print_tour();
                    //System.out.println("final cost = "+cost_final);
                    penalty += routes.get(route_idx).route.get(routes.get(route_idx).size - 1)[2] - last_time;
                    //System.out.println("penalty = "+(routes.get(route_idx).route.get(routes.get(route_idx).size-1)[2] - last_time));
                } else {
                    penalty += request.p;
                }
                current_num ++;
            }
            save = "using "+time_all +" to assign " + current_num + " requests, " + served + " served, " + date_list.get(counter) + " penalty is " + penalty+"\n";
            System.out.print(save);
            counter += 1;
        }
    }
}
