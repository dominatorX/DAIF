import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.util.Pair;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class grid_dis {
    private String data_file = "./NYC/ny";
    private HashMap<Integer, HashMap<Integer, Integer>> all;
    private void process() throws IOException {
        Gson gson = new Gson();
        InputStreamReader in = new InputStreamReader(new FileInputStream(data_file+"_graph_j.json"));
        ArrayList<ArrayList<Integer>> Edges = gson.fromJson(in,
                new TypeToken<ArrayList<ArrayList<Integer>>>() {
                }.getType());
        in.close();

        HashMap<Integer, HashSet<Integer>> possible_in = new HashMap<>();
        HashMap<Integer, HashSet<Integer>> possible_out = new HashMap<>();
        for (ArrayList<Integer> Edge : Edges) {
            int x, y, c;
            x = Edge.get(0);
            y = Edge.get(1);
            c = Edge.get(2);
            if (c == 0) {
                System.out.println("zero cost");
                System.out.println(Edge);
            }
            if (!Edge.get(3).equals(Edge.get(4))) {
                int area1 = Edge.get(3);
                if (possible_in.get(area1) == null) {
                    HashSet<Integer> temp = new HashSet<>();
                    temp.add(x);
                    possible_in.put(area1, temp);
                } else {
                    possible_in.get(area1).add(x);
                }
                int area2 = Edge.get(4);
                if (possible_out.get(area2) == null) {
                    HashSet<Integer> temp = new HashSet<>();
                    temp.add(y);
                    possible_out.put(area2, temp);
                } else {
                    possible_out.get(area2).add(y);
                }
            }
        }
        all = new HashMap<>(possible_in.keySet().size());

        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (Integer origins : possible_in.keySet()) {
            executor.execute(new single(origins, possible_in.get(origins), possible_out));//, graph, reverseGraph));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        }

        String jsonObject = gson.toJson(all);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(data_file+"_inter_region_cost_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
    }

    class single implements Runnable {
        private HashMap<Integer,Integer> temp = new HashMap<>(); // The character to print
        private int id; // The number of times to repeat
        private HashMap<Integer,HashSet<Integer>> possible_out;
        private HashSet<Integer> origins;
        private CHSP.Vertex[] graph;
        /** Construct a task with a specified character and number of
         * times to print the character
         */
        single(int id, final HashSet<Integer> aim, final HashMap<Integer,HashSet<Integer>> possible_out) throws IOException {
            Gson gson = new Gson();
            InputStreamReader in = new InputStreamReader(new FileInputStream("./NYC/ny_graph_h_j.json"));
            this.graph = gson.fromJson(in, new TypeToken<CHSP.Vertex[]>(){ }.getType());
            in.close();

            this.id = id;
            this.origins = aim;
            this.possible_out = possible_out;
        }

        private void dijkstra_lengths(int N, int S, ArrayList< Integer > distanceFromSource) {
            Comparator<Pair<Integer,Integer>> comp = new ShortestPathLRU.pair_com();
            for(int i = 0; i < N; i++) distanceFromSource.add( i, 1000000);

            distanceFromSource.set(S, 0);
            PriorityQueue<Pair<Integer, Integer>> dj = new PriorityQueue<>(this.graph.length, comp);
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

                for(int i = 0; i < graph[ u ].outEdges.size(); i++) {
                    v = graph[ u ].outEdges.get( i );
                    alt = distanceFromSource.get( u ) + graph[ u ].outECost.get( i );
                    if( alt < distanceFromSource.get( v ) )
                    { 	distanceFromSource.set(v, alt);
                        dj.add( new Pair<>(-alt, v) );
                    }
                }
            }
        }
        @Override
        /** Override the run() method to tell the system
         * what task to perform
         */
        public void run() {
            for (Integer origin : this.origins) {
                ArrayList<Integer> distanceFromSource = new ArrayList<>(this.graph.length);
                dijkstra_lengths(this.graph.length, origin, distanceFromSource);
                for (Integer aims : this.possible_out.keySet()) {
                    if (!aims.equals(this.id)) {
                        for (Integer aim : this.possible_out.get(aims)) {
                            int dis = distanceFromSource.get(aim);
                            if (this.temp.getOrDefault(aims, 999999) > dis) {
                                this.temp.put(aims, dis);
                            }
                        }
                    }
                }
            }
            all.put(this.id, this.temp);
        }
    }


    public static void main(String[] args) throws IOException {
        grid_dis prune = new grid_dis();
        prune.process();
    }
}

