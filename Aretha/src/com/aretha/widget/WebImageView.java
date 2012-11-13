package com.aretha.widget;

import com.aretha.content.AsyncImageLoader;
import com.aretha.content.AsyncImageLoader.OnImageLoadListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

public class WebImageView extends ImageView implements OnImageLoadListener {
	private String mImageUrl;

	public WebImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public WebImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WebImageView(Context context) {
		super(context);
	}

	public void setImageUrl(String url) {
		mImageUrl = url;
		AsyncImageLoader.getInstance(getContext()).loadImage(url, this);
	}

	public void setImageUrl(String url, Bitmap defaultImage) {
		setImageBitmap(defaultImage);
		setImageUrl(url);
	}

	@Override
	public void onLoadSuccess(Bitmap bitmap, String loadedImageUrl,
			boolean fromCache) {
		if (mImageUrl.equals(loadedImageUrl)) {
			setImageBitmap(bitmap);
		}
	}

	@Override
	public void onLoadError(String imageUrl) {

	}

	@Override
	public boolean onPreLoad(String imageUrl) {
		return false;
	}

	@Override
	public void onLoading(String imageUrl, long loadedLength, long totalLength) {
		
	}
}
