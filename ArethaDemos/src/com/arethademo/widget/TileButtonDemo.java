package com.arethademo.widget;

import com.arethademo.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class TileButtonDemo extends Activity implements OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tile_button);
	}

	@Override
	public void onClick(View v) {
		Toast.makeText(this, R.string.tile_button_click, Toast.LENGTH_LONG)
				.show();
	}
}
