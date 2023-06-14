package RFM_Miner;
//import com.sun.deploy.util.JVMParameters;

import java.io.*;
import java.util.ArrayList;
public class experiment {
    public static void main(String[] args) throws IOException {
        BufferedReader br1_tree = new BufferedReader(new FileReader("fuzzyoutput_tree.txt"));
        BufferedReader br2_list = new BufferedReader(new FileReader("output_list.txt"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("compare.txt"));
        String line;
        ArrayList<ArrayList<String>> itemsets_tree = new ArrayList<>();
        ArrayList<ArrayList<String>> itemsets_list = new ArrayList<>();
        double count = 0.0;
           while ((line = br1_tree.readLine()) != null) {
               ArrayList<String>  itemset=new ArrayList<>();
               String[] items = line.split(" ");
               for(String item:items){
                itemset.add(item);
               }
               itemsets_tree.add(itemset);
            }
            while ((line = br2_list.readLine()) != null) {
                ArrayList<String>  itemset=new ArrayList<>();
                String[] items = line.split(" ");
                for(String item:items){
                    itemset.add(item);
                }
                itemsets_list.add(itemset);
            }


        int tree_size = itemsets_tree.size();
        int list_size=itemsets_list.size();
        for(int i = 0; i< tree_size; i++) {
            for (int j = 0; j < list_size; j++) {
                if (itemsets_list.get(j).size()==itemsets_tree.get(i).size()){
                   if( itemsets_list.get(j).containsAll(itemsets_tree.get(i))) {
                    count++;
                    bw.write(String.valueOf(itemsets_tree.get(i)));
                    bw.newLine();
                  }
                }
            }
        }
            bw.close();
            br1_tree.close();
            br2_list.close();

            double d1 = count /tree_size;
            double d2 = count / list_size;
            System.out.println("相同的个数为" + count);
            System.out.println("模糊RFM_tree模式个数为" + tree_size);
            System.out.println("模糊RFM_list模式个数为" + list_size);
            System.out.println("共同模式占模糊RFM_tree模式的" + "recall:" + d1);
            System.out.println("共同模式占模糊RFM_list模式的" + "precision:" + d2);
        }
}