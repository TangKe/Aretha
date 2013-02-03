package com.arethademos.widget;

import com.aretha.widget.PageIndicator;
import com.aretha.widget.PageIndicator.OnPageChangeListener;
import com.arethademos.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

public class PageIndicatorDemo extends Activity implements OnPageChangeListener {
	private PageIndicator mPageIndicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.page_indicator);

		mPageIndicator = (PageIndicator) findViewById(R.id.pageIndicator);
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
			mPageIndicator.setDotColor(getResources().getColor(
					R.color.aretha_red));
			break;
		case R.id.white:
			mPageIndicator.setDotColor(getResources().getColor(
					R.color.aretha_white));
			break;
		case R.id.dot_radius:
			mPageIndicator.setDotRadius(mPageIndicator.getDotRadius() + 2,
					TypedValue.COMPLEX_UNIT_PX);
			break;
		case R.id.dot_spacing:
			mPageIndicator.setDotSpacing(mPageIndicator.getDotSpacing() + 2,
					TypedValue.COMPLEX_UNIT_PX);
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
