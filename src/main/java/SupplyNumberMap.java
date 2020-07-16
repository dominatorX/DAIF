import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import static java.lang.Integer.*;

public class SupplyNumberMap {
    final Gson gson = new Gson();
    HashSet<Integer>regions;
    ArrayList<Integer>time_spans;
    int [] node2region;
    HashMap<Integer, HashMap<Integer,Double>> SN = new HashMap<>();
    final double gamma = 0.0016;
    //final double gamma = 0.00047757986111111113;
    //final double gamma = 0d;
    final int alpha = 1;
    final double [] beta = {10000*2.3682, 3800*2.3682, 1400*2.3682, 498*2.3682, 183*2.3682, 67*2.3682};//, 2.5};
    //final double [] beta = {1000000, 380000, 140000, 49800, 18300, 6700};//, 2.5}
    //final double [] beta = {0, 0, 0, 0, 0, 0};
    //final double [] beta = {23682d, 23682d, 23682d, 23682d, 23682d, 23682d};
    final int total_spans = 6;
    int next_idx = total_spans;
    final int per_span = 900;
    final int minutes = per_span/60;
    ArrayList<ArrayList<Double>> LB_list;
    //ArrayList<int[]> SN_m = new ArrayList<>();
    ArrayList<Double> LBC = new ArrayList<>(total_spans);

    public void init(String graph_file, ArrayList<Route> routes, ArrayList<Integer> time_spans, ArrayList<int[]> DN_m) throws FileNotFoundException {
        this.time_spans = (ArrayList<Integer>) time_spans.clone();
        this.LB_list = this.gson.fromJson(new InputStreamReader(new FileInputStream("./LB_inc2k.json")),
                        new TypeToken<ArrayList<ArrayList<Double>>>(){ }.getType());
        this.node2region = gson.fromJson(new InputStreamReader(new FileInputStream(graph_file+"_node2region_j.json")),
                new TypeToken<int[]>(){ }.getType());
        this.regions = gson.fromJson(new InputStreamReader(new FileInputStream(graph_file+"_regions_j.json")),
                new TypeToken<HashSet<Integer>>(){ }.getType());
        for(int time:this.time_spans){
            HashMap<Integer,Double> temp = new HashMap<>();
            for(int region :this.regions){
                temp.put(region,0d);
            }
            this.SN.put(time,temp);
        }
        for (Route route:routes) {
            int region = this.node2region[route.route.get(0)[0]];
            for (int time_span :this.time_spans)
                this.SN.get(time_span).put(region,this.SN.get(time_span).get(region) + this.per_span * this.gamma);
        }

        for(int i=0;i<total_spans;i++){
            int time = time_spans.get(i);
            LBC.add(beta[i] * gamma * (safe_lookup(DN_m.get(i)[0], Collections.min(SN.get(time).values()).intValue()) -
                    safe_lookup(DN_m.get(i)[1], Collections.max(SN.get(time).values()).intValue())));
        }
        // max shift for balance, maybe later on, cannot simply explain
    }

