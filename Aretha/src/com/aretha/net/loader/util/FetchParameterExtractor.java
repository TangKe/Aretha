package com.aretha.net.loader.util;

import java.util.List;

import org.apache.http.NameValuePair;

import com.aretha.net.Fetch;

public interface FetchParameterExtractor {
	public List<NameValuePair> extract(Fetch fetch);
}
