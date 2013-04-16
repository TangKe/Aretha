/* Copyright (c) 2011-2012 Tang Ke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
