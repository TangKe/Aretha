package com.arethademo.widget;

import com.aretha.widget.PageIndicator;
import com.aretha.widget.PageIndicator.OnPageChangeListener;
import com.arethademo.R;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class PageIndicatorDemo extends Activity implements OnPageChangeListener {
	private PageIndicator mPageIndicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.page_indicator);

		mPageIndicator = (PageIndicator) findViewById(R.id.page_indicator);
		mPageIndicator.setOnPageChangeListener(this);
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

	@Override
	public void onPageChange(int pageIndex) {
		Toast.makeText(this, getString(R.string.page_change, pageIndex),
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onNextPage() {
		Toast.makeText(this, getString(R.string.next_page), Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public void onPrevPage() {
		Toast.makeText(this, getString(R.string.prev_page), Toast.LENGTH_SHORT)
				.show();
	}
}
