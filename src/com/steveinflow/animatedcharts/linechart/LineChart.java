package com.steveinflow.animatedcharts.linechart;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.steveinflow.animatedcharts.linechart.LineChartDataset.LineChartDataItem;

public class LineChart extends View implements OnTouchListener{

	/**xy = top left corner coordinates*/
	private int currentX; private int currentY;
	private float realX; private float realY;
	
	/**Real values are set at construction, independent of animation*/
	private int currentWidth; private int currentHeight;
	private float realWidth; private float realHeight;
	
	/**the maximum number that will show on the y axis*/
	private double maxValue = 0;
	
	private boolean gridAnimating = false;
	private boolean animateGridNextTime = false; 
	private Point pointToExpandFrom;
	
	private LineChartParent parent;
	Handler mHandler;
	
	int y_ticks = 5;
	final int X_TICKS = 10;
	private int numPoints;
	
	
	private int fillAlpha = 0;
	private final float FULL_FILL_ALPHA = 100; 
	private int everythingButGridAlpha = 255;
	private int gridAlpha = 255;
	
	//point values
	private static final long CIRCLE_ANIM_DURATION = 200;
	public final float NORMAL_RADIUS;
	public final float EXPAND_RADIUS;
	public final int APPROX_CHAR_WIDTH;
	public final int POPUP_TEXT_SIZE;
	
	/**The oval is used in the transition grid expansion animation*/
	private int ovalAlpha = 0;
	private int ovalWidth;
	private int ovalHeight;
	private float ovalStrokeWidth = LinePoint.CIRCLE_STROKE_WIDTH;
	private float ovalRadiusFactor = 1;
	
	final float STROKE_WIDTH = 6f;
	
	LineChartDataset dataset;
	
	private ArrayList<LinePoint> points;
	
	private Point origin;
	private Point center;
	
	Paint paintText;
	Paint paintConnections;
	Paint paintTicks;
	Paint paintAxes;
	Paint paintGrid;
	Paint paintFill;
	Paint paintOval;
	
	final int TEXT_X_DISTANCE;
	final int TEXT_Y_DISTANCE;
	final int TEXT_SIZE;
	
	final int CURVE_TENSION = 5;
	
	private String[] titles = {"Monthly", "Weekly", "Daily", "Hourly"};
	private static int titleIndex = 0;
	
	public LineChart(LineChartParent parent, int x, int y, int width, int height, LineChartDataset dataset) {
		super(parent.getContext());
		this.setOnTouchListener(this);
		this.parent = parent;
		this.dataset = dataset;
		numPoints = dataset.size();
		
		this.currentX = x; this.currentY = y; 
		this.realX = x; this.realY = y;
		this.currentWidth = width; this.currentHeight = height;
		this.realWidth = width; this.realHeight = height;
		
		origin = new Point(x, y + height);
		center = new Point(x + width/2, y + height/2);
		
		//set size constants
		TEXT_SIZE = 20;
		NORMAL_RADIUS = TEXT_SIZE/2;
		EXPAND_RADIUS = 2 * NORMAL_RADIUS;
		
		//1080 was the width of the screen that I optimized the layout for
		APPROX_CHAR_WIDTH = 20 * width / 1080;
		POPUP_TEXT_SIZE = 40 * width /1080;	
		TEXT_X_DISTANCE = 60 * width / 1080;
		TEXT_Y_DISTANCE = 50 * width /1080;
		
		ovalWidth = (int) EXPAND_RADIUS;
		ovalHeight = (int) EXPAND_RADIUS;
		
		
		mHandler = new Handler();
		
		getMax();
		initPaints();
		
		setPoints();
	}
	
	/**Find the maximum value to use on the y axis */
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
		int howMany = 0;
		while(upTo < maxValue){
			upTo += (divisor);
			howMany++;
		}
		maxValue = upTo;
		
