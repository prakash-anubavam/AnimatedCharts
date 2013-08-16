package com.example.piechart;

import java.util.HashMap;
import java.util.Random;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class ChartFragment extends Fragment implements OnClickListener{
	PieChart pc;
	ViewGroup container;
	Random rand;

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
		initChart();
	}
	
	public void initChart() {
		
		HashMap<String, Double> map = generateData();
		
		ChartDataset dataset = new ChartDataset(map);
		
		//context, width, height, x, y, dataset
		pc = new PieChart(this, 600, 600, 200, 100, dataset);
		container.removeAllViews();
		container.addView(pc);
		pc.replayAnimation();
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

	@Override
	public void onClick(View arg0) {
		Log.d("items", "onClick");
		initChart();
	}
	
}