    private void shift_in_region(
            int region,int arr,int leave,int slack,ArrayList<HashMap<Integer,HashMap<Integer,Double>>> SS){
        int start = arr / 60;
        int end = leave / 60;
        double out_ad = (arr % 60) * this.gamma;
        double put_ad = (leave % 60) * this.gamma;
        double per_ad = 60 * this.gamma;
        int slack_time;

        ArrayList<HashMap<Integer,Double>> SST = new ArrayList<>();
        int all;
        for (all = 0; all < slack; all++) {
            SST.add(new HashMap<>());
        }

        if (end!=start) {
            if (leave < this.time_spans.get(0)) {
                if (slack > this.minutes-1 - end % this.minutes) {
                    for (slack_time = this.minutes-1 - end % this.minutes; slack_time < min(slack, this.minutes-1 - start % this.minutes); slack_time++) {

                        if (slack_time != 0) {
                            SST.set(slack_time, (HashMap<Integer, Double>)SST.get(slack_time - 1).clone());
                        }
                        HashMap<Integer, Double> temp = SST.get(slack_time);
                        if(slack_time == this.minutes-1 - end % this.minutes){
                            temp.put(time_spans.get(0), temp.getOrDefault(time_spans.get(0), 0d) + put_ad);
                        }else {
                            temp.put(time_spans.get(0), temp.getOrDefault(time_spans.get(0), 0d) + per_ad);
                        }
                        SST.set(slack_time, temp);
                    }

                    for (slack_time = this.minutes-1 - start % this.minutes; slack_time < min(slack, 2*this.minutes-1 - end % this.minutes); slack_time++) {
                        if (slack_time == this.minutes-1 - start % this.minutes){
                            HashMap<Integer, Double> temp = (HashMap<Integer, Double>) SST.get(slack_time - 1).clone();
                            temp.put(time_spans.get(0), temp.getOrDefault(time_spans.get(0), 0d) + per_ad-out_ad);
                            SST.set(slack_time, temp);
                        }else {
                            SST.set(slack_time, (HashMap<Integer, Double>) SST.get(slack_time - 1).clone());
                        }
                    }

                    if (slack > 2*this.minutes-1 - end % this.minutes) {
                        int num_span;
                        int current_span;
                        for (num_span = 1; num_span < min(this.total_spans, (slack + end % this.minutes) / this.minutes); num_span++) {
                            for (slack_time = this.minutes-1 - end % this.minutes; slack_time < min(slack - num_span * this.minutes, this.minutes-1 - start % this.minutes); slack_time++) {
                                current_span = slack_time + this.minutes * num_span;

                                HashMap<Integer, Double> temp = (HashMap<Integer, Double>) SST.get(current_span - 1).clone();
                                if(slack_time == this.minutes-1 - end % this.minutes){
                                    temp.put(time_spans.get(num_span), temp.getOrDefault(time_spans.get(num_span), 0d) + put_ad);
                                    temp.put(time_spans.get(num_span - 1), temp.getOrDefault(time_spans.get(num_span - 1), 0d) - put_ad);
                                }else{
                                    temp.put(time_spans.get(num_span - 1), temp.getOrDefault(time_spans.get(num_span - 1), 0d) - per_ad);
                                    temp.put(time_spans.get(num_span), temp.getOrDefault(time_spans.get(num_span), 0d) + per_ad);
                                }
                                SST.set(current_span, temp);


                            }
                            for (slack_time = this.minutes-1 - start % this.minutes; slack_time < min(slack - num_span * this.minutes, this.minutes*2-1 - end % this.minutes); slack_time++) {
                                current_span = slack_time + this.minutes * num_span;
                                if(slack_time == this.minutes-1 - start % this.minutes){
                                    HashMap<Integer, Double> temp = (HashMap<Integer, Double>) SST.get(current_span - 1).clone();
                                    temp.put(time_spans.get(num_span), temp.getOrDefault(time_spans.get(num_span), 0d) + per_ad-out_ad);
                                    temp.put(time_spans.get(num_span - 1), temp.getOrDefault(time_spans.get(num_span - 1), 0d) - per_ad+out_ad);
                                    SST.set(current_span, temp);
                                }else {
                                    SST.set(current_span, (HashMap<Integer, Double>) SST.get(current_span - 1).clone());
                                }
                            }
                        }
                    }
                    if (slack > (this.total_spans+1)*this.minutes-1 - end % this.minutes) {
                        for (slack_time = (this.total_spans+1)*this.minutes-1 - end % this.minutes; slack_time < min(slack, (this.total_spans+1)*this.minutes-1 - start % this.minutes); slack_time++) {
                            HashMap<Integer, Double> temp = (HashMap<Integer, Double>) SST.get(slack_time - 1).clone();
                            if(slack_time == (this.total_spans+1)*this.minutes-1 - end % this.minutes) {
                                temp.put(time_spans.get(this.total_spans-1), temp.getOrDefault(time_spans.get(this.total_spans-1), 0d) - put_ad);
                            }else {
                                temp.put(time_spans.get(this.total_spans - 1), temp.getOrDefault(time_spans.get(this.total_spans - 1), 0d) - per_ad);
                            }
                            SST.set(slack_time, temp);
                        }
                    }
                    if (slack >= (this.total_spans+1)*this.minutes-1 - start % this.minutes){
                        HashMap<Integer, Double> temp = (HashMap<Integer, Double>)SST.get((this.total_spans+1)*this.minutes-2 - start % this.minutes).clone();
                        temp.put(time_spans.get(this.total_spans-1), temp.getOrDefault(time_spans.get(this.total_spans-1), 0d) - per_ad+out_ad);
                        SST.set(slack_time, temp);
                    }
                }
            } else if (arr < this.time_spans.get(this.total_spans-1) + this.per_span) {
                int idx, idx_b = -1, idx_e = -1;
                for (idx = 0; idx < this.total_spans; idx++) {
                    if (this.time_spans.get(idx) <= arr && arr < this.time_spans.get(idx) + this.per_span) {
                        idx_b = idx;
                    }
                    if (this.time_spans.get(idx) <= leave && leave < this.time_spans.get(idx) + this.per_span) {
                        idx_e = idx;
                        break;
                    }
                }

                int out = start, put = end;
                boolean b_flag = (idx_b == -1), e_flag = (idx_e == -1);
                for (idx = 0; idx < slack; idx++) {
                    if (idx != 0) {
                        SST.set(idx, (HashMap<Integer, Double>) SST.get(idx - 1).clone());
                    }
                    if (e_flag) {
                        if (b_flag) {
                            out += 1;
                            if (out % this.minutes == 0) {
                                b_flag = false;
                                idx_b = 0;

                                HashMap<Integer, Double> temp = SST.get(idx);
                                temp.put(time_spans.get(0), temp.getOrDefault(time_spans.get(0), 0d) - out_ad);
                                SST.set(idx, temp);
                            }
                        } else {
                            out += 1;
                            if (out % this.minutes == 0) {
                                idx_b += 1;
                                if (idx_b == this.total_spans) {
                                    break;
                                }
                            }
                            HashMap<Integer, Double> temp = SST.get(idx);
                            if(out%this.minutes==0) {
                                temp.put(time_spans.get(idx_b), temp.getOrDefault(time_spans.get(idx_b), 0d) - out_ad);
                                temp.put(time_spans.get(idx_b-1), temp.getOrDefault(time_spans.get(idx_b-1), 0d) - per_ad+out_ad);
                            }else {
                                temp.put(time_spans.get(idx_b), temp.getOrDefault(time_spans.get(idx_b), 0d) - per_ad);
                            }
                            SST.set(idx, temp);
                        }
                    } else {
                        put += 1;
                        if (put % this.minutes == 0) {
                            idx_e += 1;
                            if (idx_e == this.total_spans) {
                                e_flag = true;
                            }
                        }
                        if (!e_flag) {
                            HashMap<Integer, Double> temp = SST.get(idx);
                            if(put % this.minutes == 0) {
                                temp.put(time_spans.get(idx_e), temp.getOrDefault(time_spans.get(idx_e), 0d) + put_ad);
                                temp.put(time_spans.get(idx_e-1), temp.getOrDefault(time_spans.get(idx_e-1), 0d) + per_ad-put_ad);
                            }else{
                                temp.put(time_spans.get(idx_e), temp.getOrDefault(time_spans.get(idx_e), 0d) + per_ad);
                            }
                            SST.set(idx, temp);
                        }
                        if (b_flag) {
                            out += 1;
                            if (out % this.minutes == 0) {
                                b_flag = false;
                                idx_b = 0;

                                HashMap<Integer, Double> temp = SST.get(idx);
                                temp.put(time_spans.get(0), temp.getOrDefault(time_spans.get(0), 0d) - out_ad);
                                SST.set(idx, temp);
                            }
                        } else {
                            out += 1;
                            if (out % this.minutes == 0) {
                                idx_b += 1;
                                if (idx_b == this.total_spans) {
                                    break;
                                }
                            }
                            HashMap<Integer, Double> temp = SST.get(idx);
                            if(out%this.minutes==0) {
                                temp.put(time_spans.get(idx_b), temp.getOrDefault(time_spans.get(idx_b), 0d) - out_ad);
                                temp.put(time_spans.get(idx_b-1), temp.getOrDefault(time_spans.get(idx_b-1), 0d) - per_ad+out_ad);
                            }else {
                                temp.put(time_spans.get(idx_b), temp.getOrDefault(time_spans.get(idx_b), 0d) - per_ad);
                            }
                            SST.set(idx, temp);
                        }
                    }
                }
            }
        }
        if(SS.get(0).containsKey(region)) {
            for (int idx = 0; idx < SS.size(); idx++) {
                for(int time:SST.get(idx).keySet()) {
                    SS.get(idx).get(region).put(time, SS.get(idx).get(region).getOrDefault(time, 0d)+SST.get(idx).get(time));
                }
            }
        }else {
            for (int idx = 0; idx < SS.size(); idx++) {
                SS.get(idx).put(region, SST.get(idx));
            }
        }
    }

