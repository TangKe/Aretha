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
package com.aretha.animation;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * View can translate as the {@link Path}
 * 
 * @author Tank
 * 
 */
public class PathAnimation extends Animation {
	private Path mPath;
	private PathMeasure mPathMeasure;
	private float mPathLength;

	private float[] mCurrentPosition;

	public PathAnimation(Path path) {
		setPath(path);
		mCurrentPosition = new float[2];
	}

	public Path getPath() {
		return mPath;
	}

	public void setPath(Path path) {
		this.mPath = path;

		if (null != path) {
			mPathMeasure = new PathMeasure(path, false);
			mPathLength = mPathMeasure.getLength();
		}
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		final PathMeasure pathMeasure = mPathMeasure;
		if (null == pathMeasure) {
			return;
		}

		final float[] currentPosition = mCurrentPosition;
		pathMeasure.getPosTan(mPathLength * interpolatedTime, currentPosition,
				null);
		t.getMatrix().setTranslate(currentPosition[0], currentPosition[1]);
	}
}
