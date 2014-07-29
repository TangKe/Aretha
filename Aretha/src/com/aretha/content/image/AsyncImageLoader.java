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
package com.aretha.content.image;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;

import com.aretha.content.CacheManager;
import com.aretha.content.FileCacheManager;
import com.aretha.content.FileCacheManager.OnWriteListener;
import com.aretha.net.HttpConnectionHelper;

/**
 * Load remote image to local cache, then notify the UI thread, then read from
 * cache and decode bitmap avoid {@link OutOfMemoryError}
 * 
 * @author Tank
 */
public class AsyncImageLoader {
	private final static int STATUS_SUCCESS = 1 << 0;
	private final static int STATUS_ERROR = 1 << 1;
	private final static int STATUS_CANCEL = 1 << 2;

	private final static String LOG_TAG = "AsyncImageLoader";
	private Context mContext;

	private static AsyncImageLoader mImageLoader;

	private FileCacheManager mFileCacheManager;

	private ExecutorService mExecutor;
	private volatile LinkedList<ImageLoadingTask> mTaskList;

	private Handler mImageLoadedHandler;

	private ReentrantReadWriteLock mMainLock = new ReentrantReadWriteLock();

	private int mScreenWidth;
	private int mScreenHeight;

	public static AsyncImageLoader getInstance(Context context) {
		if (mImageLoader == null) {
			mImageLoader = new AsyncImageLoader(context);
		}
		return mImageLoader;
	}

	private AsyncImageLoader(Context context) {
		mContext = context.getApplicationContext();
		mFileCacheManager = new FileCacheManager(context);
		mExecutor = Executors.newCachedThreadPool();
		mTaskList = new LinkedList<ImageLoadingTask>();
		DisplayMetrics displayMetrics = context.getResources()
				.getDisplayMetrics();
		mScreenWidth = displayMetrics.widthPixels;
		mScreenHeight = displayMetrics.heightPixels;
		// will notify the main thread
		mImageLoadedHandler = new ImageLoadHandler(context.getMainLooper());
	}

	/**
	 * Add image load request
	 * 
	 * @param uri
	 * @param listener
	 */
	public void loadImage(Uri uri, OnImageLoadListener listener) {
		loadImage(uri, 0, 0, listener, true);
	}

	/**
	 * Add image load request
	 * 
	 * @param uri
	 * @param listener
	 * @param readCache
	 *            true read cache file if exist
	 */
	public void loadImage(Uri uri, int targetWidth, int targetHeight,
			OnImageLoadListener listener, boolean readCacheIfExist) {
		doLoadImage(obtainImageLoadingTask(uri, targetWidth, targetHeight,
				listener, readCacheIfExist));
	}

	/**
	 * @see #loadImage(URI, OnImageLoadListener)
	 * @param url
	 * @param listener
	 */
	public void loadImage(String url, OnImageLoadListener listener) {
		if (null == url || url.length() <= 0) {
			return;
		}
		loadImage(Uri.parse(url), listener);
	}

	/**
	 * Cancel a image load request before is was been execute. if you want to
	 * cancel it in progress, please see {@link OnImageLoadListener}
	 * 
	 * @param uri
	 */
	public void cancel(Uri uri) {
		Lock lock = mMainLock.writeLock();
		ImageLoadingTask task = obtainImageLoadingTask(uri, 0, 0, null, false);
		lock.lock();
		mTaskList.remove(task);
		lock.unlock();
	}

	/**
	 * @see #cancel(URI)
	 * @param url
	 */
	public void cancel(String url) {
		if (null == url) {
			return;
		}
		cancel(Uri.parse(url));
	}

	/**
	 * Generate the load task
	 * 
	 * @param uri
	 * @param listener
	 * @param readCacheIfExist
	 * @return
	 */
	private ImageLoadingTask obtainImageLoadingTask(Uri uri, int width,
			int height, OnImageLoadListener listener, boolean readCacheIfExist) {
		if (null == uri || null == listener) {
			return null;
		}
		ImageLoadingTask task = new ImageLoadingTask();
		task.listener = listener;
		task.uri = uri;
		task.targetWidth = width <= 0 ? mScreenWidth : width;
		task.targetHeight = height <= 0 ? mScreenHeight : height;
		task.readCacheIfExist = readCacheIfExist;
		return task;
	}

