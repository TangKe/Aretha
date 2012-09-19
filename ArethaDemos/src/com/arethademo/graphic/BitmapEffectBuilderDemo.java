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
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

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

		prepareImage(this);
	}

	void prepareImage(Context context) {
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.logo_large);

		int color = getResources().getColor(R.color.aretha_green);
		BitmapEffectBuilder bitmapEffectBuilder = BitmapEffectBuilder
				.getInstance();
		generateAndAddImage(bitmap, R.string.bitmap_effect_builder_original,
				context);
		generateAndAddImage(bitmapEffectBuilder.buildHighlight(bitmap,
						Color.argb(50, 255, 255, 255),
						Color.argb(150, 255, 255, 255), 0.5f, false),
				R.string.bitmap_effect_builder_highlight, context);
		generateAndAddImage(bitmapEffectBuilder.buildDropShadow(bitmap, 3,
				color, 0, 5, true),
				R.string.bitmap_effect_builder_shadow, context);
		generateAndAddImage(
				bitmapEffectBuilder.buildOuterGlow(bitmap, 10, color, true),
				R.string.bitmap_effect_builder_glow, context);
		generateAndAddImage(
				bitmapEffectBuilder.buildReflection(bitmap, 0.5f, true),
				R.string.bitmap_effect_builder_reflection,
				context);
	}

	void generateAndAddImage(Bitmap bitmap, int textResId, Context context) {
		LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		ImageView imageView = new ImageView(context);
		imageView.setScaleType(ScaleType.CENTER);
		imageView.setImageBitmap(bitmap);
		linearLayout.addView(imageView, new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1));

		TextView textView = new TextView(context);
		textView.setText(textResId);
		textView.setGravity(Gravity.CENTER_HORIZONTAL);
		linearLayout.addView(textView, new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

		mWorkspace.addView(linearLayout, new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT));
	}

	@Override
	public void onPageChange(int pageIndex) {
		mPageIndicator.setActivePage(pageIndex);
	}

	@Override
	public void onPageCountChange() {
		mPageIndicator.setPageNumber(mWorkspace.getChildCount());
	}

}
