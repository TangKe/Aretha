package com.arethademos.widget;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import com.aretha.widget.AccelerateLinearDecelerateProgressBar;
import com.arethademos.R;

public class AccelerateLinearDecelerateProgressBarDemo extends Activity {
	private AccelerateLinearDecelerateProgressBar mAccelerateLinearDecelerateProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accelerate_linear_decelerate_progress_bar);
		mAccelerateLinearDecelerateProgressBar = (AccelerateLinearDecelerateProgressBar) findViewById(R.id.accelerateLinearDecelerateProgressBar);
	}

	public void onClick(View v) {
		int id = v.getId();
		AccelerateLinearDecelerateProgressBar accelerateLinearDecelerateProgressBar = mAccelerateLinearDecelerateProgressBar;
		if (id == R.id.radius_plus) {
			accelerateLinearDecelerateProgressBar.setDotRadius(
					accelerateLinearDecelerateProgressBar.getDotRadius() + 1,
					TypedValue.COMPLEX_UNIT_PX);
		} else if (id == R.id.radius_minus) {
			accelerateLinearDecelerateProgressBar.setDotRadius(
					accelerateLinearDecelerateProgressBar.getDotRadius() - 1,
					TypedValue.COMPLEX_UNIT_PX);
		} else if (id == R.id.space_plus) {
			accelerateLinearDecelerateProgressBar.setDotSpacing(
					accelerateLinearDecelerateProgressBar.getDotSpacing() + 1,
					TypedValue.COMPLEX_UNIT_PX);
		} else if (id == R.id.space_minus) {
			accelerateLinearDecelerateProgressBar.setDotSpacing(
					accelerateLinearDecelerateProgressBar.getDotSpacing() - 1,
					TypedValue.COMPLEX_UNIT_PX);
		} else if (id == R.id.duration_plus) {
			accelerateLinearDecelerateProgressBar
					.setDuration(accelerateLinearDecelerateProgressBar
							.getDuration() + 1000);
		} else if (id == R.id.duration_minus) {
			accelerateLinearDecelerateProgressBar
					.setDuration(accelerateLinearDecelerateProgressBar
							.getDuration() - 1000);
		} else if (id == R.id.dot_plus) {
			accelerateLinearDecelerateProgressBar
					.setDotCount(accelerateLinearDecelerateProgressBar
							.getDotCount() + 1);
		} else if (id == R.id.dot_minus) {
			accelerateLinearDecelerateProgressBar
					.setDotCount(accelerateLinearDecelerateProgressBar
							.getDotCount() - 1);
		} else if (id == R.id.yellow) {
			accelerateLinearDecelerateProgressBar.setDotColor(Color.YELLOW);
		}
	}
}
