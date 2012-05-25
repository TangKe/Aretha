/* Copyright (c) 2011 Tank Tang
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;

import com.aretha.net.HttpConnectionHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Load remote image to local cache, then read from cache and decode bitmap
 * avoid {@link OutOfMemoryError}
 * 
 * @author Tank
 */
public class AsyncImageLoader {
	private final static int MAX_ACCEPTABLE_SAMPLE_SIZE = 5;
	private final static int MAX_LOADING_THREAD = 2;

	private final static int STATUS_SUCCESS = 1;
	private final static int STATUS_ERROR = 2;
	private final static String LOG_TAG = "AsyncImageLoader";

	private static AsyncImageLoader mImageLoader;

	private FileCacheManager mFileCacheManager;

	private ThreadPoolExecutor mExecutor;
	private ArrayBlockingQueue<Runnable> mRunnableQueue;
	private LinkedList<ImageLoadingTask> mTaskList;

	private Handler mImageLoadedHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			ImageLoadingTask task = (ImageLoadingTask) msg.obj;
			switch (msg.what) {
			case STATUS_SUCCESS:
				// if this ImageLoadingTask has been canceled before it done. we can not invoke the callback.
				if (mTaskList.remove(task)) {
					task.listener.onLoaded(task.bitmap, task.uri.toString());
				}
				break;
			case STATUS_ERROR:

				break;
			}
		}
	};

	public static AsyncImageLoader getInstance(Context context) {
		if (mImageLoader == null) {
			mImageLoader = new AsyncImageLoader(context);
		}
		return mImageLoader;
	}

	private AsyncImageLoader(Context context) {
		mFileCacheManager = new FileCacheManager(context);
		mRunnableQueue = new ArrayBlockingQueue<Runnable>(100);
		mExecutor = new ThreadPoolExecutor(1, MAX_LOADING_THREAD, 0,
				TimeUnit.MILLISECONDS, mRunnableQueue);
		mTaskList = new LinkedList<ImageLoadingTask>();
	}

	/**
	 * 
	 * @param uri
	 * @param listener
	 */
	public void loadImage(URI uri, OnImageLoadedListener listener) {
		doLoadImage(obtainImageLoadingTask(uri, listener));
	}

	/**
	 * 
	 * @param url
	 * @param listener
	 */
	public void loadImage(String url, OnImageLoadedListener listener) {
		loadImage(URI.create(url), listener);
	}

	/**
	 * 
	 * @param uri
	 */
	public void cancel(URI uri) {
		ImageLoadingTask task = obtainImageLoadingTask(uri, null);
		mRunnableQueue.remove(task);
	}

	/**
	 * 
	 * @param url
	 */
	public void cancel(String url) {
		if (null == url) {
			return;
		}
		cancel(URI.create(url));
	}

	/**
	 * Generate the load task
	 * 
	 * @param uri
	 * @param listener
	 * @return
	 */
	private ImageLoadingTask obtainImageLoadingTask(URI uri,
			OnImageLoadedListener listener) {
		ImageLoadingTask task = new ImageLoadingTask();
		task.listener = listener;
		task.uri = uri;
		return task;
	}

	/**
	 * Execute the load action
	 */
	private void doLoadImage(ImageLoadingTask imageLoadingTask) {
		if (null == imageLoadingTask) {
			return;
		}

		if (null == imageLoadingTask.uri || null == imageLoadingTask.listener) {
			return;
		}

		mTaskList.add(imageLoadingTask);
		mExecutor.execute(imageLoadingTask);
	}

	/**
	 * Save the image {@link InputStream}
	 * 
	 * @param context
	 * @param imageIdentifier
	 * @param inputstream
	 * @return
	 */
	public boolean saveBitmapStream(String imageIdentifier,
			InputStream inputStream) {
		return mFileCacheManager.writeCacheFile(imageIdentifier, inputStream);
	}

	/**
	 * According imageIdentifier to get cached {@link Bitmap} If the
	 * {@link OutOfMemoryError} occur. method will reduce the quality of
	 * {@link Bitmap}
	 * 
	 * @see CacheManager
	 * 
	 * @param imageIdentifier
	 * @param sampleSize
	 *            If set to a value > 1, requests the decoder to sub sample the
	 *            original image, returning a smaller image to save memory.
	 * @return The cached bitmap, or null not found.
	 */
	public Bitmap readCachedBitmap(String imageIdentifier, int sampleSize) {
		InputStream inputStream = mFileCacheManager
				.readCacheFile(imageIdentifier);

		if (inputStream == null) {
			return null;
		}

		Options decodeOptions = new Options();
		decodeOptions.inSampleSize = sampleSize;

		try {
			Log.i(LOG_TAG, "Decode will began!");
			return BitmapFactory.decodeStream(inputStream, null, decodeOptions);
		} catch (OutOfMemoryError e) {
			Log.w(LOG_TAG,
					"Bitmap decode out of memory! decrease image size! current sample size: "
							+ sampleSize);
			if (sampleSize > MAX_ACCEPTABLE_SAMPLE_SIZE) {
				Log.e(LOG_TAG, "Bitmap decode out of memory!");
				return null;
			}
			return readCachedBitmap(imageIdentifier, ++sampleSize);
		}
	}

	/**
	 * The listener to listen the image load action
	 * 
	 * @author Tank
	 * 
	 */
	public interface OnImageLoadedListener {
		public void onLoaded(Bitmap bitmap, String loadedImageUrl);
	}

	private class ImageLoadingTask implements Runnable {
		public URI uri;
		public OnImageLoadedListener listener;
		public Bitmap bitmap;

		@Override
		public boolean equals(Object o) {
			if (o instanceof ImageLoadingTask) {
				return uri.equals(((ImageLoadingTask) o).uri);
			}

			return super.equals(o);
		}

		@Override
		public void run() {
			bitmap = readCachedBitmap(uri.toString(), 1);
			Handler handler = mImageLoadedHandler;
			if (null != bitmap && null != listener) {
				Log.d(LOG_TAG, "Image cache found!");
				handler.sendMessage(handler.obtainMessage(STATUS_SUCCESS, this));
				return;
			}

			HttpConnectionHelper connection = HttpConnectionHelper
					.getInstance();
			HttpResponse response = connection.execute(connection
					.obtainHttpGetRequest(uri, null));
			try {
				if (null != response) {
					Log.d(LOG_TAG,
							"Image cache not found, get it in async method!");
					saveBitmapStream(uri.toString(), response.getEntity()
							.getContent());
					bitmap = readCachedBitmap(uri.toString(), 1);
					if (null == bitmap) {
						Log.d(LOG_TAG, String.format(
								"Delete the broken image cache! url: %s",
								uri.toString()));
						mFileCacheManager.deleteCache(uri.toString());
						handler.sendEmptyMessage(STATUS_ERROR);
						return;
					}
					handler.sendMessage(handler.obtainMessage(STATUS_SUCCESS,
							this));
				} else {
					handler.sendEmptyMessage(STATUS_ERROR);
				}
			} catch (IOException e) {
				handler.sendEmptyMessage(STATUS_ERROR);
			}
		}
	}
}