		Log.d("maxValue", "divisored up: " + maxValue);
	}

	private int getDigits(double input) {
		if(input / 10 < 1) return 1;
		else return 1 + getDigits(input / 10);
	}

	private void setPoints() {
		points = new ArrayList<LinePoint>();
		
		int size = dataset.size();
		int x_offset = currentWidth/(size - 1);
		
		int pointX; int pointY;
		
		for(int i = 0; i < size; i++){
			LineChartDataItem item = dataset.getItem(i);
			pointX = currentX + (i * x_offset); 
			double data = item.getData();
			pointY = (int)Math.round((origin.y) - (data / maxValue) * currentHeight);
			
			points.add(new LinePoint(item.getLabel(), data, i, pointX, pointY));
		}
		
		for(LinePoint point : points){
			//point.setY(origin.y);
		}
	}
	
	public void initPaints(){

		paintText = new Paint();
		paintText.setColor(Color.GRAY);
		paintText.setStrokeWidth(STROKE_WIDTH);
		paintText.setTextSize(TEXT_SIZE);
		
		paintConnections = new Paint();
		paintConnections.setColor(Color.BLACK);
		paintConnections.setStyle(Paint.Style.STROKE);
		paintConnections.setStrokeWidth(STROKE_WIDTH);
		
		paintGrid = new Paint();
		paintGrid.setColor(Color.GRAY);
		paintGrid.setStyle(Paint.Style.STROKE);
		paintGrid.setStrokeWidth(STROKE_WIDTH/4);
		
		paintFill = new Paint();
		paintFill.setColor(Color.GRAY);
		paintFill.setStyle(Paint.Style.FILL);
		
		paintOval = new Paint();
		paintOval.setColor(Color.BLACK);
		paintOval.setStyle(Paint.Style.STROKE);
		paintOval.setStrokeWidth(STROKE_WIDTH);
	}

	private void setPaintParams() {
		paintText.setAlpha(everythingButGridAlpha);
		paintFill.setAlpha(fillAlpha);
		paintConnections.setAlpha(everythingButGridAlpha);
		paintGrid.setAlpha(gridAlpha);
		paintOval.setAlpha(ovalAlpha);
		paintOval.setStrokeWidth(ovalStrokeWidth);
	}

	
