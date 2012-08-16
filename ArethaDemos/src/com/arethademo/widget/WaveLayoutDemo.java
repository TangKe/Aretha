package com.arethademo.widget;

import com.aretha.widget.WaveLayout;
import com.aretha.widget.WaveLayout.OnWaveLayoutChangeListener;
import com.arethademo.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class WaveLayoutDemo extends Activity implements
		OnWaveLayoutChangeListener, AnimationListener {
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
}
