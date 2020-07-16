import javafx.util.Pair;
import java.util.*;

class Rtree_search_only {
    int min_child = 5;
    int max_child = 10;
    private int id_parents = Integer.MAX_VALUE-1;
    private node split1 = null;
    private node split2 = null;
    private int total_num = 0;
    private node head = null;
    static class Rec{
        int id;
        double l, r, d, u;
        double area;
        Rec(double x1, double x2, double y1, double y2, int id){
            l = x1;
            r = x2;
            d = y1;
            u = y2;
            area = (r-l)*(u-d);
            this.id = id;
        }
    }
    class node{
        int size;
        Rec node_rec;
        ArrayList<node> children;
        node(node node){
            node_rec = new Rec(node.node_rec.l, node.node_rec.r, node.node_rec.d, node.node_rec.u, id_parents--);
            size = 1;
            children = new ArrayList<>();
            children.add(node);
        }

        node(Rec rec){
            node_rec = rec;
            size = 1;
            children = null;
        }

        void addAll(HashMap<Integer, node> entries){
            children.addAll(entries.values());
            for(node new_:entries.values()){
                node_rec.l = Double.min(node_rec.l, new_.node_rec.l);
                node_rec.r = Double.max(node_rec.r, new_.node_rec.r);
                node_rec.d = Double.min(node_rec.d, new_.node_rec.d);
                node_rec.u = Double.max(node_rec.u, new_.node_rec.u);
            }
            node_rec.area = (node_rec.r-node_rec.l)*(node_rec.u-node_rec.d);
            size+=entries.size();
        }

        void add(node nw){
            children.add(nw);
            node_rec.l = Double.min(node_rec.l, nw.node_rec.l);
            node_rec.r = Double.max(node_rec.r, nw.node_rec.r);
            node_rec.d = Double.min(node_rec.d, nw.node_rec.d);
            node_rec.u = Double.max(node_rec.u, nw.node_rec.u);
            node_rec.area = (node_rec.r-node_rec.l)*(node_rec.u-node_rec.d);
            size ++;
        }
    }

    private void split(HashMap<Integer, node> entries){
        int[] init = pickseeds(entries);
        split1 = new node(entries.get(init[0]));
        split2 = new node(entries.get(init[1]));
        entries.remove(init[0]);
        entries.remove(init[1]);
        while(!entries.isEmpty()){
            if(split1.size+entries.size()==min_child){
                split1.addAll(entries);
                break;
            }else if(split2.size+entries.size()==min_child){
                split2.addAll(entries);
                break;
            }else{
                pick_next(entries, split1, split2);
            }
        }
    }

    private int[] pickseeds(HashMap<Integer, node> entries){
        int[] best = new int[2];
        double largest = -1;
        double max_l = -1;
        for(int id1: entries.keySet()){
            Rec r1 = entries.get(id1).node_rec;
            for(int id2: entries.keySet()){
                if(id1 > id2){
                     Rec r2 = entries.get(id2).node_rec;
                     double area = (Double.max(r1.r,r2.r)-Double.min(r1.l,r2.l))*
                             (Double.max(r1.u,r2.u)-Double.min(r1.d,r2.d))-
                             r1.area - r2.area;
                     if(area>largest){
                         if(area == 0){
                             double line = Math.pow(Double.min(Math.abs(r1.r-r2.r),Math.abs(r1.l-r2.l)), 2f)+
                                     Math.pow(Double.min(Math.abs(r1.u-r2.u),Math.abs(r1.d-r2.d)), 2f);
                             if(line>max_l){
                                 max_l = line;
                                 best[0] = id1;
                                 best[1] = id2;
                             }
                         }else {
                             largest = area;
                             best[0] = id1;
                             best[1] = id2;
                         }
                     }
                }
            }
        }
        return best;
    }

    private void pick_next(HashMap<Integer, node> entries, node n1, node n2){
        double max_diff = -1;
        double max_dl = -1;
        int next_id = -1;
        int side = -1;
        Rec r1 = n1.node_rec;
        Rec r2 = n2.node_rec;

        for(node nw:entries.values()){
            Rec rn = nw.node_rec;
            double a1 = ((Double.max(r1.r,rn.r)-Double.min(r1.l,rn.l))*
                    (Double.max(r1.u,rn.u)-Double.min(r1.d,rn.d))-r1.area);
            double a2 = ((Double.max(r2.r,rn.r)-Double.min(r2.l,rn.l))*
                    (Double.max(r2.u,rn.u)-Double.min(r2.d,rn.d))-r2.area);
            if (Math.abs(a1-a2)>max_diff){
                if(a1==0 && a2==0){
                    double l1 = Math.pow(Double.min(Math.abs(r1.r-rn.r),Math.abs(r1.l-rn.l)), 2f)+
                            Math.pow(Double.min(Math.abs(r1.u-rn.u),Math.abs(r1.d-rn.d)), 2f);
                    double l2 = Math.pow(Double.min(Math.abs(r2.r-rn.r),Math.abs(r2.l-rn.l)), 2f)+
                            Math.pow(Double.min(Math.abs(r2.u-rn.u),Math.abs(r2.d-rn.d)), 2f);
                    if(Math.abs(l1-l2)>max_dl){
                        max_dl = Math.abs(l1-l2);
                        next_id = rn.id;
                        if(l1>l2) side = 0;
                        else side = 1;
                    }
                }else {
                    max_diff = Math.abs(a1-a2);
                    next_id = rn.id;
                    if (a1 > a2) side = 0;
                    else side = 1;
                }
            }
        }
        if(side==1) n1.add(entries.get(next_id));
        else n2.add(entries.get(next_id));
        entries.remove(next_id);
    }

