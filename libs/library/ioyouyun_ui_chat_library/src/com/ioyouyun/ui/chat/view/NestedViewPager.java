package com.ioyouyun.ui.chat.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class NestedViewPager extends ViewPager{
	private ViewGroup parent;

	public NestedViewPager(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public NestedViewPager(Context context, AttributeSet attrs){
		super(context, attrs);
	}
	
	public void setNestedpParent(ViewGroup parent){
		this.parent = parent;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if(parent != null){
			parent.requestDisallowInterceptTouchEvent(true);
		}
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub
		if(parent != null){
			parent.requestDisallowInterceptTouchEvent(true);
		}
		return super.onInterceptTouchEvent(arg0);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub
		if(parent != null){
			parent.requestDisallowInterceptTouchEvent(true);
		}
		return super.onTouchEvent(arg0);
	}

}
