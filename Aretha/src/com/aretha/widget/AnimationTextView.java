package com.aretha.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class AnimationTextView extends TextView {

	public AnimationTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public AnimationTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AnimationTextView(Context context) {
		super(context);
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		invalidateDrawablesState();
	}
	
	@Override
	protected void onFocusChanged(boolean focused, int direction,
			Rect previouslyFocusedRect) {
		super.onFocusChanged(focused, direction, previouslyFocusedRect);
		invalidateDrawablesState();
	}

	/**
	 * Decide which {@link Drawable} should be animated
	 */
	private void invalidateDrawablesState() {
		// Compound drawables
		Drawable[] drawables = getCompoundDrawables();
		for (Drawable drawable : drawables) {
			invalidateAnimationDrawableState(drawable);
		}

		// Background
		invalidateAnimationDrawableState(getBackground());
	}

	/**
	 * Animate the drawable passed
	 * @param drawable
	 */
	private void invalidateAnimationDrawableState(Drawable drawable) {
		if (null == drawable || !(drawable instanceof AnimationDrawable)) {
			return;
		}

		AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
		if (getVisibility() == View.VISIBLE) {
			animationDrawable.start();
		} else {
			animationDrawable.stop();
		}
	}
}
