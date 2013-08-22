package com.example.animatedcharts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LineChartDataset {
	
	private List<LineChartDataItem> items;
	
	public LineChartDataset(Map<String, Double> data){
		setData(data);
	}
	
	public void setData(Map<String, Double> data) {
		items = new ArrayList<LineChartDataItem>();
		
		int index = 0;
		for(Entry<String, Double> entry : data.entrySet()){
			LineChartDataItem item = new LineChartDataItem(
					entry.getKey(), entry.getValue(), index);
			
			items.add(item);
			index++;
		}
	}
	
	public LineChartDataItem getItem(int index){
		return items.get(index);
	}
	
	public int size(){ return items.size(); }
	
	public class LineChartDataItem {
		private double data;
		private String label;
		private int index;
		
		public LineChartDataItem(String label, double data, int index){
			this.data = data;
			this.label = label;
			this.index = index;
		}
		
		public double getData(){ return data; }
		public String getLabel(){ return label; }
		public int getIndex(){ return index; } 
	}

}
