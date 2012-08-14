package com.arethademo.widget;

import com.aretha.widget.WaveLayout;
import com.aretha.widget.WaveLayout.OnWaveLayoutChangeListener;
import com.arethademo.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class WaveLayoutDemo extends Activity implements
		OnWaveLayoutChangeListener {
	private WaveLayout mWaveLayout;
	private TextView mHintText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wave_layout);

		mWaveLayout = (WaveLayout) findViewById(R.id.waveLayout);
		mWaveLayout.setOnWaveLayoutChangeListener(this);
		mHintText = (TextView) findViewById(R.id.hint);
	}

	@Override
	public void onIndexChange(View child, int index) {
		TextView text = (TextView) child;
		mHintText.setText(text.getText());
	}
}
