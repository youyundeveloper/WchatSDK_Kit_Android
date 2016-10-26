package com.ioyouyun.ui.chat.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class MyTouchRelativeLayout extends RelativeLayout{
	
	private boolean interceptMove = false;
	
	private List<EventActionUpListener> eventActionUpListeners;

	public MyTouchRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		eventActionUpListeners = new ArrayList<EventActionUpListener>();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
        case MotionEvent.ACTION_MOVE:
            return interceptMove;
        case MotionEvent.ACTION_UP:
        	for(int i =0;i<eventActionUpListeners.size();i++){
				eventActionUpListeners.get(i).handle();
			}
        	break;
		}
		return false;
	}
	
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			for(int i =0;i<eventActionUpListeners.size();i++){
				eventActionUpListeners.get(i).handle();
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	public void addEventActionUpListener(EventActionUpListener listener){
		eventActionUpListeners.add(listener);
	}
	
	public interface EventActionUpListener{
		public void handle();
	}

	public boolean isInterceptMove() {
		return interceptMove;
	}

	public void setInterceptMove(boolean interceptMove) {
		this.interceptMove = interceptMove;
	}
	
}
