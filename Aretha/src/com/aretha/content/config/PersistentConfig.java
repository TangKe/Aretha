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
package com.aretha.content.config;

import java.lang.reflect.Field;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * A simple wrapper for the {@link SharedPreferences}, all the subclass's field
 * with {@link PersistentConfigEntry} can be saved into the xml file, but
 * remember only {@link String}, {@link Integer}, {@link Long}, {@link Boolean},
 * {@link Float} can be accept. <br>
 * usage: <br>
 * <code>
 * class YourClass extends PersistentConfig{
 * 
 * 		public YourClass(Context context) {
 * 			super(context);
 * 		}
 * 
 * 		{@literal @}PersistentConfigEntry
 * 		private String yourConfigField;
 * 
 * } 
 * </code> <br>
 * then, just set the value to your field and call {@link #save()}. Done!
 * 
 * @author tangke
 * 
 */
public abstract class PersistentConfig {
	private static String TAG;

	private Context mContext;

	public PersistentConfig(Context context) {
		mContext = context;
		TAG = getClass().getSimpleName();
		read();
	}

	private void read() {
		Class<?> clazz = getClass();
		Field[] fields = clazz.getFields();
		SharedPreferences sharedPreferences = mContext.getSharedPreferences(
				TAG, Context.MODE_PRIVATE);
		Map<String, ?> preferences = sharedPreferences.getAll();

		for (Field field : fields) {
			field.setAccessible(true);
			PersistentConfigEntry annotation = field
					.getAnnotation(PersistentConfigEntry.class);
			String key = field.getName();

			// ignore the field without PersistentConfigEntry
			if (null == annotation) {
				continue;
			}

			// use the PersistentConfigEntry key first
			String tempKey = annotation.key();
			key = tempKey.length() == 0 ? key : tempKey;

			Object value = preferences.get(key);
			String valueString = String.valueOf(value);

			// not saved in preference and has default value in
			// PersistentConfigEntry
			if (null != value) {
				Class<?> type = field.getType();
				if (type == String.class) {
					value = valueString;
				} else if (type == Integer.class || type == int.class) {
					value = Integer.parseInt(valueString);
				} else if (type == Float.class || type == float.class) {
					value = Float.parseFloat(valueString);
				} else if (type == Boolean.class || type == boolean.class) {
					value = Boolean.parseBoolean(valueString);
				} else if (type == Long.class || type == long.class) {
					value = Long.parseLong(valueString);
				} else if (isPersistentable(type)) {
					try {
						value = type.newInstance();
						((Persistentable) value).depersistent(valueString);
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}

				}
			}

			try {
				field.set(this, value);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public void save() {
		Class<?> clazz = getClass();
		Field[] fields = clazz.getFields();

		SharedPreferences sharedPreferences = mContext.getSharedPreferences(
				TAG, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		for (Field field : fields) {
			PersistentConfigEntry annotation = field
					.getAnnotation(PersistentConfigEntry.class);
			if (null == annotation) {
				continue;
			}

			Class<?> type = field.getType();
			String key = annotation.key();
			String name = key.length() == 0 ? field.getName() : key;
			// Only accept these types
			field.setAccessible(true);
			try {
				Object value = field.get(this);
				if (null == value) {
					editor.remove(name);
					continue;
				}

				if (type == String.class) {
					editor.putString(name, (String) value);
				} else if (type == Integer.class || type == int.class) {
					editor.putInt(name, (Integer) value);
				} else if (type == Float.class || type == float.class) {
					editor.putFloat(name, (Float) value);
				} else if (type == Boolean.class || type == boolean.class) {
					editor.putBoolean(name, (Boolean) value);
				} else if (type == Long.class || type == long.class) {
					editor.putLong(name, (Long) value);
				} else if (isPersistentable(type)) {
					String persistent = ((Persistentable) value).persistent();
					editor.putString(name, persistent);
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		editor.commit();
	}

	private Boolean isPersistentable(Class<?> type) {
		Class<?>[] interfaces = type.getInterfaces();

		for (Class<?> interfaceClass : interfaces) {
			if (interfaceClass == Persistentable.class) {
				return true;
			}
		}
		return false;
	}
}
