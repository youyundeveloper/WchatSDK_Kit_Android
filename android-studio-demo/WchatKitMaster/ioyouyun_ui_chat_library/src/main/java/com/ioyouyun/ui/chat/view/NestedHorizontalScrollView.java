package com.ioyouyun.ui.chat.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

public class NestedHorizontalScrollView extends HorizontalScrollView {
	
	private ViewGroup parent;

	public NestedHorizontalScrollView(Context context, AttributeSet attrs) {
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
