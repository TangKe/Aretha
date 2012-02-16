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

		t.clear();
		t.setTransformationType(Transformation.TYPE_BOTH);

		float degree = 0;
		if (childLeft <= 0) {
			degree = (-90 - rotateDegree) / childWidth * Math.abs(childLeft)
					+ rotateDegree;
			t.setAlpha(1.0f - 1.0f / childWidth * Math.abs(childLeft));
		} else {
			degree = mRotateDegree;
		}

		Matrix matrix = t.getMatrix();
		final Camera camera = mCamera;
		camera.save();
		camera.rotateY(degree);
		if (childLeft <= 0) {
			camera.translate(0, 0, childLeft);
		}else{
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
