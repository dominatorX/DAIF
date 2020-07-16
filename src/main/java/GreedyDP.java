import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class GreedyDP {
    public static void main(String[] args) throws IOException {
        final int work_num;
        if (args.length>0){
            work_num = Integer.parseInt(args[0]);
        }else {
            work_num = 3000;
        }
        final double detour_factor;
        if (args.length>1){
            detour_factor = Double.parseDouble(args[1]);
        }else {
            detour_factor = 1.3;
        }
        final int penalty_weight = 30;

        int work_cap = 3;
        if (args.length>2){
            work_cap = Integer.parseInt(args[2]);
        }

        String save;
        String graph_file = "./NYC/ny";
        String data_file = "./NYC/ny";
        ArrayList<String> date_list = new ArrayList<>();
        date_list.add("02");

        ShortestPathLRU SPC = new ShortestPathLRU();
        SPC.init(graph_file, data_file);
        GridPrune Grid = new GridPrune();
        Grid.init(graph_file);

        Gson gson = new Gson();
        InputStreamReader in = new InputStreamReader(new FileInputStream(graph_file+"_locations_j.json"));
        ArrayList<Double[]> locations  = gson.fromJson(in,
                new TypeToken<ArrayList<Double[]>>(){ }.getType());
        in.close();

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
            // for worker_location in worker_locations[:1000]:
            //    routes.append(Route(-10, 4, [(worker_location, -10, -10, 0)]))
            int work_idx;
            for (work_idx = 10000; work_idx < 10000+work_num; work_idx++) {
                Route temp = new Route();
                ArrayList<int[]> temp_route = new ArrayList<>();
                int[] temp_loc = {worker_location.get(work_idx)[1], -10, -10, 0, 6666, 0};
                temp_route.add(temp_loc);
                temp.init(-10, work_cap, temp_route);

                routes.add(temp);
            }
            System.out.println("we have" + routes.size() + "routes");
            Insertion insertor = new Insertion();

            long time_all = 0;
            long penalty = 0;
            int time_idx = 0;
            int test_num = 500000;
            int current_num = 0;
            int served = 0;

            in = new InputStreamReader(new FileInputStream(data_file+"_Task" + date_list.get(counter) + ".json"));
            ArrayList<int[]> request_list = gson.fromJson(in,
                    new TypeToken<ArrayList<int[]>>() {
                    }.getType());
            in.close();
            //long starTime=System.currentTimeMillis();
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
                }
                long start_assign=System.currentTimeMillis();
                Comparator<Pair<Integer,Integer>> comp = new ShortestPathLRU.pair_com();
                PriorityQueue<Pair<Integer, Integer>> candidate = new PriorityQueue<>(routes.size(), comp);

                int cost_final = Integer.MAX_VALUE;
                int route_idx = -1;
                int[] insert_idx = {-1, -1};
                int rou_idx;
                for (rou_idx = 0; rou_idx < routes.size(); rou_idx++) {
                    Route route_ = routes.get(rou_idx);
                    if (Grid.reachable(route_.route.get(0)[0], request.ls, time_left - route_.route.get(0)[2])) {
                        route_.update(request.tr, SPC, false);
                        int bound = route_.rabound(request, SPC.dis(request.ls, request.le), locations);

                        if (bound == 0) {
                            Pair<Pair<Integer,Integer>,Integer> info;
                            info = insertor.Leinsertion(route_, request, SPC, dist);
                            if (info != null) {
                                int cost = info.getValue();
                                if (cost < cost_final) {
                                    cost_final = cost;
                                    route_idx = rou_idx;
                                    insert_idx[0] = info.getKey().getKey();
                                    insert_idx[1] = info.getKey().getValue();
                                }
                            }
                        }

                        else if(bound<cost_final) candidate.add(new Pair<>(-bound, rou_idx));
                    }
                }

                Pair<Integer, Integer> x;
                while (!candidate.isEmpty()){
                    x = candidate.poll();
                    if(-x.getKey()>=cost_final){
                        break;
                    }else {
                        rou_idx = x.getValue();
                        Route route_ = routes.get(rou_idx);
                        //num_check ++;
                        Pair<Pair<Integer,Integer>,Integer> info;
                        info = insertor.Leinsertion(route_, request, SPC, dist);

                        if (info != null) {
                            int cost = info.getValue();
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
		            //System.out.println("new insertion:");
                    //routes.get(route_idx).print_tour();
                    //int last_time = routes.get(route_idx).route.get(routes.get(route_idx).size - 1)[2];
                    routes.get(route_idx).rainsert(insert_idx[0], insert_idx[1], request, SPC, dist);
                    //routes.get(route_idx).print_tour();
                    //System.out.println("final cost = "+cost_final);
                    penalty += cost_final;
                    //System.out.println("penalty = "+(routes.get(route_idx).route.get(routes.get(route_idx).size-1)[2] - last_time));
                } else {
                    penalty += request.p;
                }
                current_num ++;
            }
            //long endTime=System.currentTimeMillis();
            //long Time=endTime-starTime;
            //System.out.println("time cost = "+Time);
            save = "using "+time_all +" to assign " + current_num + " requests, " + served + " served, " + date_list.get(counter) + " penalty is " + penalty+"\n";
            System.out.print(save);
            counter += 1;
        }
    }
}
