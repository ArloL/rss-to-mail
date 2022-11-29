package io.github.arlol.feed;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

import com.apptasticsoftware.rssreader.Item;
import com.apptasticsoftware.rssreader.RssReader;

public class SilentRssReader extends RssReader {

	@Override
	public Stream<Item> read(String url) {
		try {
			return super.read(url);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
