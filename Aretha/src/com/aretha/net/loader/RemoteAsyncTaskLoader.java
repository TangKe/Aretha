package com.aretha.net.loader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.aretha.net.HttpConnectionHelper;
import com.aretha.net.loader.util.BaseFetchParameterExtractor;
import com.aretha.net.loader.util.Fetch;
import com.aretha.net.loader.util.FetchParameterExtractor;
import com.aretha.net.parser.Parser;
import com.aretha.util.Utils;

public class RemoteAsyncTaskLoader<Result> extends AsyncTaskLoader<Result> {
	private HttpConnectionHelper mHelper;
	private Fetch mFetch;
	private Result mResult;
	private Parser<Reader, ? extends Result, ?> mParser;
	private FetchParameterExtractor mParameterExtractor;

	public RemoteAsyncTaskLoader(Context context, Fetch fetch,
			Parser<Reader, ? extends Result, ?> parser) {
		super(context);
		mHelper = HttpConnectionHelper.getInstance();
		mFetch = fetch;
		mParser = parser;
		mParameterExtractor = new BaseFetchParameterExtractor();
	}

	@Override
	protected void onStartLoading() {
		if (null == mFetch) {
			deliverResult(null);
		}

		if (null == mResult) {
			forceLoad();
		} else {
			deliverResult(mResult);
		}
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	@Override
	public Result loadInBackground() {
		final Fetch fetch = mFetch;
		final HttpConnectionHelper helper = mHelper;

		HttpUriRequest request = helper.obtainHttpRequest(
				fetch.getFetchMethod(), fetch.getUrl(),
				mParameterExtractor.extract(fetch));
		Utils.debug(request.getURI().toString());
		if (fetch.onPreFetch(request)) {
			return mResult;
		}
		HttpResponse response = helper.execute(request);
		fetch.onPostFetch(request);
		if (null == response) {
			return null;
		}
		try {
			Result result = mResult = mParser.parse(fetch,
					new InputStreamReader(response.getEntity().getContent()));
			Utils.debug(result);
			return result;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
