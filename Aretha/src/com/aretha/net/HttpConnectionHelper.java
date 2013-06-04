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
package com.aretha.net;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.text.format.DateUtils;

/**
 * A helper class for http connection
 * 
 * @author Tank
 */
public class HttpConnectionHelper implements HttpRequestInterceptor,
		HttpResponseInterceptor {
	private final static int SECOND_IN_MILLIS = (int) DateUtils.SECOND_IN_MILLIS;
	private final static int DEFAULT_CONNECTION_TIMEOUT = 20;

	private final static int MAX_CONNECTION_NUMBER = 100;

	private static HttpConnectionHelper mHttpConnectionHelper;

	private DefaultHttpClient mHttpClient;
	private CookieStore mCookieStore;
	private HttpParams mParams;

	private OnExecuteListenner mOnExecuteListenner;

	private HttpConnectionHelper() {
		HttpParams params = mParams = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, MAX_CONNECTION_NUMBER);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory
				.getSocketFactory(), 443));

		/**
		 * android SDK not support MultiThreadedHttpConnectionManager
		 * temporarily, so use the {@link ThreadSafeClientConnManager} instead
		 */
		ThreadSafeClientConnManager threadSafeClientConnManager = new ThreadSafeClientConnManager(
				params, schemeRegistry);

		HttpConnectionParams.setConnectionTimeout(params,
				DEFAULT_CONNECTION_TIMEOUT * SECOND_IN_MILLIS);
		HttpConnectionParams.setSoTimeout(params, DEFAULT_CONNECTION_TIMEOUT
				* SECOND_IN_MILLIS);

		HttpConnectionParams.setSocketBufferSize(params, 8192);

		mHttpClient = new DefaultHttpClient(threadSafeClientConnManager, params);

		mHttpClient.addRequestInterceptor(this);
		mHttpClient.addResponseInterceptor(this);

		mCookieStore = mHttpClient.getCookieStore();
	}

	/**
	 * Get the instance of {@link HttpConnectionHelper}
	 * 
	 * @return
	 */
	public static HttpConnectionHelper getInstance() {
		mHttpConnectionHelper = mHttpConnectionHelper == null ? new HttpConnectionHelper()
				: mHttpConnectionHelper;
		return mHttpConnectionHelper;
	}

	/**
	 * Clear all the cookies
	 */
	public void clearCookie() {
		mCookieStore.clear();
	}

	/**
	 * Set request timeout
	 * 
	 * @param timeout
	 */
	public void setConnectionTimeout(int timeout) {
		HttpConnectionParams.setConnectionTimeout(mParams, timeout);
	}

	/**
	 * Obtain a {@link HttpUriRequest} request
	 * 
	 * @param type
	 *            Type of request, see {@link #TYPE_GET}, {@link #TYPE_POST};
	 * @param url
	 *            Remote url
	 * @param params
	 *            Request parameters
	 * @return
	 */
	public HttpUriRequest obtainHttpRequest(HttpRequestMethod type, String url,
			List<NameValuePair> params) {
		HttpUriRequest request = null;
		switch (type) {
		default:
		case GET:
			request = obtainHttpGetRequest(url, params);
			break;
		case POST:
			request = obtainHttpPostRequest(url, params);
			break;
		}
		return request;
	}

	/**
	 * Obtain a {@link HttpPost} request
	 * 
	 * @param url
	 *            Remote url
	 * @param params
	 *            Request parameters
	 * @return
	 */
	public HttpPost obtainHttpPostRequest(String url, List<NameValuePair> params) {
		try {
			return obtainHttpPostRequest(new URI(url), params);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Obtain a {@link HttpPost} request
	 * 
	 * @param url
	 * @param file
	 *            File to upload
	 * @return
	 */
	public HttpPost obtainHttpPostRequest(String url, File file) {
		if (null == file || null == url) {
			throw new IllegalArgumentException("url and file can not be null");
		}

		HttpPost post = new HttpPost(url);
		FileEntity entity = new FileEntity(file, "binary/octet-stream");
		post.setEntity(entity);
		return post;
	}

	/**
	 * Obtain a {@link HttpPost} request
	 * 
	 * @param uri
	 *            uri
	 * @param params
	 *            Request parameters
	 * @return
	 */
	public HttpPost obtainHttpPostRequest(URI uri, List<NameValuePair> params) {
		HttpPost post = new HttpPost(uri);
		if (isParamsNull(params)) {
			try {
				post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return post;
	}

	/**
	 * Obtain a {@link HttpGet} request
	 * 
	 * @param url
	 *            Remote url
	 * @param params
	 *            Request parameters
	 * @return
	 */
	public HttpGet obtainHttpGetRequest(URI uri, List<NameValuePair> params) {
		if (null == uri) {
			return null;
		}
		return obtainHttpGetRequest(uri.toString(), params);
	}

	/**
	 * Obtain a {@link HttpGet} request
	 * 
	 * @param uri
	 *            Remote uri
	 * @param params
	 * @return
	 */
	public HttpGet obtainHttpGetRequest(String url, List<NameValuePair> params) {
		if (null == url) {
			return null;
		}
		StringBuilder urlBuilder = new StringBuilder(url);
		if (isParamsNull(params)) {
			urlBuilder.append(url.contains("?") ? "&" : "?");
			for (NameValuePair param : params) {
				urlBuilder.append(URLEncoder.encode(param.getName()));
				urlBuilder.append("=");
				urlBuilder.append(URLEncoder.encode(param.getValue()));
				urlBuilder.append("&");
			}
		}
		HttpGet get = new HttpGet(
				urlBuilder.toString().endsWith("&") ? urlBuilder.substring(0,
						urlBuilder.length() - 1) : urlBuilder.toString());
		return get;
	}

	/**
	 * Check the whether the parameters are null
	 * 
	 * @param params
	 * @return
	 */
	private boolean isParamsNull(List<NameValuePair> params) {
		return params != null && params.size() != 0;
	}

	/**
	 * Execute a request
	 * 
	 * @param request
	 * @return
	 */
	public HttpResponse execute(HttpUriRequest request) {
		HttpResponse response = null;
		try {
			response = mHttpClient.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	/**
	 * 
	 * @return
	 */
	public OnExecuteListenner getOnExecuteListenner() {
		return mOnExecuteListenner;
	}

	/**
	 * 
	 * @param onExecuteListenner
	 */
	public void setOnExecuteListenner(OnExecuteListenner onExecuteListenner) {
		this.mOnExecuteListenner = onExecuteListenner;
	}

	@Override
	public void process(HttpResponse response, HttpContext context)
			throws HttpException, IOException {
		if (null != mOnExecuteListenner) {
			mOnExecuteListenner.onResponse(response);
		}
	}

	@Override
	public void process(HttpRequest request, HttpContext context)
			throws HttpException, IOException {
		if (null != mOnExecuteListenner) {
			mOnExecuteListenner.onRequest(request);
		}
	}

	/**
	 * 
	 * @author Tank
	 * 
	 */
	public interface OnExecuteListenner {
		public void onRequest(HttpRequest request);

		public void onResponse(HttpResponse response);
	}
}
