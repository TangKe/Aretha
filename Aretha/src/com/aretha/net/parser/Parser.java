package com.aretha.net.parser;

import java.io.Closeable;
import java.io.IOException;

import com.aretha.net.loader.util.Fetch;

public abstract class Parser<Input extends Closeable, Result, Parser> {
	public Result parse(Fetch fetch, Input input) {
		if (null == input) {
			return null;
		}

		Parser parser = makeParser(input);
		if (null == parser) {
			throw new IllegalStateException(
					"You must specified a Parse to parse the result");
		}

		try {
			return doParse(fetch, parser);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public abstract Parser makeParser(Input input);

	public abstract Result doParse(Fetch fetch, Parser parser) throws Exception;
}