    public ArrayList<HashMap<Integer,HashMap<Integer,Double>>> supply_shift(int l1, int l2, int arr, int leave, int slack, ShortestPathLRU SPC){
        int total;
        if (slack % 60 == 0) {
            total = slack / 60;
        } else {
            total = slack / 60 + 1;
        }
        ArrayList<HashMap<Integer, HashMap<Integer, Double>>> SS = new ArrayList<>(total);
        int all;
        for (all = 0; all < total; all++) {
            HashMap<Integer, HashMap<Integer, Double>> temp = new HashMap<>();
            SS.add(temp);
        }
        if (total!=0 && arr < this.time_spans.get(this.total_spans-1) + this.per_span) {
            if (this.node2region[l1] == this.node2region[l2]) {
                int region = this.node2region[l1];
                this.shift_in_region(region, arr, leave, total, SS);
            } else {
                String info = SPC.ex_dis(l1, l2);
                int begin;
                int end = arr;
                ArrayList<ArrayList<Integer>> TT = this.gson.fromJson(info.split("/")[1],
                        new TypeToken<ArrayList<ArrayList<Integer>>>() {
                        }.getType());
                for (ArrayList<Integer> tour : TT) {
                    begin = end;
                    end = begin + tour.get(2);
                    int region = tour.get(0);
                    this.shift_in_region(region, begin, end, total, SS);
                }
                this.shift_in_region(TT.get(TT.size() - 1).get(1), end, end + Integer.parseInt(info.split("/")[2]), total, SS);
            }
        }
        return SS;
    }

