/* Copyright (c) 2011-2012 Tang Ke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aretha.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class OverScrollWrapper extends FrameLayout {
	private final static int DURATION = 750;

	private final static int DIRECTION_LEFT = 1 << 1;
	private final static int DIRECTION_RIGHT = 1 << 2;
	private final static int DIRECTION_TOP = 1 << 3;
	private final static int DIRECTION_BOTTOM = 1 << 4;

	private final static int FLAG_DRAGGING = 1 << 1;

	// private List<FixedViewInfo> mScrollInfo;
	private int mOverScrollDirection;

	private int mOverScrollState;
	private int mPrvateFlags;

	private Scroller mScroller;

	public OverScrollWrapper(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mScroller = new Scroller(context);
		// mScrollInfo = new ArrayList<OverScrollWrapper.FixedViewInfo>();
	}

	public OverScrollWrapper(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public OverScrollWrapper(Context context) {
		this(context, null);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if ((mPrvateFlags & FLAG_DRAGGING) == FLAG_DRAGGING) {
			return true;
		}

		final float x = ev.getX();
		final float y = ev.getY();

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:

			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			break;
		}

		return super.onInterceptTouchEvent(ev);
	}

	// @Override
	// protected void onLayout(boolean changed, int l, int t, int r, int b) {
	// final int count = getChildCount();
	// if (0 != count) {
	// View child = getChildAt(0);
	// child.layout(l, t, r, b);
	// }
	// }

	// @Override
	// protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// measureChildren(widthMeasureSpec, heightMeasureSpec);
	//
	// final List<FixedViewInfo> scrollInfo = mScrollInfo;
	// final int count = scrollInfo.size();
	// for (int index = 0; index < count; index++) {
	// FixedViewInfo fixedViewInfo = scrollInfo.get(index);
	// measureChild(fixedViewInfo.view, widthMeasureSpec,
	// heightMeasureSpec);
	// }
	// }

	// private class FixedViewInfo {
	// public View view;
	// public int direction;
	// }

}
