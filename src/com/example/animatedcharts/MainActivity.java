package com.example.animatedcharts;

import com.example.piechart.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends FragmentActivity {

	ChartFragment chartFrag;
	ItemListFragment itemFrag;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		chartFrag = (ChartFragment)getSupportFragmentManager().findFragmentById(R.id.chart_frag);
		itemFrag = (ItemListFragment)getSupportFragmentManager().findFragmentById(R.id.list_frag);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void setData(PieChartDataset dataset){
		itemFrag.setData(dataset);
		
	}
	
	public void arcClicked(int arcIndex){
		itemFrag.boldItem(arcIndex);
	}

	public void listItemClicked(int itemIndex) {
		Log.d("item click", "" + itemIndex);
		chartFrag.inflateArc(itemIndex);
	}
	
	public void newDataButtonClicked(){
		chartFrag.newData();
	}

}