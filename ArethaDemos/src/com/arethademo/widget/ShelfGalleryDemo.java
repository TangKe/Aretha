package com.arethademo.widget;

import com.aretha.widget.ReflectionImageView;
import com.aretha.widget.ShelfGallery;
import com.arethademo.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ShelfGalleryDemo extends Activity {
	private ShelfGallery mShelfGallery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shelf_gallery);

		mShelfGallery = (ShelfGallery) findViewById(R.id.shelf_gallery);
		mShelfGallery.setSpacing(-100);
		mShelfGallery.setAdapter(new GalleryAdapter());
	}

	class GalleryAdapter extends BaseAdapter {
		private int[] res = new int[] { R.drawable.things, R.drawable.incoming,
				R.drawable.qq, R.drawable.simulator, R.drawable.sparrow,
				R.drawable.versions, R.drawable.xcode };

		@Override
		public int getCount() {
			return 200;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ReflectionImageView image;
			if (convertView != null) {
				image = (ReflectionImageView) convertView;
			} else {
				image = new ReflectionImageView(parent.getContext());
			}

			image.setImageResource(res[position % res.length]);
			return image;
		}

	}
}
