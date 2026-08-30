package io.github.arlol.fake;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hc.client5.http.cache.CacheResponseStatus;
import org.apache.hc.client5.http.cache.HttpCacheContext;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;

public final class FakeHttpClient extends CloseableHttpClient {

	@FunctionalInterface
	public interface Responder {

		ClassicHttpResponse respond(ClassicHttpRequest request)
				throws IOException;

	}

	private final Responder responder;
	private final List<ClassicHttpRequest> requests = new ArrayList<>();
	private CacheResponseStatus cacheResponseStatus = CacheResponseStatus.CACHE_MISS;

	public FakeHttpClient(Responder responder) {
		this.responder = responder;
	}

	public void cacheResponseStatus(CacheResponseStatus cacheResponseStatus) {
		this.cacheResponseStatus = cacheResponseStatus;
	}

	public List<ClassicHttpRequest> requests() {
		return List.copyOf(requests);
	}

	@Override
	public <T> T execute(
			ClassicHttpRequest request,
			HttpContext context,
			HttpClientResponseHandler<? extends T> handler
	) throws IOException {
		requests.add(request);
		HttpCacheContext.cast(context)
				.setCacheResponseStatus(cacheResponseStatus);
		try {
			return handler.handleResponse(responder.respond(request));
		} catch (HttpException e) {
			throw new IOException(e);
		}
	}

	@Override
	protected CloseableHttpResponse doExecute(
			HttpHost target,
			ClassicHttpRequest request,
			HttpContext context
	) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		// nothing to release
	}

	@Override
	public void close(CloseMode closeMode) {
		// nothing to release
	}

}
