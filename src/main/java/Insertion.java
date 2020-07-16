import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class Insertion {
    public Pair<Pair<Integer,Integer>,Integer> Leinsertion(Route route, Request request, ShortestPathLRU SPC, int dis_){
        //route.update(request.tr, SPC, false);
        int[] idx_ = {-1, -1};
        int cost_ = Integer.MAX_VALUE;
        ArrayList<Integer> Dio = new ArrayList<>();
        ArrayList<Integer> Plc = new ArrayList<>();
        // route ddl, arr, slack, picked -> 1, 2, 4, 5
        int j;
        int cost_j;
        Dio.add(Integer.MAX_VALUE);
        Dio.add(Integer.MAX_VALUE);
        Plc.add(-1);
        Plc.add(-1);
        for (j = 1; j < route.size + 1; j++) {

            if (route.feasible(j, j, request, SPC, dis_)) {
                int cost_t = route.racost(j, j, request, SPC, dis_);
                if (cost_t < cost_) {
                    cost_ = cost_t;
                    idx_[0] = j;
                    idx_[1] = j;
                }
            }
            if (j > 1 && Dio.get(j) != Integer.MAX_VALUE) {
                int dis1 = SPC.dis(route.route.get(j - 1)[0], request.le);
                if(dis1!=-1) {
                    if (j == route.size) {
                        cost_j = dis1 + Dio.get(j);
                        if (cost_j + route.route.get(j - 1)[2] > request.td) {
                            cost_j = Integer.MAX_VALUE;
                        }
                    } else {
                        int dis2 = SPC.dis(request.le, route.route.get(j)[0]);
                        if(dis2!=-1) {
                            int arr = dis1 + route.route.get(j - 1)[2] + Dio.get(j);
                            cost_j = dis2 - route.route.get(j)[2] + arr;
                            if (cost_j > route.route.get(j)[4] || arr > request.td) {
                                cost_j = Integer.MAX_VALUE;
                            }
                        }else {
                            cost_j = Integer.MAX_VALUE;
                        }
                    }
                    if (cost_j < cost_) {
                        cost_ = cost_j;
                        idx_[0] = Plc.get(j);
                        idx_[1] = j;
                    }
                }
            }

            if (j == route.size) break;
            if (route.route.get(j)[2] > request.td) {
                break;
            }

            if (route.route.get(j)[5] + request.a > route.capacity ||
                    route.route.get(j - 1)[5] + request.a > route.capacity) {
                Dio.add(Integer.MAX_VALUE);
                Plc.add(-1);
            } else {
                int dis1 = SPC.dis(route.route.get(j - 1)[0], request.ls);
                int dis2 = SPC.dis(request.ls, route.route.get(j)[0]);
                if(dis1!=-1 && dis2!=-1) {
                    int det = dis1 + dis2 + route.route.get(j - 1)[2] - route.route.get(j)[2];
                    if (det > route.route.get(j)[4] || det > Dio.get(j)) {
                        Dio.add(Dio.get(j));
                        Plc.add(Plc.get(j));
                    } else {
                        Dio.add(det);
                        Plc.add(j);
                    }
                }else {
                    Dio.add(Dio.get(j));
                    Plc.add(Plc.get(j));
                }
            }
        }

        if (idx_[0] == -1 || cost_ > request.p) {
            return null;
        } else {
            return new Pair<>(new Pair<>(idx_[0], idx_[1]), cost_);
        }
    }

    public Pair<Pair<Integer,Integer>,Double> DPinsertion(Route route, Request request, HashMap<Integer, HashMap<Integer,Integer>>  DN, SupplyNumberMap SNM, ShortestPathLRU SPC, int dis_){
        //route.update(request.tr, SPC,true);
        int [] idx_ = {-1,-1};
        double cost_ = Double.MAX_VALUE;
        int i,j;
        for (i=1;i<route.size+1;i++) {
            if (route.route.get(i - 1)[2] > request.td - dis_) {
                break;
            } // check if arrive too late
            if (i!=route.route.size() && route.route.get(i)[0] == route.route.get(i-1)[0]){
                continue;
            } // pass if visit same node
            for (j = i; j < route.size + 1; j++) {
                if (route.route.get(j - 1)[2] > request.td) {
                    break;
                } else if ((j==route.route.size() || route.route.get(j)[0] != route.route.get(j-1)[0]) &&
                        route.feasible(i, j, request, SPC, dis_))
                {
                    double cost_t = route.cost(i, j, request, SNM, DN, SPC, dis_);
                    if (cost_t < cost_) {
                        cost_ = cost_t;
                        idx_[0] = i;
                        idx_[1] = j;
                    }
                }
                if(route.route.get(j-1)[0]==request.le){
                    break;
                } // check if arrive destination
            }
            if(route.route.get(i-1)[0]==request.ls){
                break;
            }  // check if arrive source
        }
        if (idx_[0] == -1 || cost_ > request.p) {
            return null;
        } else{
            return new Pair<>(new Pair<>(idx_[0],idx_[1]),cost_);
        }
    }

    public Pair<Pair<Integer,Integer>,Double> Bainsertion(Route route, Request request, HashMap<Integer, HashMap<Integer,Integer>>  DN, SupplyNumberMap SNM, ShortestPathLRU SPC, int dis_){
        //route.update(request.tr, SPC,false);
        int [] idx_ = {-1,-1};
        double cost_old = route.cost_old(SNM,DN,SPC);
        double cost_ = Double.MAX_VALUE;
        int i,j;
        for (i=1;i<route.size+1;i++) {
            if (route.route.get(i - 1)[2] > request.td - dis_) {
                break;
            } // check if arrive too late
            if (i!=route.route.size() && route.route.get(i)[0] == route.route.get(i-1)[0]){
                continue;
            } // pass if visit same node
            for (j = i; j < route.size + 1; j++) {
                if (route.route.get(j - 1)[2] > request.td) {
                    break;
                } else if ((j==route.route.size() || route.route.get(j)[0] != route.route.get(j-1)[0]) &&
                        route.feasible(i, j, request, SPC, dis_)) {
                    double cost_t = route.cost_new(i, j, request, SNM, DN, SPC)-cost_old;
                    if (cost_t < cost_) {
                        cost_ = cost_t;
                        idx_[0] = i;
                        idx_[1] = j;
                    }
                }
                if(route.route.get(j-1)[0]==request.le){
                    break;
                } // check if arrive destination
            }
            if(route.route.get(i-1)[0]==request.ls){
                break;
            }  // check if arrive source
        }
        if (idx_[0] == -1 || cost_ > request.p) {
            return null;
        } else{
            return new Pair<>(new Pair<>(idx_[0],idx_[1]),cost_);
        }
    }
}
