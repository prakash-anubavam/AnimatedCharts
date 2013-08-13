package com.example.piechart;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		PieChart pc = new PieChart(this);
		LinearLayout container = new LinearLayout(this);
		Button bn = new Button(this);
		bn.setText("Replay");
		
		
		container.addView(pc);
		container.addView(bn);
		
		setContentView(container);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
}
