package com.arethademo.widget;

import com.aretha.widget.ClickWheelView;
import com.arethademo.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

public class ClickWheelViewDemo extends Activity {
	private ClickWheelView mClickWheelView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.click_wheel_view);
		mClickWheelView = (ClickWheelView) findViewById(R.id.clickWheelView);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fling:
			Boolean tag = (Boolean) v.getTag();
			Boolean isFlingEnabled = null == tag ? false : !tag;
			mClickWheelView.setFlingEnabled(isFlingEnabled);
			Button flingButton = (Button) v;
			flingButton.setTag(isFlingEnabled);
			flingButton
					.setText(isFlingEnabled ? R.string.click_wheel_view_disable_fling
							: R.string.click_wheel_view_enable_fling);
			break;
		case R.id.radius_plus:
			mClickWheelView.setRadius(mClickWheelView.getRadius() + 10,
					TypedValue.COMPLEX_UNIT_PX);
			break;
		case R.id.radius_minus:
			mClickWheelView.setRadius(mClickWheelView.getRadius() - 10,
					TypedValue.COMPLEX_UNIT_PX);
			break;
		}
	}
}
