import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class DemandNumberMap {
    private HashSet<Integer> regions = new HashSet<>();
    ArrayList<Integer> time_spans = new ArrayList<>();
    private HashMap<Integer,HashMap<Integer,Integer>> DNA = new HashMap<>();
    HashMap<Integer,HashMap<Integer,Integer>> DN = new HashMap<>();
    private ArrayList<String> date_list = new ArrayList<>();
    int total_spans = 6;
    private int next_idx = total_spans;
    int per_span = 900;
    ArrayList<int[]> DN_m = new ArrayList<>();
    //int data_idx = 1;
    public void init(String DN_file, HashSet<Integer> regions, ArrayList<Integer> time_spans, ArrayList<String> date_list) throws IOException {
        this.regions = (HashSet<Integer>) regions.clone();
        this.time_spans = (ArrayList<Integer>) time_spans.clone();
        this.date_list = (ArrayList<String>) date_list.clone();
        Gson gson = new Gson();
        InputStreamReader in = new InputStreamReader(new FileInputStream(DN_file));
        this.DNA = gson.fromJson(in,
                new TypeToken<HashMap<Integer, HashMap<Integer, Integer>>>() {
                }.getType());
        in.close();
        int i;
        for (int key : this.regions) {
            HashMap<Integer, Integer> temp = new HashMap<>();
            for (i=0; i < this.total_spans; i++) {
                temp.put(this.per_span * i, this.DNA.get(i).get(key));
            }
            this.DN.put(key, temp);
        }

        for(i=0;i<this.total_spans;i++){
            this.DN_m.add(new int[]{Collections.max(DNA.get(i).values()),
                    Collections.min(DNA.get(i).values())});
        }
    }

    public void update() {
        int removed_span = this.time_spans.remove(0);
        this.time_spans.add((this.time_spans.get(this.total_spans - 2) + this.per_span));
        if (this.next_idx >= 86400 / this.per_span) {
            for (Integer key : this.regions) {
                this.DN.get(key).remove(removed_span);
                this.DN.get(key).put(this.time_spans.get(this.total_spans - 1), 0);
            }
            this.DN_m.remove(0);
            this.DN_m.add(new int[]{0, 0});
        } else {
            for (Integer key : this.regions) {
                this.DN.get(key).remove(removed_span);
                this.DN.get(key).put(this.time_spans.get(this.total_spans - 1), this.DNA.get(this.next_idx).get(key));
            }
            this.DN_m.remove(0);
            this.DN_m.add(new int[]{Collections.max(DNA.get(this.next_idx).values()),
                    Collections.min(DNA.get(this.next_idx).values())});
        }
        this.next_idx += 1;

    }
}
