package com.aretha.net;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.aretha.net.HttpRequestMethod;

public abstract class Fetch {
	private List<NameValuePair> mParameters;

	public Fetch() {
		mParameters = new ArrayList<NameValuePair>();
	}

	public abstract String getUrl();

	public HttpRequestMethod getFetchMethod() {
		return HttpRequestMethod.GET;
	}

	public final List<NameValuePair> getFetchParameters() {
		try {
			mParameters.clear();
			onCreateFetchParameters(getClass(), mParameters);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return mParameters;
	}

	/**
	 * 使用反射获取当前类的所有属性, 并且自动添加成参数
	 * 
	 * @param parameters
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	protected void onCreateFetchParameters(Class clazz,
			List<NameValuePair> parameters) throws IllegalArgumentException,
			IllegalAccessException {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			Class fieldClass = field.getType();
			Object value = field.get(this);
			if ((!fieldClass.isPrimitive() && fieldClass != String.class && !fieldClass
					.isArray())
					|| Modifier.isFinal(field.getModifiers())
					|| null == value) {
				continue;
			}

			field.setAccessible(true);
			FetchParameter annotation = field
					.getAnnotation(FetchParameter.class);
			NameValuePair nameValuePair = new BasicNameValuePair(
					null == annotation ? field.getName()
							: annotation.aliasName(),
					fieldClass.isArray() ? arrayToString(value, "", "")
							: String.valueOf(value));
			parameters.add(nameValuePair);
		}

		Class superClass = clazz.getSuperclass();
		if (null != superClass) {
			onCreateFetchParameters(superClass, parameters);
		}
	}

	public static String arrayToString(Object array, String prefix,
			String postfix) {
		StringBuilder builder = new StringBuilder();
		if (null == array) {
			return builder.toString();
		}

		int length = Array.getLength(array);
		builder.append(prefix);
		for (int index = 0; index < length; index++) {
			builder.append(Array.get(array, index));
			builder.append(",");
		}
		if (0 != length) {
			builder.delete(builder.length() - 1, builder.length());
		}
		builder.append(postfix);
		return builder.toString();
	}

	public boolean onPreFetch(HttpRequest request) {
		return false;
	}

	public void onPostFetch(HttpRequest request) {

	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface FetchParameter {
		String aliasName() default "";
	}
}
