package RFM_Miner;
import java.io.*;
import java.util.*;

/**
 * This is an implementation of the "F-RFM-Miner"
 * @author Yanlin Qi
 */
public class Algo_F_RFM_Miner{
	private boolean Enable_LA;
	private boolean use_URSP_strategy;



	private double maxMemory = 0;
	/**
	 * the time at which the algorithm started
	 */
	public long startTimestamp = 0;

	/**
	 * the time at which the algorithm ended
	 */
	public long endTimestamp = 0;
	public long endTimestamp1 = 0;
	public long endTimestamp2 = 0;
	public long endTimestamp3 = 0;
	public long endTimestamp4 = 0;

	/** the number of high-fuzzy itemsets generated */

	ArrayList<String> remain_items = new ArrayList<>();
	/**
	 * Map to remember the TWU of each item
	 */
	Map<String, Double> mapItemLowSUM;
	Map<String, Double> mapItemMiddleSUM;
	Map<String, Double> mapItemHighSUM;
	Map<String, Double> mapItemSUM;
	Map<String, String> mapItemRegion;
	Map<String,Double> mapFuzItemToVal = new HashMap<String,Double>();
	/** The eucs structure:  key: item   key: another item   value: twu */
	HashMap<String, Map<String, Double>> mapFMAP;
	HashMap<String,Map<String,double[]>> mapURFS = null;
	HashMap<String,Map<String,HashMap<String,Double>>> mapURFS1 = null;

	HashMap<String, Map<String, Double>> mapRMAP;
	Map<Integer, Double> tidTime = new HashMap<Integer, Double>();
	Map<String, Double> itemprice = new LinkedHashMap<String, Double>();

	ArrayList<Map<String,Map<String, Double>>> fuzzytrans= new ArrayList<Map<String,Map<String, Double>>>();
	/**
	 * writer to write the output file
	 */
	BufferedWriter writer = null;
	BufferedWriter writer1 = null;
	/**
	 * the number of FFI-list that was constructed
	 */
	private int joinCount;
	private int joinCount1;
	public int FFICount = 0;
	public int huiCount = 0;
	public  int EUCS_prune = 0;
	public  int ERCS_prune = 0;
	public int URFS_prune = 0;
	private int URFS_prune_turn=0;

	public int pre_RFU_Prune = 0;
	private Object maxF,minF,maxR,minR,maxU,minU;
	List<RFUList> listOfRFULists = new ArrayList<RFUList>();
	/**
	 * buffer for storing the current itemset that is mined when performing mining
	 * the idea is to always reuse the same buffer to reduce memory usage.
	 */
	final int BUFFERS_SIZE = 200;
	private String[] itemsetBuffer = null;
	HashMap<String, Double> mapItemToTWU = new HashMap<>();
	HashMap<String, Double> mapItemToRecency = new HashMap<>();
	private double decayThreshold = 0.01;//0.01
	private double currentRecency;
	private double timeCurrent = 100;

	private RFUList RFUListOfItem;
	private int candidate_num = 0;
	private int recursive_num = 0;
	private int prefixlength=0;
	private int true_pattern_num = 0;
    private  int candidate_decrease = 0 ;
	private ArrayList<Double> trans_Recency= new ArrayList<>();
	private BufferedWriter writer3;
	private String dataset;
	private double minRecency = 0d;
	private double minUtility=0.0;
	private double minSupport=0.0;
    private double 	minUtilityRatio=0.0;
    private double 	 totalUtility1 = 0d;
	private int candi_1_item_num=0;
	private int candi_2_item_num=0;
	private int candi_3_item_num=0;
	private int candi_large4_item_num=0;
	private int true_1_item_num=0;
	private  int true_2_item_num=0;
	private int true_3_item_num=0;
	private int true_out_4_item_num=0;
	//private int joinCount1;

	/**
	 * this class represent an item and its fuzzy in a transaction
	 */
	class Pair {
		String item = null;
		double ifs = 0.0;
		double rfs = 0.0;
		double utility = 0.0;
		double rutil = 0.0;
		double Rvalue = 0.0;
	}

//	class Pair1{
//		String item = 0;
//		int utility = 0;
//	}

	/**
	 * Default constructor
	 */
	public Algo_F_RFM_Miner(boolean Enable_LA, boolean use_URSP_strategy) {
		this.use_URSP_strategy = use_URSP_strategy;
		this.Enable_LA = Enable_LA;
	}

