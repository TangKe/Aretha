package com.aretha.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

public class ClickWheelView extends ViewGroup {
	private final static int STATE_IDLE = 0x0001;
	private final static int STATE_SCROLL = 0x0002;
	private float mRadius = 200;

	private int mRotateDegee;

	private int mCenterX;
	private int mCenterY;

	private int mTouchSlop;
	private int mFlingVelocity;
	private float mPressedX;
	private float mPressedY;

	private float mLastMotionX;
	private float mLastMotionY;
	private int mState = STATE_IDLE;

	public ClickWheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		initialize(context);
	}

	public ClickWheelView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ClickWheelView(Context context) {
		this(context, null);
	}

	private void initialize(Context context) {
		ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
		mTouchSlop = viewConfiguration.getScaledTouchSlop();

		setStaticTransformationsEnabled(true);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final float childCount = getChildCount();
		final int centerX = mCenterX;
		final int centerY = mCenterY;
		final float radianPerChild = (float) (2 * Math.PI / childCount);
		final int[] coordinate = new int[2];
		final float radius = mRadius;
		final float rotateRadian = (float) (mRotateDegee * Math.PI / 180);

		for (int index = 0; index < childCount; index++) {
			View child = getChildAt(index);
			int measuredWidth = child.getMeasuredWidth();
			int measuredHeight = child.getMeasuredHeight();
			computeChildCoordinate(coordinate, index, measuredWidth,
					measuredHeight, radianPerChild, radius, rotateRadian,
					centerX, centerY);
			child.layout(coordinate[0], coordinate[1],
					coordinate[0] + child.getMeasuredWidth(), coordinate[1]
							+ child.getMeasuredHeight());
		}
	}

	protected void computeChildCoordinate(int[] coordinate, int index,
			int childWidth, int childHeight, float radianPerChild,
			float radius, float rotateRadian, int centerX, int centerY) {
		float radian = index * radianPerChild + rotateRadian;
		int xRange = (int) Math.round(radius * Math.cos(radian));
		int yRange = (int) Math.round(radius * Math.sin(radian));
		coordinate[0] = centerX + xRange - childWidth / 2;
		coordinate[1] = centerY + yRange - childHeight / 2;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int count = getChildCount();
		final float radius = mRadius;

		measureChildren(widthMeasureSpec, heightMeasureSpec);

		int measureWidth = 0, measureHeight = 0;
		int maxChildWidth = 0, maxChildHeight = 0;
		for (int index = 0; index < count; index++) {
			View child = getChildAt(index);
			maxChildWidth = Math.max(maxChildWidth, child.getMeasuredWidth());
			maxChildHeight = Math
					.max(maxChildHeight, child.getMeasuredHeight());
		}

		measureWidth += Math.ceil(maxChildWidth / 2) + Math.abs(radius) * 2;
		measureHeight += Math.ceil(maxChildHeight / 2) + Math.abs(radius) * 2;

		measureWidth += getPaddingLeft() + getPaddingRight();
		measureHeight += getPaddingTop() + getPaddingBottom();

		setMeasuredDimension(resolveSize(measureWidth, widthMeasureSpec),
				resolveSize(measureHeight, heightMeasureSpec));
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final float x = ev.getX();
		final float y = ev.getY();
		final int touchSlop = mTouchSlop;

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mPressedX = x;
			mPressedY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mState == STATE_IDLE
					&& (Math.abs(x - mPressedX) > touchSlop || Math.abs(y
							- mPressedY) > touchSlop)) {
				/**
				 * So we began to intercept the MotionEvent from child, and
				 * cancel the pending long press from all children of this view
				 */
				final int childCount = getChildCount();
				for (int index = 0; index < childCount; index++) {
					View child = getChildAt(index);
					// child.cancelLongPress();
				}
				return true;
			}

			break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final float x = event.getX();
		final float y = event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mLastMotionY = y;
			/**
			 * I want to receive MotionEvent
			 */
			return true;
		case MotionEvent.ACTION_MOVE:
			final int touchSlop = mTouchSlop;
			if (mState == STATE_IDLE
					&& (Math.abs(x - mPressedX) > touchSlop || Math.abs(y
							- mPressedY) > touchSlop)) {
				mState = STATE_SCROLL;
			}

			if (mState == STATE_SCROLL) {
				final int centerX = mCenterX;
				final int centerY = mCenterY;
				float lastDegree = getDegeeByCoordinate(mLastMotionX,
						mLastMotionY, centerX, centerY);
				float degree = getDegeeByCoordinate(x, y, centerX, centerY);
				mRotateDegee += (degree - lastDegree);
			}
			mLastMotionX = x;
			mLastMotionY = y;
			break;
		}

		requestLayout();

		return super.onTouchEvent(event);
	}

	private float getDegeeByCoordinate(float x, float y, float centerX,
			float centerY) {
		float distanceX = centerX - x;
		float distanceY = centerY - y;
		float bevelEdgeLength = (float) Math.hypot(distanceX, distanceY);
		float cosAngle = distanceX / bevelEdgeLength;
		float degree = (float) Math.acos(cosAngle);
		if (centerY < y) {
			degree = -degree;
		}
		degree = (float) (degree * (180f / Math.PI));
		if (degree < 0) {
			degree = 360 + degree;
		}
		return degree;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mCenterX = w / 2;
		mCenterY = h / 2;
	}
}