    private void supply_con(int l1,int l2,int start_time,ShortestPathLRU SPC,boolean delete) {
        String info = SPC.ex_dis(l1, l2);
        int distance = Integer.parseInt(info.split("/")[0]);
        ArrayList<ArrayList<Integer>> TT = this.gson.fromJson(info.split("/")[1],
                new TypeToken<ArrayList<ArrayList<Integer>>>() {
                }.getType());
        int time_left = Integer.parseInt(info.split("/")[2]);
        if (TT == null || TT.size()==0) {
            int begin = start_time;
            int finish_time = start_time + distance;
            int area = this.node2region[l1];
            if (finish_time > this.time_spans.get(0)) {
                for (int time_span : this.time_spans) {
                    if (time_span + this.per_span > begin) {               // has contribution
                        if (time_span + this.per_span > finish_time) {           // final interval
                            if (delete) {
                                this.SN.get(time_span).put(area, this.SN.get(time_span).get(area) - (finish_time - max(time_span, begin)) * this.gamma);
                            } else {
                                this.SN.get(time_span).put(area, this.SN.get(time_span).get(area) + (finish_time - max(time_span, begin)) * this.gamma);
                            }
                            break;
                        } else {                                       // middle
                            if (delete) {
                                this.SN.get(time_span).put(area, this.SN.get(time_span).get(area) - (time_span + this.per_span - max(time_span, begin)) * this.gamma);
                            } else {
                                this.SN.get(time_span).put(area, this.SN.get(time_span).get(area) + (time_span + this.per_span - max(time_span, begin)) * this.gamma);
                            }
                        }
                    }
                }
            }
        } else {                                                   // case 2: in different regions
            boolean flag = false;                                         // flag: if this tour has contribution
            int begin = start_time;
            int idx_now=-1;
            for (ArrayList<Integer> tour : TT) {
                int finish_time = begin + tour.get(2);
                int area = tour.get(0);
                if (!flag) {
                    if (finish_time > this.time_spans.get(0)) {
                        flag = true;
                        idx_now = 0;
                    } else {
                        begin = finish_time;
                        continue;
                    }
                }
                int time_span_idx;
                for (time_span_idx = idx_now;time_span_idx < this.total_spans; time_span_idx++) {
                    int time_span = this.time_spans.get(time_span_idx);
                    if (time_span + this.per_span > begin) {  // has contribution
                        if (time_span + this.per_span >= finish_time) {  // final interval
                            if (delete) {
                                this.SN.get(time_span).put(area, this.SN.get(time_span).get(area) - (finish_time - max(time_span, begin)) * this.gamma);
                            } else {
                                this.SN.get(time_span).put(area, this.SN.get(time_span).get(area) + (finish_time - max(time_span, begin)) * this.gamma);
                            }
                            idx_now = time_span_idx;
                            begin = finish_time;
                            break;
                        } else {
                            if (delete) {
                                this.SN.get(time_span).put(area, this.SN.get(time_span).get(area) - (time_span + this.per_span - max(time_span, begin)) * this.gamma);
                            } else {
                                this.SN.get(time_span).put(area, this.SN.get(time_span).get(area) + (time_span + this.per_span - max(time_span, begin)) * this.gamma);
                            }
                            if(time_span_idx == this.total_spans-1){
                                begin = finish_time;
                                idx_now = time_span_idx+1;
                            }
                        }
                    }
                }
            }
            int finish_time = begin + time_left;
            int area = TT.get(TT.size() - 1).get(1);
            if (!flag) {
                if (finish_time > this.time_spans.get(0)) {
                    idx_now = 0;
                    flag = true;
                }
            }

            if (flag) {
                int time_span_idx;
                for (time_span_idx = idx_now; time_span_idx < this.total_spans; time_span_idx++) {
                    int time_span = this.time_spans.get(time_span_idx);
                    if (time_span + this.per_span > begin) {  // has contribution
                        if (time_span + this.per_span >= finish_time) {  // final interval
                            if (delete) {
                                this.SN.get(time_span).put(area, this.SN.get(time_span).get(area) - (finish_time - max(time_span, begin)) * this.gamma);
                            } else {
                                this.SN.get(time_span).put(area, this.SN.get(time_span).get(area) + (finish_time - max(time_span, begin)) * this.gamma);
                            }
                            break;
                        } else {                                       // middle
                            if (delete) {
                                this.SN.get(time_span).put(area, this.SN.get(time_span).get(area) - (time_span + this.per_span - max(time_span, begin)) * this.gamma);
                            } else {
                                this.SN.get(time_span).put(area, this.SN.get(time_span).get(area) + (time_span + this.per_span - max(time_span, begin)) * this.gamma);
                            }
                        }
                    }
                }
            }
        }
    }

