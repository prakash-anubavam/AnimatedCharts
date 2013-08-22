package com.example.animatedcharts;

import java.util.ArrayList;
import java.util.List;

import com.example.animatedcharts.PieChartDataset.PieChartDataItem;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.OvershootInterpolator;

public class PieChart extends View implements OnTouchListener{
	
	private ArrayList<ArcView> arcs;
	
	int x; int y;
	int width; int height;
	PieChartDataset mData;
	
	Paint mLinePainter;
	ArrayList<Point> mLineEndPoints;
	
	ChartFragment parent;
	Point mCenter;
	
	public PieChart(ChartFragment context, int x, int y, int width, int height, PieChartDataset data) {
		super(context.getActivity());
		this.width = width;
		this.height = height;
		this.x = x;
		this.y = y;
		this.parent = context;
		
		mCenter = new Point(x + width/2, y + height/2);
		mData = data;
		
		addArcs();
	
		initLineEndPoints(width);

		this.setOnTouchListener(this);
	}

	private void initLineEndPoints(int width) {
		final int STROKE = 4;
		
		mLineEndPoints = new ArrayList<Point>();
		//Add the points that are at the end of the white lines.
		for(ArcView arc : arcs){
			float endAngle = arc.getEndAngle();
			Point endPoint = getEndPoint(mCenter,  endAngle, width/2);
			mLineEndPoints.add(endPoint);
		}
		
		mLinePainter = new Paint();
		
		mLinePainter.setStrokeWidth(STROKE);
		mLinePainter.setColor(Color.WHITE);
	}
	
	@Override 
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	private void addArcs() {
		int endOfLastArc = 0;
		int index = 0;
		arcs = new ArrayList<ArcView>(); 
		
		List<PieChartDataItem> dataItems = mData.getData();
		for(PieChartDataItem data : dataItems){
			int sweep = (int) Math.round(data.getPercentage() * 360);
			
			arcs.add(new ArcView(this, x, y, width, height, endOfLastArc, sweep, 0f, index));
			endOfLastArc = endOfLastArc + sweep;
			index++;
		}
	}
	
	protected void onDraw(Canvas canvas){
		for(ArcView arc : arcs){
			arc.draw(canvas);
		}
		
		//Draw white lines
		for(Point endPoint : mLineEndPoints){
			canvas.drawLine(mCenter.x, mCenter.y, endPoint.x, endPoint.y, mLinePainter);
		}
	}
	
	private Point getEndPoint(Point center, float angle, int radius) {
		Point result = new Point();
		
		radius = (int)(radius * ArcView.EXPAND_SCALE);
		
		//angle on our circle is different from trig angle
		double fixedAngle = -(450 - angle);
		
		double angleRad = Math.toRadians(fixedAngle);
		
		double sin = Math.sin(angleRad);
		double cos = Math.cos(angleRad);
		
		Log.d("point", String.format("angleRad %s, angleDeg %s, angleFixed %s" +
				" sin %s, cos %s", angleRad, angle, fixedAngle, sin, cos));
		
		int yOffset = (int)(sin * radius);
		result.y = mCenter.y + yOffset;
		result.x = mCenter.x + (int)(cos * radius);
		
		return result;
	}

///////////////////////////////////////////////////////
//touch
///////////////////////////////////////////////////////
	@Override
	public boolean onTouch(View arg0, MotionEvent e) {
		Log.d("tag", "onTouch");
		
		int eventY = (int)e.getY();
		int eventX = (int)e.getX();
		
		int arcIndex = whichArcIsThePointIn(new Point(eventY, eventX));
		parent.arcClicked(arcIndex);
		
		return inflateArcIndex(arcIndex);
	}

	public boolean inflateArcIndex(int arcIndex) {
		for(ArcView arc : arcs){
			if(arcIndex == arc.getIndex() && !arc.isExpanded()){
				arc.expand();			
			}
			else{
				if(arc.isExpanded()){
					arc.deflate();
				}
			}
		}
		return false;
	}
	
	private int whichArcIsThePointIn(Point loc) {
		int result = -1;
		
		Point center = new Point(y + height/2, x + width/2);
		float radius = width * .75f;	//slight buffer outside
		
		int distance = getDistance(loc, center);
		Log.d("touch", String.format("Center %s, TouchPoint %s, distance %s, radius %s",
				center, loc, distance, radius));
		
		//point is in the circle
		boolean inCircle = distance < radius;
		Log.d("touch", "Touched in circle: " + inCircle);
		if(inCircle){
			double angle = getAngle(center, loc);
			Log.d("touch", "Angle: " + angle);
			
			for(ArcView arc : arcs){
				//if the angle is within this arc
				if(angle > arc.getBeginAngle() && angle < arc.getEndAngle()){
					result = arc.getIndex();
				}
			}
		}
		
		Log.d("touch", "In arc: " + result);
		return result;
	}
	
	private static int getDistance(Point point1, Point point2) {
		return Math.abs(point1.x - point2.x) + Math.abs(point2.y - point1.y);
	}
	
