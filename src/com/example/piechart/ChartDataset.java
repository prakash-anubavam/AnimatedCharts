package com.example.piechart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

public class ChartDataset {
	private double sum = 0;
	private ArrayList<DataItem> items;
	
	public ChartDataset(){
		
	}
	
	public ChartDataset(Map<String, Double> data){
		setData(data);
	}

	public void setData(Map<String, Double> data) {
		items = new ArrayList<DataItem>();
		
		for(Entry<String, Double> entry : data.entrySet()){
			sum+= entry.getValue();
		}
		
		for(Entry<String, Double> entry : data.entrySet()){
			
			//adds percentages to items
			DataItem item = new DataItem(entry.getKey(), entry.getValue(), sum);
			items.add(item);
		}
	}

	@SuppressWarnings("unchecked")
	private void sortData() {
		Log.d("sort", items.toString());
		Collections.sort(items);
		Log.d("sort", items.toString());
	}
	
	public List<DataItem> getData(){
		return items;
	}
	
	public class DataItem implements Comparable<DataItem>{
		private double itemData;
		private String itemLabel;
		/**Kept as a decimal, not an actual percentage*/
		private double itemPercentage;
		
		public DataItem(String label, double data, double total){
			this.itemData = data; 
			this.itemLabel = label;
			this.itemPercentage = data/total;			
		}

		@Override
		public int compareTo(DataItem other) {
			if(other instanceof DataItem){
				double data1 = this.itemData;
				double data2 = ((DataItem)other).itemData;
				
				if(data2 == data1) return 0;
				if(data1 > data2) return -1;
				else return 1;
			}
			
			return 0;
		}
		
		public double getPercentage(){
			return itemPercentage;
		}
		
		public double getData(){
			return itemData;
		}
		
		public String getLabel(){
			return itemLabel;
		}
		
	}
	
}