    public void contribution(Route route,ShortestPathLRU SPC,boolean delete) {
        if (route.route.get(route.size - 1)[2] <= this.time_spans.get(0)) {
            int area_last = this.node2region[route.route.get(route.size - 1)[0]];
            double sn;
            for (int time_span : this.time_spans) {
                sn = this.per_span * this.gamma;
                if (delete) {
                    this.SN.get(time_span).put(area_last, this.SN.get(time_span).get(area_last) - sn);
                } else {
                    this.SN.get(time_span).put(area_last, this.SN.get(time_span).get(area_last) + sn);
                }
            }
        } else {
            int path_idx;
            for (path_idx = 0; path_idx < route.size - 1; path_idx++) {
                this.supply_con(route.route.get(path_idx)[0], route.route.get(path_idx + 1)[0],
                        route.route.get(path_idx)[2], SPC, delete);
            }
            int time_last = route.route.get(route.size - 1)[2];
            int area_last = this.node2region[route.route.get(route.size - 1)[0]];
            boolean flag = true;
            for (int time_span : this.time_spans) {
                if (flag) {
                    if (time_last <= time_span + this.per_span) {
                        if (delete) {
                            this.SN.get(time_span).put(area_last,
                                    this.SN.get(time_span).get(area_last) - (time_span + this.per_span - max(time_span, time_last)) * this.gamma);
                        } else {
                            this.SN.get(time_span).put(area_last,
                                    this.SN.get(time_span).get(area_last) + (time_span + this.per_span - max(time_span, time_last)) * this.gamma);
                        }
                        flag = false;
                    }
                } else {
                    double sn = this.per_span * this.gamma;
                    if (delete) {
                        this.SN.get(time_span).put(area_last, this.SN.get(time_span).get(area_last) - sn);
                    } else {
                        this.SN.get(time_span).put(area_last, this.SN.get(time_span).get(area_last) + sn);
                    }
                }
            }
        }
    }

