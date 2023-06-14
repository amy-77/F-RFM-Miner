package RFM_Miner;

import java.io.*;
public class MainTest_F_RFM_Miner_saveToFile {
	public static void main(String [] arg) throws IOException{
		String output = ".//output_list.txt";
		String output1 = ".//output_list1.txt";
		String output3 = ".//output_no_minSup1.txt";
		Boolean Enable_LA = true;
		Boolean use_URSP_strategy = true;
		BufferedWriter writer3 = new BufferedWriter(new FileWriter(output3));
		Boolean use_Util_Rate = false;
      	Integer[] times = {120};
		Double[] minUtil_ratios = {0.2};
    	String[] datasets = {"paper_db"};
		Double[] minSups_initial = {0.8};
		Double[] minRes_intial = {0.01};
		Integer[] minUtil = {20};
		String[] paths = {"test/paper_db.txt"};
		String[] paths_price = {"test/price.txt"};
		String[] paths_time = {"test/time.txt"};

		    int i=0;
			String dataset = datasets[i];
			String input = paths[i];
			String input2 = paths_time[i];
			String input3 = paths_price[i];
			int timeCurrent = times[i];
			double minsup=minSups_initial[i];
			double minRecency=minRes_intial[i];
			int minUtility=minUtil[i];
			double min_utility_ratio=minUtil_ratios[i];

			  Algo_F_RFM_Miner F_RFM_Miner  = new Algo_F_RFM_Miner(Enable_LA, use_URSP_strategy);
		      F_RFM_Miner.runAlgorithm(dataset, writer3, input, input2, input3, output, output1,
					  use_Util_Rate, minsup, minUtility, min_utility_ratio, minRecency, timeCurrent);
		      F_RFM_Miner.printStats(minsup, min_utility_ratio, minUtility, minRecency);
		      F_RFM_Miner.writeout_all_minSup();
		      writer3.close();
      }
  }