	/**Use arctan to get the angle between the two points*/
	private static double getAngle(Point center, Point point){
		int xDiff = point.x - center.x;
		int yDiff = point.y - center.y;
		double result = Math.toDegrees(Math.atan2(yDiff, xDiff));
		
		return convertAngle(result);
	}
	
	/**The angle returned by arctan is not compatible with the angles
	 * used by the arcs. 0 is at due south and it goes counter clockwise
	 * as opposed to clockwise. Also, anything in the 1st and 4th quadrant is a negative angle.
	 * 
	 * This method's logic converts the arctan angle into an angle on the arc's circle, 0 at due north.
	 */
	private static double convertAngle(double angle){
		return 180 - angle;
	}
	
///////////////////////////////////////////////////////
//opening animation
///////////////////////////////////////////////////////
	public void replayAnimation(){
		for(ArcView arc : arcs){
			arc.setScale(0);
		}
		openingAnimation();
	}
	
	private void openingAnimation() {
		final long ANIMATION_SPEED = 350;
		final long DELAY = 25;
		
		List<Animator> animations = new ArrayList<Animator>();
		
		for(ArcView arc : arcs){
			ObjectAnimator anim = ObjectAnimator.ofFloat(arc, "scale", 0f, 1f).setDuration(ANIMATION_SPEED);
			anim.setInterpolator(new OvershootInterpolator());
			animations.add(anim);
		}
		
		final List<Animator> anims = animations;
		final Handler handler = new Handler();
		
		Runnable startNextAnim = new Runnable() {
			
			@Override
			public void run() {
				anims.get(nextAnim).start();
				nextAnim++;
				if(nextAnim < anims.size()){
					handler.postDelayed(this, DELAY);
				}
				else nextAnim = 0;
			}
		};
		
		startNextAnim.run();
		
	}
	static int nextAnim = 0;
	
	
	
	
///////////////////////////////////////////////////////
//arcview
///////////////////////////////////////////////////////
	private class ArcView extends ShapeDrawable {
		private int left;
		private int top;
		private int height;
		private int width;
		
		private int scaledLeft;
		private int scaledTop;
		private int scaledHeight;
		private int scaledWidth;
		
		private float beginAngle;
		private float sweepAngle;
		private int index;
		private int color;
		
		private boolean expanded;
		private static final float EXPAND_SCALE = 1.1f;
		
		
		/**determines the size of the arc as a measure
		 * of how far it extends from the center. 
		 */
		private float scale = 1;
		
		public ArcView(View parent, int x, int y, int height, int width,
				float beginAngle, float sweepAngle, float scale, int index){
			super(new ArcShape(beginAngle, sweepAngle));			
			
			this.left = x; this.top = y; this.height = height; this.width = width;
			
			this.beginAngle = beginAngle;
			
			this.sweepAngle = sweepAngle;
			this.index = index;
			
			this.color = 0xff080808 + (0xff080808 * index * 3);
			
			getPaint().setColor(color);
			setScale(scale);
			setCallback(parent);
			
			Log.d("arc", String.format("%s Arc created, beginAngle %s, endAngle %s",
					getIndex(), getBeginAngle(), getEndAngle()));
		}
		
		public int getIndex() {
			return index;
		}

		
		/**Keep the center in the same place, but change size
		 * of bounding rectangle to simulate the arc growing outward.
		 * @param scale the scale
		 */
		public void setScale(float scale){
			
			Log.d("tag", "setting scale: " + scale);
			this.scale = scale;
			
			scaledLeft = (int) (left + (width  - (width * scale))/2);
			scaledTop = (int) (top + (height - (height * scale))/2);
			scaledHeight = (int) (height - (height - (height * scale)));
			scaledWidth = (int) (width - (width - (width * scale)));
			
			this.setBounds(scaledLeft, scaledTop, scaledLeft + scaledWidth, scaledTop + scaledHeight);
			
			//0 degrees is at due east. We want it at due north
			this.setShape(new ArcShape(beginAngle - 90, sweepAngle));
		}

		@Override 
		public void draw(Canvas canvas){
			super.draw(canvas);
			
			invalidate();
		}
		
		
		public void expand(){
			final long DURATION = 100;
			ObjectAnimator expandAnim = ObjectAnimator.ofFloat(this, "scale", 1f, EXPAND_SCALE);
			expandAnim.setDuration(DURATION);
			expandAnim.start();
			expanded = true;
		}
		
		public void deflate(){
			final long DURATION = 100;
			ObjectAnimator deflateAnim = ObjectAnimator.ofFloat(this, "scale", EXPAND_SCALE, 1f);
			deflateAnim.setDuration(DURATION);
			deflateAnim.start();
			expanded = false;
		}
		
		public boolean isExpanded(){
			return expanded;
		}
		
		public float getBeginAngle(){
			return beginAngle;
		}
		
		public float getEndAngle(){
			return beginAngle + sweepAngle;
		}
		
		public int getColor(){
			return color;
		}
	}

	
}
