/* Copyright (c) 2011 Tang Ke
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
import java.util.HashMap;
import java.util.UUID;
import java.util.Map.Entry;

/**
 * A simple cache util for global manage the RAM data and use
 * {@link SoftReference} to avoid {@link OutOfMemoryError}.
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

	private HashMap<String, SoftReference<Object>> mDataMap;

	private CacheManager() {
		mDataMap = new HashMap<String, SoftReference<Object>>();
	}

	/**
	 * Add data and return the bind tag.
	 * 
	 * @param data
	 * @return Generated tag
	 */
	public String addData(Object data) {
		String uuidTag = UUID.randomUUID().toString();
		addData(data, uuidTag);
		return uuidTag;
	}

	/**
	 * Add data with customize tag
	 * 
	 * @param data
	 * @param customizedTag
	 */
	public void addData(Object data, String customizedTag) {
		if (null == data || null == customizedTag
				|| 0 == customizedTag.length()) {
			return;
		}

		mDataMap.put(customizedTag, new SoftReference<Object>(data));
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
	 * @param CustomizedTag
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
			if (reference != null && reference.equals(data)) {
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
}