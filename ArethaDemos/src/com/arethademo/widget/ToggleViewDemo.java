package com.arethademo.widget;

import com.aretha.widget.ToggleView;
import com.arethademo.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

public class ToggleViewDemo extends Activity {
	private ToggleView mToggleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.toggle_view);
		mToggleView = (ToggleView) findViewById(R.id.toggleView);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.toggle:
			mToggleView.toggle();
			break;
		case R.id.on:
			mToggleView.setToggle(true, true);
			break;
		case R.id.off:
			mToggleView.setToggle(false, true);
			break;
		case R.id.radius_minus:
			mToggleView.setRadius(mToggleView.getRadius() - 3,
					TypedValue.COMPLEX_UNIT_PX);
			break;
		case R.id.radius_plus:
			mToggleView.setRadius(mToggleView.getRadius() + 3,
					TypedValue.COMPLEX_UNIT_PX);
			break;
		}
	}
}
