package com.aretha.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

public class ClickWheelView extends ViewGroup {
	private float mRadius;

	private float mTouchSlop;

	public ClickWheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		initialize();
	}

	public ClickWheelView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ClickWheelView(Context context) {
		this(context, null);
	}

	private void initialize() {
		mTouchSlop = new ViewConfiguration().getScaledTouchSlop();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int count = getChildCount();

		measureChildren(widthMeasureSpec, heightMeasureSpec);

		int measureWidth = 0, measureHeight = 0;
		int maxChildWidth = 0, maxChildHeight = 0;
		for (int index = 0; index < count; index++) {
			View child = getChildAt(index);
			maxChildWidth = Math.max(maxChildWidth, child.getMeasuredWidth());
			maxChildHeight = Math
					.max(maxChildHeight, child.getMeasuredHeight());
		}

		measureHeight += Math.ceil(maxChildWidth / 2);
		measureHeight += Math.ceil(maxChildHeight / 2);

		measureWidth += getPaddingLeft() + getPaddingRight();
		measureHeight += getPaddingTop() + getPaddingBottom();

		setMeasuredDimension(resolveSize(measureWidth, widthMeasureSpec),
				resolveSize(measureHeight, heightMeasureSpec));
	}
}