//////////////////////////////////////////////////////////////////////////
//Draw
//////////////////////////////////////////////////////////////////////////
	@Override
	public void draw(Canvas canvas){
		invalidate();
		setPaintParams();
		setYTicks();
		
		drawOval(canvas);
		

		int y_increment = currentHeight / y_ticks;
		int x_increment = currentWidth / X_TICKS;
		drawGrid(canvas, y_increment, x_increment);
		
		if(!gridAnimating){
			drawTitle(canvas);
			
			x_increment = currentWidth / points.size();
			drawYLabels(canvas, y_increment);
			drawXLabels(canvas, x_increment);
			
			if(points.size() >= 2){  	//no lines to draw
				drawConnections(canvas);
			}
			
			for(LinePoint point : points){
				point.draw(canvas);
			}
		}
	}

	private void drawTitle(Canvas canvas) {
		canvas.drawText(titles[titleIndex], realX + realWidth/2 -25, realY - 30, paintText);
	}

	private void drawOval(Canvas canvas) {
		RectF rect = new RectF(currentX, currentY, currentX + currentWidth, currentY + currentHeight);
		canvas.drawRoundRect(rect, currentWidth * ovalRadiusFactor, currentHeight * ovalRadiusFactor, paintOval);
	}

	/**Determine how many "ticks" will be on the y axis.
	 * The algorithm essentially wants to make the numbers at the
	 * ticks look as clean as possible- more 0s, more even looking divisions.*/
	private void setYTicks() {
		
		int digits = getDigits(maxValue);
		int smaller = (int) (maxValue / (Math.max(1, Math.pow(10, digits - 2))));
		
		double divisor = 5;
		while(divisor <= 9){
			if(smaller % divisor == 0) break;
			divisor++;
		}
		
		if(divisor >= 9){
			divisor = 5;
		}
		
		y_ticks = (int)divisor;
	}

	private void drawYLabels(Canvas canvas, int y_increment){
		for(int i = 0; i < y_ticks + 1; i++){
			String val = "" + (i * (int)maxValue/y_ticks);
			canvas.drawText(val, currentX - TEXT_X_DISTANCE, origin.y - i * y_increment, paintText);
		}
	}
	
	private void drawXLabels(Canvas canvas, int x_increment) {
		for(int i = 0; i < points.size(); i++){
			LinePoint dataPoint = points.get(i);
			String val = dataPoint.getLabel();  
			canvas.drawText(val, dataPoint.getX() - 20, origin.y + TEXT_Y_DISTANCE, paintText);
		}
	}
	
	private void drawGrid(Canvas canvas, int y_increment, int x_increment){
		
		//draw y ticks
		for(int i = 0; i <= y_ticks; i++){
			Point left = new Point(currentX, currentY + currentHeight- y_increment * i);
			Point right = new Point(currentX + currentWidth, currentY + currentHeight - y_increment * i);
			canvas.drawLine(left.x, left.y, right.x,  right.y, paintGrid);
		}
		
		//draw x ticks
		for(int j = 0; j <= X_TICKS; j++){
			Point top = new Point(currentX + x_increment * j, currentY);
			Point bot = new Point(currentX + x_increment * j, currentY + currentHeight);
			canvas.drawLine(top.x, top.y, bot.x, bot.y, paintGrid);
		}
	}

	/**Draw the path connecting the points */
	private void drawConnections(Canvas canvas) {
		
		Path connectingPath = new Path();

		int size = points.size();
		if(size <= 1) return;
		
		setControlPoints(size);
		
		LinePoint first = points.get(0);
		
		connectingPath.moveTo(first.getX(),  first.getY());
		
		for(int i = 1; i < size; i++){
			LinePoint current = points.get(i);
			LinePoint prev = points.get(i- 1);
			
			connectingPath.cubicTo(prev.getControlX() + prev.getX(), prev.getControlY() + prev.getY(), 
					current.getX() - current.getControlX(), current.getY() - current.getControlY(),
					current.getX(), current.getY());
		}
		
		drawFill(canvas, connectingPath, first);		
		canvas.drawPath(connectingPath, paintConnections);
	}

	/**We're going to use Bezier curves to path through the points smoothly. 
	 * First we set a control point for each data point that will help
	 * determine its curve later.
	 */
	private void setControlPoints(int size) {
		
		//set first point's control point
		LinePoint first = points.get(0);
		LinePoint second = points.get(1);
		first.setControlPoint(new Point(
				(second.getX() - first.getX())/CURVE_TENSION,
				(second.getY() - first.getY())/CURVE_TENSION));
		
		//set middle points' control points
		for(int i = 1; i < size - 1; i++){
			Point before = points.get(i -1).getLocation();
			Point after = points.get(i + 1).getLocation();
			Point control = new Point();
			
			control.x = (after.x - before.x)/CURVE_TENSION;
			control.y = (after.y - before.y)/CURVE_TENSION;
			points.get(i).setControlPoint(control);
		}
		
		//set last point's control point
		LinePoint last = points.get(size -1);
		LinePoint penul = points.get(size -2);
		last.setControlPoint(new Point(
				(last.getX() - penul.getX())/CURVE_TENSION,
				(last.getY() - penul.getY())/CURVE_TENSION));
	}
	
	/**Fill the area underneath the line*/
	private void drawFill(Canvas canvas, Path connectingPath, LinePoint first) {
		
		//The fill path follows the same connections between the points,
		//and then creates a closed figure on the bottom part of the grid
		Path fillPath = new Path();
		fillPath.addPath(connectingPath);
		fillPath.lineTo(origin.x + currentWidth, points.get(numPoints-1).getY());
		fillPath.lineTo(origin.x + currentWidth, origin.y);
		fillPath.lineTo(origin.x, origin.y);
		fillPath.lineTo(first.getX(), first.getY());
		
		canvas.drawPath(fillPath, paintFill);
	}
	
	
