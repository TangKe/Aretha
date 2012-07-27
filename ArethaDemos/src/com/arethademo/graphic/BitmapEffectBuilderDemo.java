package com.arethademo.graphic;

import com.aretha.widget.PageIndicator;
import com.aretha.widget.Workspace;
import com.aretha.widget.Workspace.OnWorkspaceChangeListener;
import com.aretha.widget.graphic.BitmapEffectBuilder;
import com.arethademo.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class BitmapEffectBuilderDemo extends Activity implements
		OnWorkspaceChangeListener {
	private Workspace mWorkspace;
	private PageIndicator mPageIndicator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bitmap_effect_builder);
		mWorkspace = (Workspace) findViewById(R.id.workspace);
		mWorkspace.setOnWorkspaceChangeListener(this);
		mPageIndicator = (PageIndicator) findViewById(R.id.pageIndicator);
		mPageIndicator.setPageNumber(mWorkspace.getChildCount());
		prepareImage(this);
	}

	void prepareImage(Context context) {
		final Workspace workspace = mWorkspace;
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.logo);

		ImageView image1 = (ImageView) workspace.getChildAt(0);
		ImageView image2 = (ImageView) workspace.getChildAt(1);
		ImageView image3 = (ImageView) workspace.getChildAt(2);
		ImageView image4 = (ImageView) workspace.getChildAt(3);

		BitmapEffectBuilder bitmapEffectBuilder = BitmapEffectBuilder
				.getInstance();
		image1.setImageBitmap(bitmapEffectBuilder.buildOuterGlow(bitmap, 3,
				0xff33b5e5, false));
		image2.setImageBitmap(bitmapEffectBuilder.buildOuterGlow(bitmap, 3,
				0xff33b5e5, true));
		image3.setImageBitmap(bitmapEffectBuilder.buildReflection(bitmap, 0.5f,
				false));
		image4.setImageBitmap(bitmapEffectBuilder.buildReflection(bitmap, 0.5f,
				true));
	}

	@Override
	public void onPageChange(int pageIndex) {
		mPageIndicator.setActivePage(pageIndex);		
	}

	@Override
	public void onPageCountChange() {
		
	}

}
