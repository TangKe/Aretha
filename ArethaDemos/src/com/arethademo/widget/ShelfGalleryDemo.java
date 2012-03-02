package com.arethademo.widget;

import com.aretha.widget.ShelfGallery;
import com.arethademo.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;

public class ShelfGalleryDemo extends Activity implements OnItemClickListener {
	private ShelfGallery mShelfGallery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shelf_gallery);

		mShelfGallery = (ShelfGallery) findViewById(R.id.shelf_gallery);
		mShelfGallery.setAdapter(new GalleryAdapter());
		mShelfGallery.setSpacing(-150);
		mShelfGallery.setOnItemClickListener(this);
	}

	class GalleryAdapter extends BaseAdapter {
		private int[] res = new int[] { R.drawable.things, R.drawable.incoming,
				R.drawable.qq, R.drawable.simulator, R.drawable.sparrow,
				R.drawable.versions, R.drawable.xcode };
		private String[] tag = new String[] { "things", "incoming", "qq",
				"simulator", "sparrow", "versions", "xcode" };

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
			ImageView image;
			if (convertView != null) {
				image = (ImageView) convertView;
			} else {
				image = new ImageView(parent.getContext());
			}

			image.setImageResource(res[position % res.length]);
			image.setTag(tag[position % res.length]);
			return image;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Toast.makeText(getApplicationContext(), (String) arg1.getTag(),
				Toast.LENGTH_SHORT).show();
	}
}
