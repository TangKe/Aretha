package com.arethademos.widget;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

import com.aretha.widget.ClickWheelView;
import com.arethademos.R;

public class ClickWheelViewDemo extends Activity {
	private ClickWheelView mClickWheelView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.click_wheel_view);
		mClickWheelView = (ClickWheelView) findViewById(R.id.clickWheelView);
	}

	public void onClick(View v) {
		final int id = v.getId();
		if (id == R.id.fling) {
			Boolean tag = (Boolean) v.getTag();
			Boolean isFlingEnabled = null == tag ? false : !tag;
			mClickWheelView.setFlingEnabled(isFlingEnabled);
			Button flingButton = (Button) v;
			flingButton.setTag(isFlingEnabled);
			flingButton
					.setText(isFlingEnabled ? R.string.click_wheel_view_disable_fling
							: R.string.click_wheel_view_enable_fling);
		} else if (id == R.id.radius_plus) {
			mClickWheelView.setRadius(mClickWheelView.getRadius() + 10,
					TypedValue.COMPLEX_UNIT_PX);
		} else if (id == R.id.radius_minus) {
			mClickWheelView.setRadius(mClickWheelView.getRadius() - 10,
					TypedValue.COMPLEX_UNIT_PX);
		}
	}
}
