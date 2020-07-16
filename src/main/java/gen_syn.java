import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;

public class gen_syn {
    public static void main(String[] args) throws IOException {
        String data_file = "./NYC/ny";
        String raw_file = "_output_price_12";
        String gen_file = "./NYC/syn";
        String out_raw = gen_file+"_output_price";
        FileWriter fileWritter = new FileWriter(out_raw);

        Gson gson = new Gson();
        InputStreamReader in = new InputStreamReader(new FileInputStream(data_file+"_node2region_j.json"));
        int [] node2region = gson.fromJson(in,
                new TypeToken<int []>() {
                }.getType());
        in.close();

        in = new InputStreamReader(new FileInputStream(data_file+"_regions_j.json"));
        HashSet<Integer> region = gson.fromJson(in,
                new TypeToken<HashSet<Integer>>() {
                }.getType());
        in.close();

        HashMap<Integer, Integer> tempD = new HashMap<>(region.size());
        for(int reg:region) tempD.put(reg, 0);
        HashMap<Integer, HashMap<Integer, Integer>> DNM = new HashMap<>(24*60/15);
        for(int i=0; i<24*60/15; i++) DNM.put(i, (HashMap<Integer, Integer>)tempD.clone());
        ArrayList<HashMap<Integer, HashMap<Integer, Integer>>> end = new ArrayList<>();
        for (int i = 0; i < 24 * 60; i++) end.add(new HashMap<>());

        HashSet<Integer> days = new HashSet<>();

        HashMap<Integer, HashMap<Integer, Integer>> time_fre = new HashMap<>(24*60);
        for(int i=0; i<24*60; i++)time_fre.put(i, new HashMap<>(31));

        try {
            File file = new File(data_file+raw_file);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String str;

            while ((str = br.readLine())!=null) {
                int day = Integer.parseInt(str.split(" ")[0]);
                days.add(day);
                int time = Integer.parseInt(str.split(" ")[2]);
                int loc = Integer.parseInt(str.split(" ")[1]);
                int locd = Integer.parseInt(str.split(" ")[3]);
                DNM.get(time/15).put(node2region[loc], DNM.get(time/15).get(node2region[loc])+1);
                time_fre.get(time).put(day, time_fre.get(time).getOrDefault(day, 0)+1);
                if (!end.get(time).containsKey(loc)) {
                    HashMap<Integer, Integer> temp = new HashMap<>();
                    temp.put(locd, 1);
                    end.get(time).put(loc, temp);
                } else if (!end.get(time).get(loc).containsKey(locd)) {
                    end.get(time).get(loc).put(locd, 1);
                } else {
                    end.get(time).get(loc).put(locd, end.get(time).get(loc).get(locd) + 1);
                }
            }

            br.close();
            fr.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Integer> list_day = new ArrayList<>(days);
        int day_size = days.size();
        for(int i:DNM.keySet()){
            for(int reg:DNM.get(i).keySet()){
                DNM.get(i).put(reg, (DNM.get(i).get(reg)+day_size/2)/day_size);
            }
        }

        String jsonObject = gson.toJson(DNM);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(gen_file+"_DNM.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        System.out.println("start");
        HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> end_dis = new HashMap<>();
        HashMap<Integer, ArrayList<Integer>> start_dis = new HashMap<>();
        for (int time = 0; time < 60 * 24; time++) {
            end_dis.put(time, new HashMap<>());
            ArrayList<Integer> start = new ArrayList<>();
            ArrayList<Integer> fre_a = new ArrayList<>();
            for (int loc : end.get(time).keySet()) {
                start.add(loc);
                int num = 0;
                for (int temp : end.get(time).get(loc).values()) num += temp;
                fre_a.add(num);
                ArrayList<Integer> where = new ArrayList<>();
                for (int key : end.get(time).get(loc).keySet()) {
                    for(int n_=0;n_< end.get(time).get(loc).get(key);n_++){
                        where.add(key);
                    }
                }
                end_dis.get(time).put(loc, where);
            }
            ArrayList<Integer> where = new ArrayList<>();
            for (int n_=0;n_<start.size();n_++) {
                for(int n__=0;n__<fre_a.get(n_);n__++){
                    where.add(start.get(n_));
                }
            }
            start_dis.put(time, where);
        }
        System.out.println("collected");

        ArrayList<Integer> dis_t = new ArrayList<>();
        for (int time = 0; time < end.size(); time++){
            for (Integer freq : end.get(time).keySet()) {
                for (int mfre : end.get(time).get(freq).values()) {
                    for (int n_=0;n_<mfre;n_++)dis_t.add(time);
                }
            }
        }
        System.out.println("cap finished");

        ShortestPathLRU SPC = new ShortestPathLRU();
        SPC.init(data_file, data_file);

        Random rad = new Random();
        ArrayList<int[]> tasks = new ArrayList<>();
        int counter = 0;

        for(int time=0; time<60*24; time++){
            int num = time_fre.get(time).get(list_day.get(rad.nextInt(day_size)));
            for(int i=0;i<num;i++) {
                int sub_time = rad.nextInt(60);
                int loc = start_dis.get(time).get(rad.nextInt(start_dis.get(time).size()));
                int locd = end_dis.get(time).get(loc).get(rad.nextInt(end_dis.get(time).get(loc).size()));

                if (loc == locd) continue;
                int dist = SPC.dis(loc, locd);
                if (dist == -1) continue;

                counter++;
                int[] task = {time * 60 + sub_time, loc, locd, 1};
                tasks.add(task);
            }
        }
        System.out.println(counter);
        jsonObject = gson.toJson(tasks);
        out = new OutputStreamWriter(new FileOutputStream(gen_file+"_Task.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();

        for(int i=3;i<20;i++) {
            for (int time = 0; time < 60 * 24; time++) {
                int num = time_fre.get(time).get(list_day.get(rad.nextInt(day_size)));
                for (int j = 0; j < num; j++) {
                    int loc = start_dis.get(time).get(rad.nextInt(start_dis.get(time).size()));
                    int locd = end_dis.get(time).get(loc).get(rad.nextInt(end_dis.get(time).get(loc).size()));

                    if (loc == locd) continue;
                    int dist = SPC.dis(loc, locd);
                    if (dist == -1) continue;
                    fileWritter.write(i + " " + loc + " " + time + " " + locd + " 0\n");
                    fileWritter.flush();
                    counter++;
                }
            }
        }
        fileWritter.close();
        System.out.println(counter);
        SPC = new ShortestPathLRU();
        SPC.intializeFrequent(data_file, gen_file, out_raw);

    }
}
