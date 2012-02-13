package com.arethademo.widget;

import com.aretha.widget.PageIndicator;
import com.arethademo.R;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class PageIndicatorDemo extends Activity {
	private PageIndicator mPageIndicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.page_indicator);

		mPageIndicator = (PageIndicator) findViewById(R.id.page_indicator);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.page_plus:
			mPageIndicator.setPageNumber(mPageIndicator.getPageNumber() + 1);
			break;
		case R.id.page_minus:
			mPageIndicator.setPageNumber(mPageIndicator.getPageNumber() - 1);
			break;
		case R.id.red:
			mPageIndicator.setDotColor(Color.RED);
			break;
		case R.id.white:
			mPageIndicator.setDotColor(Color.WHITE);
			break;
		case R.id.dot_radius:
			mPageIndicator.setDotRadius(mPageIndicator.getDotRadius() + 2);
			break;
		case R.id.dot_spacing:
			mPageIndicator.setDotSpacing(mPageIndicator.getDotSpacing() + 2);
			break;
		default:
			break;
		}
	}
}