    public HashMap<Integer, HashMap<Integer,Double>> path_con(int l1, int l2,int start_time,ShortestPathLRU SPC){
        HashMap<Integer, HashMap<Integer,Double>> sn = new HashMap<>();
        if (l2 == -1) {                                            // case 1: no region changed
            int begin = start_time;
            int area = this.node2region[l1];
            boolean flag = false;
            for (int time_span : this.time_spans) {
                if (flag) {
                    sn.get(area).put(time_span, this.per_span * this.gamma);
                } else if (time_span > begin) {
                    flag = true;
                    HashMap<Integer, Double> temp = new HashMap<>();
                    if(time_span!=this.time_spans.get(0)){
                        temp.put(time_span-this.per_span, (time_span - begin) * this.gamma);
                    }
                    temp.put(time_span, this.per_span * this.gamma);
                    sn.put(area, temp);
                }
            }
            return sn;
        }
        String info = SPC.ex_dis(l1, l2);
        int distance = Integer.parseInt(info.split("/")[0]);
        ArrayList<ArrayList<Integer>> TT = this.gson.fromJson(info.split("/")[1],
                new TypeToken<ArrayList<ArrayList<Integer>>>() {
                }.getType());
        int time_left = Integer.parseInt(info.split("/")[2]);

        if (TT==null || TT.size()==0){                                            // case 1: no region changed
            int begin = start_time;
            int finish_time = start_time + distance;
            int area = this.node2region[l1];
            if (finish_time > this.time_spans.get(0)){             // case 1.1: has contribution
                for (int time_span :this.time_spans){
                    if (time_span+this.per_span > begin){               // has contribution
                        HashMap<Integer,Double> temp = new HashMap<>();
                        sn.put(area,temp);
                        if (time_span+this.per_span> finish_time){            // final interval
                            sn.get(area).put(time_span, (finish_time - max(time_span, begin)) * this.gamma);
                            break;
                        }else { // middle
                            sn.get(area).put(time_span, (time_span + this.per_span - max(time_span, begin)) * this.gamma);
                        }
                    }
                }
            }
        }else {                                                   // case 2: in different regions
            boolean flag = false;                                         // flag: if this tour has contribution
            int begin = start_time;
            int idx_now = -1;
            for (ArrayList<Integer> tour : TT) {
                int finish_time = begin + tour.get(2);
                int area = tour.get(0);
                if (!flag) {
                    if (finish_time > this.time_spans.get(0)) {
                        flag = true;
                        idx_now = 0;
                    } else {
                        begin = finish_time;
                        continue;
                    }
                }
                int time_span_idx;
                for (time_span_idx = idx_now; time_span_idx < this.total_spans; time_span_idx++) {
                    int time_span = this.time_spans.get(time_span_idx);
                    if (time_span + this.per_span > begin) {  // has contribution
                        if (time_span + this.per_span >= finish_time) {  // final interval
                            HashMap<Integer, Double> temp = sn.getOrDefault(area, new HashMap<>());
                            temp.put(time_span, temp.getOrDefault(time_span, 0d) +
                                    (finish_time - max(time_span, begin)) * this.gamma);
                            sn.put(area, temp);
                            idx_now = time_span_idx;
                            begin = finish_time;
                            break;
                        } else {                                       // middle
                            HashMap<Integer, Double> temp = sn.getOrDefault(area, new HashMap<>());
                            temp.put(time_span, temp.getOrDefault(time_span, 0d) +
                                    (time_span + this.per_span - max(time_span, begin)) * this.gamma);
                            sn.put(area, temp);
                            if (time_span_idx == this.total_spans-1){
                                time_span_idx ++;
                                begin = finish_time;
                            }
                        }
                    }
                }
            }
            int finish_time = begin + time_left;
            int area = TT.get(TT.size() - 1).get(1);
            if (!flag) {
                if (finish_time > this.time_spans.get(0)) {
                    idx_now = 0;
                } else {
                    return sn;
                }
            }
            int time_span_idx;
            for (time_span_idx = idx_now; time_span_idx < this.total_spans; time_span_idx++) {
                int time_span = this.time_spans.get(time_span_idx);
                if (time_span + this.per_span > begin) {  // has contribution
                    if (time_span + this.per_span >= finish_time) {  // final interval
                        HashMap<Integer, Double> temp = sn.getOrDefault(area, new HashMap<>());
                        temp.put(time_span, temp.getOrDefault(time_span, 0d) +
                                (finish_time - max(time_span, begin)) * this.gamma);
                        sn.put(area, temp);
                        break;
                    } else {                                       // middle
                        HashMap<Integer, Double> temp = sn.getOrDefault(area, new HashMap<>());
                        temp.put(time_span, temp.getOrDefault(time_span, 0d) +
                                (time_span + this.per_span - max(time_span, begin)) * this.gamma);
                        sn.put(area, temp);
                    }
                }
            }
        }
        return sn;
    }

