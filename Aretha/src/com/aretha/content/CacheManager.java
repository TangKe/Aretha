/* Copyright (c) 2011-2012 Tank Tang
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
package com.aretha.content;

import java.lang.ref.SoftReference;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.util.Log;

/**
 * A simple cache utility for global manage the RAM data and use
 * {@link SoftReference} to avoid {@link OutOfMemoryError}
 * 
 * @author Tank
 */
public class CacheManager {
	private static CacheManager mAppDataManager;

	public static CacheManager getInstance() {
		if (mAppDataManager == null) {
			mAppDataManager = new CacheManager();
		}
		return mAppDataManager;
	}

	private final static String LOG_TAG = "CacheManager";
	private final static long RELEASE_INTERVAL = 10000L;
	private ConcurrentHashMap<String, SoftReference<Object>> mDataMap;
	private Thread mReleaseThread;
	private Runnable mReleaseRunnable;

	private CacheManager() {
		mDataMap = new ConcurrentHashMap<String, SoftReference<Object>>();
	}

	/**
	 * Add data and return the bind tag.
	 * 
	 * @param data
	 * @return Generated tag
	 */
	public String addData(Object data) {
		String containTag = getTag(data);
		if (null != containTag) {
			return containTag;
		}

		String uuidTag = UUID.randomUUID().toString();
		addData(data, uuidTag);
		return uuidTag;
	}

	/**
	 * Add data with customize tag
	 * 
	 * @param data
	 * @param tag
	 */
	public void addData(Object data, String tag) {
		if (null == data || null == tag || 0 == tag.length()) {
			return;
		}

		mDataMap.put(tag, new SoftReference<Object>(data));
	}

	/**
	 * Clear all the cached data
	 */
	public void clear() {
		mDataMap.clear();
	}

	/**
	 * Get specified data by tag
	 * 
	 * @param tag
	 * @return
	 */
	public Object getData(String tag) {
		SoftReference<Object> dataReference = mDataMap.get(tag);

		// Can not found
		if (null == dataReference) {
			return null;
		}

		Object data = dataReference.get();

		// if the data has been recycled by GC. Remove it
		if (null == data) {
			mDataMap.remove(tag);
			return null;
		}
		return data;
	}

	/**
	 * Get the tag which specified to the data.
	 * 
	 * @param data
	 * @return
	 */
	public String getTag(Object data) {
		if (data == null) {
			return null;
		}

		for (Entry<String, SoftReference<Object>> entry : mDataMap.entrySet()) {
			Object reference = (entry.getValue()).get();
			if (reference != null && reference == data) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * 
	 * @param tag
	 * @return
	 */
	public boolean hasData(String tag) {
		return mDataMap.containsKey(tag) & mDataMap.get(tag) == null;
	}

	/**
	 * Remove specified data
	 * 
	 * @param tag
	 */
	public void removeData(String tag) {
		mDataMap.remove(tag);
	}

	public void enableBrokenReferenceAutoRelease() {
		if(mReleaseRunnable == null){
			mReleaseRunnable = new BrokenReferenceReleaseRunnable(
					RELEASE_INTERVAL, mDataMap);
		}
		
		if (mReleaseThread == null || !mReleaseThread.isAlive()) {
			mReleaseThread = new Thread(mReleaseRunnable);
			mReleaseThread.start();
		}
	}

	public void disableBrokenReferenceAutoRelease() {
		if (mReleaseThread == null) {
			return;
		}
		mReleaseThread.stop();
	}

	class BrokenReferenceReleaseRunnable implements Runnable {
		private long mReleaseInterval;
		private ConcurrentHashMap<String, SoftReference<Object>> mMap;

		public BrokenReferenceReleaseRunnable(long releaseInterval,
				ConcurrentHashMap<String, SoftReference<Object>> map) {
			this.mMap = map;
			this.mReleaseInterval = releaseInterval;
		}

		@Override
		public void run() {
			try {
				while (true) {
					Log.i(LOG_TAG, "Releasing");
					Set<Entry<String, SoftReference<Object>>> entrySet = mMap
							.entrySet();
					for (Entry<String, SoftReference<Object>> entry : entrySet) {
						// released by GC, remove from dataMap
						if (entry.getValue().get() == null) {
							Log.i(LOG_TAG,
									String.format(
											"Release broken reference object with key %s",
											entry.getKey()));
							mMap.remove(entry.getKey());
						}
					}
					Thread.sleep(mReleaseInterval);
				}
			} catch (InterruptedException e) {

			}
		}
	}
}
