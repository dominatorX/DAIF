import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;

import java.io.*;
import java.util.*;

public class ShortestPathLRU {
    final private Gson gson = new Gson();
    private BiSP.Vertex[] graph;
    private BiSP.Vertex[] reverseGraph;
    private CHSP.Vertex[] CHgraph;
    private HashMap<Integer,HashMap<Integer, ArrayList<Integer>>> edges = new HashMap<>();
    private ArrayList<Boolean> frequentPickup;
    private ArrayList<Boolean> frequentDrop;
    private HashMap< Integer, HashMap<Integer, Integer> > distanceFrequentNodes;
    private HashMap<Integer, HashMap<Integer, String>> pathFrequentNodes;
    private final int SD_size = 64*1024*1024;
    private final int SP_size = 4*1024*1024;
    private LinkedHashMap<String, Integer> cacheSD = new LinkedHashMap<>(SD_size, 0.75f){
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > SD_size;
        }
    };
    private LinkedHashMap<String, ArrayList<Integer>> cacheSP = new LinkedHashMap<>(SP_size, 0.75f){
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > SP_size;
        }
    };
    private LinkedHashMap<String, String> cachePR = new LinkedHashMap<>(SP_size, 0.75f){
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > SP_size;
        }
    };
    private CHSP.BidirectionalDijkstra bd = new CHSP.BidirectionalDijkstra();
    private long count = 0;

    public void init(String graph_file, String data_set) throws IOException {
        InputStreamReader in = new InputStreamReader(new FileInputStream(graph_file+"_graph_o_j.json"));
        this.graph = gson.fromJson(in,
                new TypeToken<BiSP.Vertex[]>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(graph_file+"_graph_r_j.json"));
        this.reverseGraph = gson.fromJson(in,
                new TypeToken<BiSP.Vertex[]>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(graph_file+"_graph_h_j.json"));
        this.CHgraph = gson.fromJson(in, new TypeToken<CHSP.Vertex[]>(){ }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(graph_file+"_edges_j.json"));
        this.edges = gson.fromJson(in,
                new TypeToken<HashMap<Integer,HashMap<Integer, ArrayList<Integer>>>>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(data_set+"_fre_dis.json"));
        distanceFrequentNodes = gson.fromJson(in,
                new TypeToken<HashMap< Integer, HashMap<Integer, Integer> > >() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(data_set+"_fre_path.json"));
        pathFrequentNodes = gson.fromJson(in,
                new TypeToken<HashMap< Integer, HashMap<Integer, String> > >() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(data_set+"_fre_pick.json"));
        frequentPickup = gson.fromJson(in,
                new TypeToken<ArrayList<Boolean>>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(data_set+"_fre_drop.json"));
        frequentDrop = gson.fromJson(in,
                new TypeToken<ArrayList<Boolean>>() {
                }.getType());
        in.close();
    }

    public void intializeFrequent(String graph_file, String data_set, String req) throws IOException {
        InputStreamReader in = new InputStreamReader(new FileInputStream(graph_file+"_graph_o_j.json"));
        this.graph = gson.fromJson(in,
                new TypeToken<BiSP.Vertex[]>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(graph_file+"_graph_r_j.json"));
        this.reverseGraph = gson.fromJson(in,
                new TypeToken<BiSP.Vertex[]>() {
                }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(graph_file+"_graph_h_j.json"));
        this.CHgraph = gson.fromJson(in, new TypeToken<CHSP.Vertex[]>(){ }.getType());
        in.close();
        in = new InputStreamReader(new FileInputStream(graph_file+"_edges_j.json"));
        this.edges = gson.fromJson(in,
                new TypeToken<HashMap<Integer,HashMap<Integer, ArrayList<Integer>>>>() {
                }.getType());
        in.close();
        this.pathFrequentNodes = new HashMap<>();
        this.distanceFrequentNodes = new HashMap<>();

        int N = this.graph.length;
        this.frequentPickup = new ArrayList<>(Collections.nCopies(N,false));
        this.frequentDrop = new ArrayList<>(Collections.nCopies(N,false));

        ArrayList<Integer> pick = new ArrayList<>(Collections.nCopies(N,0));
        ArrayList<Integer> drop = new ArrayList<>(Collections.nCopies(N,0));

        try {
            File file = new File(req);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String str;

            while ((str = br.readLine())!=null) {
                pick.set(Integer.parseInt(str.split(" ")[1]), pick.get(Integer.parseInt(str.split(" ")[1])) + 1);
                drop.set(Integer.parseInt(str.split(" ")[3]), drop.get(Integer.parseInt(str.split(" ")[3])) + 1);
            }

            br.close();
            fr.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Integer> pick_array = (ArrayList<Integer>) pick.clone();
        ArrayList<Integer> drop_array = (ArrayList<Integer>) drop.clone();

        Collections.sort(pick_array);
        Collections.sort(drop_array);

        int threshold = pick_array.get( pick_array.size()*95/100 );

        for( int i = 0; i< N; i++ ) {
            frequentPickup.set( i , pick.get(i) >= threshold);
        }

        threshold = drop_array.get( drop_array.size()*95/100 );

        for( int i = 0; i< N; i++ ) {
            frequentDrop.set( i , drop.get(i) >= threshold);
        }

        for( int i = 0; i< N; i++ ) {
            if( !frequentPickup.get(i) )
                continue;
            //System.out.println("build for node "+i);
            distanceFrequentNodes.put(i, new HashMap<>());

            ArrayList< Integer > distanceFromSourceL = new ArrayList<>( N );
            dijkstra_lengths(N, i, distanceFromSourceL);

            for( int j = 0;  j < N; j++ ) {
                if( frequentDrop.get( j ) ) {
                    if(distanceFromSourceL.get( j ) != 1000000){
                        distanceFrequentNodes.get( i ).put(j, distanceFromSourceL.get( j ));
                    }else {
                        distanceFrequentNodes.get( i ).put(j, -1);
                    }
                }
            }
        }
        String jsonObject = gson.toJson(frequentPickup);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(data_set+"_fre_pick.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        jsonObject = gson.toJson(frequentDrop);
        out = new OutputStreamWriter(new FileOutputStream(data_set+"_fre_drop.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        jsonObject = gson.toJson(distanceFrequentNodes);
        out = new OutputStreamWriter(new FileOutputStream(data_set+"_fre_dis.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        jsonObject = gson.toJson(pathFrequentNodes);
        out = new OutputStreamWriter(new FileOutputStream(data_set+"_fre_path.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
    }

    public static class pair_com implements Comparator<Pair<Integer,Integer>>{
        public int compare(Pair<Integer,Integer> node1, Pair<Integer,Integer> node2){
            return (Integer.compare(node2.getKey(), node1.getKey()));
        }
    }

    private void dijkstra_lengths(int N, int S, ArrayList< Integer > distanceFromSource) {
        Comparator<Pair<Integer,Integer>> comp = new pair_com();
        ArrayList<Integer> prevNode = new ArrayList<>(N);
        for(int i = 0; i < N; i++)
        { 	distanceFromSource.add( i, 1000000);
            prevNode.add(i, -1);
        }

        distanceFromSource.set(S, 0);
        PriorityQueue<Pair<Integer, Integer>> dj = new PriorityQueue<>(graph.length, comp);
        dj.add( new Pair<>(0, S) );

        Pair<Integer, Integer> x;
        int u, v;
        Integer alt;

        while( dj.size() != 0 )
        {
            x = dj.poll();
            u = x.getValue();

            if( distanceFromSource.get( u ) >= 1000000 )
                break;

            for(int i = 0; i < graph[ u ].adjList.size(); i++) {
                v = graph[ u ].adjList.get( i );
                alt = distanceFromSource.get( u ) + graph[ u ].costList.get( i );
                if( alt < distanceFromSource.get( v ) )
                { 	distanceFromSource.set(v, alt);
                    dj.add( new Pair<>(-alt, v) );
                    prevNode.set(v, u);
                }
            }
        }
        for(int i = 0; i < N; i++) {
            if( distanceFromSource.get( i ) >= 1000000 ) {
                distanceFromSource.set(i, 1000000);
                prevNode.set(i, -1);
            }
        }

        pathFrequentNodes.put(S, new HashMap<>());
        for (int node=0; node<frequentDrop.size();node++) {
            if (!frequentDrop.get(node)){
                continue;
            }
            ArrayList<Integer> path = new ArrayList<>();
            int now = node;
            while (true) {
                path.add(now);
                if ((now == S) || (prevNode.get(now) == -1))
                    break;
                now = prevNode.get(now);
            }
            Collections.reverse(path);

            if (path.get(0)!=S){
                pathFrequentNodes.get(S).put(node, "-1/null/0");
                continue;
            }
            ArrayList<ArrayList<Integer>> tour = new ArrayList<>();
            int time_here = 0;
            int time_all = 0;
            int i;
            for(i=0;i<path.size()-1;i++){
                ArrayList<Integer> info = this.edges.get(path.get(i)).get(path.get(i+1));
                time_all += info.get(0);
                if(info.get(1)!=-1000){
                    ArrayList<Integer> temp = new ArrayList<>();
                    temp.add(info.get(1));
                    temp.add(info.get(2));
                    temp.add(time_here+info.get(0));
                    tour.add(temp);
                    time_here = 0;
                } else {
                    time_here += info.get(0);
                }
            }
            pathFrequentNodes.get(S).put(node, time_all+"/"+this.gson.toJson(tour)+"/"+time_here);
        }
    }

    public int dis(int l1, int l2){
        if (l1 == l2){
            return 0;
        }
        if( frequentPickup.get(l1) && frequentDrop.get(l2) ) {
            return distanceFrequentNodes.get(l1).get(l2);
        }
        String key = l1+"-"+l2;
        Integer ans = cacheSD.get(key);
        if (ans == null) {
            ans = bd.computeDist(this.CHgraph, l1, l2, this.count++);
            cacheSD.put(key, ans);
        }
        return ans;
    }

    public String ex_dis(int l1, int l2){
        if (l1 == l2){
            return "0/null/0";
        }

        if( frequentPickup.get(l1) && frequentDrop.get(l2) ) {
            return pathFrequentNodes.get(l1).get(l2);
        }

        String key = l1+"-"+l2;
        if( cachePR.get( key ) != null ) {
            return cachePR.get( key );
        }

        ArrayList<Integer> path = cacheSP.get(key);
        if (path == null) {
            path = BiSP.computePath(this.graph, this.reverseGraph, l1, l2);
            cacheSP.put(key, path);
        }
        if (path==null){
            cachePR.put( key, "-1/null/0");
            return "-1/null/0";
        }
        ArrayList<ArrayList<Integer>> tour = new ArrayList<>();
        int time_here = 0;
        int time_all = 0;
        int i;
        for(i=0;i<path.size()-1;i++){
            ArrayList<Integer> info = this.edges.get(path.get(i)).get(path.get(i+1));
            time_all += info.get(0);
            if(info.get(1)!=-1000){
                ArrayList<Integer> temp = new ArrayList<>();
                temp.add(info.get(1));
                temp.add(info.get(2));
                temp.add(time_here+info.get(0));
                tour.add(temp);
                time_here = 0;
            } else {
                time_here += info.get(0);
            }
        }
        String answer = time_all+"/"+this.gson.toJson(tour)+"/"+time_here;
        cachePR.put( key, answer);
        return answer;
    }

    int [] current_loc(int l1, int l2, int start_time, int current_time, ArrayList<Pair<Integer, Integer>> current_path) {
        int [] current = new int[2];

        String key=l1+"-"+l2;
        ArrayList<Integer> path = cacheSP.get(key);
        if (path == null) {
            path = BiSP.computePath(this.graph, this.reverseGraph, l1, l2);
            cacheSP.put(key, path);
        }

        int time_all = start_time;
        int i;
        boolean flag = true;
        for (i=0;i<path.size()-1;i++) {
            int cost = this.edges.get(path.get(i)).get(path.get(i + 1)).get(0);
            if(flag) {
                if (time_all >= current_time) {
                    current[0] = path.get(i);
                    current[1] = time_all;
                    flag = false;
                    current_path.add(new Pair<>(path.get(i), time_all));
                }
            }else{
                current_path.add(new Pair<>(path.get(i), time_all));
            }
            time_all += cost;
        }
        if(flag){
            return null;
        }else {
            return current;
        }
    }

    public static void main(String[] args) throws IOException {
        ShortestPathLRU SPC = new ShortestPathLRU();
        SPC.intializeFrequent("./NYC/ny","./NYC/ny", "./NYC/ny_output_price_12");
    }
}
