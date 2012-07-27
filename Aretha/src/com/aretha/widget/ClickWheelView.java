package com.aretha.widget;

import com.aretha.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Scroller;

/**
 * A simple view position the child view on edge of circle, user can rotate it
 * by finger, if you want determine the children view by {@link Adapter} like
 * the {@link ListView}, you should extends the {@link AdapterView}, write your
 * own code.
 * 
 * @author tangke
 * 
 */
public class ClickWheelView extends ViewGroup {
	private final static int STATE_IDLE = 0x0001;
	private final static int STATE_SCROLLING = 0x0002;
	private final static int STATE_FLING = 0x0003;

	private int mRotateDegee;
	private int mDegreePerChild;

	private int mCenterX;
	private int mCenterY;
	private float mRadius;
	private boolean mIsFlingEnabled;

	private int mTouchSlop;
	private int mMinFlingVelocity;
	private float mPressedX;
	private float mPressedY;

	private float mLastMotionX;
	private float mLastMotionY;
	private int mState = STATE_IDLE;

	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;

	private Paint mPaint;

	public ClickWheelView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ClickWheelView);

		mCenterX = a.getInt(R.styleable.ClickWheelView_centerX, -1);
		mCenterY = a.getInt(R.styleable.ClickWheelView_centerY, -1);
		mRadius = a.getFloat(R.styleable.ClickWheelView_radius, 300);
		mIsFlingEnabled = a.getBoolean(R.styleable.ClickWheelView_flingEnabled,
				true);

		a.recycle();
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
		mMinFlingVelocity = ViewConfiguration.getMinimumFlingVelocity();
		mScroller = new Scroller(context);
		// setStaticTransformationsEnabled(true);

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(0xffffffff);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final float childCount = getChildCount();
		final int centerX = mCenterX;
		final int centerY = mCenterY;
		final float radianPerChild = (float) (mDegreePerChild * Math.PI / 180);
		final int[] coordinate = new int[2];
		final float radius = mRadius;
		// rotate 90 degree CCW to restore the first child to the top
		final float rotateRadian = (float) ((mRotateDegee - 90) * Math.PI / 180);

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
			mPressedX = mLastMotionX = x;
			mPressedY = mLastMotionY = y;
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
					child.cancelLongPress();
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
		final int centerX = mCenterX;
		final int centerY = mCenterY;

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mLastMotionY = y;
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			/**
			 * I want to receive MotionEvent
			 */
			return true;
		case MotionEvent.ACTION_MOVE:
			final int touchSlop = mTouchSlop;
			if (mState == STATE_IDLE
					&& (Math.abs(x - mPressedX) > touchSlop || Math.abs(y
							- mPressedY) > touchSlop)) {
				requestDisallowInterceptTouchEvent(true);
				mState = STATE_SCROLLING;
			}

			if (mState == STATE_SCROLLING) {
				float lastDegree = getDegeeByCoordinate(mLastMotionX,
						mLastMotionY, centerX, centerY);
				float degree = getDegeeByCoordinate(x, y, centerX, centerY);
				mRotateDegee += (degree - lastDegree);
			}
			mLastMotionX = x;
			mLastMotionY = y;
			break;
		case MotionEvent.ACTION_UP:
			final VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(200);
			float velocityX = velocityTracker.getXVelocity();
			float velocityY = velocityTracker.getYVelocity();
			int velocity = (int) Math.hypot(velocityX, velocityY);

			if (Math.abs(velocity) > mMinFlingVelocity && mIsFlingEnabled) {
				mState = STATE_FLING;
				mScroller.fling(mRotateDegee, mRotateDegee, (int) velocityX,
						(int) velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE,
						Integer.MIN_VALUE, Integer.MAX_VALUE);
			}

			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			requestDisallowInterceptTouchEvent(false);
			break;
		}
		invalidate();
		requestLayout();

		return super.onTouchEvent(event);
	}

	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
		canvas.drawLine(mCenterX, mCenterY, child.getLeft() + child.getWidth()
				/ 2, child.getTop() + child.getHeight() / 2, mPaint);
		return super.drawChild(canvas, child, drawingTime);
	}

	@Override
	public void computeScroll() {
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			mRotateDegee = mScroller.getCurrX();
			invalidate();
			requestLayout();
		} else {
			mState = STATE_IDLE;
		}
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
		mCenterX = mCenterX == -1 ? w / 2 : mCenterX;
		mCenterY = mCenterY == -1 ? h : mCenterY;
		mDegreePerChild = 360 / getChildCount();
	}

	public int getCenterX() {
		return mCenterX;
	}

	public int getCenterY() {
		return mCenterY;
	}

	public void setCenter(int centerX, int centerY) {
		mCenterY = centerX;
		mCenterY = centerY;
		requestLayout();
	}

	public boolean isFlingEnabled() {
		return mIsFlingEnabled;
	}

	public void setFlingEnabled(boolean isFlingEnabled) {
		this.mIsFlingEnabled = isFlingEnabled;
	}

	/**
	 * Return the radius of this view group
	 * 
	 * @return the size (in pixels).
	 */
	public float getRadius() {
		return mRadius;
	}

	/**
	 * Set radius (in scaled pixel)
	 * 
	 * @param radius
	 */
	public void setRadius(float radius) {
		setRadius(radius, TypedValue.COMPLEX_UNIT_DIP);
	}

	/**
	 * Set radius
	 * 
	 * @param radius
	 * @param unit
	 *            the unit to use, see {@link TypedValue}
	 */
	public void setRadius(float radius, int unit) {
		final Resources resources = getContext().getResources();
		this.mRadius = Math.max(
				0,
				TypedValue.applyDimension(unit, radius,
						resources.getDisplayMetrics()));
		requestLayout();
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		SavedState savedState = new SavedState(super.onSaveInstanceState());
		savedState.radius = mRadius;
		savedState.isFlingEnabled = mIsFlingEnabled;
		savedState.centerX = mCenterX;
		savedState.centerY = mCenterY;
		return savedState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());

		mRadius = savedState.radius;
		mIsFlingEnabled = savedState.isFlingEnabled;
		mCenterX = savedState.centerX;
		mCenterY = savedState.centerY;
	}

	static class SavedState extends BaseSavedState {
		public float radius;
		public boolean isFlingEnabled;
		public int centerX;
		public int centerY;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			radius = in.readInt();
			isFlingEnabled = in.readInt() == 0 ? false : true;
			centerX = in.readInt();
			centerY = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeFloat(radius);
			out.writeInt(isFlingEnabled ? 1 : 0);
			out.writeInt(centerX);
			out.writeInt(centerY);
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}
