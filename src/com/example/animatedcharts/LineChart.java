package com.example.animatedcharts;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;

import com.example.animatedcharts.LineChartDataset.LineChartDataItem;

public class LineChart extends View implements OnClickListener{

	/**xy = top left corner coordinates*/
	private int x; private int y;
	private int width; private int height;
	/**Real values are set at construction, independent of animation*/
	private int realWidth; private int realHeight;
	
	private double maxValue = 0;
	
	int y_ticks = 5;
	final int X_TICKS = 10;
	
	final float STROKE_WIDTH = 6f;
	
	LineChartDataset dataset;
	
	private List<DataPoint> points;
	
	private Point origin;
	
	Paint paintText;
	Paint paintLines;
	Paint paintTicks;
	Paint paintAxes;
	Paint paintGrid;
	
	final int TEXT_X_DISTANCE = 60;
	final int TEXT_Y_DISTANCE = 50;
	final int TEXT_SIZE = 20;
	
	final int CURVE_TENSION = 5;
	
	public LineChart(LineChartParent parent, int x, int y, int width, int height, LineChartDataset dataset) {
		super(parent.getContext());
		this.setOnClickListener(this);
		
		this.x = x; this.y = y; 
		this.width = width; this.height = height;
		this.realWidth = width; this.realHeight = height;
		
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
		Log.d("maxValue", "highestdatapoint: " + maxValue);
		
		maxValue = Math.round(1.1 * maxValue);
		Log.d("maxValue", "x 1.1: " + maxValue);
		
		int numDigits = getDigits(maxValue);
		int specificity = Math.max(numDigits - 2, 1);
		int divisor = (int)Math.pow(10, (specificity));
		Log.d("maxValue", "divisor: " + divisor);
		
		int upTo = divisor;
		while(upTo < maxValue){
			upTo += (divisor);
		}
		maxValue = upTo;
		
		Log.d("maxValue", "divisored up: " + maxValue);
	}

	private int getDigits(double input) {
		if(input / 10 < 1) return 1;
		else return 1 + getDigits(input / 10);
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
		
		paintGrid = new Paint();
		paintGrid.setColor(Color.GRAY);
		paintGrid.setStyle(Paint.Style.STROKE);
		paintGrid.setStrokeWidth(STROKE_WIDTH/4);
		
	}
	

	@Override
	public void onClick(View arg0) {
		Log.d("animate", "animations started");
		animatePoints();
		invalidate();
	}
	
//////////////////////////////////////////////////////////////////////////
//Draw
//////////////////////////////////////////////////////////////////////////
	@Override
	public void draw(Canvas canvas){
		Log.d("draw", "" + points.size());
		setYTicks();
		
		int y_increment = height / y_ticks;
		int x_increment = width / X_TICKS;
		drawGrid(canvas, y_increment, x_increment);
		
		x_increment = width / points.size();
		drawYLabels(canvas, y_increment);
		drawXLabels(canvas, x_increment);
		
		if(points.size() >= 2){  	//no lines to draw
			drawConnections(canvas);
		}
		
		for(DataPoint point : points){
			point.draw(canvas);
		}
	}

	private void setYTicks() {
		int digits = getDigits(maxValue);
		double smaller = maxValue / (Math.max(1, Math.pow(10, digits - 2)));
		
		double divisor = 5;
		while(divisor <= 9){
			if(smaller % divisor == 0) break;
			divisor++;
		}
		if(divisor >= 9){
			maxValue += Math.pow(10,  Math.max((digits -1),1));
			setYTicks();
		}
		
		y_ticks = (int)divisor;
	}

	private void drawYLabels(Canvas canvas, int y_increment){
		for(int i = 0; i < y_ticks + 1; i++){
			String val = "" + (i * (int)maxValue/y_ticks);
			canvas.drawText(val, x - TEXT_X_DISTANCE, origin.y - i * y_increment, paintText);
		}
	}
	
	private void drawXLabels(Canvas canvas, int x_increment) {
		for(int i = 0; i < points.size(); i++){
			DataPoint dataPoint = points.get(i);
			String val = dataPoint.getLabel();  
			canvas.drawText(val, dataPoint.getX() - 20, origin.y + TEXT_Y_DISTANCE, paintText);
		}
	}
	
	private void drawGrid(Canvas canvas, int y_increment, int x_increment){
		//draw y ticks
		for(int i = 0; i <= y_ticks; i++){
			Point left = new Point(origin.x, origin.y- y_increment * i);
			Point right = new Point(origin.x + width, origin.y - y_increment * i);
			canvas.drawLine(left.x, left.y, right.x,  right.y, paintGrid);
		}
		
		//draw x ticks
		for(int j = 0; j <= X_TICKS; j++){
			Point top = new Point(origin.x + x_increment * j, origin.y - height);
			Point bot = new Point(origin.x + x_increment * j, origin.y);
			canvas.drawLine(top.x, top.y, bot.x, bot.y, paintGrid);
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
	
	public void setHeight(int newHeight){ height = newHeight; }
	public int getRealHeight(){ return realHeight; }
	public void setWidth(int newWidth){ width = newWidth; }
	public int getRealWidth(){ return realWidth; }
	
	
//////////////////////////////////////////////////////////////////////////
//Animate
//////////////////////////////////////////////////////////////////////////	
	public void animatePoints(){
		final long DURATION = 1000;
		
		ArrayList<Animator> animations = new ArrayList<Animator>();
		AnimatorSet set = new AnimatorSet();
		
		for(int i = 0; i < points.size(); i++){
			DataPoint point = points.get(i);
			int y2 = point.getReal().y;
			
			ObjectAnimator anim = ObjectAnimator.ofFloat(point, "radius", point.RADIUS, point.EXPAND);
			anim.setDuration(DURATION);
			animations.add(anim);
		}
		
		set.playTogether(animations);
		set.start();
	}
	
	
	
	
//////////////////////////////////////////////////////////////////////////
//DataPoint
//////////////////////////////////////////////////////////////////////////
	private class DataPoint extends ShapeDrawable{
		public int RADIUS = 10;
		public int EXPAND = 30;
		
		private int index;
		private String label;
		private Point location;
		private Point realLocation;
		
		/**The control point is used to determine the bezier curve that goes through
		 * this datapoint*/
		private Point controlPoint;
		
		private int radius = RADIUS; 
		Paint mPaint;
		
		public DataPoint(String label, int index, int x, int y){
			location = new Point(x, y);
			realLocation = new Point(x,y);
			
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
			
			mPaint.setColor(Color.BLACK);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeWidth(4f);
			
			canvas.drawCircle(location.x, location.y, radius, mPaint);
		}
		public void setY(float y){ 
			location.y = (int)y;
		}
		
		public Point getPoint(){ return location; }
		public int getY(){ return location.y; }
		public int getX(){ return location.x; }
		public int getRadius(){ return radius; }
		public void setRadius(float r) { radius = (int)r; }
		public String getLabel(){ return label; }
		
		public void setControlPoint(Point point){ controlPoint = point; }
		public Point getControlPoint(){ return controlPoint; }
		public Point getReal(){ return realLocation; }
		
		public int getControlX(){ return controlPoint.x; }
		public int getControlY(){ return controlPoint.y; }
	}
	
	
	public interface LineChartParent{
		public void LineChartItemClicked();
		
		public Context getContext();
	}

	
}