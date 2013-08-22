package com.example.animatedcharts;

import java.util.HashMap;
import java.util.Random;

import com.example.piechart.R;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class ChartFragment extends android.support.v4.app.Fragment implements LineChart.LineChartParent{
	PieChart pc;
	LineChart lc;
	ViewGroup container;
	Random rand;
	PieChartDataset data;

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
		
		HashMap<String, Double> map = generateData();
		
		PieChartDataset dataset = new PieChartDataset(map);
		LineChartDataset lineData = new LineChartDataset(map);
		
		//context, x, y, width, height,  dataset
		pc = new PieChart(this, 270, 100, 400, 400,  dataset);
		lc = new LineChart(this, 100, 100, 800, 400,  lineData);
		container.removeAllViews();
		container.addView(lc);
		//pc.replayAnimation();
		
		((MainActivity)getActivity()).setData(dataset);
	}

	private HashMap<String, Double> generateData() {
		HashMap<String, Double> map =  new HashMap<String, Double>();
		rand = new Random();
		final int MAX_ITEM = 5000;
		final int numItems = rand.nextInt(3) + 3;
		
		for(int i = 0; i < numItems; i++){
			Double d = ((double)rand.nextInt(MAX_ITEM));
			String s = "Item " + i;
			map.put(s, d);
		}
		
		return map;
	}
	
	public PieChartDataset getData(){
		return data;
	}
	
	public void arcClicked(int arcIndex){
		((MainActivity)getActivity()).arcClicked(arcIndex);
	}
	
	public void inflateArc(int item){
		pc.inflateArcIndex(item);
	}
	
	public void newData(){
		initCharts();
	}
	
	
	@Override
	public void LineChartItemClicked() {
		// TODO Auto-generated method stub
	}

	@Override
	public Context getContext() {
		return getActivity();
	}
}