//////////////////////////////////////////////////////////////////////////
//Getters and setters for animations
//////////////////////////////////////////////////////////////////////////
	public int getCurrentHeight(){ return currentHeight; }
	public void setCurrentHeight(float newHeight){ currentHeight = (int) newHeight; }
	public float getRealHeight(){ return realHeight; }
	
	public int getCurrentWidth(){ return currentWidth; }
	public void setCurrentWidth(float newWidth){ currentWidth = (int) newWidth; }
	public float getRealWidth(){ return realWidth; }
	
	public int getCurrentX(){ return currentX; }
	public void setCurrentX(float val){ currentX = (int) val; }
	public int getCurrentY(){ return currentY; }
	public void setCurrentY(float val){ currentY = (int) val; }
	
	public int getEverthingButGridAlpha(){ return everythingButGridAlpha; }
	public void setEverythingButGridAlpha(float val){ everythingButGridAlpha = (int) val; }
	
	public int gridAlpha(){ return gridAlpha; }
	public void setGridAlpha(float val){ gridAlpha = (int)val; }
	
	public int getOvalAlpha(){ return ovalAlpha; }
	public void setOvalAlpha(float val){ ovalAlpha = (int)val; }
	
	public int getOvalWidth(){ return ovalWidth; }
	public void setOvalWidth(float val){ ovalWidth = (int)ovalWidth; }
	public int getOvalHeight(){ return ovalHeight; }
	public void setOvalHeight(float val){ ovalHeight = (int)ovalHeight; }
	
	public float getOvalStrokeWidth(){ return ovalStrokeWidth; }
	public void setOvalStrokeWidth(float val){ ovalStrokeWidth = val; }
	
	public float getOvalRadiusFactor(){ return ovalRadiusFactor; }
	public void setOvalRadiusFactor(float val){ ovalRadiusFactor = val; }
	