    double safe_lookup(int demand, int supply){
        if (demand >= this.LB_list.size()) {
            supply = supply * this.LB_list.size() / demand;
            demand = this.LB_list.size() - 1;
        }
        if (supply < this.LB_list.get(demand).size()) {
            return this.LB_list.get(demand).get(supply);
        }else return 0d;
    }

    public double pure(HashMap<Integer,HashMap<Integer,Double>> sn,HashMap<Integer,HashMap<Integer,Integer>> DN) {
        double cost = 0;
        int idx;
        for (int area : sn.keySet()) {
            for (int time_span : sn.get(area).keySet()) {
                if (time_span >= this.time_spans.get(0)) {
                    idx = (time_span - this.time_spans.get(0)) / this.per_span;

                    int demand = DN.get(area).get(time_span);
                    int supply = this.SN.get(time_span).get(area).intValue();

                    cost -= this.beta[idx] * sn.get(area).get(time_span) * safe_lookup(demand, supply);
                }
            }
        }
        return cost;
    }

    public int bound_shift_cost(int slack, int dt, int arr){
        int i;
        double cost=0;
        int first_period=0;

        if(arr>=time_spans.get(total_spans-1)+per_span) return 0;

        if(slack>per_span) {
            dt += per_span-slack;
            slack = per_span;
        }

        if(arr>time_spans.get(0)) {
            for (i = 0; i < total_spans; i++) {
                if (arr > time_spans.get(i)) {
                    if (arr + slack > time_spans.get(i) + per_span) {
                        cost -= (time_spans.get(i) + per_span - arr+dt-slack) * LBC.get(i);
                        first_period = i + 1;
                    } else first_period = i;
                }
            }
        }

        for(i=first_period;i<total_spans;i++)
            cost -= dt *LBC.get(i);

        return (int)cost;
    }

    public int bound_self_cost(int slack, int dt, int arr, int dur){
        // in the first span, the whole arr+dur+slack are shift
        int i;
        double cost=0;
        int first_period=0;
        int counted = slack-dt;

        if(arr>=time_spans.get(total_spans-1)+per_span) return 0;

        if(slack>per_span) {
            dt += per_span-slack;
            slack = per_span;
        }

        if(arr>time_spans.get(0)) {
            for (i = 0; i < total_spans; i++) {
                if (arr > time_spans.get(i)) {
                    if (arr + slack +dur > time_spans.get(i) + per_span) {
                        cost -= (time_spans.get(i) + per_span - arr-counted) * LBC.get(i);
                        int cover = 1;
                        while (arr + slack +dur > time_spans.get(i) + per_span*(cover+1)){
                            int idx_now = cover+i;
                            if(idx_now==total_spans) return (int) cost;
                            cost -= (per_span-counted) *LBC.get(idx_now);
                            cover++;
                        }
                        first_period = i + cover;
                    } else first_period = i;
                }
            }
        }

        for(i=first_period;i<total_spans;i++)
            cost -= dt * LBC.get(i);
        return (int) cost;
    }

    private void con_for_single_span(int l1,int l2,int start_time,ShortestPathLRU SPC) {
        int time_span = this.time_spans.get(this.total_spans-1);
        if (start_time>time_span+this.per_span){
            return;
        }
        String info = SPC.ex_dis(l1, l2);
        int distance = Integer.parseInt(info.split("/")[0]);
        ArrayList<ArrayList<Integer>> TT = this.gson.fromJson(info.split("/")[1],
                new TypeToken<ArrayList<ArrayList<Integer>>>() {
                }.getType());
        int time_left = Integer.parseInt(info.split("/")[2]);
        if (TT==null || TT.size()==0) {                                            // case 1: no region changed
            int begin = start_time;
            int finish_time = start_time + distance;
            int area = this.node2region[l1];
            this.SN.get(time_span).put(area, this.SN.get(time_span).get(area) + (min(time_span + this.per_span, finish_time) - max(time_span, begin))* this.gamma);
        } else {                                                   // case 2: in different regions
            boolean flag = false;                                         // flag: if this tour has contribution
            int begin = start_time;
            for (ArrayList<Integer> tour : TT) {
                int finish_time = begin + tour.get(2);
                int area = tour.get(0);
                if (!flag) {
                    if (finish_time > time_span) {
                        flag = true;
                    } else {
                        begin = finish_time;
                        continue;
                    }
                }
                this.SN.get(time_span).put(area, this.SN.get(time_span).get(area) + (min(time_span + this.per_span, finish_time) - max(time_span, begin)) * this.gamma);
                begin = finish_time;
                if (begin >= time_span + this.per_span) {
                    break;
                }
            }
            if (begin < time_span + this.per_span) {
                int finish_time = begin + time_left;
                int area = TT.get(TT.size() - 1).get(1);
                this.SN.get(time_span).put(area, this.SN.get(time_span).get(area) + (min(time_span + this.per_span, finish_time) - max(time_span, begin)) * this.gamma);
            }
        }
    }