	/**
	 * Execute the load action
	 */
	private void doLoadImage(ImageLoadingTask imageLoadingTask) {
		if (null == imageLoadingTask) {
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
		return saveBitmapStream(imageIdentifier, inputStream, null);
	}

	/**
	 * Save the image {@link InputStream}
	 * 
	 * @param imageIdentifier
	 * @param inputStream
	 * @param onWriteListener
	 * @return
	 */
	public boolean saveBitmapStream(String imageIdentifier,
			InputStream inputStream, OnWriteListener onWriteListener) {
		return mFileCacheManager.writeCacheFile(imageIdentifier, inputStream,
				onWriteListener);
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
	public Bitmap readCachedBitmap(String imageIdentifier, int targetWidth,
			int targetHeight) {
		InputStream inputStream = mFileCacheManager
				.readCacheFile(imageIdentifier);

		if (inputStream == null) {
			return null;
		}

		Options decodeOptions = new Options();
		decodeOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(inputStream, null, decodeOptions);
		int factor = (int) Math.ceil(decodeOptions.outWidth * 1.0f
				/ targetWidth);
		factor = (int) Math.max(factor, decodeOptions.outHeight * 1.0f
				/ targetHeight);
		Log.d(LOG_TAG, "Current image factor: " + factor);
		decodeOptions.inJustDecodeBounds = false;
		decodeOptions.inSampleSize = factor;
		// the system can purge the space of Bitmap use automatically
		decodeOptions.inPurgeable = true;
		decodeOptions.inInputShareable = true;

		return BitmapFactory.decodeStream(
				mFileCacheManager.readCacheFile(imageIdentifier), null,
				decodeOptions);
	}

	/**
	 * The listener to listen the image load action
	 * 
	 * @author Tank
	 * 
	 */
	public interface OnImageLoadListener {
		/**
		 * Invoked when the image has been loaded
		 * 
		 * @param bitmap
		 * @param imageUrl
		 * @param fromCache
		 */
		public void onLoadSuccess(Bitmap bitmap, String imageUrl,
				boolean fromCache);

		/**
		 * 
		 * @param imageUrl
		 */
		public void onLoadError(String imageUrl);

		/**
		 * You can decide whether to intercept the image load request in this
		 * method. Such as Wi-Fi only or this image is with the extension name
		 * 'png'
		 * 
		 * @param imageUrl
		 * @return true to cancel this request, otherwise.
		 */
		public boolean onPreLoad(String imageUrl);

		/**
		 * Publish the progress of one image loading task. invoke from
		 * <b>NOT</b> main thread
		 * 
		 * @param imageUrl
		 * @param loadedLength
		 * @param totalLength
		 */
		public void onLoading(String imageUrl, long loadedLength,
				long totalLength);
	}

	private class ImageLoadingTask implements Runnable, OnWriteListener {
		public Uri uri;
		public OnImageLoadListener listener;
		public Bitmap bitmap;
		public boolean isLoadFromCache;
		public boolean readCacheIfExist;
		public long totleBytes;
		public int targetWidth;
		public int targetHeight;

		@Override
		public boolean equals(Object o) {
			if (o instanceof ImageLoadingTask) {
				return uri.equals(((ImageLoadingTask) o).uri);
			}

			return super.equals(o);
		}

		@Override
		public void run() {
			Message message = mImageLoadedHandler.obtainMessage(STATUS_SUCCESS,
					this);
			if (listener != null && listener.onPreLoad(uri.toString())) {
				message.what = STATUS_CANCEL;
				message.sendToTarget();
				return;
			}
			String cacheIdentifier = uri.toString();
			long cacheLength = mFileCacheManager
					.getCacheFileLength(cacheIdentifier);
			if (cacheLength > 0 && readCacheIfExist) {
				bitmap = readCachedBitmap(cacheIdentifier, targetWidth,
						targetHeight);
				if (null != bitmap) {
					Log.d(LOG_TAG, "Image cache found!");
					isLoadFromCache = true;
					message.sendToTarget();
					return;
				}
			}

			Log.d(LOG_TAG, "Image cache not found, get it in async method!");
			InputStream inputStream = null;
			try {
				if (uri.getScheme().startsWith("content:")) {
					ParcelFileDescriptor fileDescriptor = mContext
							.getContentResolver().openFileDescriptor(uri, "r");
					totleBytes = fileDescriptor.getStatSize();
					inputStream = new FileInputStream(
							fileDescriptor.getFileDescriptor());
				} else {
					// Began to load from network
					HttpConnectionHelper connection = HttpConnectionHelper
							.getInstance();
					HttpResponse response = connection.execute(connection
							.obtainHttpGetRequest(URI.create(uri.toString()),
									null));

					HttpEntity entity = response.getEntity();
					inputStream = entity.getContent();
					totleBytes = entity.getContentLength();
				}

				saveBitmapStream(uri.toString(), inputStream, this);
				bitmap = readCachedBitmap(uri.toString(), targetWidth,
						targetHeight);
				if (null == bitmap) {
					Log.d(LOG_TAG, String.format(
							"Delete the broken image cache! url: %s",
							uri.toString()));
					mFileCacheManager.deleteCache(uri.toString());
					message.what = STATUS_ERROR;
					message.sendToTarget();
					return;
				}
				message.sendToTarget();
				return;
			} catch (Exception e) {
				Log.d(LOG_TAG, e.getMessage());
			} finally {
				try {
					inputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			message.what = STATUS_ERROR;
			message.sendToTarget();
		}

		@Override
		public void onWriting(int saveBytes) {
			listener.onLoading(uri.toString(), saveBytes, totleBytes);
		}
	}

	private class ImageLoadHandler extends Handler {
		public ImageLoadHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			ImageLoadingTask task = (ImageLoadingTask) msg.obj;
			boolean isRemove = mTaskList.remove(task);
			switch (msg.what) {
			case STATUS_SUCCESS:
				// if this ImageLoadingTask has been canceled before it done. we
				// can not invoke the callback.
				if (isRemove) {
					task.listener.onLoadSuccess(task.bitmap,
							task.uri.toString(), task.isLoadFromCache);
				}
				break;
			case STATUS_ERROR:
				task.listener.onLoadError(task.uri.toString());
				break;
			case STATUS_CANCEL:
				break;
			}
		}
	}
}
