package com.aretha.adapter;

import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Provide a general use of adapter�� improve the performance of scroll
 * 
 * @author Tank
 * 
 * @param <Data>
 *            data type
 * @param <Holder>
 *            sub class of {@link ViewHolder}
 */
public abstract class AbstractBaseAdapter<Data, Holder extends ViewHolder<Data>>
		extends BaseAdapter {
	protected Context mContext;
	private LayoutInflater mInflater;
	private Collection<Data> mData;

	public Collection<Data> getData() {
		return mData;
	}

	@Override
	public int getCount() {
		if (null == mData) {
			mData = onLoadData();
		}
		return mData.size();
	}

	@Override
	public Data getItem(int position) {
		if (position >= mData.size() || 0 > position) {
			return null;
		}

		for (Data data : mData) {
			if (0 == position--) {
				return data;
			}
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		if (null == mInflater) {
			mContext = parent.getContext();
			mInflater = LayoutInflater.from(mContext);
		}

		Holder holder;

		int type = getItemViewType(position);
		Data item = getItem(position);
		if (null == convertView) {
			convertView = onCreateView(mInflater, type, parent);
			holder = onBindViewHolder(convertView, type);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.item = item;
		holder.onResetViews();
		holder.onConfigViews(position, item, type);

		return convertView;
	}

	@Override
	public void notifyDataSetInvalidated() {
		mData = onLoadData();
		super.notifyDataSetInvalidated();
	}

	/**
	 * BaseListAdapter will call this method to get adapter data when needed
	 * 
	 * @return
	 */
	public abstract List<Data> onLoadData();

	/**
	 * just return the ViewHolder according to the type of view
	 * 
	 * @param view
	 *            the inflated view returned by
	 *            {@link #onCreateView(LayoutInflater, int, ViewGroup)} method
	 * @param type
	 *            type of current position return by
	 *            {@link #getItemViewType(int)}
	 * @return
	 */
	public abstract Holder onBindViewHolder(View view, int type);

	/**
	 * create a new view if AdapterView's recycler is empty
	 * 
	 * @param inflater
	 * @param type
	 * @param parent
	 * @return
	 */
	public abstract View onCreateView(LayoutInflater inflater, int type,
			ViewGroup parent);
}
