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
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;

public class ShelfGallery extends Gallery {
	private Camera mCamera;
	private float mRotateDegree = 0f;

	public ShelfGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	public ShelfGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public ShelfGallery(Context context) {
		super(context);
		initialize();
	}

	private void initialize() {
		mCamera = new Camera();
		setStaticTransformationsEnabled(true);
		setChildrenDrawingOrderEnabled(true);
		setFadingEdgeLength(0);
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		return childCount - 1 - i;
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		applyChildTransformation(child, t);
		return true;
	}

	protected void applyChildTransformation(View child, Transformation t) {
		final int childWidth = child.getWidth();
		final int childLeft = child.getLeft();
		final float rotateDegree = mRotateDegree;
		final Camera camera = mCamera;

		t.clear();
		t.setTransformationType(Transformation.TYPE_BOTH);

		float degree = 0.0f;
//		float alpha = 1.0f;
		if (childLeft <= 0) {
			degree = (-90 - rotateDegree) / childWidth * Math.abs(childLeft)
					+ rotateDegree;
//			alpha = 1.0f - 1.0f / childWidth * Math.abs(childLeft);
		} else {
			degree = mRotateDegree;
		}
//		t.setAlpha(alpha);
		Matrix matrix = t.getMatrix();
		camera.save();
		camera.rotateY(degree);
		if (childLeft <= 0) {
			camera.translate(0, 0, childLeft * 1.5f);
		} else {
			camera.translate(0, 0, Math.abs(childLeft) * 1.5f);
		}

		camera.getMatrix(matrix);
		if (childLeft <= 0) {
			matrix.postTranslate(Math.abs(childLeft), 0);
		}
		camera.restore();
	}

	public float getRotateDegree() {
		return mRotateDegree;
	}

	public void setRotateDegree(float rotateDegree) {
		this.mRotateDegree = rotateDegree;
	}
}
