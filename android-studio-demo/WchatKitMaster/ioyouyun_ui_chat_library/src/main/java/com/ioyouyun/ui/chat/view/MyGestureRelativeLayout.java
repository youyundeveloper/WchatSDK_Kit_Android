package com.ioyouyun.ui.chat.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class MyGestureRelativeLayout extends RelativeLayout implements OnGestureListener {
	public static final int FLING_MIN_DISTANCE = 50;
	public static final int FLING_MIN_VELOCITY = 0;
	
	private float xDistance, yDistance, lastX, lastY;
	private GestureDetector mGestureDetector;
	private List<MyGestureObserver> gestureObservers;
	private boolean interceptMove = false;

	public MyGestureRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
//		TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.MyGestureRelativelayout);
//		interceptMove = typedArray.getBoolean(R.styleable.MyGestureRelativelayout_interceptMove, true);
		mGestureDetector = new GestureDetector(context,this);
		gestureObservers = new ArrayList<MyGestureObserver>();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		mGestureDetector.onTouchEvent(ev);
		if(interceptMove){
			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				xDistance = yDistance = 0f;
				lastX = ev.getX();
				lastY = ev.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				final float curX = ev.getX();
				final float curY = ev.getY();
				xDistance += Math.abs(curX - lastX);
				yDistance += Math.abs(curY - lastY);
				lastX = curX;
				lastY = curY;
				if(xDistance > yDistance && (xDistance > FLING_MIN_DISTANCE || yDistance > FLING_MIN_DISTANCE)){
					return true;	            	
				}
			}
		}
		return false;		
	}
	
	
	
	public void setInterceptMove(boolean interceptMove) {
		this.interceptMove = interceptMove;
	}
	
	public boolean getInterceptMove(){
		return this.interceptMove;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		mGestureDetector.onTouchEvent(ev);
		return super.onTouchEvent(ev);		
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		boolean flag = false;
		if(e1.getX() - e2.getX() > FLING_MIN_DISTANCE 
				&& Math.abs(e1.getX()-e2.getX()) > Math.abs(e1.getY()-e2.getY())
				&& Math.abs(velocityX) > FLING_MIN_VELOCITY){
			notifyGestureObservers(MyGestureObserver.DIRECTION_LEFT);
			flag = true;
		}else if(e2.getX() - e1.getX() > FLING_MIN_DISTANCE 
				&& Math.abs(e1.getX()-e2.getX()) > Math.abs(e1.getY()-e2.getY())
				&& Math.abs(velocityX) > FLING_MIN_VELOCITY){
			notifyGestureObservers(MyGestureObserver.DIRECTION_RIGHT);
			flag = true;
		}
		return flag;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
	
	private void notifyGestureObservers(int direction){
		for(int i =0;i<gestureObservers.size();i++){
			gestureObservers.get(i).handle(direction);
		}
	}
	
	public void removeGestureListener(MyGestureObserver observer){
		gestureObservers.remove(observer);
	}
	
	public void setGestureListener(MyGestureObserver observer){
		gestureObservers.add(observer);
	}
	
	public interface MyGestureObserver{
		public static int DIRECTION_LEFT = 1;
		public static int DIRECTION_RIGHT = 2;
		public void handle(int direction);
	}

}
