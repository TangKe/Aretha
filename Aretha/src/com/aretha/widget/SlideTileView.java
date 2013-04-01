package com.aretha.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.aretha.R;

public class SlideTileView extends ViewGroup {
	private int mGravity;

	public SlideTileView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.SlideTileView, defStyle, 0);

		a.recycle();
	}

	public SlideTileView(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.slideTileViewStyle);
	}

	public SlideTileView(Context context) {
		this(context, null);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int gravity = mGravity;
		final int width = getWidth();
		final int height = getHeight();

		final int count = getChildCount();
		for (int index = 0; index < count; index++) {
			final View child = getChildAt(index);
			int left, right, top, bottom = 0;
			switch (gravity) {
			case Gravity.LEFT:
				break;
			case Gravity.TOP:

				break;
			case Gravity.RIGHT:

				break;
			case Gravity.BOTTOM:

				break;
			case Gravity.LEFT | Gravity.TOP:
				break;
			case Gravity.RIGHT | Gravity.BOTTOM:
				break;
			case Gravity.LEFT | Gravity.BOTTOM:
				break;
			case Gravity.RIGHT | Gravity.TOP:
				break;
			}
			child.layout(l, count, gravity, b);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(resolveSize(0, widthMeasureSpec),
				resolveSize(0, heightMeasureSpec));
	}
}
