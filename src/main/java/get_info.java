import com.google.gson.Gson;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class get_info {
    public static void main(String[] args) throws IOException {
        String data_type = "./NYC/ny";
        FileReader fr1 = new FileReader(data_type+"_edge");
        BufferedReader bf1 = new BufferedReader(fr1);
        FileReader fr2 = new FileReader(data_type+"Index");
        BufferedReader bf2 = new BufferedReader(fr2);
        BufferedReader loc = new BufferedReader(new FileReader(data_type+"_location"));
        BufferedReader Yedges = new BufferedReader(new FileReader(data_type+"_edge_time"));

        final double [] range_ = {40.51d, 40.91d, -74.26d, -73.7d};
        HashMap<Long,Integer> r_nodes_reverse = new HashMap<>();
        String str1,str2;
        bf2.readLine();

        int total_nodes = 61298;
        while ((str2 = bf2.readLine()) != null) {
            str1 = bf1.readLine();
            r_nodes_reverse.put(Long.parseLong(str1.split(",")[0]),Integer.parseInt(str2.split(" ")[0]));
            r_nodes_reverse.put(Long.parseLong(str1.split(",")[1]),Integer.parseInt(str2.split(" ")[1]));
        }

        bf1.close();
        fr1.close();
        bf2.close();
        fr2.close();

        double lan,lon;

        ArrayList<ArrayList<Double>> lalo2id = new ArrayList<>(total_nodes);
        int [] node2region = new int[total_nodes];
        HashMap<Integer, HashSet<Integer>> region2node = new HashMap <>();
        HashSet<Integer> region = new HashSet<>();
        int i;
        for (i=0;i<total_nodes;i++){
            lalo2id.add(new ArrayList<>(2));
        }
        while ((str1 = loc.readLine()) != null) {

            lan = Double.parseDouble(str1.split(",")[1]);
            if (lan>40.91){
                lan = 40.909999;
            }
            lon = Double.parseDouble(str1.split(",")[2]);
            ArrayList<Double> temp = new ArrayList<>(2);
            temp.add(lan);
            temp.add(lon);
            int id = r_nodes_reverse.get(Long.parseLong(str1.split(",")[0]));
            int diff1,diff2;
            diff1 = (int)((lan-range_[0])*50);
            diff2 = (int)((lon-range_[2])*50);
            int area = diff1*28 + diff2;
            lalo2id.set(id,temp);
            node2region[id] = area;
            region.add(area);
            if (region2node.get(area)==null){
                HashSet<Integer> nodes = new HashSet<>();
                nodes.add(id);
                region2node.put(area, nodes);
            }else {
                region2node.get(area).add(id);
            }
        }
        loc.close();

        ArrayList<ArrayList<Integer>> Edges = new ArrayList<>();
        while ((str1 = Yedges.readLine()) != null) {
            ArrayList<Integer> Edge = new ArrayList<>();
            int source = r_nodes_reverse.get(Long.parseLong(str1.split(",")[0]));
            int des = r_nodes_reverse.get(Long.parseLong(str1.split(",")[1]));
            Edge.add(source);
            Edge.add(des);
            int weight = (int)(Double.parseDouble(str1.split(",")[4])*60);
            if (weight==0){
                Edge.add(1);
            }else {
                Edge.add(weight);
            }
            if (node2region[source]==node2region[des]){
                Edge.add(0);
                Edge.add(0);
            }else {
                Edge.add(node2region[source]);
                Edge.add(node2region[des]);
            }
            Edges.add(Edge);
        }

        Gson gson = new Gson();
        String jsonObject = gson.toJson(r_nodes_reverse);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(data_type+"_nodes_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        jsonObject = gson.toJson(region2node);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_region2node_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        jsonObject = gson.toJson(node2region);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_node2region_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        jsonObject = gson.toJson(region);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_regions_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        System.out.print("finish saving dictionary");

        jsonObject = gson.toJson(lalo2id);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_rtree_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        jsonObject = gson.toJson(Edges);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_graph_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        jsonObject = gson.toJson(lalo2id);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_locations_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        System.out.print("1.finish saving graph");

        int n = lalo2id.size();	//number of vertices in the graph.

        CHSP.Vertex[] graph = new CHSP.Vertex[n];

        //initialize the graph.
        for(i=0;i<n;i++){
            graph[i] = new CHSP.Vertex(i);
        }

        //get edges
        for (ArrayList<Integer> Edge:Edges) {
            int x, y, c;
            x = Edge.get(0);
            y = Edge.get(1);
            c = Edge.get(2);

            graph[x].outEdges.add(y);
            graph[x].outECost.add(c);
            graph[y].inEdges.add(x);
            graph[y].inECost.add(c);
        }

        CHSP.PreProcess process = new CHSP.PreProcess();
        process.processing(graph);

        jsonObject = gson.toJson(graph);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_graph_h_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        System.out.print("2.finish generating CH graph");

        BiSP.Vertex [] bi_graph = new BiSP.Vertex[n];
        BiSP.Vertex [] reverseGraph = new BiSP.Vertex[n];
        HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> edges = new HashMap<>();

        //initialize the vertices.
        for(i=0;i<n;i++){
            bi_graph[i]=new BiSP.Vertex(i);
            reverseGraph[i]=new BiSP.Vertex(i);
        }

        //get the edges.
        for(ArrayList<Integer> Edge:Edges){
            int u, v;
            int w;
            u = Edge.get(0);  //start vertex
            v = Edge.get(1);   //end vertex
            w = Edge.get(2);   //weight of edge

            bi_graph[u].adjList.add(v);
            bi_graph[u].costList.add(w);

            reverseGraph[v].adjList.add(u);
            reverseGraph[v].costList.add(w);

            if (edges.get(u)==null){
                HashMap<Integer,ArrayList<Integer>> temp0 = new HashMap<>();
                edges.put(u, temp0);
            }
            ArrayList<Integer> temp1 = new ArrayList<>(3);
            temp1.add(w);
            if (Edge.get(3).equals(Edge.get(4))){
                temp1.add(-1000);
                temp1.add(-1000);
            }else {
                temp1.add(Edge.get(3));
                temp1.add(Edge.get(4));
            }
            edges.get(u).put(v,temp1);
        }
        jsonObject = gson.toJson(edges);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_edges_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        jsonObject = gson.toJson(bi_graph);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_graph_o_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
        jsonObject = gson.toJson(reverseGraph);
        out = new OutputStreamWriter(new FileOutputStream(data_type+"_graph_r_j.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        System.out.print("3.finish generating Bi graph");
    }
}
