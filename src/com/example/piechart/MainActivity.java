package com.example.piechart;

import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class MainActivity extends Activity implements OnClickListener{
	PieChart pc;
	ViewGroup container;
	Random rand;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		container = (RelativeLayout)findViewById(R.id.container);
		container.setBackgroundColor(Color.WHITE);
		
		initChart();
	}

	public void initChart() {
		HashMap<String, Double> map = generateData();
		
		ChartDataset dataset = new ChartDataset(map);
		
		pc = new PieChart(this, 600, 600, 200, 150, dataset);
		container.removeAllViews();
		container.addView(pc);
		pc.replayAnimation();
	}

	private HashMap<String, Double> generateData() {
		HashMap<String, Double> map =  new HashMap<String, Double>();
		rand = new Random();
		
		Double random1 = (double)rand.nextInt(5000);
		Double random2 = (double)rand.nextInt(5000);
		Double random3 = (double)rand.nextInt(5000);
		
		Log.d("items" +
				"", String.format("1. %s, 2. %s, 3. %s", random1, random2, random3));
		
		map.put("item1", random1);
		map.put("item2", random2);
		map.put("item3", random3);
		
		return map;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		Log.d("items", "onClick");
		initChart();
	}
	
}
