package com.example.animatedcharts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.piechart.R;

public class ItemListFragment extends android.support.v4.app.Fragment implements OnClickListener {

	ArrayList<TextView> dataViews;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dataViews = new ArrayList<TextView>();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_item_list_fragment, container, false);
		view.setBackgroundColor(Color.WHITE);
		return view;
	}

	private void setViews() {
		((Button)getView().findViewById(R.id.new_data_button)).setOnClickListener(this);
		((Button)getView().findViewById(R.id.change_chart_button)).setOnClickListener(this);
		
		dataViews.add((TextView)getView().findViewById(R.id.item1));
		dataViews.add((TextView)getView().findViewById(R.id.item2));
		dataViews.add((TextView)getView().findViewById(R.id.item3));
		dataViews.add((TextView)getView().findViewById(R.id.item4));
		dataViews.add((TextView)getView().findViewById(R.id.item5));
		
		for(TextView view : dataViews){
			view.setVisibility(View.INVISIBLE);
			view.setOnClickListener(this);
		}
	}

	//TODO turn this into listview manipulations
	public void setData(Map<String, Double> data) {
		Log.d("setData", "setting data, size " + data.size());
		setViews();
		
		Iterator<String> it = data.keySet().iterator();
		
		for(int i = 0; i < data.size(); i++){
			TextView view = dataViews.get(i);
			String label = it.next();
			
			Log.d("views", "setting view " + view.getId() + " label " + label);
			String str = String.format("%s: %s", 
					label, data.get(label));
			view.setText(str);
			view.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View view) {
		Log.d("button", "click");
		if(view.getId() == R.id.new_data_button){
			((MainActivity)getActivity()).newDataButtonClicked();
			Log.d("button", "new data");
			return;
		}
		if(view.getId() == R.id.change_chart_button){
			Log.d("button", "change chart");
			((MainActivity)getActivity()).changeChart();
			return;
		}
		
		//if list item clicked
		int item = Integer.parseInt(((TextView)view).getText().toString().substring(5, 6));
		item--;
		((MainActivity)getActivity()).listItemClicked(item);
		
		boldItem(item);
	}

	public void boldItem(int item) {
		Log.d("inflate", "inflating " + item);
		int size = dataViews.size();
		for(int i = 0; i < size; i ++){
			TextView view = dataViews.get(i);
			Typeface style = view.getTypeface();
			if(style != null && style.getStyle() == Typeface.BOLD){
				view.setTypeface(null, Typeface.NORMAL);
			}
			else{
				if(i == item){
					view.setTypeface(null, Typeface.BOLD);
				}
				else{
					view.setTypeface(null, Typeface.NORMAL);
				}
			}
		}
	
	}

}
