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
package com.aretha.content.config;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * A simple wrapper for the {@link SharedPreferences}, all the subclass's field
 * with {@link ConfigEntry} can be saved into the xml file, but remember only
 * {@link String}, {@link Integer}, {@link Long}, {@link Boolean}, {@link Float}
 * can be accept. <br>
 * usage: <br>
 * <code>
 * class YourClass extends Config{
 * 
 * 		public YourClass(Context context) {
 * 			super(context);
 * 		}
 * 
 * 		{@literal @}ConfigEntry private String yourConfigField;
 * 
 * } 
 * </code> <br>
 * then, just set the value to your field and call {@link #save()}. Done!
 * 
 * @author tangke
 * 
 */
public abstract class Config {
	private static String TAG;

	private Context mContext;

	public Config(Context context) {
		mContext = context;
		TAG = getClass().getSimpleName();
		read();
	}

	private void read() {
		Class<?> clazz = getClass();
		SharedPreferences sharedPreferences = mContext.getSharedPreferences(
				TAG, Context.MODE_PRIVATE);
		Map<String, ?> preferences = sharedPreferences.getAll();
		Set<?> entries = preferences.entrySet();

		for (Object obj : entries) {
			Entry<String, ?> entry = (Entry<String, ?>) obj;
			try {
				Field field = clazz.getField(entry.getKey());
				field.setAccessible(true);
				field.set(this, entry.getValue());
			} catch (SecurityException e) {
			} catch (NoSuchFieldException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}
	}

	public void save() {
		Class<?> clazz = getClass();
		Field[] fields = clazz.getDeclaredFields();

		SharedPreferences sharedPreferences = mContext.getSharedPreferences(
				TAG, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		for (Field field : fields) {
			ConfigEntry annotation = field.getAnnotation(ConfigEntry.class);
			if (null == annotation) {
				continue;
			}

			Class<?> type = field.getType();
			String tag = annotation.tag();
			String name = tag.length() == 0 ? field.getName() : tag;
			// Only accept these types
			field.setAccessible(true);
			try {
				if (type == String.class) {
					String value;
					value = (String) field.get(this);
					editor.putString(name, null == value ? "" : value);
				} else if (type == Integer.class || type == int.class) {
					editor.putInt(name, field.getInt(this));
				} else if (type == Float.class || type == float.class) {
					editor.putFloat(name, field.getFloat(this));
				} else if (type == Boolean.class || type == boolean.class) {
					editor.putBoolean(name, field.getBoolean(this));
				} else if (type == Long.class || type == long.class) {
					editor.putLong(name, field.getLong(this));
				}
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}
		editor.commit();
	}
}