	/**
	 * Run the algorithm
	 *
	 * @param input      the input file path
	 * @param output     the output file path
	 * @param minSupport the minimum support threshold
	 * @throws IOException exception if error while writing the file
	 */
	public void runAlgorithm( String dataset, BufferedWriter writer3, String input, String input2, String input3, String output,String output1,
							  boolean useRate, double minSupport, int minUtility, double minUtilityRatio, double minRecency, int timeCurrent) throws IOException {

		startTimestamp = System.currentTimeMillis();
		// reset maximum
		MemoryLogger.getInstance().reset();
		// initialize the buffer for storing the current itemset

		itemsetBuffer = new String[BUFFERS_SIZE];
		writer = new BufferedWriter(new FileWriter(output));
		writer1 = new BufferedWriter(new FileWriter(output1));
		this.writer3=writer3;
		this.dataset=dataset;
		this.minUtility=minUtility;
		this.minSupport=minSupport;
		this.minRecency=minRecency;
		this.minUtilityRatio=minUtilityRatio;
		this.totalUtility1=totalUtility1;
		Load_TimeTable(input2);
		Load_UtilityTable(input3);
		//  We create three  map to store the low, middle, high summation of each item
		mapItemLowSUM = new HashMap<String, Double>();
		mapItemMiddleSUM = new HashMap<String, Double>();
		mapItemHighSUM = new HashMap<String, Double>();
		double max = 0d;
		double min = 0d;
		List<Double> count = new ArrayList<Double>();
		BufferedReader myInput = null;
		BufferedReader myInput11 = null;
		String thisLine;
		mapFMAP =  new HashMap<String, Map<String, Double>>();
		mapRMAP =  new HashMap<String, Map<String, Double>>();
		mapURFS = new HashMap<String, Map<String, double[]>>();
		mapURFS1 = new HashMap<String, Map<String, HashMap<String,Double>>>();

		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));
			while ((thisLine = myInput.readLine()) != null) {
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}
				String itemAndQuals[] = thisLine.split(" ");
				for (String itemAndQual : itemAndQuals) {
					String tmp[] = itemAndQual.split(",");
					double quantity = Double.parseDouble(tmp[1]);
					count.add(quantity);
				}
			}
			max = Collections.max(count);
			min = Collections.min(count);


			myInput11 = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));

			while ((thisLine = myInput11.readLine()) != null) {
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
						|| thisLine.charAt(0) == '@') {
					continue;
				}
				Map<String,Map<String, Double>> fuzzyitems=new HashMap<>();

				String itemAndQuals[] = thisLine.split(" ");
				for (String itemAndQual : itemAndQuals) {
					String tmp[] = itemAndQual.split(",");
					String item = tmp[0];
					double quantity = Double.parseDouble(tmp[1]);
					Regions regions = new Regions(quantity, 3, 1, 11);//0.5*min,1.5*max
					// add low value
					fuzzyitems.put(item,regions.mapfuzLevel);

					if (mapItemLowSUM.containsKey(item)) {
						double low = mapItemLowSUM.get(item);
						low += regions.low;
						mapItemLowSUM.put(item, low);
					} else {
						mapItemLowSUM.put(item, regions.low);
					}
					// add middle value
					if (mapItemMiddleSUM.containsKey(item)) {
						double middle = mapItemMiddleSUM.get(item);
						middle += regions.middle;
						mapItemMiddleSUM.put(item, middle);
					} else {
						mapItemMiddleSUM.put(item, regions.middle);
					}
					// add high value
					if (mapItemHighSUM.containsKey(item)) {
						double high = mapItemHighSUM.get(item);
						high += regions.high;
						mapItemHighSUM.put(item, high);
					} else {
						mapItemHighSUM.put(item, regions.high);
					}
				}
				fuzzytrans.add(fuzzyitems);
			}
			MemoryLogger.getInstance().checkMemory();

		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}


		itemsetBuffer = new String[BUFFERS_SIZE];

		List<RFUList> listOfRFULists = new ArrayList<RFUList>();
		Map<String, RFUList> mapItemToRFUList = new HashMap<String, RFUList>();
		mapItemSUM = new HashMap<String, Double>();
		mapItemRegion = new HashMap<String, String>();

		Set<String> trueItems = mapItemLowSUM.keySet();
		for (String item : trueItems) {
			 double low = mapItemLowSUM.get(item);
			 double middle = mapItemMiddleSUM.get(item);
			 double high = mapItemHighSUM.get(item);
			if (low >= middle && low >= high) {
				mapItemSUM.put(item, low);
				mapItemRegion.put(item, "L");
				//fuzzyItems.add(item+".L");
				mapFuzItemToVal.put(item+".L",low);
			} else if (middle >= low && middle >= high) {
				mapItemSUM.put(item, middle);
				mapItemRegion.put(item, "M");
				//fuzzyItems.add(item+".M");
				mapFuzItemToVal.put(item+".M",middle);
			} else if (high >= low && high >= middle) {
				mapItemSUM.put(item, high);
				mapItemRegion.put(item, "H");
				//fuzzyItems.add(item+".H");
				mapFuzItemToVal.put(item+".H",high);
			}
		}



		MemoryLogger.getInstance().checkMemory();
		endTimestamp2 = System.currentTimeMillis();//计算模糊数据库的时间


		try {
			BufferedReader myInput1 = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));
			String thisLine1;
			int tid = 0;

			while ((thisLine1 = myInput1.readLine()) != null) {
				if (thisLine1.isEmpty() == true ||
						thisLine1.charAt(0) == '#' || thisLine1.charAt(0) == '%'
						|| thisLine1.charAt(0) == '@') {
					continue;
				}

				double transactionUtility = 0d;
				double currentRecency = Math.pow((1 - decayThreshold), (timeCurrent - tidTime.get(tid)));
				trans_Recency.add(currentRecency);
				String itemAndQuals[] = thisLine1.split(" ");
	//ArrayList<Map<String,Map<String, Double>>> fuzzytrans= new ArrayList<Map<String,Map<String, Double>>>();

	           for(String itemAndQual : itemAndQuals) {
		             String tmp[] = itemAndQual.split(",");
		             String item = tmp[0];
		             double quantity = Double.parseDouble(tmp[1]);
			         if (mapItemSUM.containsKey(item)) {
				       if (fuzzytrans.get(tid).get(item).keySet().contains(mapItemRegion.get(item))) {
					     transactionUtility += quantity * itemprice.get(item);
						   totalUtility1 += transactionUtility;
				          }
			          }
	                }
				for(String itemAndQual : itemAndQuals) {
					String tmp[] = itemAndQual.split(",");
					String item = tmp[0];
					if (mapItemSUM.containsKey(item)) {
						if (fuzzytrans.get(tid).get(item).keySet().contains(mapItemRegion.get(item))) {

							Double twu = mapItemToTWU.get(item);
							twu = (twu == null) ? transactionUtility : twu + transactionUtility;
							mapItemToTWU.put(item, twu);

							Double SumRecency = mapItemToRecency.get(item);
							SumRecency = (SumRecency == null) ? currentRecency : SumRecency + currentRecency;
							mapItemToRecency.put(item, SumRecency);
						}
					}
				}

				tid++;
			}

			if (useRate)
				this.minUtility = (int) Math.ceil (minUtilityRatio * totalUtility1);


			for(String item: mapItemSUM.keySet()) {
				if (mapItemSUM.get(item) >= minSupport && mapItemToTWU.get(item) >= minUtility && mapItemToRecency.get(item) >= minRecency) {
					//  create an empty Fuzzy List of every item
					remain_items.add(item);
					RFUList rfuList = new RFUList(item);
					mapItemToRFUList.put(item, rfuList);
					// add the item to the list of Fuzzy list items
					listOfRFULists.add(rfuList);
				}
			}

			Collections.sort(listOfRFULists, new Comparator<RFUList>() {
				public int compare(RFUList o1, RFUList o2) {
					//return compare_Desc_Sup(o1.item, o2.item);
					 return compareItems1(o1.item, o2.item);
					//return compare_Re_Asc(o1.item, o2.item);
					 // return compare_Asc_Sup(o1.item, o2.item);
					// return compare_Re_Desc(o1.item, o2.item);
					// return compare_lexi(o1.item,o2.item);
				}
			});

			MemoryLogger.getInstance().checkMemory();


		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}



		int length = mapItemSUM.size();
		Object[] obj = mapItemSUM.values().toArray();//把value的值放在集合collection里，把集合转成数组升序排序
		Arrays.sort(obj);
		maxF=obj[length-1];
		minF=obj[0];

		Object[] obj1 = mapItemToRecency.values().toArray();//把value的值放在集合collection里，把集合转成数组升序排序
		Arrays.sort(obj1);
		maxR=obj1[length-1];
		minR=obj1[0];

		Object[] obj2 = mapItemToTWU.values().toArray();//把value的值放在集合collection里，把集合转成数组升序排序
		Arrays.sort(obj2);
		maxU=obj2[length-1];
		minU=obj2[0];

		endTimestamp3 = System.currentTimeMillis();//计算模糊数据库的时间



		try {
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));
			int tid = 0;
			double remainingUtility = 0.0;

			while ((thisLine = myInput.readLine()) != null) {
				if (thisLine.isEmpty() == true || thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%' || thisLine.charAt(0) == '@') {
					continue;
				}
				double totalUtility = 0.0;
				double TU=0.0;
				List<Pair> revisedTransaction = new ArrayList<Pair>();
				String itemAndQuals[] = thisLine.split(" ");
				for(String itemAndQual : itemAndQuals) {
					String tmp[] = itemAndQual.split(",");
					String item = tmp[0];
					double quantity = Double.parseDouble(tmp[1]);
					String level = mapItemRegion.get(item);
					Pair pair = new Pair();
					pair.item = item;
					pair.Rvalue = trans_Recency.get(tid);
					pair.utility = itemprice.get(item) * quantity;

					if(fuzzytrans.get(tid).get(item).containsKey(mapItemRegion.get(item))){
					  if (mapItemSUM.get(pair.item) >= minSupport && mapItemToTWU.get(pair.item) >= minUtility && mapItemToRecency.get(pair.item) >= minRecency) { //item=5不满足min支持度，被踢出
						  pair.ifs = fuzzytrans.get(tid).get(item).get(level);
						if (fuzzytrans.get(tid).get(item).get(level) != null) {
							totalUtility += pair.utility;
						    TU += pair.utility;
						}
						if (pair.ifs > 0 && pair.utility > 0)
							revisedTransaction.add(pair);
					}

				  }

				}
				// SORT THE  TRANSACTION IN ASCENDING ORDER OF PROMISING ITEMS
				Collections.sort(revisedTransaction, new Comparator<Pair>() {
					public int compare(Pair o1, Pair o2) {
						//	return compare_Desc_Sup(o1.item, o2.item);
							return  compareItems1(o1.item, o2.item);
						//  return compare_Re_Asc(o1.item, o2.item);
						//   return compare_Asc_Sup(o1.item, o2.item);
						// return compareTWU_Desc(o1.item, o2.item);
						// return compare_Re_Desc(o1.item, o2.item);
						// return compare_lexi(o1.item,o2.item);
					}
				});


				for (int i = 0; i < revisedTransaction.size() ; i++) {
					Pair pair = revisedTransaction.get(i);
					remainingUtility = totalUtility - pair.utility;
					pair.rutil = remainingUtility;
					totalUtility = remainingUtility;
					Element element = new Element(tid, pair.ifs, pair.rfs, pair.utility, pair.rutil, pair.Rvalue);//这里要改
					RFUListOfItem = mapItemToRFUList.get(pair.item);
					RFUListOfItem.addElement(element);

					if (use_URSP_strategy) {
						Map<String, double[]> mapFMAPItem1 = mapURFS.get(pair.item);
						if (mapFMAPItem1 == null) {
							mapFMAPItem1 = new HashMap<String, double[]>();
							mapURFS.put(pair.item, mapFMAPItem1);
						}
						for (int j = i + 1; j < revisedTransaction.size(); j++) {
							Pair pairAfter = revisedTransaction.get(j);
							double[] URSP = mapFMAPItem1.get(pairAfter.item);

							if (URSP == null) {
								URSP = new double[3];
								URSP[0] = Double.parseDouble(String.format("%.4f",TU));
								URSP[1] = Double.parseDouble(String.format("%.4f",pair.Rvalue));
								URSP[2] = Double.parseDouble(String.format("%.4f",Double.min(pair.ifs,pairAfter.ifs)));// Px和Py合并，这里计算if(xy)。 utilityListOfItem.getSupport();
								mapFMAPItem1.put(pairAfter.item, URSP);
							} else {
								URSP[0] = URSP[0] + Double.parseDouble(String.format("%.4f",TU));
								URSP[1] = URSP[1] + Double.parseDouble(String.format("%.4f",pair.Rvalue));
								URSP[2] = URSP[2] + Double.parseDouble(String.format("%.4f",Double.min(pair.ifs,pairAfter.ifs)));//utilityListOfItem.getSupport();
								mapFMAPItem1.put(pairAfter.item, URSP);
							}
						}
					}
					// END OPTIMIZATION of FHM
				}
				tid++;
			}

			MemoryLogger.getInstance().checkMemory();

		} catch (Exception e) {
			// to catch error while reading the input file
			e.printStackTrace();
		} finally {
			if (myInput != null) {
				myInput.close();
			}
		}
		// check the memory usage
	    MemoryLogger.getInstance().checkMemory();
		endTimestamp4= System.currentTimeMillis();

		int remove_num=0;
		ArrayList<Integer> remove_Ids=new ArrayList<>();
		ArrayList<RFUList> listOfRFULists1=new ArrayList<>();
		for (int i = 0; i <listOfRFULists.size(); i++) {
			RFUList X = listOfRFULists.get(i);
				if (X.sumIfs >= minSupport && X.sumIutils+X.sumRutils >= minUtility && X.sumRvalue >= minRecency) {
					writeOut1(itemsetBuffer, 0, X.item, X.sumIfs, X.sumIutils, X.sumRvalue);
					candidate_num += 1;
					listOfRFULists1.add(X);
				  }else{
					 remove_Ids.add(i);
					 remove_num+=1;
				  }
			  }

		listOfRFULists=listOfRFULists1;

		Collections.sort(listOfRFULists, new Comparator<RFUList>() {
			public int compare(RFUList o1, RFUList o2) {
				//return compare_Desc_Sup(o1.item, o2.item);
				return compareItems1(o1.item, o2.item);
				//return compare_Re_Asc(o1.item, o2.item);
				 // return compare_Asc_Sup(o1.item, o2.item);
				 // return compareTWU_Desc(o1.item, o2.item);
				// return compareTWU_Desc(o1.item, o2.item);
				// return compare_Re_Desc(o1.item, o2.item);
				//return compare_lexi(o1.item,o2.item);
			}
		});

		HRFUIMiner(itemsetBuffer, 0, null, listOfRFULists, minSupport, minUtility, minRecency);

		MemoryLogger.getInstance().checkMemory();
		writer.close();
		writer1.close();
		endTimestamp = System.currentTimeMillis();

	}



	private int compare_Desc_Sup(String item1, String item2) {
		double compare = mapItemSUM.get(item2) - mapItemSUM.get(item1);
		// if the same, use the lexical order otherwise use the TWU
		//return Double.compare(compare,0.0);
		if (Double.compare(compare,0.0)==0){
			return item1.compareTo(item2); //CompareTo表示按照字典顺序排序
		}
		else {
			return Double.compare(compare,0.0);
		}
	 }
	private int compare_Asc_Sup(String item1, String item2) {
		double compare = mapItemSUM.get(item1) - mapItemSUM.get(item2);
		// if the same, use the lexical order otherwise use the TWU
		//return Double.compare(compare,0.0);
		if (Double.compare(compare,0.0)==0){
			return item1.compareTo(item2); //CompareTo表示按照字典顺序排序
		}
		else {
			return Double.compare(compare,0.0);
		}
	}
	private int compare_Re_Asc(String item1, String item2) {
		double compare = mapItemToRecency.get(item1) - mapItemToRecency.get(item2);
		// if the same, use the lexical order otherwise use the TWU
		//return Double.compare(compare,0.0);
		if (Double.compare(compare,0.0)==0){
			return item1.compareTo(item2); //CompareTo表示按照字典顺序排序
		}
		else {
			return Double.compare(compare,0.0);
		}
	}
	private int compare_Re_Desc(String item1, String item2) {
		double compare = mapItemToRecency.get(item2) - mapItemToRecency.get(item1);
		// if the same, use the lexical order otherwise use the TWU
		if (Double.compare(compare,0.0)==0){
			return item1.compareTo(item2);
		}
		else {
			return Double.compare(compare,0.0);
		}
	}


	private int compare_lexi(String item1, String item2) {
		// if the same, use the lexical order
			return item1.compareTo(item2);
	}

	  private int compareItems1(String item1, String item2) {
		//double compare1 = listOfRFULists.get(item1) - mapItemSUM.get(item2);
		double compare = mapItemToTWU.get(item1) - mapItemToTWU.get(item2);
		// if the same, use the lexical order otherwise use the TWU
		  if (Double.compare(compare,0.0)==0){
			  return item1.compareTo(item2);
		  }
		  else {
			  return Double.compare(compare,0.0);
		  }
		  //return Double.compare(compare,0.0);
	}

	private int compareTWU_Desc(String item1, String item2) {
		//double compare1 = listOfRFULists.get(item1) - mapItemSUM.get(item2);
		double compare = mapItemToTWU.get(item2) - mapItemToTWU.get(item1);
		// if the same, use the lexical order otherwise use the TWU
		if (Double.compare(compare,0.0)==0){
			return item1.compareTo(item2);
		}
		else {
			return Double.compare(compare,0.0);
		}
	}
	/**
	 * This is the main sub-proceedings
	 *
	 * @param prefix       This is the current prefix. Initially, it is empty.
	 * @param prefixLength Current length of promising itemset
	 * @param RFULs        A FUZZY-LIST
	 * @param minSupport   The minimum support threshold count
	 * @throws IOException
	 */


	private void HRFUIMiner(String[] prefix, int prefixLength, RFUList pRFUL, List<RFUList> RFULs, double minSupport, int minUtility, double minRecency) throws IOException {
		// For each extension X of prefix P
		for (int i = 0; i < RFULs.size(); i++) {
			RFUList X = RFULs.get(i);
			if (X.sumIfs >= minSupport && X.sumIutils >= minUtility && X.sumRvalue >= minRecency) {  // X=1.list
				// save to file
				true_pattern_num += 1;
				writeOut(prefix, prefixLength, X.item, X.sumIfs, X.sumIutils, X.sumRvalue);
			}
			// If the sum of the remaining fuzzy utilities for X is higher than minSupport, we explore extensions of X.
			if (X.sumIfs >= minSupport && X.sumIutils + X.sumRutils >= minUtility && X.sumRvalue >= minRecency) {    //这里我们认为recency不能作为剪枝的条件
				List<RFUList> exRFULs = new ArrayList<RFUList>();
				for (int j = i + 1; j < RFULs.size(); j++) {//X是ULs里的第i项，Y取ULs里的第j项，扩展X
					RFUList Y = RFULs.get(j);
					  String [] Px = new String[prefixLength+1];
					  System.arraycopy(prefix, 0, Px, 0, prefixLength);
					  Px[prefixLength] = X.item;

				       if(use_URSP_strategy==true ) {
						Map<String, double[]> mapURS = mapURFS.get(X.item);
						if (mapURS != null) {
							double[] URSP = mapURS.get(Y.item);
							if (URSP != null && (URSP[0] < minUtility || URSP[1] < minRecency || URSP[2] < minSupport)) {
								URFS_prune += 1;
								continue;
							}
						}
					}


				    RFUList rful = construct(pRFUL, X, Y, minUtility, minSupport, minRecency);
					if(rful != null) {
						 if(rful.sumIutils!=0 && rful.sumIfs!=0 && rful.sumRvalue!=0){
						//=== END HUP-Miner
						exRFULs.add(rful);
						writeOut2(prefix, prefixLength, X.item, Y.item, rful.sumIfs, rful.sumIutils, rful.sumRvalue);
						candidate_num += 1;

					  }
					}else {
						candidate_decrease += 1;
					  }
					}


					// We create new prefix pX
					itemsetBuffer[prefixLength] = X.item;
					// We make a recursive call to discover all FFIs with the prefix X
					recursive_num += 1;
					HRFUIMiner(itemsetBuffer, prefixLength + 1, X, exRFULs, minSupport, minUtility, minRecency);


			}
		}
	}


	/**
	 * This method constructs the fuzzy list of pXY
	 *
	 * @param pRFUL
	 * @param px : the fuzzy list of px
	 * @param py : the fuzzy list of py
	 */


	private boolean checkChainEUCPStrategy(String[] Px, String itemY) {
		for (String itemX : Px) {
			Map<String,  double[]> mapURS = mapURFS.get(itemX);
			if (mapURS != null) {
				double[] URSP = mapURS.get(itemY);
				if (URSP != null && (URSP[0] < minUtility || URSP[1] < minRecency || URSP[2] < minSupport)) {
					//URSP_prune += 1;
					return true;
				}
			}
		}
		return false;
	}


	private RFUList construct(RFUList pRFUL, RFUList px, RFUList py, int minUtility, double minSupport,double minRecency){
		// create an empy fuzzy list for pxy
		RFUList pxyRFUL = new RFUList(py.item);
		// for each element in the fuzzy list of px
		double totalUtility = px.sumIutils + px.sumRutils;
		double totalRvalue = px.sumRvalue;
		double totalIf = px.sumIfs;
		for (Element ex : px.elements) {
			// do a binary search to find element ey in py with tid = ex.tid
			Element ey = findElementWithTID(py, ex.tid); //找ey中是否有相同的tid
			if (ey == null) {
				// BEGIN   LA-prune  HUP-Miner
				if (Enable_LA) {
					totalUtility -= (ex.iutils + ex.rutils); //扩展XY时，减去只在X中出现 但是 不在Y中出现的事务。
					totalRvalue -= ex.Rvalue;
					totalIf -= ex.ifs;
					if (totalUtility < minUtility || totalIf < minSupport || totalRvalue < minRecency) {
						pre_RFU_Prune += 1;
						return null;
					}
					// END  LA-prune  HUP-Miner
				}
					continue;
				}


			if (pRFUL == null) {
				// Create the new element
					Element eXY = new Element(ex.tid, Double.min(ex.ifs, ey.ifs), ey.rfs, ex.iutils + ey.iutils, ey.rutils,ex.Rvalue);
					// add the new element to the utility list of pXY
					pxyRFUL.addElement(eXY);
			} else {
				Element e = findElementWithTID(pRFUL, ex.tid);
				if (e != null) {
					// Create new element
						Element eXY = new Element(ex.tid, Double.min(ex.ifs, ey.ifs), ey.rfs, ex.iutils + ey.iutils - e.iutils, ey.rutils, ex.Rvalue);
						// add the new element to the utility list of pXY
						pxyRFUL.addElement(eXY);
				}
			}
		}
		// return the fuzzy list of pXY.
		return pxyRFUL;
	}


	/**
	 * Do a binary search to find the element with a given tid in a fuzzy list
	 * @param rfulist the fuzzy list
	 * @param tid  the tid
	 * @return  the element or null if none has the tid.
	 */
	private Element findElementWithTID(RFUList rfulist, int tid){
		List<Element> list = rfulist.elements;
		// perform a binary search to check if  the subset appears in  level k-1.
        int first = 0;
        int last = list.size() - 1;
        // the binary search
        while( first <= last )
        {
        	int middle = ( first + last ) >>> 1; // divide by 2
            if(list.get(middle).tid < tid){
            	first = middle + 1;  //  the itemset compared is larger than the subset according to the lexical order
            }
            else if(list.get(middle).tid > tid){
            	last = middle - 1; //  the itemset compared is smaller than the subset  is smaller according to the lexical order
            }
            else{
            	return list.get(middle);//list.get(middle).tid == tid
            }
        }
		return null;
	}

	/**
	 * Append an item to an itemset
	 * @param itemset an itemset represented as an array of integers
	 * @param item the item to be appended
	 * @return the resulting itemset as an array of integers
	 */
	private String[] appendItem(String[] itemset, String item) {
		String [] newgen = new String[itemset.length+1];
		System.arraycopy(itemset, 0, newgen, 0, itemset.length);
		newgen[itemset.length] = item;
		return newgen;
	}


	/**
	 * Method to write a  fuzzy frequent itemset to the output file.
	 * @param  prefix to be writent o the output file
	 * @param item to be appended to the prefix
	 * @param sumIutils the fuzzy of the prefix concatenated with the item
	 * @param prefixLength the prefix length
	 */
	private void writeOut(String[] prefix, int prefixLength, String item, double sumIfs, double sumIutils, double sumRvalue) throws IOException {
		huiCount++;
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		int item_len=0;
		for (int i = 0; i < prefixLength; i++) {
			buffer.append(prefix[i]);
			item_len+=1;
			buffer.append(' ');
		}
		// append the last item
		buffer.append(item);
		buffer.append(' ');
		buffer.append("," +"  #Util: "+ sumIutils); //HUI.getExactUtility());
		buffer.append("  #Freq: " + String.format("%.3f",sumIfs));
		buffer.append("  #Re: " + String.format("%.3f", sumRvalue));
		// write to file
		if (item_len == 0) {
			true_1_item_num += 1;
		} else if ( item_len == 1) {
			true_2_item_num += 1;
		} else if (item_len == 2) {
			true_3_item_num += 1;
		}else if(item_len>=3){
			true_out_4_item_num+=1;
		}
		writer.write(buffer.toString());
		writer.newLine();
	}

	private void writeOut1(String[] prefix, int prefixLength, String item, double sumIfs, double sumIutils, double sumRvalue) throws IOException {
		//FFICount++; // increase the number of high fuzzy itemsets found
		huiCount++;
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		int item_len=0;
		// append the prefix
		for (int i = 0; i < prefixLength; i++) {
			buffer.append(prefix[i]);
			item_len+=1;
			buffer.append(' ');
		}
		// append the last item
		buffer.append(item);
		buffer.append(' ');
		buffer.append("," +"  #Util: "+ sumIutils); //HUI.getExactUtility());
		buffer.append("  #Freq: " + String.format("%.3f",sumIfs));
		buffer.append("  #Re: " + String.format("%.3f", sumRvalue));
		// write to file
		if (item_len == 0) {
			candi_1_item_num += 1;
		} else if ( item_len == 1) {
			candi_2_item_num += 1;
		} else if (item_len == 2) {
			candi_3_item_num += 1;
		}else if(item_len>=3){
			candi_large4_item_num+=1;
		}
		writer1.write(buffer.toString());
		writer1.newLine();
	}

	private void writeOut2(String[] prefix, int prefixLength, String item1, String item2, double sumIfs, double sumIutils, double sumRvalue) throws IOException {
		huiCount++;
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		int item_len=0;
		for (int i = 0; i < prefixLength; i++) {
			buffer.append(prefix[i]);
			buffer.append(' ');
			item_len+=1;
		}
		// append the last item
		buffer.append(item1);
		buffer.append(' ');
		buffer.append(item2);
		buffer.append("," +"  #Util: "+ sumIutils); //HUI.getExactUtility());
		buffer.append("  #Freq: " + String.format("%.3f",sumIfs));
		buffer.append("  #Re: " + String.format("%.3f", sumRvalue));
		// write to file
		if (item_len == 0) {
			candi_2_item_num += 1;
		} else if ( item_len == 1) {
			candi_3_item_num += 1;
		} else if (item_len >= 2) {
			candi_large4_item_num += 1;
		}
		writer1.write(buffer.toString());
		writer1.newLine();
	}

	public void writeout_all_minUtil() throws IOException{
		StringBuilder buffer3 = new StringBuilder();
		buffer3.append(dataset);
		buffer3.append(" ");
		buffer3.append(minUtility);
		buffer3.append(" ");
		buffer3.append((endTimestamp - startTimestamp)/1000.0 );
		buffer3.append(" ");
		buffer3.append(true_pattern_num);
		buffer3.append(" ");
		buffer3.append(candidate_num);
		// write to file
		writer3.write(buffer3.toString());
		writer3.newLine();
		writer3.flush();

	}

	public void writeout_all_minRe() throws IOException{
		StringBuilder buffer3 = new StringBuilder();
		buffer3.append(dataset);
		buffer3.append(" ");
		buffer3.append(minRecency);
		buffer3.append(" ");
		buffer3.append((endTimestamp - startTimestamp)/1000.0 );
		buffer3.append(" ");
		buffer3.append(true_pattern_num);
		buffer3.append(" ");
		buffer3.append(candidate_num);
		// write to file
		writer3.write(buffer3.toString());
		writer3.newLine();
		writer3.flush();
	}

	public void writeout_all_minSup() throws IOException{
		StringBuilder buffer3 = new StringBuilder();
		buffer3.append(dataset);
		buffer3.append(" ");
		buffer3.append(minSupport);
		buffer3.append(" ");
		buffer3.append((endTimestamp - endTimestamp4)/ 1000.0 );//"Mining time: "+
		buffer3.append(" ");
		buffer3.append(true_pattern_num);
		buffer3.append(" ");
		buffer3.append(candidate_num);
		buffer3.append(" ");
		buffer3.append(pre_RFU_Prune);
		buffer3.append(" ");
		buffer3.append(URFS_prune);
		buffer3.append(" ");
		buffer3.append(candi_1_item_num);
		buffer3.append(" ");
		buffer3.append(true_1_item_num);
		buffer3.append(" ");
		buffer3.append(candi_2_item_num);
		buffer3.append(" ");
		buffer3.append(true_2_item_num);
		buffer3.append(" ");
		buffer3.append(candi_3_item_num);
		buffer3.append(" ");
		buffer3.append(true_3_item_num);
		buffer3.append(" candi4:");
		buffer3.append(candi_large4_item_num);
		buffer3.append(" true4:");
		buffer3.append(true_out_4_item_num);
		// write to file
		writer3.write(buffer3.toString());
		writer3.newLine();
		writer3.flush();
	}

	private void Load_TimeTable(String timeTableInput) {
		try {
			String thisline;

			BufferedReader br = new BufferedReader(new FileReader(timeTableInput));
			while ((thisline = br.readLine()) != null) {
				String[] tmp = thisline.split(",");
				int tid = Integer.parseInt(tmp[0]);
				Double time = Double.parseDouble(tmp[1]);
				tidTime.put(tid, time);
			}
			br.close();
		} catch (Exception e) {
			System.out.println("Error about loading the time table (in ConnectionTextFile.java): " + e.toString());
		}
	}

	/**
	 * Loading Utility Table
	 *
	 * @param utility_table_input the path of utiltiy_table file
	 */
	public void Load_UtilityTable(String utility_table_input) {
		try {
			String thisline;
			BufferedReader br = new BufferedReader(new FileReader(utility_table_input)); // �_�n
			while ((thisline = br.readLine()) != null) {
				String[] tmp = thisline.split(",");
				String item = tmp[0];
				Double profit = Double.parseDouble(tmp[1]);
				itemprice.put(item, profit);
			}
			br.close();
		} catch (Exception e) {
			System.out.println("Eity table (in rror about loading the utilConnectionTextFile.java): " + e.toString());
		}
	}



	/**
	 * Method to check the memory usage and keep the maximum memory usage.
	 */
	private void checkMemory() {
		// get the current memory usage
		double currentMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024d / 1024d;
		// if higher than the maximum until now  replace the maximum with the current memory usage
		if (currentMemory > maxMemory) {
			maxMemory = currentMemory;
		}
	}
	
	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats(double min_support,double minUtilityRatio, int minUtility, double minRecency) {
		System.out.println("=============  RFU-list ALGORITHM v.2.15 - STATS =============");
		System.out.println("dataset: "+ dataset);
		System.out.println("minUtilityRatio: "+ minUtilityRatio);
		System.out.println("minFval: "+ min_support+", "+"minRval: "+ minRecency +", "+"minUval: "+ minUtility);
		System.out.println("Fval: "+String.format("%.1f", minF)+"~"+String.format("%.1f", maxF)+" , "+"Rval: "+String.format("%.1f", minR)+"~"+String.format("%.1f", maxR)+" , "+"Uval: "+String.format("%.1f", minU)+"~"+String.format("%.1f", maxU));
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp)/1000.0 + " s");
		System.out.println(" Memory ~ " + String.format("%.4f", MemoryLogger.getInstance().getMaxMemory()) + " MB");  //明明最大内存是是5.819？？为什么显示为4？
		System.out.println(" candidate_num " + candidate_num);
		System.out.println(" true_num " + true_pattern_num);
		System.out.println("Pre_RFU_prune: "+ pre_RFU_Prune);
		System.out.println(" URFS_prune " + URFS_prune);
		System.out.println("totalUtil: "+ totalUtility1+" minUtil: "+(int) Math.ceil (minUtilityRatio * totalUtility1)+" Util_ratio: "+ (double)minUtility/totalUtility1);
		System.out.println("First traverse fuzzy time: "+ (endTimestamp2- startTimestamp)/ 1000.0 + " s" );
		System.out.println("Second traverse: "+ (endTimestamp3 - endTimestamp2)/ 1000.0 + " s" );
		System.out.println("Construct RFML & URFS: "+ (endTimestamp4 - endTimestamp3)/ 1000.0 + " s" );
		System.out.println("Mining time: "+ (endTimestamp - endTimestamp4)/ 1000.0 + " s" );
	}
}