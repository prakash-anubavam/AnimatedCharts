package com.steveinflow.animatedcharts.piechart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

public class PieChartDataset {
	private double sum = 0;
	private ArrayList<PieChartDataItem> items;
	
	public PieChartDataset(){
		
	}
	
	public PieChartDataset(Map<String, Double> data){
		setData(data);
	}

	public void setData(Map<String, Double> data) {
		items = new ArrayList<PieChartDataItem>();
		
		for(Entry<String, Double> entry : data.entrySet()){
			sum+= entry.getValue();
		}
		
		int index = 0;
		for(Entry<String, Double> entry : data.entrySet()){
			
			//adds percentages to items
			PieChartDataItem item = new PieChartDataItem(entry.getKey(), entry.getValue(), sum, index);
			items.add(item);
			index++;
		}
		sortData();
	}

	@SuppressWarnings("unchecked")
	private void sortData() {
		Collections.sort(items);
	}
	
	public List<PieChartDataItem> getData(){
		return items;
	}
	
	public int getIndexForItem(int item){
		for(int i = 0; i < items.size(); i++){
			if(items.get(i).getIndex() == item){
				return i;
			}
		}
		return -1;
	}
	
	public class PieChartDataItem implements Comparable<PieChartDataItem>{
		private double itemData;
		private String itemLabel;
		/**Kept as a decimal, not an actual percentage*/
		private double itemPercentage;
		private int index;
		
		public PieChartDataItem(String label, double data, double total, int index){
			this.itemData = data; 
			this.itemLabel = label;
			this.itemPercentage = data/total;			
			this.index = index;
		}

		@Override
		public int compareTo(PieChartDataItem other) {
			if(other instanceof PieChartDataItem){
				double data1 = this.itemData;
				double data2 = ((PieChartDataItem)other).itemData;
				
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
		
		public int getIndex(){
			return index;
		}
		
	}
	
}
