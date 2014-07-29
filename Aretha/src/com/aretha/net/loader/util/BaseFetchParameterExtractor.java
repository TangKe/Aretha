package com.aretha.net.loader.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class BaseFetchParameterExtractor implements FetchParameterExtractor {

	@Override
	public List<NameValuePair> extract(Fetch fetch) {
		try {
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			createFetchParameters(fetch.getClass(), fetch, parameters);
			return parameters;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void createFetchParameters(Class clazz, Object object,
			List<NameValuePair> parameters) throws IllegalArgumentException,
			IllegalAccessException {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			Class fieldClass = field.getType();
			Object value = field.get(object);
			if (Modifier.isFinal(field.getModifiers()) || null == value) {
				continue;
			}

			field.setAccessible(true);
			FetchParameter annotation = field
					.getAnnotation(FetchParameter.class);

			NameValuePair nameValuePair = new BasicNameValuePair(
					null == annotation ? field.getName()
							: annotation.aliasName(),
					fieldClass.isArray() ? arrayToString(value, "", "", ",")
							: String.valueOf(value));
			parameters.add(nameValuePair);
		}

		Class superClass = clazz.getSuperclass();
		if (null != superClass) {
			createFetchParameters(superClass, object, parameters);
		}
	}

	public static String arrayToString(Object array, String prefix,
			String postfix, String separator) {
		StringBuilder builder = new StringBuilder();
		if (null == array) {
			return builder.toString();
		}

		int length = Array.getLength(array);
		builder.append(prefix);
		for (int index = 0; index < length; index++) {
			builder.append(Array.get(array, index));
			builder.append(separator);
		}
		if (0 != length) {
			builder.delete(builder.length() - separator.length(),
					builder.length());
		}
		builder.append(postfix);
		return builder.toString();
	}

}