package RFM_Miner;

/* This is an implementation of the FFI-Miner algorithm. 
* 
* Copyright (c) 2016 FFI-Miner
* 
* This file is part of the SPMF DATA MINING SOFTWARE * (http://www.philippe-fournier-viger.com/spmf). 
* 
* 
* SPMF is free software: you can redistribute it and/or modify it under the * terms of the GNU General Public License as published by the Free Software * Foundation, either version 3 of the License, or (at your option) any later * version. * 

* SPMF is distributed in the hope that it will be useful, but WITHOUT ANY * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR * A PARTICULAR PURPOSE. See the GNU General Public License for more details. * 
* 
* You should have received a copy of the GNU General Public License along with * SPMF. If not, see . 
* 
* @author Ting Li
*/

import java.util.HashMap;
import java.util.Map;

public class Regions {
		double low = 0d;// low region
		double middle = 0d;// middle region
		double high = 0d;// high region
	    Map<String, Double> mapfuzLevel= new HashMap<String, Double>(); //存的是模糊等级和项的隶属度
	
	public Regions(double quantity, int regionsNumber, double min, double max){  //构造方法
		//calculate the regions value
		double mid = min + (max - min)/2;
		if(regionsNumber == 3){
			// if there are 3 regions
			if (quantity > 0 && quantity <= min){
				this.low = 1.0;
				this.high = 0;
				this.middle = 0;
				mapfuzLevel.put("L",this.low);
			}else if (quantity >= min && quantity < mid){
				this.middle = (float)((quantity-min)/(mid-min));
				this.low =  (float)((mid-quantity)/(mid-min));
				this.high = 0;
				mapfuzLevel.put("M",this.middle);
				mapfuzLevel.put("L",this.low);
			}else if (quantity >= mid && quantity< max){
				this.low = 0.0;
				this.high = (float)((quantity-mid)/(max-mid));//(float) ((quantity-mid)/(max-mid));
				this.middle = (float)((max-quantity)/(max-mid));
				mapfuzLevel.put("H",this.high);
				mapfuzLevel.put("M",this.middle);
			}else{
				this.low = 0;
				this.middle = 0;
				this.high = 1.0;
				mapfuzLevel.put("H",this.high);
			}
		}

		if(regionsNumber==2){
			// if there are 2 regions
			this.middle = 0;
			if(quantity > 0 && quantity<=1){
				this.low = 1;
				this.high = 0;
			}else if(quantity > 1 && quantity<=11){
				this.low = (float) (-0.1*quantity + 1.1);
				this.high = (float) (0.1*quantity  - 0.1);
			}else{
				this.low = 0;
				this.high = 1;
			}
		}
	}
}
