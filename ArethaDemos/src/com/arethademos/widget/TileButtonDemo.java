package com.arethademos.widget;

import com.arethademos.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

public class TileButtonDemo extends Activity implements OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
		setContentView(R.layout.tile_button);
	}

	@Override
	public void onClick(View v) {
		Toast.makeText(this, R.string.tile_button_click, Toast.LENGTH_LONG)
				.show();
	}
}
