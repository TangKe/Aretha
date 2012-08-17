package com.arethademo.widget;

import com.aretha.widget.WaveLayout;
import com.aretha.widget.WaveLayout.OnWaveLayoutChangeListener;
import com.arethademo.R;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

public class WaveLayoutDemo extends Activity implements
		OnWaveLayoutChangeListener, AnimationListener, OnClickListener {
	private final static int DIALOG_ORIENTATION = 0x00010000;
	private final static int DIALOG_GRAVITY = 0x00020000;
	private WaveLayout mWaveLayout;
	private TextView mHintText;

	private Animation mFadeIn;
	private Animation mFadeOut;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wave_layout);

		mWaveLayout = (WaveLayout) findViewById(R.id.waveLayout);
		mWaveLayout.setOnWaveLayoutChangeListener(this);
		mHintText = (TextView) findViewById(R.id.hint);

		mFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		mFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
	}

	@Override
	public void onIndexChange(View child, int index) {
		TextView text = (TextView) child;
		mHintText.setText(text.getText());
	}

	@Override
	public void onWaveBegin() {
		mHintText.setVisibility(View.VISIBLE);
		mHintText.startAnimation(mFadeIn);
	}

	@Override
	public void onWaveEnd() {
		mHintText.setVisibility(View.INVISIBLE);
		mHintText.startAnimation(mFadeOut);
	}

	@Override
	public void onAnimationEnd(Animation arg0) {

	}

	@Override
	public void onAnimationRepeat(Animation arg0) {

	}

	@Override
	public void onAnimationStart(Animation arg0) {

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Builder builder = new Builder(this);
		switch (id) {
		case R.id.change_wave_layout_position_dialog:
			builder.setTitle(R.string.wave_layout_position_dialog_title)
					.setSingleChoiceItems(R.array.wave_layout_positions, 2,
							this);
			break;
		}
		return builder.create();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.position:
			showDialog(R.id.change_wave_layout_position_dialog);
			break;
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		LayoutParams layoutParams = (LayoutParams) mWaveLayout
				.getLayoutParams();
		int gravity = 0;
		int orientation = 0;
		switch (which) {
		case 0:
			gravity = Gravity.LEFT;
			layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
			orientation = WaveLayout.VERTICAL;
			break;
		case 1:
			gravity = Gravity.TOP;
			layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
			orientation = WaveLayout.HORIZONTAL;
			break;
		case 2:
			gravity = Gravity.RIGHT;
			layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
			orientation = WaveLayout.VERTICAL;
			break;
		case 3:
			gravity = Gravity.BOTTOM;
			layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
			orientation = WaveLayout.HORIZONTAL;
			break;
		}
		mWaveLayout.setGravity(gravity);
		mWaveLayout.setOrientation(orientation);
		dialog.dismiss();

	}
}
