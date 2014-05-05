package com.aretha.net.parser;

import java.io.Reader;

import com.aretha.net.loader.util.Fetch;
import com.google.gson.stream.JsonReader;

public abstract class JsonParser<Result> extends
		Parser<Reader, Result, JsonReader> {
	@Override
	public final Result doParse(Fetch fetch, JsonReader parser)
			throws Exception {
		parser.beginObject();
		Result result = parseResult(parser);
		parser.endObject();
		return result;
	}

	public abstract Result parseResult(JsonReader reader) throws Exception;

	@Override
	public final JsonReader makeParser(Reader input) {
		return new JsonReader(input);
	}
}
