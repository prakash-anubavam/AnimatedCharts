package com.example.animatedcharts;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.piechart.R;

public class ChartFragment extends android.support.v4.app.Fragment implements LineChart.LineChartParent{
	PieChart pc;
	LineChart lc;
	ViewGroup container;
	Random rand;
	PieChartDataset pieData;
	HashMap<String, Double> myMap;
	boolean pieChartShowing = false;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.chart_container, container, false);
		return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		init();
	}

	public void init(){
		container = (RelativeLayout)getView().findViewById(R.id.container);
		container.setBackgroundColor(Color.WHITE);
		initCharts();
	}
	
	public void initCharts() {
		
		myMap = generateData();
		
		PieChartDataset dataset = new PieChartDataset(myMap);
		LineChartDataset lineData = new LineChartDataset(myMap);
		
		//context, x, y, width, height,  dataset
		pc = new PieChart(this, 270, 100, 400, 400,  dataset);
		lc = new LineChart(this, 100, 100, 800, 400,  lineData);
		showChart();
		 
		((MainActivity)getActivity()).setData(myMap);
	}

	private void showChart() {
		if(pieChartShowing){
			container.removeAllViews();
			container.addView(pc);
			pc.replayAnimation();
		}
		else{
			container.removeAllViews();
			container.addView(lc);
			lc.replayAnimation();
		}
		
	}

	private HashMap<String, Double> generateData() {
		myMap =  new HashMap<String, Double>();
		rand = new Random();
		final int powerOf10 = rand.nextInt(4) + 1;
		final int numItems = rand.nextInt(3) + 3;
		
		for(int i = 0; i < numItems; i++){
			Double d = ((double)rand.nextInt((int)Math.pow(10, powerOf10)));
			String s = "Item " + (i +1) ;
			myMap.put(s, d);
		}
		
		return myMap;
	}
	
	public HashMap<String, Double> getData(){
		return myMap;
	}
	
	public void arcClicked(int arcIndex){
		((MainActivity)getActivity()).chartItemClicked(arcIndex);
	}
	
	public void inflateItem(int item){
		if(pieChartShowing){
			pc.inflateItem(item);
		}
		else lc.inflatePoint(item);
	}
	
	
	public void newData(){
		initCharts();
	}
	
	@Override
	public void LineChartItemClicked(int pointIndex) {
		((MainActivity)getActivity()).chartItemClicked(pointIndex);
	}

	@Override
	public Context getContext() {
		return getActivity();
	}

	public void changeChart() {
		pieChartShowing = !pieChartShowing;
		showChart();
	}
}
