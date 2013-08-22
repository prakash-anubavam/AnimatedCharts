package com.example.animatedcharts;

import java.util.List;
import java.util.Map;

public abstract class Dataset {

	public Dataset(){}
	
	public abstract void setData(Map<String, Double> data);
	
	public abstract <E extends DataItem> List<E> getData();
	
	
	public abstract class DataItem{
		public abstract double getData();
		public abstract String getLabel();
	}
}
