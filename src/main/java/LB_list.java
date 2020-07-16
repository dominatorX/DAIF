import com.google.gson.Gson;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import static java.lang.Math.exp;

public class LB_list {
    /**
     * Generate the check list for Lower bound of Balance score
     * If the shift is too small (1e-10), we treat the following diff as 0
     */
    public static void main(String[] args) throws IOException {
        int demand = 2000;
        int supply = 6000;
        ArrayList<ArrayList<Double>> LB_list = new ArrayList<>(demand);
        ArrayList<ArrayList<Double>> dn_list = new ArrayList<>(supply);
        ArrayList<ArrayList<Double>> LB_inc = new ArrayList<>(demand);
        int dn,sn,k;
        for (k=0;k<supply;k++){
            ArrayList<Double> temp = new ArrayList<>(demand);
            temp.add(0d);
            dn_list.add(temp);

        }
        for (dn=1;dn<demand;dn++) {
            dn_list.get(0).add(exp(-dn));
            for (k = 1; k < supply; k++) {
                dn_list.get(k).add(dn_list.get(k - 1).get(dn) * dn / k);
            }
        }
        ArrayList<Double> temp0 = new ArrayList<>();
        temp0.add(0d);
        temp0.add(0d);
        LB_list.add(temp0);
        for (dn=1;dn<demand;dn++) {
            ArrayList<Double> temp = new ArrayList<>();
            temp.add(0d);
            for (sn=1;sn<supply;sn++) {
                double Sn = sn;
                for (k=0;k<sn;k++){
                    Sn -= (sn-k)*dn_list.get(k).get(dn);
                }
                temp.add(Sn);
            }
            LB_list.add(temp);

        }
        for (dn=0;dn<demand;dn++) {
            ArrayList<Double> temp = new ArrayList<>();
            for (sn = 0; sn < supply-1; sn++) {
                temp.add(LB_list.get(dn).get(sn + 1) - LB_list.get(dn).get(sn));
                if (temp.get(sn) < 1e-10d) {
                    LB_inc.add(temp);
                    break;
                }
            }
        }
        Gson gson = new Gson();
        String jsonObject = gson.toJson(LB_inc);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream("./LB_inc2k.json"));
        out.write(jsonObject, 0, jsonObject.length());
        out.close();
    }
}
