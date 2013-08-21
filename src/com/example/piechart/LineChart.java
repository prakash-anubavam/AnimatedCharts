package com.example.piechart;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.View;

import com.example.piechart.LineChartDataset.LineChartDataItem;

public class LineChart extends View {

	/**xy = top left corner coordinates*/
	private int x; private int y;
	private int width; private int height;
	
	private double maxValue = 0;
	
	final int Y_TICKS = 5;
	final int X_TICKS = 10;
	
	final int STROKE_WIDTH = 4;
	
	LineChartDataset dataset;
	
	private List<DataPoint> points;
	
	private Point origin;
	
	Paint paintText;
	Paint paintLines;
	Paint paintTicks;
	Paint paintAxes;
	
	final int Y_INCREMENT = height / Y_TICKS;
	final int X_OFFSET = 50;
	final int TEXT_SIZE = 20;
	
	public LineChart(LineChartParent parent, int x, int y, int width, int height, LineChartDataset dataset) {
		super(parent.getContext());
		
		this.x = x; this.y = y; 
		this.width = width; this.height = height;
		origin = new Point(x, y + height);
		
		this.dataset = dataset;
		
		getMax();
		initPaints();
		
		setPoints();
	}
	
	private void getMax() {
		for(int i = 0; i < dataset.size(); i++){
			maxValue = Math.max(maxValue, dataset.getItem(i).getData());
		}
		maxValue = Math.round(1.25 * maxValue);
		Log.d("maxValue", "" + maxValue);
	}

	private void setPoints() {
		points = new ArrayList<DataPoint>();
		
		int size = dataset.size();
		int x_offset = width/size;
		
		int pointX; int pointY;
		
		for(int i = 0; i < size; i++){
			LineChartDataItem item = dataset.getItem(i);
			pointX = x + (i * x_offset); 
			pointY = (int)Math.round((origin.y) - (item.getData() / maxValue) * height);
			points.add(new DataPoint("Item: " + i, i, pointX, pointY));
		}
	}
	
	public void initPaints(){
		paintAxes = new Paint();
		paintAxes.setColor(Color.BLACK);
		paintAxes.setStrokeWidth(STROKE_WIDTH);
		

		paintText = new Paint();
		paintText.setColor(Color.BLACK);
		paintText.setStrokeWidth(STROKE_WIDTH);
		paintText.setTextSize(TEXT_SIZE);
		
		paintLines = new Paint();
		paintLines.setColor(Color.BLACK);
		paintLines.setStyle(Paint.Style.STROKE);
		paintLines.setStrokeWidth(STROKE_WIDTH);
		
	}
	
	@Override
	public void draw(Canvas canvas){
		
		canvas.drawLine(x, y, x, y + height, paintAxes); //y axis
		canvas.drawLine(x, origin.y, x + width, origin.y, paintAxes); //x axis
		
		Log.d("draw", "" + points.size());
		
		drawTicks(canvas);
		
		if(points.size() >= 2){  	//no lines to draw
			drawConnections(canvas);
		}
	}
	
	
	private void drawTicks(Canvas canvas){
		
		for(int i = 0; i < Y_TICKS + 1; i++){
			String val = "" + (i * (int)maxValue/Y_TICKS);
			canvas.drawText(val, x - X_OFFSET, origin.y - i * Y_INCREMENT, paintText);
		}
	}

	private void drawConnections(Canvas canvas) {
		
		Path path = new Path();
		
		DataPoint first = points.get(0);
		path.moveTo(first.getX(),  first.getY());
		
		int size = points.size();
		for(int i = 1; i < size - 1; i++){
			DataPoint next = points.get(i);
			DataPoint nextAfter = points.get(i + 1);
			
			/*int x2 = (first.getX() + next.getX())/2;
			int y2 = (first.getY() + next.getY())/2;*/
			
			path.quadTo(next.getX(), next.getY(), nextAfter.getX(), nextAfter.getY());
			first = next;
			
			/*canvas.drawLine(first.getX(),  first.getY(), 
					next.getX(), next.getY(), paintLines);
			first = next;*/
			
		}
		
		canvas.drawPath(path, paintLines);
	}
	
	
//////////////////////////////////////////////////////////////////////////
//DataPoint
//////////////////////////////////////////////////////////////////////////
	private class DataPoint extends ShapeDrawable{
		public int MAX_RADIUS = 10;
		
		private int index;
		private String label;
		private Point location;
		private int radius = 0; 
		
		public DataPoint(String label, int index, int x, int y){
			location = new Point(x, y);
			this.index = index;
			this.label = label;
			
			OvalShape ovalShape = new OvalShape();
			ovalShape.resize(radius, radius);
			this.setShape(ovalShape);
		}
		
		@Override 
		public void draw(Canvas canvas){
			Paint paint = new Paint();
			paint.setColor(Color.GRAY);
			canvas.drawCircle(location.x, location.y, radius, paint);
		}
		
		public int getY(){ return location.y; }
		public int getX(){ return location.x; }
		public void setY(int y){ location.y = y; }
		public int getRadius(){ return radius; }
		public void setRadius(int r) { radius = r; } 
		
	}
	
	
	public interface LineChartParent{
		public void LineChartItemClicked();
		
		public Context getContext();
	}
	
}
