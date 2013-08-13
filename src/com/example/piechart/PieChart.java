package com.example.piechart;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.OvershootInterpolator;

public class PieChart extends View implements OnClickListener{
	
	private ArrayList<ArcView> arcs;
	
	int x; int y;
	int width; int height;
	
	public PieChart(Context context) {
		super(context);
		width = 200;
		height = 200;
		x = 100;
		y = 50;
		
		addArcs(width, height, x, y);
		//this.setOnClickListener(this);
		
	}
	
	@Override 
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	private void addArcs(int width, int height, int x, int y) {
		arcs = new ArrayList<ArcView>(); 
		arcs.add(new ArcView(this, x, y, width, height, -90, 165, 0f, 0));
		arcs.add(new ArcView(this, x, y, width, height, 75, 135, 0f, 1));
		arcs.add(new ArcView(this, x, y, width, height, 210, 60, 0f, 2));
	}
	
	protected void onDraw(Canvas canvas){
		for(ArcView arc : arcs){
			arc.draw(canvas);
		}
	}

	@Override
	public void onClick(View view) {
		Log.d("tag", "onClick");
		int arcIndex = handleClick(null);
		
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
	}
	
	@Override 
	public boolean onTouchEvent(MotionEvent e){
		e.getX();
		e.getY();
		return false;
	}
	
	private int handleClick(Point loc) {
		int result = -1;
		int numArcs = arcs.size();
		
		Point center = new Point(x + width/2, y + height/2);
		int diameter = width/2;
		
		int distance = getDistance(loc, center);
		
		//point is in the circle
		if(distance < diameter){
			//TODO get angle
		}
		else{
			replayAnimation();
		}
		
		return result;
	}
	
	private int getDistance(Point point1, Point point2) {
		return Math.abs(point1.x - point2.x) + Math.abs(point2.y - point1.y);
	}
	
	

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
		
		private boolean expanded;
		private static final float EXPAND_SCALE = 1.2f;
		
		
		/**determines the size of the arc as a measure
		 * of how far it extends from the center. 
		 */
		private float scale = 1;
		
		public ArcView(View parent, int x, int y, int height, int width,
				float beginAngle, float sweepAngle, float scale, int index){
			super(new ArcShape(beginAngle, sweepAngle));			
			
			this.left = x; this.top = y; this.height = height; this.width = width;
			this.beginAngle = beginAngle; this.sweepAngle = sweepAngle;
			
			getPaint().setColor(0xff080808 + (0xff080808 * index * 3));
			setScale(scale);
			setCallback(parent);
		}
		
		public int getIndex() {
			return index;
		}

		/**Keep the center in the same place, but change size
		 * of bounding rectangle to change size.
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
			this.setShape(new ArcShape(beginAngle, sweepAngle));
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
	}
}
