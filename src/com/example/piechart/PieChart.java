package com.example.piechart;

import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.OvershootInterpolator;

public class PieChart extends View implements OnClickListener{
	
	private ArrayList<ArcView> arcs;
	
	public PieChart(Context context) {
		super(context);
		int width = 300;
		int height = 300;
		int x = 0;
		int y = 0;
		
		addArcs(width, height, x, y);
		
		this.setOnClickListener(this);
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
		for(ArcView arc : arcs){
			arc.setScale(0);
		}
		openingAnimation(view);
	}
	
	private void openingAnimation(View view) {
		final long ANIMATION_SPEED = 350;
		final long DELAY = 35;
		
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
		
	}
}
