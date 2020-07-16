This is the open source code for paper:
Jiachuan Wang, Peng Cheng, Libin Zheng, Chao Feng, Lei Chen, Xuemin Lin, Zheng Wang:
Demand-Aware Route Planning for Shared Mobility Services. Proc. VLDB Endow. 13(7): 979-991 (2020)
http://www.vldb.org/pvldb/vol13/p979-wang.pdf


Before running, you need to download the big processed NYC and synthetic data via:
https://drive.google.com/drive/folders/1sCVvSA9jOsOtH64IgOQV3bX_7N1Yx5w7?usp=sharing
The 4  "*_fre_*.json"  files are enough to run the algorithms. Put them under ./NYC.


We have 3 algorithms:

-----------for Algorithm GreedyDP:
Run:    mvn compile exec:java -Dexec.mainClass="GreedyDP" -Dexec.args="|W|  1+e_r  a_i"
-----------for Algorithm DAIF-B:
Run:    mvn compile exec:java -Dexec.mainClass="DAIF-B" -Dexec.args="Basic |W|  1+e_r  a_i"
-----------for Algorithm DAIF-DP:
Run:    mvn compile exec:java -Dexec.mainClass="DAIF-DP" -Dexec.args="DP |W|  1+e_r  a_i"

-----------where |W| is #_of_worker,   e_r is detour_factor,   a_i is worker's capacity

-----------for example, 3000 workers with capacity 3, allowing detour 30% for DAIF-DP algorithm:
Run:    mvn compile exec:java -Dexec.mainClass="DAIF" -Dexec.args="DP 3000 1.3 3"


Default dataset is NYC, if you want to run synthetic data, open DAIF.java or GreedyDP.java and change: data_file from "./NYC/ny" to "./NYC/syn", 
date_list.add from "02" (02 means Dec. 2rd) to "" (synthetic data has no date), and 
DNM initalized with "_DNM.json" instead of "_DNM02.json". 


Below are the steps to set up based on new datasets. 
Remember to change the data name in each java file.
---------------------------------------------------------------------
1. Prepare dataset.

To compare with algorithm SHARE, we use their graph datasets in the link https://drive.google.com/drive/folders/1DiVSOqANI3Ww0jHSKMw5VcxH04aIJsHC?usp=sharing (They had postprocessing on the graph for their shortest path query). 

two files for edges from OSM                with formats of "NYC/ny_edge" and "NYC/ny_edge_time"
vertex locations from OSM                    with format of "NYC/ny_location"
OSM index of vertices                            with format of "NYC/nyIndex"

We use public dataset for taxi requests. It is too big to upload, you can get it via:      
   wget -P ./NYC/ https://s3.amazonaws.com/nyc-tlc/trip+data/yellow_tripdata_2013-12.csv
request information                 with format of "NYC/yellow_tripdata_2013-12.csv"

And prediction result, double key dictionary DNM[time_index][grid_index] -> # of requests
Demand Number Map             with format of "NYC/ny_DNM02.json" 

----------for basic graph generation-----------------------------
2.Run:       mvn compile exec:java -Dexec.mainClass="get_info"

----------for NYC request generation and LRU initial-------------
3.Run:       mvn compile exec:java -Dexec.mainClass="get_request"

----------for grid prune-------------------------------------------
4.Run:       mvn compile exec:java -Dexec.mainClass="grid_dis"

----------for synthetic data generation---------------------------
5.Run:       mvn compile exec:java -Dexec.mainClass="gen_syn"


Note: if you have error:    
	error: invalid target release: 12
please change the pom.xml according to your jdk version.