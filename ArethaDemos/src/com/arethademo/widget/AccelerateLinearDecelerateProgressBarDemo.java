package com.arethademo.widget;

import com.aretha.widget.AccelerateLinearDecelerateProgressBar;
import com.arethademo.R;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

public class AccelerateLinearDecelerateProgressBarDemo extends Activity {
	private AccelerateLinearDecelerateProgressBar mAccelerateLinearDecelerateProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accelerate_linear_decelerate_progress_bar);
		mAccelerateLinearDecelerateProgressBar = (AccelerateLinearDecelerateProgressBar) findViewById(R.id.accelerate_linear_decelerate_progress_bar);
	}

	public void onClick(View v) {
		int id = v.getId();
		AccelerateLinearDecelerateProgressBar accelerateLinearDecelerateProgressBar = mAccelerateLinearDecelerateProgressBar;
		switch (id) {
		case R.id.radius_plus:
			accelerateLinearDecelerateProgressBar.setDotRadius(
					accelerateLinearDecelerateProgressBar.getDotRadius() + 1,
					TypedValue.COMPLEX_UNIT_PX);
			break;
		case R.id.radius_minus:
			accelerateLinearDecelerateProgressBar.setDotRadius(
					accelerateLinearDecelerateProgressBar.getDotRadius() - 1,
					TypedValue.COMPLEX_UNIT_PX);
			break;
		case R.id.space_plus:
			accelerateLinearDecelerateProgressBar.setDotSpacing(
					accelerateLinearDecelerateProgressBar.getDotSpacing() + 1,
					TypedValue.COMPLEX_UNIT_PX);
			break;
		case R.id.space_minus:
			accelerateLinearDecelerateProgressBar.setDotSpacing(
					accelerateLinearDecelerateProgressBar.getDotSpacing() - 1,
					TypedValue.COMPLEX_UNIT_PX);
			break;
		case R.id.duration_plus:
			accelerateLinearDecelerateProgressBar
					.setDuration(accelerateLinearDecelerateProgressBar
							.getDuration() + 1000);
			break;
		case R.id.duration_minus:
			accelerateLinearDecelerateProgressBar
					.setDuration(accelerateLinearDecelerateProgressBar
							.getDuration() - 1000);
			break;
		case R.id.dot_plus:
			accelerateLinearDecelerateProgressBar
					.setDotCount(accelerateLinearDecelerateProgressBar
							.getDotCount() + 1);
			break;
		case R.id.dot_minus:
			accelerateLinearDecelerateProgressBar
					.setDotCount(accelerateLinearDecelerateProgressBar
							.getDotCount() - 1);
			break;
		case R.id.yellow:
			accelerateLinearDecelerateProgressBar.setDotColor(Color.YELLOW);
			break;

		default:
			break;
		}
	}
}
