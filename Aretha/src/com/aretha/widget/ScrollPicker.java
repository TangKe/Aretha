/* Copyright (c) 2011-2013 Tang Ke
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

public class ScrollPicker extends AdapterView<BaseAdapter> {

	public ScrollPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ScrollPicker(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScrollPicker(Context context) {
		this(context, null);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		final int count = getChildCount();
		int totalHeight = 0, totalWidth = 0;
		int maxHeight = 0, maxWidth = 0;
		for (int index = 0; index < count; index++) {
			View child = getChildAt(index);
			int measuredWidth = child.getMeasuredWidth();
			int measuredHeight = child.getMeasuredHeight();
			maxWidth = Math.max(maxWidth, measuredWidth);
			maxHeight = Math.max(maxHeight, measuredHeight);
			totalWidth += measuredWidth;
			totalHeight += measuredHeight;
		}

		maxWidth += getPaddingLeft() + getPaddingRight();
		maxHeight += getPaddingTop() + getPaddingBottom();
		setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec),
				resolveSize(totalHeight, heightMeasureSpec));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int count = getChildCount();

		final int top = getPaddingTop();
		final int left = getPaddingLeft();
		for (int index = 0; index < count; index++) {
			View view = getChildAt(index);
			view.layout(l, left, r, b);
		}
	}

	@Override
	public BaseAdapter getAdapter() {
		return null;
	}

	@Override
	public View getSelectedView() {
		return null;
	}

	@Override
	public void setAdapter(BaseAdapter adapter) {
		
	}

	@Override
	public void setSelection(int position) {
		
	}
}
