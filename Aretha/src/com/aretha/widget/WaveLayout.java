package com.aretha.widget;

import com.aretha.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

public class WaveLayout extends ViewGroup {
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;

	private int mGravity;
	private int mOrientation;

	private int mCurrentWaveCrestPosition;
	private int mCurrentWaveAmplitude;
	private int mMaxWaveAmplitude;

	private Scroller mScroller;

	public WaveLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.WaveLayout);
		mMaxWaveAmplitude = a.getInteger(R.styleable.WaveLayout_maxAmplitude,
				(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
						80, context.getResources().getDisplayMetrics()));
		mOrientation = a.getInt(R.styleable.WaveLayout_orientation, HORIZONTAL);
		mGravity = a.getInt(R.styleable.WaveLayout_gravity, Gravity.BOTTOM);
		a.recycle();
		initialize(context);
	}

	public WaveLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WaveLayout(Context context) {
		this(context, null);
	}

	private void initialize(Context context) {
		mScroller = new Scroller(context);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int childCount = getChildCount();
		final int currentWaveCrestPosition = mCurrentWaveCrestPosition;
		final boolean isHorizontal = mOrientation == HORIZONTAL;
		final int currentWaveRadius = mCurrentWaveAmplitude;
		final int gravity = mGravity;

		int height = b - t;
		int width = r - l;
		int offset = 0;
		for (int index = 0; index < childCount; index++) {
			View child = getChildAt(index);
			int childWidth = child.getMeasuredWidth();
			int childHeight = child.getMeasuredHeight();
			int position = computeChildPosition(childWidth, childHeight,
					offset, currentWaveCrestPosition, currentWaveRadius,
					isHorizontal);

			if (isHorizontal) {
				switch (gravity) {
				case Gravity.TOP:
					child.layout(offset, position, offset + childWidth,
							childHeight + position);
					break;
				case Gravity.BOTTOM:
					child.layout(offset, height - childHeight - position,
							offset + childWidth, height - position);
					break;
				}

			} else {
				switch (gravity) {
				case Gravity.LEFT:
					child.layout(position, offset, position + childWidth,
							offset + childHeight);
					break;
				case Gravity.RIGHT:
					child.layout(width - position - childWidth, offset, width
							- position, offset + childHeight);
					break;
				}

			}

			offset += isHorizontal ? childWidth : childHeight;
		}
	}

	protected int computeChildPosition(int width, int height, int offset,
			int currentWaveCrestPosition, int waveRadius, boolean isHorizontal) {
		int var = (isHorizontal ? offset + width / 2 : offset + height / 2)
				- currentWaveCrestPosition;
		return (int) (Math.max(0, -Math.pow(var, 2) / waveRadius / 2
				+ waveRadius));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		final int childCount = getChildCount();

		int maxChildWidth = 0, maxChildHeight = 0;
		int totalChildrenWidth = 0, totalChildHeight = 0;

		for (int index = 0; index < childCount; index++) {
			View child = getChildAt(index);
			int measuredChildWidth = child.getMeasuredWidth();
			int measuredChildHeight = child.getMeasuredHeight();

			totalChildrenWidth += measuredChildWidth;
			totalChildHeight += measuredChildHeight;

			maxChildWidth = Math.max(maxChildWidth, measuredChildWidth);
			maxChildHeight = Math.max(maxChildHeight, measuredChildHeight);
		}

		int measuredWidth = 0;
		int measuredHeight = 0;

		switch (mOrientation) {
		case HORIZONTAL:
			measuredWidth = totalChildrenWidth;
			measuredHeight = maxChildHeight + mCurrentWaveAmplitude;
			break;
		case VERTICAL:
			measuredWidth = maxChildWidth + mCurrentWaveAmplitude;
			measuredHeight = totalChildHeight;
			break;
		}

		measuredWidth += getPaddingLeft() + getPaddingRight();
		measuredHeight += getPaddingTop() + getPaddingBottom();

		setMeasuredDimension(resolveSize(measuredWidth, widthMeasureSpec),
				resolveSize(measuredHeight, heightMeasureSpec));
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			mCurrentWaveAmplitude = mScroller.getCurrX();
			invalidate();
			requestLayout();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = super.onTouchEvent(event);
		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			mScroller.startScroll(mCurrentWaveAmplitude, 0, mMaxWaveAmplitude
					- mCurrentWaveAmplitude, 0);
		case MotionEvent.ACTION_MOVE:
			mCurrentWaveCrestPosition = (int) (mOrientation == HORIZONTAL ? event
					.getX() : event.getY());
			result = true;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			mScroller.startScroll(mCurrentWaveAmplitude, 0,
					0 - mCurrentWaveAmplitude, 0);
			break;
		}
		invalidate();
		requestLayout();
		return result;
	}
}
