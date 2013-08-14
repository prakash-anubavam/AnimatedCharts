package com.example.piechart;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

public class MainActivity extends Activity implements OnClickListener{
	PieChart pc;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		RelativeLayout container = (RelativeLayout)findViewById(R.id.container);
		Button bn = (Button)findViewById(R.id.refresh_button);
		
		bn.setOnClickListener(this);
		
		pc = new PieChart(this, 600, 600, 200, 150);
		container.addView(pc);
		pc.replayAnimation();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View arg0) {
		pc.replayAnimation();
	}
	
}
