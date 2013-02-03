package com.aretha.widget;

import java.util.HashMap;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.aretha.R;

public class FontTextView extends TextView {

	public FontTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.FontTextView, defStyle, 0);
		String fontPath = a.getString(R.styleable.FontTextView_fontPath);
		if (null != fontPath && 0 != fontPath.length()) {
			setAssetFont(fontPath);
		}
		a.recycle();
	}

	public FontTextView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.textViewStyle);
	}

	public FontTextView(Context context) {
		this(context, null);
	}

	public void setAssetFont(String path) {
		Typeface typeface = TypefaceCache.getInstance(getContext())
				.loadTypeface(path);
		if (null != typeface) {
			setTypeface(typeface);
		}
	}

	static class TypefaceCache {
		private static TypefaceCache mTypefaceCache;
		private HashMap<String, Typeface> mTypefaces;
		private Context mContext;

		public TypefaceCache(Context context) {
			mTypefaces = new HashMap<String, Typeface>();
			mContext = context;
		}

		public static TypefaceCache getInstance(Context context) {
			if (null == mTypefaceCache) {
				mTypefaceCache = new TypefaceCache(context);
			}
			return mTypefaceCache;
		}

		public Typeface loadTypeface(String path) {
			if (null == path) {
				return null;
			}
			Typeface typeface = mTypefaces.get(path);
			if (null == typeface) {
				typeface = Typeface.createFromAsset(mContext.getAssets(), path);
			}

			return typeface;
		}
	}

}