//////////////////////////////////////////////////////////////////////////
//Animate
//////////////////////////////////////////////////////////////////////////	
	public void replayAnimation(){
		Log.d("replay", "replay linechart");
		this.fillAlpha = 0;
		
		if(animateGridNextTime){
			animateGrid(pointToExpandFrom);
		}
		else{
			for(LinePoint point : points){
				point.setCircleAlpha(0);
				point.setY(origin.y);
				if(point.isExpanded())point.expandOrDeflate();
			}
			
			animatePoints();
		}
	}
	
	public void setPointToExpandFrom(Point where){
		this.pointToExpandFrom = where;
		animateGridNextTime = true;
	}
	
	public void animateGrid(Point pointToExpandFrom){
		final long GRID_DURATION = 800;
		final long OVAL_DURATION = GRID_DURATION/3;
		
		inflatePoint(-1);
		gridAnimating = true;
		gridAlpha = 0;
		ovalAlpha = 255;
		
		
		AnimatorSet gridAnims = new AnimatorSet();
		TimeInterpolator inter = new AccelerateDecelerateInterpolator();
		
		ObjectAnimator widthAnim = ObjectAnimator.ofFloat(this, "currentWidth", 0f, realWidth);
		widthAnim.setDuration(GRID_DURATION);
		widthAnim.setInterpolator(inter);
		
		ObjectAnimator heightAnim = ObjectAnimator.ofFloat(this, "currentHeight", 0f, realHeight);
		heightAnim.setDuration(GRID_DURATION);
		heightAnim.setInterpolator(inter);
		
		ObjectAnimator xAnim = ObjectAnimator.ofFloat(this, "currentX", (float)pointToExpandFrom.x, realX);
		xAnim.setDuration(GRID_DURATION);
		xAnim.setInterpolator(inter);
		
		ObjectAnimator yAnim = ObjectAnimator.ofFloat(this, "currentY", (float)pointToExpandFrom.y, realY);
		yAnim.setDuration(GRID_DURATION);
		yAnim.setInterpolator(inter);
		
		final ObjectAnimator gridAlphaAnim = ObjectAnimator.ofFloat(this, "gridAlpha", 0f, 255f);
		gridAlphaAnim.setDuration(OVAL_DURATION/2);
		gridAlphaAnim.setInterpolator(new AccelerateInterpolator());
		
		ObjectAnimator ovalStrokeAnim = ObjectAnimator.ofFloat(this, "ovalStrokeWidth", LinePoint.CIRCLE_STROKE_WIDTH, 0);
		ovalStrokeAnim.setDuration(OVAL_DURATION);
		
		final ObjectAnimator ovalAlphaAnim = ObjectAnimator.ofFloat(this, "ovalAlpha", 255f, 0);
		ovalAlphaAnim.setDuration(OVAL_DURATION/2);
		
		ObjectAnimator ovalRadiusAnim = ObjectAnimator.ofFloat(this, "ovalRadiusFactor", 1, 0);
		ovalRadiusAnim.setDuration(OVAL_DURATION);
		
		gridAnims.playTogether(widthAnim, heightAnim, xAnim, yAnim, ovalStrokeAnim, ovalRadiusAnim);
		gridAnims.start();
		
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				ovalAlphaAnim.start();
			}
		}, (long) (OVAL_DURATION * .65));
		
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				gridAlphaAnim.start();
			}
		}, (long) (OVAL_DURATION * .75));
		
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				animateFadeIn(); 
				gridAnimating = false;
			}
		}, (long) (GRID_DURATION * 1.1));
		
		titleIndex = (titleIndex + 1) % titles.length;
		animateGridNextTime = false;
	}
	
	public void animateFadeIn(){
		final long FADE_DURATION = 500;
		List<Animator> allAnims = new ArrayList<Animator>(); 
		
		ObjectAnimator fill = ObjectAnimator.ofFloat(this, "fillAlpha", 0f, FULL_FILL_ALPHA);
		fill.setDuration(FADE_DURATION);
		allAnims.add(fill);
		
		ObjectAnimator everythingElse = ObjectAnimator.ofFloat(this, "everythingButGridAlpha", 0f, 255f);
		everythingElse.setDuration(FADE_DURATION);
		allAnims.add(everythingElse);
		
		for(LinePoint point : points){
			ObjectAnimator pointAnim = ObjectAnimator.ofFloat(point, "circleAlpha", 0f, 255f);
			pointAnim.setDuration(FADE_DURATION);
			allAnims.add(pointAnim); 
		}
		
		AnimatorSet fadeAnims = new AnimatorSet();
		fadeAnims.playTogether(allAnims);
		fadeAnims.start();
	}
	
	
	public void animatePoints(){
		final long DURATION = 800;
		
		ArrayList<Animator> upAnimations = new ArrayList<Animator>();
		ArrayList<Animator> fadeAnimations = new ArrayList<Animator>();
		
		AnimatorSet upSet = new AnimatorSet();
		final AnimatorSet fadeSet = new AnimatorSet();
		
		for(int i = 0; i < points.size(); i++){
			LinePoint point = points.get(i);
			int y2 = point.getReal().y;
			
			ObjectAnimator up = ObjectAnimator.ofFloat(point, "y", origin.y, y2);
			up.setDuration(DURATION);
			up.setInterpolator(new OvershootInterpolator());
			upAnimations.add(up);
			
			ObjectAnimator pointFade = ObjectAnimator.ofFloat(point, "circleAlpha", 0f, point.getFullAlpha());
			pointFade.setDuration(DURATION/4);
			fadeAnimations.add(pointFade);
		}
		
		upSet.playTogether(upAnimations);
		fadeSet.playTogether(fadeAnimations);
		
		upSet.start();
		
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				animateFill(DURATION);
				fadeSet.start();
			}
		}, (long)(DURATION * .75));
	}
	
	public void animateFill(long dur){
		ObjectAnimator fillFadeIn = ObjectAnimator.ofFloat(this, "fillAlpha", 0f, FULL_FILL_ALPHA);
		fillFadeIn.setDuration(dur);
		fillFadeIn.setInterpolator(new DecelerateInterpolator());
		fillFadeIn.start();
	}
	
	public void setFillAlpha(float val){
		fillAlpha = (int)val;
	}
	
	//Touch
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if(event.getAction() != MotionEvent.ACTION_DOWN) return false;

		Point touchPoint = new Point((int)event.getX(), (int)event.getY()); 		
		Log.d("touch", "touched at " + touchPoint + ", action " + event.getAction());
		
		int index = -1;
		
		for(int i = 0; i < points.size(); i++){
			LinePoint point = points.get(i);
			
			//describe a bounding rectangle for simplicity
			final double BUFFER = 5;
			int left = (int) (point.getX() - (point.getRadius() * BUFFER)); 
			int right  = (int) (point.getX() + (point.getRadius() * BUFFER));
			int down = (int) (point.getY() + (point.getRadius() * BUFFER));
			int up = (int) (point.getY() - (point.getRadius() * BUFFER));
			
			boolean inside = touchPoint.x > left && touchPoint.x < right
					&& touchPoint.y < down && touchPoint.y > up;
					
			boolean expanded = point.isExpanded();
			if(inside && !expanded){
				index = point.getIndex();
				Log.d("touch", "expanding " + index);
				point.expandOrDeflate();
			}
			
			//expand the grid out from the touched point
			else if(inside && expanded){
				point.expandOrDeflate();
				animateGridNextTime = true;
				
				parent.LineChartItemDoubleClicked(point);
			}
			
			else if(expanded){
				point.expandOrDeflate();
			}
			
		}
		
		parent.LineChartItemClicked(index);
		
		return true;
	}
	
	public void inflatePoint(int which){
		for(int i = 0; i < points.size(); i++){
			LinePoint point = points.get(i);
			if(point.getIndex() == which && !point.isExpanded()){
				point.expandOrDeflate();
			}
			else if(point.isExpanded()){
				point.expandOrDeflate();
			}
		}
	}
	
	
