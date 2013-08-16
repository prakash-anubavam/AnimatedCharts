package com.example.piechart;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.piechart.ChartDataset.DataItem;

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

	public void setData(ChartDataset dataset) {
		setViews();
		
		List<DataItem> items = dataset.getData();
		for(int i = 0; i < items.size(); i++){
			DataItem item = items.get(i);
			TextView view = dataViews.get(i);
			int percentage = (int)Math.round(item.getPercentage() * 100);
			String str = String.format("Item %s: %s, %s", 
					i, (int)item.getData(), percentage);
			view.setText(str + "%");
			view.setVisibility(View.VISIBLE);
		}
		
	}

	@Override
	public void onClick(View view) {
		int item = Integer.parseInt(((TextView)view).getText().toString().substring(5, 6));
		
		((MainActivity)getActivity()).listItemClicked(item);
		
		inflateItem(item);
	}

	private void inflateItem(int item) {
		final int REG_TEXT_SIZE = 20;
		final int INFLATED_TEXT_SIZE = 27;
		
		int size = dataViews.size();
		for(int i = 0; i < size; i ++){
			TextView view = dataViews.get(i);
			if(i == item){
				view.setTextSize(INFLATED_TEXT_SIZE);
			}
			else{
				view.setTextSize(REG_TEXT_SIZE);
			}
		}
	
	}

}
