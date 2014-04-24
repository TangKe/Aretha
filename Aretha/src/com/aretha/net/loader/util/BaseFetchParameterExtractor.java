package com.aretha.net.loader.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.aretha.net.Fetch;

public class BaseFetchParameterExtractor implements FetchParameterExtractor {

	@Override
	public List<NameValuePair> extract(Fetch fetch) {
		try {
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			createFetchParameters(fetch.getClass(), parameters);
			return parameters;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected void createFetchParameters(Class clazz,
			List<NameValuePair> parameters) throws IllegalArgumentException,
			IllegalAccessException {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			Class fieldClass = field.getType();
			if ((!fieldClass.isPrimitive() && fieldClass != String.class)
					|| Modifier.isFinal(field.getModifiers())
					|| null == field.get(this)) {
				continue;
			}

			field.setAccessible(true);
			NameValuePair nameValuePair = new BasicNameValuePair(
					field.getName(), String.valueOf(field.get(this)));
			parameters.add(nameValuePair);
		}

		Class superClass = clazz.getSuperclass();
		if (null != superClass) {
			createFetchParameters(superClass, parameters);
		}
	}

}