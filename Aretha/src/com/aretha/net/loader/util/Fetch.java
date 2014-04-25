package com.aretha.net.loader.util;

import java.lang.reflect.Array;

import org.apache.http.HttpRequest;

import com.aretha.net.HttpRequestMethod;

public abstract class Fetch {
	public abstract String getUrl();

	public HttpRequestMethod getFetchMethod() {
		return HttpRequestMethod.GET;
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
}
