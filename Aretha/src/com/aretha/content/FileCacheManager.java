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
package com.aretha.content;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

import com.aretha.util.Utils;

/**
 * A simple class to manage the cached file which saved in the
 * /data/data/{package name}/files/ folder or a specified directory
 * 
 * @author Tank
 */
public class FileCacheManager {
	private File mCacheFileDirectory;

	public FileCacheManager(Context context) {
		this(context, null);
	}

	public FileCacheManager(Context context, File cacheDirectory) {
		if (null == context) {
			throw new IllegalArgumentException("context can not be null");
		}

		if (null != cacheDirectory && cacheDirectory.canWrite()) {
			mCacheFileDirectory = cacheDirectory;
		} else {
			final File externalCacheDir = context.getExternalCacheDir();
			mCacheFileDirectory = null == externalCacheDir ? context
					.getCacheDir() : externalCacheDir;
		}
	}

	/**
	 * This method allow developer to specified a location for cache file to
	 * store in
	 * 
	 * @param directory
	 */
	public void setCacheDirectory(File directory) {
		if (directory == null || !directory.isDirectory()
				|| !directory.canWrite()) {
			return;
		}
		mCacheFileDirectory = directory;
	}

	/**
	 * Get current directory where cache file stored in
	 * 
	 * @return
	 */
	public File getCacheDirectory() {
		return mCacheFileDirectory;
	}

	public long getCacheFileLength(String cacheIdentifier) {
		File cacheFile = createCacheFile(cacheIdentifier);
		if (null != cacheFile) {
			return cacheFile.length();
		}
		return 0;
	}

	/**
	 * Save a file to the cache folder
	 * 
	 * @param cacheIdentifier
	 *            cache file identifier
	 * @param inputstream
	 * 
	 * @return success or not
	 */
	public boolean writeCacheFile(String cacheIdentifier,
			InputStream inputStream) {
		return writeCacheFile(cacheIdentifier, inputStream, null);
	}

	/**
	 * Save a file to the cache folder
	 * 
	 * @param cacheIdentifier
	 * @param inputStream
	 * @param onWriteListener
	 *            listener to listen the progress of write
	 * @return
	 */
	public boolean writeCacheFile(String cacheIdentifier,
			InputStream inputStream, OnWriteListener onWriteListener) {
		File cacheFile = createCacheFile(cacheIdentifier);
		try {
			if (!cacheFile.exists()) {
				cacheFile.createNewFile();
			}

			BufferedOutputStream fileOutputStream = new BufferedOutputStream(
					new FileOutputStream(cacheFile));

			int read;
			int totalCount = 0;
			byte[] buffer = new byte[8 * 1024];
			while ((read = inputStream.read(buffer)) != -1) {
				fileOutputStream.write(buffer, 0, read);
				totalCount += read;

				if (null != onWriteListener) {
					onWriteListener.onWriting(totalCount);
				}
			}
			fileOutputStream.flush();
			return true;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return false;
	}

	/**
	 * Read a cached file
	 * 
	 * @param cacheIdentifier
	 * @return Cache file's {@link InputStream}
	 */
	public InputStream readCacheFile(String cacheIdentifier) {
		try {
			return new FileInputStream(createCacheFile(cacheIdentifier));
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	/**
	 * Check whether the cached file exist
	 * 
	 * @param cacheIdentifier
	 * @return
	 */
	public boolean hasCacheFile(String cacheIdentifier) {
		File cacheFile = createCacheFile(cacheIdentifier);
		return hasCacheFile(cacheFile);
	}

	/**
	 * Check whether the cached file exist
	 * 
	 * @param cacheFile
	 * @return
	 */
	public boolean hasCacheFile(File cacheFile) {
		return cacheFile.exists();
	}

	/**
	 * Delete cached file
	 * 
	 * @param cacheIdentifier
	 * @return
	 */
	public boolean deleteCache(String cacheIdentifier) {
		File cacheFile = createCacheFile(cacheIdentifier);
		boolean isExists = hasCacheFile(cacheFile);

		boolean isDelete = cacheFile.delete();

		return isExists && isDelete;
	}

	/**
	 * Clear all cached files
	 * 
	 * @return
	 */
	public boolean clearAllCaches() {
		File[] files = mCacheFileDirectory.listFiles();
		for (File file : files) {
			if (!file.isDirectory()) {
				file.delete();
			}
		}
		return mCacheFileDirectory.delete();
	}

	/**
	 * Create a {@link File} as the rule, you can override it to change the
	 * default rule
	 * 
	 * @param cacheIdentifier
	 * @return
	 */
	public File createCacheFile(String cacheIdentifier) {
		if (null == cacheIdentifier) {
			throw new IllegalArgumentException(
					"cacheIdentifier can not be null");
		}

		String cacheFileName = Utils.getUUID(cacheIdentifier).toString();
		return new File(mCacheFileDirectory, cacheFileName);
	}

	public interface OnWriteListener {
		public void onWriting(int saveBytes);
	}
}
