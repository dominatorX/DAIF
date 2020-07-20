import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static java.lang.Integer.min;

public class Route {
    int time, capacity, size;
    ArrayList<Pair<Integer, Integer>> current_path;
    ArrayList<int[]> route;
    private ArrayList<ArrayList<HashMap<Integer, HashMap<Integer, Double>>>> TSS;

    public void init(int start_time, int cap_worker, ArrayList<int[]> request_info) {
        this.time = start_time;
        this.capacity = cap_worker;
        this.route = request_info;
        this.size = this.route.size();
        this.current_path = new ArrayList<>();
    }

    void update(int current_time, ShortestPathLRU SPC, boolean flag) {
        if (this.time < current_time) {
            if (this.size == 1) {
                if(this.route.get(0)[2]<current_time) {
                    this.route.get(0)[1] = current_time;
                    this.route.get(0)[2] = current_time;
                }
                this.route.get(0)[4] = 6666;
                this.route.get(0)[5] = 0;
                this.time = current_time;
            } else {
                if (this.route.get(1)[2]>current_time && this.route.get(0)[0] != this.route.get(1)[0]){
                    if (this.current_path.get(this.current_path.size()-1).getValue()<current_time){
                        this.route.remove(0);
                        if(flag) this.TSS.remove(0);
                        this.size -= 1;
                        if (this.size != 1) {
                            while (this.route.get(0)[0] == this.route.get(1)[0]) {
                                this.route.remove(0);
                                if(flag) this.TSS.remove(0);
                                this.size -= 1;
                                if(this.size==1){
                                    break;
                                }
                            }
                        }
                        if (this.size == 1) {
                            if(this.route.get(0)[2]<current_time) {
                                this.route.get(0)[1] = current_time;
                                this.route.get(0)[2] = current_time;
                            }
                            if(flag) this.TSS.clear();
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.time = current_time;
                            return;
                        }
                        this.current_path = new ArrayList<>();
                        this.time = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0], this.route.get(0)[2], current_time, this.current_path)[1];
                        return;
                    }else {
                        if(this.current_path.get(0).getValue()<current_time) {
                            while (this.current_path.get(0).getValue() < current_time) {
                                this.current_path.remove(0);
                            }
                            this.route.get(0)[0] = this.current_path.get(0).getKey();
                            this.route.get(0)[1] = this.current_path.get(0).getValue();
                            this.route.get(0)[2] = this.current_path.get(0).getValue();
                            this.route.get(0)[3] = 0;
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.time = current_time;
                            return;
                        }
                    }
                }

                while (this.route.get(1)[2] <= current_time) {
                    this.route.remove(0);
                    if(flag) this.TSS.remove(0);
                    this.size -= 1;
                    if (this.size == 1) {
                        if(this.route.get(0)[2]<current_time) {
                            this.route.get(0)[1] = current_time;
                            this.route.get(0)[2] = current_time;
                        }
                        if(flag) this.TSS.clear();
                        this.route.get(0)[3] = 0;
                        this.route.get(0)[4] = 6666;
                        this.route.get(0)[5] = 0;
                        this.current_path = new ArrayList<>();
                        break;
                    }
                }
                if (this.size != 1) {
                    while (this.route.get(0)[0]==this.route.get(1)[0]){
                        this.route.remove(0);
                        if(flag) this.TSS.remove(0);
                        this.size -= 1;
                        if (this.size == 1) {
                            if(this.route.get(0)[2]<current_time) {
                                this.route.get(0)[1] = current_time;
                                this.route.get(0)[2] = current_time;
                            }
                            if(flag) this.TSS.clear();
                            this.route.get(0)[3] = 0;
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.time = current_time;
                            this.current_path = new ArrayList<>();
                            return;
                        }
                    }
                    this.current_path = new ArrayList<>();
                    int[] temp = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0], this.route.get(0)[2],
                            current_time, this.current_path);
                    if(temp!=null){
                        this.route.get(0)[0] = temp[0];
                        this.route.get(0)[1] = temp[1];
                        this.route.get(0)[2] = temp[1];
                        this.route.get(0)[3] = 0;
                        this.route.get(0)[4] = 6666;

                        this.time = temp[1];
                    }else{
                        this.route.remove(0);
                        if(flag) this.TSS.remove(0);
                        this.size -= 1;
                        if (this.size != 1) {
                            while (this.route.get(0)[0] == this.route.get(1)[0]) {
                                this.route.remove(0);
                                if(flag) this.TSS.remove(0);
                                this.size -= 1;
                                if(this.size==1){
                                    break;
                                }
                            }
                        }
                        if (this.size == 1) {
                            if(this.route.get(0)[2]<current_time) {
                                this.route.get(0)[1] = current_time;
                                this.route.get(0)[2] = current_time;
                            }
                            if(flag) this.TSS.clear();
                            this.route.get(0)[4] = 6666;
                            this.route.get(0)[5] = 0;
                            this.time = current_time;
                            return;
                        }
                        this.current_path = new ArrayList<>();
                        this.time = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0],
                                this.route.get(0)[2], current_time, this.current_path)[1];
                    }

                }else {
                    if(this.route.get(0)[2]<current_time) {
                        this.route.get(0)[1] = current_time;
                        this.route.get(0)[2] = current_time;
                    }
                    this.route.get(0)[3] = 0;
                    this.route.get(0)[4] = 6666;
                    this.route.get(0)[5] = 0;
                    this.current_path = new ArrayList<>();
                    this.time = current_time;
                }
            }
        }
    }
    public void print_tour(){
        StringBuilder info = new StringBuilder();
        info.append(this.route.get(0)[0]);
        info.append("(");
        info.append(this.route.get(0)[2]);
        info.append(",");
        info.append(this.route.get(0)[3]);
        info.append(")");
        int i;
        for (i=1;i<this.size;i++) {
            int[] aim = this.route.get(i);
            info.append("-->");
            info.append(aim[0]);
            info.append("(");
            info.append(aim[2]);
            info.append(",");
            info.append(aim[3]);
            info.append(")");
        }
        System.out.println(info);
    }
    public boolean feasible(int idx1, int idx2, Request request, ShortestPathLRU SPC, int dis_) {
        if (idx1 == idx2) {
            int arr1 = this.route.get(idx1 - 1)[2];
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
            if (dis1 == -1) {
                return false;
            }
            if (arr1 + dis1 > request.td - dis_) {
                return false;
            }
            if (this.size > idx1) {
                int dis2 = SPC.dis(request.le, this.route.get(idx1)[0]);
                if (dis2 == -1) {
                    return false;
                }
                int detour = dis_ + dis1 + dis2 + this.route.get(idx1 - 1)[2] - this.route.get(idx1)[2];
                if (detour > this.route.get(idx1)[4]) {
                    return false;
                }
            }
            return this.route.get(idx1 - 1)[5] + request.a <= this.capacity;
        } else {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
            if (dis1 == -1) {
                return false;
            }
            int dis2 = SPC.dis(request.ls, this.route.get(idx1)[0]);
            if (dis2 == -1) {
                return false;
            }
            int arr1 = this.route.get(idx1 - 1)[2];
            int detour1 = dis2 + dis1 + this.route.get(idx1 - 1)[2] - this.route.get(idx1)[2];
            if (arr1 + dis1 > request.td - dis_) {
                return false;
            }
            if (detour1 > this.route.get(idx1)[4]) {
                return false;
            }
            int route_idx;
            for (route_idx = idx1 - 1; route_idx < idx2; route_idx++) {
                if (this.route.get(route_idx)[5] + request.a > this.capacity) {
                    return false;
                }
            }

            int dis3 = SPC.dis(this.route.get(idx2 - 1)[0], request.le);
            if (dis3 == -1) {
                return false;
            }
            int arr2 = this.route.get(idx2 - 1)[2];
            if (arr2 + detour1 + dis3 > request.td) {
                return false;
            }
            if (this.size > idx2) {
                int dis4 = SPC.dis(request.le, this.route.get(idx2)[0]);
                if (dis4 == -1) {
                    return false;
                }
                int detour2 = dis3 + dis4 + this.route.get(idx2 - 1)[2] - this.route.get(idx2)[2] + detour1;
                return (detour2 <= this.route.get(idx2)[4]);
            }
        }
        return true;
    }

    int norm(Double[] x, Double[] y){
        return (int) Math.ceil(Math.sqrt(Math.pow(x[0]-y[0], 2)+Math.pow(x[1]-y[1], 2))/10);
    }

    int rabound(Request request, int dis_, ArrayList<Double[]>locations){
        int cost_ = Integer.MAX_VALUE;
        ArrayList<Integer> Dio = new ArrayList<>();
        // route ddl, arr, slack, picked -> 1, 2, 4, 5
        int j;
        int cost_j;
        Dio.add(Integer.MAX_VALUE);
        Dio.add(Integer.MAX_VALUE);
        int ls_now = norm(locations.get(route.get(0)[0]), locations.get(request.ls));
        int le_now = norm(locations.get(route.get(0)[0]), locations.get(request.le));
        int ls_next, le_next=0;
        for (j = 1; j < this.route.size()+1; j++) {
            if(this.size>j) le_next = norm(locations.get(request.le), locations.get(this.route.get(j)[0]));
            int arr1 = route.get(j - 1)[2];
            int detour;
            TestSame: if (arr1 + ls_now +dis_ <= request.td && this.route.get(j - 1)[5] + request.a <= this.capacity){
                if (this.size > j) {
                    detour = dis_ + ls_now + le_next + arr1 - this.route.get(j)[2];
                    System.out.println(le_next);
                    if (detour <= 0) return 0;
                    if (detour > this.route.get(j)[4]) break TestSame;
                }else detour = dis_ + ls_now;

                if (detour < cost_) cost_ = detour;
            }

            TestDiff: if (j > 1 && Dio.get(j) != Integer.MAX_VALUE) {
                if (j == size) {
                    cost_j = le_now + Dio.get(j);
                    if (cost_j + arr1 > request.td) break TestDiff;
                } else {
                    int arr = le_now + arr1 + Dio.get(j);
                    cost_j = le_next - route.get(j)[2] + arr;
                    if (cost_j <= 0) return 0;
                    if (cost_j > route.get(j)[4] || arr > request.td) break TestDiff;
                }
                if (cost_j < cost_) cost_ = cost_j;
            }
            le_now = le_next;

            if (j == route.size()) break;
            if (route.get(j)[2] > request.td) break;

            ls_next = norm(locations.get(request.ls), locations.get(route.get(j)[0]));
            if (route.get(j)[5] + request.a > this.capacity ||
                    route.get(j - 1)[5] + request.a > this.capacity) Dio.add(Integer.MAX_VALUE);
            else {
                int det = ls_now + ls_next + arr1 - route.get(j)[2];
                if(det <= 0) Dio.add(0);
                else if (det > route.get(j)[4] || det > Dio.get(j)) Dio.add(Dio.get(j));
                else Dio.add(det);
            }
            ls_now = ls_next;
        }

        return cost_;
    }

    int bound(Request request, int dis_, ArrayList<Double[]>locations, SupplyNumberMap SNM){
        // If le can be inserted after the last loc of route and they are not in the same region, directly return IntMin
        int[] last = this.route.get(route.size() - 1);
        if(SNM.node2region[last[0]] != SNM.node2region[request.le]) {
            int pre_dis = norm(locations.get(last[0]), locations.get(request.ls));
            if (pre_dis + dis_ + last[2] <= request.td) return Integer.MIN_VALUE;
            int tail = norm(locations.get(last[0]), locations.get(request.le));
            if(tail+last[2]<=request.td) {
                for (int i = route.size() - 2; i > 0; i--) {
                    int now_dis = norm(locations.get(route.get(i)[0]), locations.get(request.ls));
                    int det = now_dis+pre_dis-(route.get(i + 1)[2] - route.get(i)[2]);
                    if (det < 0 || (det <= route.get(i)[4] && det + tail <= request.td)) {
                        return Integer.MIN_VALUE;
                    }
                    if (route.get(i)[5] + request.a > this.capacity) break;
                    pre_dis = now_dis;
                }
            }
        }

        int cost_ = Integer.MAX_VALUE;
        //int cost_b = Integer.MAX_VALUE;
        int temp_b;
        // now only insertions in the middle are left
        // For any place to insert le, j, record the highest balance cost save
        // Note that, even though insert ls/le between i,j may alter all the period i->j in
        // different region, restricted by the ddl, the area shift is not big and it is still close
        // enough to serve request in regions in i->j.
        // If we want to count it in,,, i->j can be totally different region,
        // ri=rle=rj, slack*dt for each following time interval
        // ri!=rle or rle!rj slack*dt for each following time interval slack+aj-ai

        // for i==j case, if


        ArrayList<Integer> Dio = new ArrayList<>();
        ArrayList<Integer> BSM = new ArrayList<>();
        // route ddl, arr, slack, picked -> 1, 2, 4, 5
        int j;
        BSM.add(Integer.MAX_VALUE);
        BSM.add(Integer.MAX_VALUE);
        Dio.add(Integer.MAX_VALUE);
        Dio.add(Integer.MAX_VALUE);
        int min_slack = 0;
        int ls_now = norm(locations.get(route.get(0)[0]), locations.get(request.ls));
        int le_now = norm(locations.get(route.get(0)[0]), locations.get(request.le));
        int ls_next, le_next;
        int arr_now = route.get(0)[2];
        int arr_next;
        int reg_now = SNM.node2region[route.get(0)[0]];
        int reg_next;
        //boolean cost_d_flag = true;
        int regs = SNM.node2region[request.ls];
        int rege = SNM.node2region[request.le];
        boolean same_reg = rege == regs;
        for (j = 1; j < this.route.size(); j++) {
            le_next = norm(locations.get(request.le), locations.get(this.route.get(j)[0]));
            arr_next = route.get(j)[2];
            reg_next = SNM.node2region[route.get(j)[0]];
            boolean same_reg_j = reg_now == reg_next;
            int max_slack = route.get(j)[4];

            if (arr_now + ls_now +dis_ <= request.td && this.route.get(j - 1)[5] + request.a <= this.capacity){
                int detour = dis_ + ls_now + le_next + arr_now - arr_next;
                if (detour <= 0) detour = 0;
                if (detour <= max_slack) { // insertable
                    if(same_reg && same_reg_j && reg_now==regs)
                        temp_b = SNM.bound_shift_cost(max_slack, max_slack, arr_next);
                    else temp_b = SNM.bound_self_cost(max_slack, max_slack, arr_now, arr_next-arr_now);
                    //if(temp_b<cost_b) cost_b=temp_b;
                    if (temp_b + detour < cost_) cost_ = temp_b + detour;
                }
            }

            if (j > 1 && Dio.get(j) != Integer.MAX_VALUE) {
                int arr = le_now + arr_now + Dio.get(j);
                int cost_j = le_next - arr_next + arr;
                if (cost_j <= 0) cost_j = 0; //cost_d_flag=false;
                if (cost_j <= max_slack && arr <= request.td) { // insertable
                    if(same_reg_j && reg_now==rege)
                        temp_b = SNM.bound_shift_cost(max_slack, max_slack-min_slack, arr_next)+BSM.get(j);
                    else temp_b = SNM.bound_self_cost(max_slack, max_slack-min_slack,
                            arr_now, arr_next-arr_now)+BSM.get(j);
                    //if(temp_b<cost_b) cost_b=temp_b;
                    if (temp_b + cost_j < cost_) cost_ = cost_j+temp_b;
                }
            }

            if (j == route.size()-1) break;
            if (arr_next > request.td) break;

            ls_next = norm(locations.get(request.ls), locations.get(route.get(j)[0]));
            if (route.get(j)[5] + request.a > this.capacity || route.get(j - 1)[5] + request.a > this.capacity) {
                Dio.add(Integer.MAX_VALUE);
                BSM.add(Integer.MAX_VALUE);
                min_slack = 0;
            } else {
                int det = ls_now + ls_next + arr_now - arr_next;
                if(det <= max_slack) {
                    if (det <= 0) Dio.add(0);
                    else if (det > Dio.get(j)) Dio.add(Dio.get(j));
                    else Dio.add(det);

                    if(same_reg_j && reg_now==regs)
                        temp_b = SNM.bound_shift_cost(max_slack, max_slack, arr_next);
                    else temp_b = SNM.bound_self_cost(max_slack, max_slack, arr_now, arr_next-arr_now);
                    if(temp_b<BSM.get(j)) {
                        if (min_slack==0) min_slack = max_slack;
                        BSM.add(temp_b);
                    }
                    else BSM.add(BSM.get(j));
                }else {
                    Dio.add(Dio.get(j));
                    BSM.add(BSM.get(j));
                }
            }

            reg_now = reg_next;
            le_now = le_next;
            ls_now = ls_next;
            arr_now = arr_next;
        }

        return cost_;
    }

    private HashMap<Integer, HashMap<Integer, Double>> clone_sn(HashMap<Integer, HashMap<Integer, Double>> Map) {
        HashMap<Integer, HashMap<Integer, Double>> aim = new HashMap<>();
        for (int key : Map.keySet()) {
            aim.put(key, (HashMap<Integer, Double>) Map.get(key).clone());
        }
        return aim;
    }

    private static void MapAdd(HashMap<Integer, HashMap<Integer, Double>> Map1, HashMap<Integer, HashMap<Integer, Double>> Map2, boolean sub) {
        HashSet<Integer> common = new HashSet<>(Map1.keySet());
        common.retainAll(Map2.keySet());
        for (int area : common) {
            HashSet<Integer> same = new HashSet<>(Map1.get(area).keySet());
            same.retainAll(Map2.get(area).keySet());
            if (sub) {
                for (int time : same) {
                    Map1.get(area).put(time, Map1.get(area).get(time) - Map2.get(area).get(time));
                }
            } else {
                for (int time : same) {
                    Map1.get(area).put(time, Map1.get(area).get(time) + Map2.get(area).get(time));
                }
            }
            HashSet<Integer> diff = new HashSet<>(Map2.get(area).keySet());
            diff.removeAll(same);
            if (sub) {
                for (int time : diff) {
                    Map1.get(area).put(time, -Map2.get(area).get(time));
                }
            } else {
                for (int time : diff) {
                    Map1.get(area).put(time, Map2.get(area).get(time));
                }
            }
        }
        HashSet<Integer> other = new HashSet<>(Map2.keySet());
        other.removeAll(common);
        if (sub) {
            for (int area : other) {
                Map1.put(area, new HashMap<>());
                for (int time : Map2.get(area).keySet()) {
                    Map1.get(area).put(time, -Map2.get(area).get(time));
                }
            }
        } else {
            for (int area : other) {
                Map1.put(area, new HashMap<>());
                for (int time : Map2.get(area).keySet()) {
                    Map1.get(area).put(time, Map2.get(area).get(time));
                }
            }
        }
    }

    private static void MapCombine(ArrayList<HashMap<Integer, HashMap<Integer, Double>>> AddMap, ArrayList<HashMap<Integer, HashMap<Integer, Double>>> SubMap) {
        int i;
        for (i = 1; i < AddMap.size(); i++) {
            MapAdd(AddMap.get(0), AddMap.get(i), false);
        }
        if(SubMap!=null) {
            for (i = 1; i < SubMap.size(); i++) {
                MapAdd(SubMap.get(0), SubMap.get(i), false);
            }
            MapAdd(AddMap.get(0), SubMap.get(0), true);
        }
    }

    private int ceil(int a, int b) {
        if (a % b == 0) {
            return a / b;
        } else {
            return a / b + 1;
        }
    }

    public double cost_old(SupplyNumberMap SNM, HashMap<Integer, HashMap<Integer, Integer>> DN, ShortestPathLRU SPC){
        if(this.size==1){
                return SNM.pure(SNM.path_con(this.route.get(0)[0], -1, this.route.get(0)[2], SPC),DN) + this.route.get(0)[2] * SNM.alpha;
        }else {
            ArrayList<HashMap<Integer, HashMap<Integer, Double>>> AddMap = new ArrayList<>();
            int i;
            for(i=0;i<this.size-1;i++){
                AddMap.add(SNM.path_con(this.route.get(i)[0], this.route.get(i+1)[0], this.route.get(i)[2], SPC));
            }
            AddMap.add(SNM.path_con(this.route.get(this.size-1)[0], -1, this.route.get(this.size-1)[2], SPC));
            MapCombine(AddMap, null);
            return SNM.pure(AddMap.get(0),DN) + this.route.get(this.size-1)[2] * SNM.alpha;
        }
    }
    public double cost_new(int idx1, int idx2, Request request, SupplyNumberMap SNM, HashMap<Integer, HashMap<Integer, Integer>> DN, ShortestPathLRU SPC){
        ArrayList<HashMap<Integer, HashMap<Integer, Double>>> AddMap = new ArrayList<>();
        if(idx1==idx2){
            int dis_ = SPC.dis(request.ls, request.le);
            if(this.size==1){
                int dis1 = SPC.dis(this.route.get(0)[0], request.ls);
                AddMap.add(SNM.path_con(this.route.get(0)[0], request.ls, this.route.get(0)[2], SPC));
                AddMap.add(SNM.path_con(request.ls, request.le, this.route.get(0)[2]+dis1, SPC));
                AddMap.add(SNM.path_con(request.le, -1, this.route.get(0)[2]+dis1+dis_, SPC));
                MapCombine(AddMap, null);
                return SNM.pure(AddMap.get(0),DN) + (this.route.get(0)[2]+dis1+dis_) * SNM.alpha;
            }else if(idx1==this.size){
                int cost_d = dis_ + SPC.dis(this.route.get(this.size - 1)[0], request.ls);
                int i;
                for (i=0;i<idx1-1;i++){
                    AddMap.add(SNM.path_con(this.route.get(i)[0], this.route.get(i+1)[0], this.route.get(i)[2], SPC));
                }
                AddMap.add(SNM.path_con(this.route.get(this.size-1)[0], request.ls, this.route.get(this.size-1)[2], SPC));
                AddMap.add(SNM.path_con(request.ls, request.le, this.route.get(this.size-1)[2]+cost_d-dis_, SPC));
                AddMap.add(SNM.path_con(request.le, -1, this.route.get(this.size-1)[2]+cost_d, SPC));
                MapCombine(AddMap, null);
                return SNM.pure(AddMap.get(0),DN) + (this.route.get(this.size-1)[2]+cost_d) * SNM.alpha;
            }else {
                int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
                int dis2 = SPC.dis(request.le, this.route.get(idx1)[0]);
                int cost_d = dis_ + dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[2];
                int i;
                for (i=0;i<idx1-1;i++){
                    AddMap.add(SNM.path_con(this.route.get(i)[0], this.route.get(i+1)[0], this.route.get(i)[2], SPC));
                }
                for (i=idx1;i<this.size-1;i++){
                    AddMap.add(SNM.path_con(this.route.get(i)[0], this.route.get(i+1)[0], this.route.get(i)[2]+cost_d, SPC));
                }
                AddMap.add(SNM.path_con(this.route.get(this.size-1)[0], -1, this.route.get(this.size-1)[2]+cost_d, SPC));

                AddMap.add(SNM.path_con(this.route.get(idx1-1)[0], request.ls, this.route.get(idx1-1)[2], SPC));
                AddMap.add(SNM.path_con(request.ls, request.le, this.route.get(idx1-1)[2]+dis1, SPC));
                AddMap.add(SNM.path_con(request.le, this.route.get(idx1)[0], this.route.get(idx1-1)[2]+dis1+dis_, SPC));
                MapCombine(AddMap, null);
                return SNM.pure(AddMap.get(0),DN) + (this.route.get(this.size-1)[2]+cost_d) * SNM.alpha;
            }
        }else if (idx2 == this.size) {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
            int dis2 = SPC.dis(request.ls, this.route.get(idx1)[0]);
            int cost_d1 = dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[2];
            int dis3 = SPC.dis(this.route.get(this.size - 1)[0], request.le);
            int cost_d = cost_d1 + dis3;
            int i;
            for (i = 0; i < idx1 - 1; i++) {
                AddMap.add(SNM.path_con(this.route.get(i)[0], this.route.get(i + 1)[0], this.route.get(i)[2], SPC));
            }
            for (i = idx1; i < this.size - 1; i++) {
                AddMap.add(SNM.path_con(this.route.get(i)[0], this.route.get(i + 1)[0], this.route.get(i)[2] + cost_d1, SPC));
            }
            AddMap.add(SNM.path_con(this.route.get(idx1 - 1)[0], request.ls, this.route.get(idx1 - 1)[2], SPC));
            AddMap.add(SNM.path_con(request.ls, this.route.get(idx1)[0], this.route.get(idx1 - 1)[2] + dis1, SPC));
            AddMap.add(SNM.path_con(this.route.get(this.size - 1)[0], request.le, this.route.get(this.size - 1)[2] + cost_d1, SPC));
            AddMap.add(SNM.path_con(request.le, -1, this.route.get(this.size - 1)[2] + cost_d, SPC));

            MapCombine(AddMap, null);
            return SNM.pure(AddMap.get(0), DN) + (this.route.get(this.size - 1)[2] + cost_d) * SNM.alpha;
        }else {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
            int dis2 = SPC.dis(request.ls, this.route.get(idx1)[0]);
            int cost_d1 = dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[2];
            int dis3 = SPC.dis(this.route.get(idx2 - 1)[0], request.le);
            int dis4 = SPC.dis(request.le, this.route.get(idx2)[0]);
            int cost_d = cost_d1 + dis3 + dis4 - this.route.get(idx2)[2] + this.route.get(idx2 - 1)[2];
            int i;
            for (i = 0; i < idx1 - 1; i++) {
                AddMap.add(SNM.path_con(this.route.get(i)[0], this.route.get(i + 1)[0], this.route.get(i)[2], SPC));
            }
            for (i = idx1; i < idx2 - 1; i++) {
                AddMap.add(SNM.path_con(this.route.get(i)[0], this.route.get(i + 1)[0], this.route.get(i)[2] + cost_d1, SPC));
            }
            for(i = idx2; i < this.size - 1; i++){
                AddMap.add(SNM.path_con(this.route.get(i)[0], this.route.get(i + 1)[0], this.route.get(i)[2] + cost_d, SPC));
            }
            AddMap.add(SNM.path_con(this.route.get(this.size-1)[0], -1, this.route.get(this.size-1)[2]+cost_d, SPC));

            AddMap.add(SNM.path_con(this.route.get(idx1 - 1)[0], request.ls, this.route.get(idx1 - 1)[2], SPC));
            AddMap.add(SNM.path_con(request.ls, this.route.get(idx1)[0], this.route.get(idx1 - 1)[2] + dis1, SPC));
            AddMap.add(SNM.path_con(this.route.get(idx2 - 1)[0], request.le, this.route.get(idx2 - 1)[2] + cost_d1, SPC));
            AddMap.add(SNM.path_con(request.le, this.route.get(idx2)[0], this.route.get(idx2 - 1)[2] + cost_d1 + dis3, SPC));
            MapCombine(AddMap, null);
            return SNM.pure(AddMap.get(0), DN) + (this.route.get(this.size - 1)[2] + cost_d) * SNM.alpha;
        }
    }

    public int racost(int idx1, int idx2, Request request, ShortestPathLRU SPC, int dis_){
        if (idx1 == idx2) {
            if (this.size == 1) {
                return dis_ + SPC.dis(this.route.get(0)[0], request.ls);
            } else if (idx1 == this.size) {
                return dis_ + SPC.dis(this.route.get(this.size - 1)[0], request.ls);
            } else {
                int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
                int dis2 = SPC.dis(request.le, this.route.get(idx1)[0]);
                return dis_ + dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[2];
            }
        } else if (idx2 == this.size) {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
            int dis2 = SPC.dis(request.ls, this.route.get(idx1)[0]);
            int cost_d1 = dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[2];
            int dis3 = SPC.dis(this.route.get(this.size - 1)[0], request.le);
            return cost_d1 + dis3;
        } else {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
            int dis2 = SPC.dis(request.ls, this.route.get(idx1)[0]);
            int cost_d1 = dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[2];
            int dis3 = SPC.dis(this.route.get(idx2 - 1)[0], request.le);
            int dis4 = SPC.dis(request.le, this.route.get(idx2)[0]);
            return cost_d1 + dis3 + dis4 - this.route.get(idx2)[2] + this.route.get(idx2 - 1)[2];
        }
    }

    public double cost(int idx1, int idx2, Request request, SupplyNumberMap SNM, HashMap<Integer, HashMap<Integer, Integer>> DN, ShortestPathLRU SPC, int dis_) {
        double cost_o;
        int cost_d;
        if (idx1 == idx2) {
            if (this.size == 1) {
                cost_d = dis_ + SPC.dis(this.route.get(0)[0], request.ls);
                ArrayList<HashMap<Integer, HashMap<Integer, Double>>> AddMap = new ArrayList<>();
                ArrayList<HashMap<Integer, HashMap<Integer, Double>>> SubMap = new ArrayList<>();
                SubMap.add(SNM.path_con(this.route.get(0)[0], -1, this.route.get(0)[2], SPC));
                AddMap.add(SNM.path_con(this.route.get(0)[0], request.ls, this.route.get(0)[2], SPC));
                AddMap.add(SNM.path_con(request.ls, request.le, this.route.get(0)[2] + cost_d - dis_, SPC));
                AddMap.add(SNM.path_con(request.le, -1, this.route.get(0)[2] + cost_d, SPC));
                MapCombine(AddMap, SubMap);
                cost_o = SNM.pure(AddMap.get(0), DN);
            } else if (idx1 == this.size) {
                cost_d = dis_ + SPC.dis(this.route.get(this.size - 1)[0], request.ls);
                ArrayList<HashMap<Integer, HashMap<Integer, Double>>> AddMap = new ArrayList<>();
                ArrayList<HashMap<Integer, HashMap<Integer, Double>>> SubMap = new ArrayList<>();
                SubMap.add(SNM.path_con(this.route.get(this.size - 1)[0], -1, this.route.get(this.size-1)[2], SPC));
                AddMap.add(SNM.path_con(this.route.get(this.size - 1)[0], request.ls, this.route.get(this.size - 1)[2], SPC));
                AddMap.add(SNM.path_con(request.ls, request.le, this.route.get(this.size - 1)[2] + cost_d - dis_, SPC));
                AddMap.add(SNM.path_con(request.le, -1, this.route.get(this.size - 1)[2] + cost_d, SPC));
                MapCombine(AddMap, SubMap);
                cost_o = SNM.pure(AddMap.get(0), DN);
            } else {
                int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
                int dis2 = SPC.dis(request.le, this.route.get(idx1)[0]);
                cost_d = dis_ + dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[2];
                ArrayList<HashMap<Integer, HashMap<Integer, Double>>> AddMap = new ArrayList<>();
                ArrayList<HashMap<Integer, HashMap<Integer, Double>>> SubMap = new ArrayList<>();

                AddMap.add(clone_sn(this.TSS.get(idx1 - 1).get(ceil(cost_d, 60))));
                /*    }catch (java.lang.IndexOutOfBoundsException e){
                    System.out.println(this.size+" "+idx1+" "+idx2+" "+this.route.size());
                    throw e;
                }*/
                SubMap.add(SNM.path_con(this.route.get(idx1 - 1)[0], this.route.get(idx1)[0], this.route.get(idx1 - 1)[2], SPC));
                AddMap.add(SNM.path_con(this.route.get(idx1 - 1)[0], request.ls, this.route.get(idx1 - 1)[2], SPC));
                AddMap.add(SNM.path_con(request.ls, request.le, this.route.get(idx1 - 1)[2] + dis1, SPC));
                AddMap.add(SNM.path_con(request.le, this.route.get(idx1)[0], this.route.get(idx1 - 1)[2] + dis1 + dis_, SPC));
                MapCombine(AddMap, SubMap);
                cost_o = SNM.pure(AddMap.get(0), DN);
            }
        } else if (idx2 == this.size) {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
            int dis2 = SPC.dis(request.ls, this.route.get(idx1)[0]);
            int cost_d1 = dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[2];
            int dis3 = SPC.dis(this.route.get(this.size - 1)[0], request.le);
            cost_d = cost_d1 + dis3;
            ArrayList<HashMap<Integer, HashMap<Integer, Double>>> AddMap = new ArrayList<>();
            ArrayList<HashMap<Integer, HashMap<Integer, Double>>> SubMap = new ArrayList<>();
            AddMap.add(clone_sn(this.TSS.get(idx1 - 1).get(ceil(cost_d1, 60))));
            SubMap.add(SNM.path_con(this.route.get(idx1 - 1)[0], this.route.get(idx1)[0], this.route.get(idx1 - 1)[2], SPC));
            SubMap.add(clone_sn(this.TSS.get(idx2 - 2).get(ceil(cost_d1, 60))));
            SubMap.add(SNM.path_con(this.route.get(this.size - 1)[0], -1, this.route.get(this.size - 1)[2], SPC));
            AddMap.add(SNM.path_con(this.route.get(idx1 - 1)[0], request.ls, this.route.get(idx1 - 1)[2], SPC));
            AddMap.add(SNM.path_con(request.ls, this.route.get(idx1)[0], this.route.get(idx1 - 1)[2] + dis1, SPC));
            AddMap.add(SNM.path_con(this.route.get(this.size - 1)[0], request.le, this.route.get(this.size - 1)[2] + cost_d1, SPC));
            AddMap.add(SNM.path_con(request.le, -1, this.route.get(this.size - 1)[2] + cost_d, SPC));
            MapCombine(AddMap, SubMap);
            cost_o = SNM.pure(AddMap.get(0), DN);
        } else {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
            int dis2 = SPC.dis(request.ls, this.route.get(idx1)[0]);
            int cost_d1 = dis1 + dis2 - this.route.get(idx1)[2] + this.route.get(idx1 - 1)[2];
            int dis3 = SPC.dis(this.route.get(idx2 - 1)[0], request.le);
            int dis4 = SPC.dis(request.le, this.route.get(idx2)[0]);
            cost_d = cost_d1 + dis3 + dis4 - this.route.get(idx2)[2] + this.route.get(idx2 - 1)[2];
            ArrayList<HashMap<Integer, HashMap<Integer, Double>>> AddMap = new ArrayList<>();
            ArrayList<HashMap<Integer, HashMap<Integer, Double>>> SubMap = new ArrayList<>();
            AddMap.add(clone_sn(this.TSS.get(idx1 - 1).get(ceil(cost_d1, 60))));
            SubMap.add(SNM.path_con(this.route.get(idx1 - 1)[0], this.route.get(idx1)[0], this.route.get(idx1 - 1)[2], SPC));
            SubMap.add(clone_sn(this.TSS.get(idx2 - 2).get(ceil(cost_d1, 60))));
            AddMap.add(clone_sn(this.TSS.get(idx2 - 1).get(ceil(cost_d, 60))));
            SubMap.add(SNM.path_con(this.route.get(idx2 - 1)[0], this.route.get(idx2)[0], this.route.get(idx2 - 1)[2], SPC));
            AddMap.add(SNM.path_con(this.route.get(idx1 - 1)[0], request.ls, this.route.get(idx1 - 1)[2], SPC));
            AddMap.add(SNM.path_con(request.ls, this.route.get(idx1)[0], this.route.get(idx1 - 1)[2] + dis1, SPC));
            AddMap.add(SNM.path_con(this.route.get(idx2 - 1)[0], request.le, this.route.get(idx2 - 1)[2] + cost_d1, SPC));
            AddMap.add(SNM.path_con(request.le, this.route.get(idx2)[0], this.route.get(idx2 - 1)[2] + cost_d1 + dis3, SPC));
            MapCombine(AddMap, SubMap);
            cost_o = SNM.pure(AddMap.get(0), DN);
        }
        return cost_d * SNM.alpha + cost_o;
    }

    public void insert(int idx1,int idx2,Request request,SupplyNumberMap SNM,ShortestPathLRU SPC, int dis_) {
        /*Gson gson = new Gson();
        String old = gson.toJson(this.route);
        double sn_sum=0;
        for(int time :SNM.SN.keySet()) sn_sum += SNM.SN.get(time).values().stream().mapToDouble(Double::doubleValue).sum();
        System.out.println(sn_sum);*/
        SNM.contribution(this, SPC, true);

        /*sn_sum=0;
        for(int time :SNM.SN.keySet()) sn_sum += SNM.SN.get(time).values().stream().mapToDouble(Double::doubleValue).sum();
        System.out.println(sn_sum);*/
        int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
        if (idx1 == idx2) {
            if (this.size > idx1) {
                int dis2 = SPC.dis(request.le, this.route.get(idx1)[0]);
                int detour = dis_ + dis1 + dis2 + this.route.get(idx1 - 1)[2] - this.route.get(idx1)[2];

                int num;
                for (num = idx1; num < this.size; num++) {
                    this.route.get(num)[2] += detour;
                    this.route.get(num)[4] -= detour;
                }
            }
            int pick1 = this.route.get(idx1 - 1)[5];
            int arr1 = this.route.get(idx1 - 1)[2];
            int slack1 = request.td - dis_ - arr1 - dis1;

            int[] temp0 = {request.ls, request.td - dis_, arr1 + dis1, request.a, slack1, pick1 + request.a};
            this.route.add(idx1, temp0);
            int[] temp1 = {request.le, request.td, arr1 + dis_ + dis1, -request.a, slack1, pick1};
            this.route.add(idx1 + 1, temp1);
            this.size += 2;

            int last_slack = this.route.get(this.size - 1)[4];
            int num;
            for (num = this.size - 2; num > 0; num--) {
                last_slack = min(this.route.get(num)[4], last_slack);
                this.route.get(num)[4] = last_slack;
            }
        } else {
            int dis2 = SPC.dis(request.ls, this.route.get(idx1)[0]);

            int detour1 = dis2 + dis1 + this.route.get(idx1 - 1)[2] - this.route.get(idx1)[2];

            int num;
            for (num = idx1; num < idx2; num++) {
                this.route.get(num)[2] += detour1;
                this.route.get(num)[4] -= detour1;
                this.route.get(num)[5] += request.a;
            }

            int dis3 = SPC.dis(this.route.get(idx2 - 1)[0], request.le);

            if (this.size > idx2) {
                int dis4 = SPC.dis(request.le, this.route.get(idx2)[0]);
                int detour2 = dis3 + dis4 + this.route.get(idx2 - 1)[2] - this.route.get(idx2)[2];

                for (num = idx2; num < this.size; num++) {
                    this.route.get(num)[2] += detour2;
                    this.route.get(num)[4] -= detour2;
                }
            }
            int arr1 = this.route.get(idx1 - 1)[2];
            int pick1 = this.route.get(idx1 - 1)[5];
            int arr2 = this.route.get(idx2 - 1)[2];
            int pick2 = this.route.get(idx2 - 1)[5];
            int slack2 = request.td - arr2 - dis3;

            int[] temp0 = {request.ls, request.td - dis_, arr1 + dis1, request.a, slack2, pick1 + request.a};
            this.route.add(idx1, temp0);
            int[] temp1 = {request.le, request.td, arr2 + dis3, -request.a, slack2, pick2 - request.a};
            this.route.add(idx2 + 1, temp1);
            this.size += 2;
            int last_slack = this.route.get(this.size - 1)[4];
            for (num = this.size - 2; num > 0; num--) {
                last_slack = min(last_slack, this.route.get(num)[4]);
                this.route.get(num)[4] = last_slack;
            }
        }

        if (idx1 == 1) {
            if (this.route.get(0)[0] == this.route.get(1)[0]) {
                while (this.route.get(0)[0] == this.route.get(1)[0]) {
                    this.route.remove(0);
                    this.size -= 1;
                    if (this.size == 1) {
                        if (this.route.get(0)[2] < request.tr) {
                            this.route.get(0)[1] = request.tr;
                            this.route.get(0)[2] = request.tr;
                        }
                        this.route.get(0)[4] = 6666;
                        this.route.get(0)[5] = 0;
                        this.current_path = new ArrayList<>();
                        this.TSS = null;
                        SNM.contribution(this, SPC, false);
                        return;
                    }
                }
            }

            this.current_path = new ArrayList<>();
            int[] temp = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0], this.route.get(0)[2],
                    request.tr, this.current_path);
            this.route.get(0)[0] = temp[0];
            this.route.get(0)[1] = temp[1];
            this.route.get(0)[2] = temp[1];
            this.route.get(0)[3] = 0;
            this.route.get(0)[4] = 6666;
            this.time = temp[1];
        }

        SNM.contribution(this, SPC, false);
        /*sn_sum=0;
        for(int time :SNM.SN.keySet()) sn_sum += SNM.SN.get(time).values().stream().mapToDouble(Double::doubleValue).sum();
        if(sn_sum>25921 || sn_sum<25919){
            System.out.println(SNM.node2region[16006]);
            System.out.println(SNM.node2region[32389]);
            System.out.println("Old: "+old);
            System.out.println("request: "+gson.toJson(request));
            System.out.println("new: "+gson.toJson(this.route));
            System.out.println(sn_sum);
            System.exit(1);
        }*/

        ArrayList<ArrayList<HashMap<Integer, HashMap<Integer, Double>>>> SSA = new ArrayList<>();
        int idx;
        SSA.add(null);
        for (idx = 1; idx <= this.size - 2; idx++) {
            SSA.add(SNM.supply_shift(this.route.get(idx)[0], this.route.get(idx + 1)[0],
                    this.route.get(idx)[2], this.route.get(idx + 1)[2], this.route.get(idx)[4], SPC));
        }
        SSA.add(SNM.supply_shift(this.route.get(this.size - 1)[0], this.route.get(this.size - 1)[0], this.route.get(this.size - 1)[2],
                this.route.get(this.size - 1)[2] + 10000, this.route.get(this.size - 1)[4], SPC));

        this.TSS = new ArrayList<>();
        int i, j;
        for (i = 0; i < this.size - 1; i++) {
            ArrayList<HashMap<Integer, HashMap<Integer, Double>>> temp = new ArrayList<>();
            for (j = 0; j < 1 + ceil(this.route.get(i + 1)[4], 60); j++) {
                temp.add(new HashMap<>());
            }
            this.TSS.add(temp);
        }
        int slack_time;
        for (slack_time = 0; slack_time < ceil(this.route.get(this.size-1)[4], 60); slack_time++) {
            this.TSS.get(this.size - 2).set(slack_time + 1, SSA.get(this.size - 1).get(slack_time));
        }

        for (idx = this.size - 2; idx > 0; idx--) {
            for (slack_time = 0; slack_time < ceil(this.route.get(idx)[4], 60); slack_time++) {
                this.TSS.get(idx - 1).set(slack_time + 1, SSA.get(idx).get(slack_time));
                MapAdd(this.TSS.get(idx - 1).get(slack_time + 1), this.TSS.get(idx).get(slack_time + 1), false);
            }
        }
    }

    public void rainsert(int idx1,int idx2,Request request,ShortestPathLRU SPC, int dis_) {
        if (idx1 == idx2) {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
            if (this.size > idx1) {
                int dis2 = SPC.dis(request.le, this.route.get(idx1)[0]);
                int detour = dis_ + dis1 + dis2 + this.route.get(idx1 - 1)[2] - this.route.get(idx1)[2];

                int num;
                for (num = idx1; num < this.size; num++) {
                    this.route.get(num)[2] += detour;
                    this.route.get(num)[4] -= detour;
                }
            }
            int pick1 = this.route.get(idx1 - 1)[5];
            int arr1 = this.route.get(idx1 - 1)[2];
            int slack1 = request.td - dis_ - arr1 - dis1;

            int[] temp0 = {request.ls, request.td - dis_, arr1 + dis1, request.a, slack1, pick1 + request.a};
            this.route.add(idx1, temp0);
            int[] temp1 = {request.le, request.td, arr1 + dis_ + dis1, -request.a, slack1, pick1};
            this.route.add(idx1 + 1, temp1);
            this.size += 2;

            int last_slack = this.route.get(this.size - 1)[4];
            int num;
            for (num = this.size - 2; num > 0; num--) {
                last_slack = min(this.route.get(num)[4], last_slack);
                this.route.get(num)[4] = last_slack;
            }
        } else {
            int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
            int dis2 = SPC.dis(request.ls, this.route.get(idx1)[0]);

            int detour1 = dis2 + dis1 + this.route.get(idx1 - 1)[2] - this.route.get(idx1)[2];

            int num;
            for (num = idx1; num < idx2; num++) {
                this.route.get(num)[2] += detour1;
                this.route.get(num)[4] -= detour1;
                this.route.get(num)[5] += request.a;
            }

            int dis3 = SPC.dis(this.route.get(idx2 - 1)[0], request.le);

            if (this.size > idx2) {
                int dis4 = SPC.dis(request.le, this.route.get(idx2)[0]);
                int detour2 = dis3 + dis4 + this.route.get(idx2 - 1)[2] - this.route.get(idx2)[2];

                for (num = idx2; num < this.size; num++) {
                    this.route.get(num)[2] += detour2;
                    this.route.get(num)[4] -= detour2;
                }
            }
            int arr1 = this.route.get(idx1 - 1)[2];
            int pick1 = this.route.get(idx1 - 1)[5];
            int arr2 = this.route.get(idx2 - 1)[2];
            int pick2 = this.route.get(idx2 - 1)[5];
            int slack2 = request.td - arr2 - dis3;

            int[] temp0 = {request.ls, request.td - dis_, arr1 + dis1, request.a, slack2, pick1 + request.a};
            this.route.add(idx1, temp0);
            int[] temp1 = {request.le, request.td, arr2 + dis3, -request.a, slack2, pick2 - request.a};
            this.route.add(idx2 + 1, temp1);
            this.size += 2;
            int last_slack = this.route.get(this.size - 1)[4];
            for (num = this.size - 2; num > 0; num--) {
                last_slack = min(last_slack, this.route.get(num)[4]);
                this.route.get(num)[4] = last_slack;
            }
        }
        if (idx1 == 1) {
            if (this.route.get(0)[0] == this.route.get(1)[0]) {
                while (this.route.get(0)[0] == this.route.get(1)[0]) {
                    this.route.remove(0);
                    this.size -= 1;
                    if (this.route.size() == 1) {
                        if (this.route.get(0)[2] < request.tr) {
                            this.route.get(0)[1] = request.tr;
                            this.route.get(0)[2] = request.tr;
                        }
                        this.route.get(0)[4] = 6666;
                        this.route.get(0)[5] = 0;
                        this.current_path = new ArrayList<>();
                        return;
                    }
                }
            }

            this.current_path = new ArrayList<>();
            int[] temp = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0], this.route.get(0)[2],
                    request.tr, this.current_path);
            this.route.get(0)[0] = temp[0];
            this.route.get(0)[1] = temp[1];
            this.route.get(0)[2] = temp[1];
            this.route.get(0)[3] = 0;
            this.route.get(0)[4] = 6666;
            this.time = temp[1];
        }
    }

    public void bainsert(int idx1,int idx2,Request request,SupplyNumberMap SNM,ShortestPathLRU SPC, int dis_) {
        SNM.contribution(this, SPC, true);
        int dis1 = SPC.dis(this.route.get(idx1 - 1)[0], request.ls);
        if(idx1 == idx2) {
            if (this.size > idx1) {
                int dis2 = SPC.dis(request.le, this.route.get(idx1)[0]);
                int detour = dis_ + dis1 + dis2 + this.route.get(idx1 - 1)[2] - this.route.get(idx1)[2];

                int num;
                for (num = idx1; num < this.size; num++) {
                    this.route.get(num)[2] += detour;
                    this.route.get(num)[4] -= detour;
                }
            }
            int pick1 = this.route.get(idx1 - 1)[5];
            int arr1 = this.route.get(idx1 - 1)[2];
            int slack1 = request.td - dis_ - arr1 - dis1;

            int[] temp0 = {request.ls, request.td - dis_, arr1 + dis1, request.a, slack1, pick1 + request.a};
            this.route.add(idx1, temp0);
            int[] temp1 = {request.le, request.td, arr1 + dis_ + dis1, -request.a, slack1, pick1};
            this.route.add(idx1 + 1, temp1);
            this.size += 2;

            int last_slack = this.route.get(this.size - 1)[4];
            int num;
            for (num = this.size - 2; num > 0; num--) {
                last_slack = min(this.route.get(num)[4], last_slack);
                this.route.get(num)[4] = last_slack;
            }
        } else {
            int dis2 = SPC.dis(request.ls, this.route.get(idx1)[0]);

            int detour1 = dis2 + dis1 + this.route.get(idx1 - 1)[2] - this.route.get(idx1)[2];

            int num;
            for (num = idx1; num < idx2; num++) {
                this.route.get(num)[2] += detour1;
                this.route.get(num)[4] -= detour1;
                this.route.get(num)[5] += request.a;
            }

            int dis3 = SPC.dis(this.route.get(idx2 - 1)[0], request.le);

            if (this.size > idx2) {
                int dis4 = SPC.dis(request.le, this.route.get(idx2)[0]);
                int detour2 = dis3 + dis4 + this.route.get(idx2 - 1)[2] - this.route.get(idx2)[2];

                for (num = idx2; num < this.size; num++) {
                    this.route.get(num)[2] += detour2;
                    this.route.get(num)[4] -= detour2;
                }
            }
            int arr1 = this.route.get(idx1 - 1)[2];
            int pick1 = this.route.get(idx1 - 1)[5];
            int arr2 = this.route.get(idx2 - 1)[2];
            int pick2 = this.route.get(idx2 - 1)[5];
            int slack2 = request.td - arr2 - dis3;

            int[] temp0 = {request.ls, request.td - dis_, arr1 + dis1, request.a, slack2, pick1 + request.a};
            this.route.add(idx1, temp0);
            int[] temp1 = {request.le, request.td, arr2 + dis3, -request.a, slack2, pick2 - request.a};
            this.route.add(idx2 + 1, temp1);
            this.size += 2;
            int last_slack = this.route.get(this.size - 1)[4];
            for (num = this.size - 2; num > 0; num--) {
                last_slack = min(last_slack, this.route.get(num)[4]);
                this.route.get(num)[4] = last_slack;
            }
        }
        if (idx1 == 1) {
            if (this.route.get(0)[0] == this.route.get(1)[0]) {
                while (this.route.get(0)[0] == this.route.get(1)[0]) {
                    this.route.remove(0);
                    this.size -= 1;
                    if (this.route.size() == 1) {
                        if (this.route.get(0)[2] < request.tr) {
                            this.route.get(0)[1] = request.tr;
                            this.route.get(0)[2] = request.tr;
                        }
                        this.route.get(0)[4] = 6666;
                        this.route.get(0)[5] = 0;
                        this.current_path = new ArrayList<>();
                        SNM.contribution(this, SPC, false);
                        return;
                    }
                }
            }

            this.current_path = new ArrayList<>();
            int[] temp = SPC.current_loc(this.route.get(0)[0], this.route.get(1)[0], this.route.get(0)[2],
                    request.tr, this.current_path);
            this.route.get(0)[0] = temp[0];
            this.route.get(0)[1] = temp[1];
            this.route.get(0)[2] = temp[1];
            this.route.get(0)[3] = 0;
            this.route.get(0)[4] = 6666;
            this.time = temp[1];
        }
        SNM.contribution(this, SPC, false);
    }

    public static void main(String[] args) throws IOException {
        Gson gson = new Gson();
        Route r = gson.fromJson("{\"time\":60,\"capacity\":3,\"size\":3,\"current_path\":[{\"key\":27526,\"value\":64},{\"key\":50299,\"value\":75},{\"key\":48665,\"value\":91},{\"key\":26958,\"value\":102},{\"key\":49110,\"value\":106},{\"key\":48733,\"value\":113},{\"key\":49365,\"value\":120},{\"key\":49665,\"value\":128},{\"key\":33544,\"value\":131}],\"route\":[[27526,64,64,0,6666,0],[45119,469,157,1,312,1],[28015,1950,1638,-1,312,0]]}",
                new TypeToken<Route>() {
                }.getType());
        Request re = gson.fromJson("{\"ls\":45119,\"le\":53443,\"tr\":60,\"td\":531,\"p\":10890,\"a\":1}",
                new TypeToken<Request>() {
                }.getType());
        int dis = 363;
        InputStreamReader in = new InputStreamReader(new FileInputStream("NYC/ny_locations_j.json"));
        ArrayList<Double[]> locations  = gson.fromJson(in,
                new TypeToken<ArrayList<Double[]>>(){ }.getType());
        in.close();
        System.out.println(r.rabound(re, dis, locations));
    }
}
