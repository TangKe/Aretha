package com.aretha.adapter;

/**
 * All ViewHolder should extend from this class
 * 
 * @author Tank
 * 
 */
public abstract class ViewHolder<Data> {
	/**
	 * will be null when data is not binded
	 */
	public Data item;

	/**
	 * invoked when a recycled ViewHolder is ready to use
	 */
	public void onResetViews() {

	}
}