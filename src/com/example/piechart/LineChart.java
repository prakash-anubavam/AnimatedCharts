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
	
	final float STROKE_WIDTH = 6f;
	
	LineChartDataset dataset;
	
	private List<DataPoint> points;
	
	private Point origin;
	
	Paint paintText;
	Paint paintLines;
	Paint paintTicks;
	Paint paintAxes;
	
	final int TEXT_DISTANCE_FROM_AXIS = 60;
	final int TEXT_SIZE = 20;
	
	final int CURVE_TENSION = 5;
	
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
		int x_offset = width/(size - 1);
		
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
		
		for(DataPoint point : points){
			point.draw(canvas);
		}
	}
	
	
	private void drawTicks(Canvas canvas){
		final int Y_INCREMENT = height / Y_TICKS;
		for(int i = 0; i < Y_TICKS + 1; i++){
			String val = "" + (i * (int)maxValue/Y_TICKS);
			canvas.drawText(val, x - TEXT_DISTANCE_FROM_AXIS, origin.y - i * Y_INCREMENT, paintText);
		}
	}

	private void drawConnections(Canvas canvas) {
		
		Path path = new Path();
		int size = points.size();
		if(size <= 1) return;
		
		setControlPoints(size);
		
		DataPoint first = points.get(0);
		
		path.moveTo(first.getX(),  first.getY());
		
		for(int i = 1; i < size; i++){
			DataPoint current = points.get(i);
			DataPoint prev = points.get(i- 1);
			
			path.cubicTo(prev.getControlX() + prev.getX(), prev.getControlY() + prev.getY(), 
					current.getX() - current.getControlX(), current.getY() - current.getControlY(),
					current.getX(), current.getY());
		}
		
		canvas.drawPath(path, paintLines);
	}

	/**We're going to use Bezier curves to path through the points smoothly. 
	 * First we set a control point for each data point that will help
	 * determine its curve later.
	 */
	private void setControlPoints(int size) {
		DataPoint first = points.get(0);
		DataPoint second = points.get(1);
		first.setControlPoint(new Point(
				(second.getX() - first.getX())/CURVE_TENSION,
				(second.getY() - first.getY())/CURVE_TENSION));
		
		for(int i = 1; i < size - 1; i++){
			Point before = points.get(i -1).getPoint();
			Point after = points.get(i + 1).getPoint();
			Point control = new Point();
			control.x = (after.x - before.x)/CURVE_TENSION;
			control.y = (after.y - before.y)/CURVE_TENSION;
			points.get(i).setControlPoint(control);
		}
		
		DataPoint last = points.get(size -1);
		DataPoint penul = points.get(size -2);
		last.setControlPoint(new Point(
				(last.getX() - penul.getX())/CURVE_TENSION,
				(last.getY() - penul.getY())/CURVE_TENSION));
	}
	
	
//////////////////////////////////////////////////////////////////////////
//DataPoint
//////////////////////////////////////////////////////////////////////////
	private class DataPoint extends ShapeDrawable{
		public int MAX_RADIUS = 10;
		
		private int index;
		private String label;
		private Point location;
		
		/**The control point is used to determine the bezier curve that goes through
		 * this datapoint*/
		private Point controlPoint;
		
		private int radius = MAX_RADIUS; 
		Paint mPaint;
		
		public DataPoint(String label, int index, int x, int y){
			location = new Point(x, y);
			this.index = index;
			this.label = label;
			
			OvalShape ovalShape = new OvalShape();
			ovalShape.resize(radius, radius);
			this.setShape(ovalShape);
			
			mPaint = new Paint();
		}
		
		@Override 
		public void draw(Canvas canvas){
			mPaint.setColor(Color.WHITE);
			mPaint.setStyle(Paint.Style.FILL);
			canvas.drawCircle(location.x, location.y, radius, mPaint);
			mPaint.setColor(Color.GRAY);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeWidth(4f);
			canvas.drawCircle(location.x, location.y, radius, mPaint);
		}
		
		public Point getPoint(){ return location; }
		public int getY(){ return location.y; }
		public int getX(){ return location.x; }
		public void setY(int y){ location.y = y; }
		public int getRadius(){ return radius; }
		public void setRadius(int r) { radius = r; } 
		
		public void setControlPoint(Point point){ controlPoint = point; }
		public Point getControlPoint(){ return controlPoint; }
		
		public int getControlX(){ return controlPoint.x; }
		public int getControlY(){ return controlPoint.y; }
	}
	
	
	public interface LineChartParent{
		public void LineChartItemClicked();
		
		public Context getContext();
	}
	
}