    void insertion(Rec rec){
        total_num++;
        if(head==null){
            head = new node(rec); // bottom leaf
            head = new node(head); // bottom node
        }else{
            choose_sub(head, new node(rec));
        }
    }

    private void choose_sub(node root, node nw){
        Rec rout_r = root.node_rec;
        Rec rn = nw.node_rec;
        double tl = Double.min(rout_r.l, rn.l);
        double tr = Double.max(rout_r.r, rn.r);
        double td = Double.min(rout_r.d, rn.d);
        double tu = Double.max(rout_r.u, rn.u);
        double ta = (rout_r.r-rout_r.l)*(rout_r.u-rout_r.d);

        boolean leaf_flag = false;
        for(node child: root.children){
            if(child.children==null){ // leaf rectangle
                leaf_flag = true;
            }
            break;
        }
        if(leaf_flag){
            if(root.size==max_child){
                HashMap<Integer, node> entries = new HashMap<>();
                for(node leaf: root.children){
                    entries.put(leaf.node_rec.id, leaf);
                }
                entries.put(rn.id, nw);
                split(entries);
                if(root==head){
                    head = new node(split1);
                    head.add(split2);
                    split1=null;
                    split2=null;
                }else {
                    return;
                }
            }else {
                root.add(nw);
            }
        }else{
            double min_i = Double.MAX_VALUE;
            double min_l = Double.MIN_VALUE;
            int best_id = -1;
            for(int idx=0;idx<root.children.size();idx++){
                Rec c_rec = root.children.get(idx).node_rec;
                if(c_rec.r>=rn.r && c_rec.l<=rn.l &&c_rec.u>=rn.u && c_rec.d<=rn.d){
                    best_id = idx;
                    break;
                }// the remaining case is in the same line
                double inc = (Double.max(c_rec.r, rn.r)-Double.min(c_rec.l, rn.l))*
                        (Double.max(c_rec.u, rn.u)-Double.min(c_rec.d, rn.d))-c_rec.area;
                if(inc<min_i){
                    if(inc==0){
                        double inl = Math.pow(Double.min(Math.abs(c_rec.r-rn.r),Math.abs(c_rec.l-rn.l)), 2f)+
                                Math.pow(Double.min(Math.abs(c_rec.u-rn.u),Math.abs(c_rec.d-rn.d)), 2f);
                        if(inl < min_l){
                            min_l = inl;
                            best_id = idx;
                        }
                    }else {
                        min_i = inc;
                        best_id = idx;
                    }
                }
            }
            choose_sub(root.children.get(best_id), nw);
            if(split1!=null){
                if(root.size==max_child){
                    root.children.remove(best_id);

                    HashMap<Integer, node> entries = new HashMap<>();
                    for(node child: root.children){
                        entries.put(child.node_rec.id, child);
                    }
                    entries.put(split1.node_rec.id, split1);
                    entries.put(split2.node_rec.id, split2);

                    split(entries);

                    if(root==head){
                        head = new node(split1);
                        head.add(split2);
                        split1=null;
                        split2=null;
                    }
                    return;
                }else{
                    root.children.add(split1);
                    root.children.add(split2);
                    split1=null;
                    split2=null;
                    root.children.remove(best_id);
                    root.size++;
                }
            }
        }
        root.node_rec.l = tl;
        root.node_rec.r = tr;
        root.node_rec.d = td;
        root.node_rec.u = tu;
        root.node_rec.area = ta;
    }

    public static class dis_com implements Comparator<Pair<Double,node>> {
        public int compare(Pair<Double,node> node1, Pair<Double,node> node2){
            return (Double.compare(node1.getKey(), node2.getKey()));
        }
    }

    int nearest(double x, double y){
        double upper = Double.MAX_VALUE;
        double dis = (Math.pow(Double.max(Double.max(x-head.node_rec.r, head.node_rec.l-x), 0f), 2f)+
                Math.pow(Double.max(Double.max(y-head.node_rec.u, head.node_rec.d-y), 0f), 2f));
        Comparator<Pair<Double, node>> comp = new dis_com();
        PriorityQueue<Pair<Double, node>> dj = new PriorityQueue<>(total_num, comp);
        dj.add( new Pair<>(dis, head) );
        Pair<Double, node> next;
        while (!dj.isEmpty()){
            next = dj.poll();
            node nn = next.getValue();
            if(nn.node_rec.id<this.total_num)return nn.node_rec.id;

            for(node child:nn.children){
                double dis_c = (Math.pow(Double.max(Double.max(x-child.node_rec.r, child.node_rec.l-x), 0f), 2f)+
                        Math.pow(Double.max(Double.max(y-child.node_rec.u, child.node_rec.d-y), 0f), 2f));
                if(child.node_rec.id<this.total_num && dis_c<upper) upper = dis_c;
                if(dis_c<=upper) dj.add(new Pair<>(dis_c, child));
            }
        }
        return -1;
    }

}