//////////////////////////////////////////////////////////////////////////
//DataPoint
//////////////////////////////////////////////////////////////////////////
	public class LinePoint extends ShapeDrawable{
		
		final static float CIRCLE_STROKE_WIDTH = 4f;
		
		private int index;
		private String label;
		private double data;
		private Point location;
		private Point realLocation;
		
		private boolean expanded = false;
		
		private int circleAlpha = 0;
		public final float FULL_CIRCLE_ALPHA = 255f;
		public float getFullAlpha(){ return FULL_CIRCLE_ALPHA; }
		
		/**The control point is used to determine the bezier curve that goes through
		 * this datapoint*/
		private Point controlPoint;
		
		private float radius = NORMAL_RADIUS; 
		Paint paintPoint;
		Paint paintPopupText;
		Paint paintBox;
		Paint paintBoxInside;
		
		public LinePoint(String label, double data, int index, int x, int y){
			location = new Point(x, y);
			realLocation = new Point(x,y);
			
			this.index = index;
			this.label = label;
			this.data = data;
			
			OvalShape ovalShape = new OvalShape();
			ovalShape.resize(radius, radius);
			this.setShape(ovalShape);
			
			initPaint();
		}
		
		public void expandOrDeflate(){
			float to = expanded ? NORMAL_RADIUS : EXPAND_RADIUS;
			float from = expanded ? EXPAND_RADIUS : NORMAL_RADIUS;
			ObjectAnimator ex = ObjectAnimator.ofFloat(this, "radius", from, to);
			ex.setDuration(CIRCLE_ANIM_DURATION);
			ex.start();
			expanded = !expanded;
		}
		
		public boolean isExpanded(){
			return expanded;
		}

		private void initPaint() {
			paintPoint = new Paint();
			setPaintInner();
			setPaintOutter();
			
			paintPopupText = new Paint();
			paintPopupText.setColor(Color.BLACK);
			paintPopupText.setTextSize(POPUP_TEXT_SIZE);
			
			paintBox = new Paint();
			paintBox.setColor(Color.BLACK);
			paintBox.setStyle(Paint.Style.STROKE);
			paintBox.setStrokeWidth(STROKE_WIDTH);
			
			paintBoxInside = new Paint();
			paintBoxInside.setColor(Color.WHITE);
			paintBoxInside.setStyle(Paint.Style.FILL);
			
		}

		private void setPaintOutter() {
			paintPoint.setColor(Color.BLACK);
			paintPoint.setStyle(Paint.Style.STROKE);
			paintPoint.setStrokeWidth(CIRCLE_STROKE_WIDTH);
		}

		private void setPaintInner() {
			paintPoint.setColor(Color.WHITE);
			paintPoint.setStyle(Paint.Style.FILL);
		}
		
		@Override 
		public void draw(Canvas canvas){
			
			setPaintInner();
			paintPoint.setAlpha(circleAlpha);
			canvas.drawCircle(location.x, location.y, radius, paintPoint);
			
			setPaintOutter();
			paintPoint.setAlpha(circleAlpha);
			canvas.drawCircle(location.x, location.y, radius, paintPoint);

			if(expanded){
				drawText(canvas);
			}
		}

		private void drawText(Canvas canvas) {
			//set the alpha to the proportion that the expand animation is done- simulate fade in
			paintPopupText.setAlpha( (int) ((1 - (EXPAND_RADIUS - radius) / (EXPAND_RADIUS - NORMAL_RADIUS)) * 255));
						
			int xOffset = (int)(TEXT_X_DISTANCE * .75);
			int numChars = label.length();

			boolean closeToRightEdge = (origin.x + realWidth) - location.x < realWidth * .25;
			

			//Put text on the left side instead
			if(closeToRightEdge){
				xOffset = (int) (-xOffset - (APPROX_CHAR_WIDTH *numChars));
			}
			
			RectF rect = getRect(closeToRightEdge, numChars);
			
			canvas.drawRoundRect(rect, radius, radius, paintBoxInside);
			canvas.drawRoundRect(rect, radius, radius, paintBox);
			
			float topRow = (float) (location.y - NORMAL_RADIUS * 3.5);			
			float bottomRow = location.y + NORMAL_RADIUS;
			
			canvas.drawText("" + (int)data, location.x + xOffset, bottomRow, paintPopupText);
			canvas.drawText(label, location.x + xOffset, topRow, paintPopupText);
		}
		
		/**Get the rectangle that the text fits in*/
		private RectF getRect(boolean close, int numDigits){
			float top = (float) (location.y - radius * 4.3);
			float bot = (float) (location.y + radius * 1.5);
			float left = 0; float right = 0;
			
			if(close){
				left = (float) (location.x - (TEXT_X_DISTANCE * .75) - (numDigits + 1) * APPROX_CHAR_WIDTH);
				right = (float) (location.x - TEXT_X_DISTANCE * .5);
			}
			else{
				left = (float) (location.x + TEXT_X_DISTANCE * .5);
				right = location.x + TEXT_X_DISTANCE + (numDigits + 1) * APPROX_CHAR_WIDTH;
			}
			
			return new RectF(left, top, right, bot);
		}
		
//Boilerplate
		public void setY(float y){ 
			location.y = (int)y;
		}
		
		public void setX(float x){
			location.x = (int)x;
		}
		
		public void setCircleAlpha(float val){
			circleAlpha = (int) val;
		}
		public int getAlpha(){ return circleAlpha; }
		
		public Point getLocation(){ return location; }
		public int getY(){ return location.y; }
		public int getX(){ return location.x; }
		public float getRadius(){ return radius; }
		public void setRadius(float r) { radius = (int)r; }
		public String getLabel(){ return label; }
		
		public void setControlPoint(Point point){ controlPoint = point; }
		public Point getControlPoint(){ return controlPoint; }
		public Point getReal(){ return realLocation; }
		
		public int getControlX(){ return controlPoint.x; }
		public int getControlY(){ return controlPoint.y; }
		
		public int getIndex() { return index; }
	}
	
	/**Any container using a LineChart must implement this interface*/
	public interface LineChartParent{
		public void LineChartItemClicked(int which);
		public void LineChartItemDoubleClicked(LinePoint where);
		public Context getContext();
	}

	
}
