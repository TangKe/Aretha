package com.arethademos.widget;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import com.aretha.widget.ToggleView;
import com.arethademos.R;

public class ToggleViewDemo extends Activity {
	private ToggleView mToggleView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.toggle_view);
		mToggleView = (ToggleView) findViewById(R.id.toggleView);
	}

	public void onClick(View v) {
		final int id = v.getId();
		if (id == R.id.toggle) {
			mToggleView.toggle();
		} else if (id == R.id.on) {
			mToggleView.setToggle(true, true);
		} else if (id == R.id.off) {
			mToggleView.setToggle(false, true);
		} else if (id == R.id.radius_minus) {
			mToggleView.setRadius(mToggleView.getRadius() - 3,
					TypedValue.COMPLEX_UNIT_PX);
		} else if (id == R.id.radius_plus) {
			mToggleView.setRadius(mToggleView.getRadius() + 3,
					TypedValue.COMPLEX_UNIT_PX);
		}
	}
}