    public void update_min_max(ArrayList<int[]> DN_m){
        /*this.SN_m = new ArrayList<>();
        for (int time : time_spans)
            this.SN_m.add(new int[]{Collections.max(SN.get(time).values()).intValue(),
                    Collections.min(SN.get(time).values()).intValue()});*/
        for(int i=0;i<total_spans;i++){
            int time = time_spans.get(i);
            LBC.set(i, beta[i] * gamma * (safe_lookup(DN_m.get(i)[0], Collections.min(SN.get(time).values()).intValue()) -
                    safe_lookup(DN_m.get(i)[1], Collections.max(SN.get(time).values()).intValue())));
        }
    }

    public void update(ArrayList<Route> routes,ShortestPathLRU SPC, ArrayList<int[]>DN_m) {
        int removed_span = this.time_spans.remove(0);
        int new_span = (this.time_spans.get(this.total_spans - 2) + this.per_span);
        this.time_spans.add(new_span);
        this.SN.remove(removed_span);
        HashMap<Integer, Double> temp = new HashMap<>();
        for(int region:regions){
            temp.put(region, 0d);
        }
        this.SN.put(new_span, temp);

        LBC.remove(0);
        for(int i=0;i<total_spans-1;i++)LBC.set(i, LBC.get(i)*beta[i]/beta[i+1]);

        if (this.next_idx < 240) {
            for (Route route : routes) {
                if (route.route.get(route.size - 1)[2] <= new_span) {
                    int area = this.node2region[route.route.get(route.size - 1)[0]];
                    this.SN.get(new_span).put(area, this.SN.get(new_span).get(area) + this.per_span * this.gamma);
                } else {
                    int start_idx = -1, end_idx = -1;
                    int idx;
                    for (idx = route.size - 1; idx > -1; idx--) {
                        if (route.route.get(idx)[2] >= new_span + this.per_span) {
                            end_idx = idx;
                        } else if (route.route.get(idx)[2] < new_span) {
                            start_idx = idx;
                            break;
                        }
                    }
                    int path_idx;
                    if (end_idx == -1) {  // total tour not longer than 1 hour
                        for (path_idx = start_idx; path_idx < route.size - 1; path_idx++) {
                            this.con_for_single_span(route.route.get(path_idx)[0], route.route.get(path_idx + 1)[0],
                                    route.route.get(path_idx)[2], SPC);
                        }
                        int area = this.node2region[route.route.get(route.size - 1)[0]];
                        this.SN.get(new_span).put(area, this.SN.get(new_span).get(area) + (new_span + this.per_span - route.route.get(route.size - 1)[2]) * this.gamma);
                    } else {
                        for (path_idx = start_idx; path_idx < end_idx; path_idx++) {
                            this.con_for_single_span(route.route.get(path_idx)[0], route.route.get(path_idx + 1)[0],
                                    route.route.get(path_idx)[2], SPC);
                        }
                    }
                }
            }
            LBC.add(beta[total_spans-1] * gamma * (safe_lookup(DN_m.get(total_spans-1)[0],
                    Collections.min(SN.get(new_span).values()).intValue()) -
                    safe_lookup(DN_m.get(total_spans-1)[1], Collections.max(SN.get(new_span).values()).intValue())));
        } else {
            this.LBC.add(0d);
        }
        //System.out.println(gson.toJson(LBC));
        this.next_idx += 1;
        double sn_sum=0;
        for(int time :this.SN.keySet()) sn_sum += this.SN.get(time).values().stream().mapToDouble(Double::doubleValue).sum();
        System.out.println(sn_sum);
    }
}

