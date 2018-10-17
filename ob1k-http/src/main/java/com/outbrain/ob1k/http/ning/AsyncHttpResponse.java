package com.outbrain.ob1k.http.ning;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.transform;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.outbrain.ob1k.concurrent.Try;
import com.outbrain.ob1k.http.TypedResponse;
import com.outbrain.ob1k.http.common.Cookie;
import com.outbrain.ob1k.http.marshalling.MarshallingStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.asynchttpclient.Response;

/**
 * @author marenzon
 */
public class AsyncHttpResponse<T> implements TypedResponse<T> {

  private final MarshallingStrategy marshallingStrategy;
  private final Type type;
  private final Response asyncHttpResponse;
  private volatile T typedBody;

  public AsyncHttpResponse(final Response asyncHttpResponse, final Type type, final MarshallingStrategy marshallingStrategy) throws IOException {

    this.asyncHttpResponse = checkNotNull(asyncHttpResponse, "asyncHttpResponse may not be null");
    this.marshallingStrategy = marshallingStrategy;
    this.type = type;
  }

  @Override
  public int getStatusCode() {

    return asyncHttpResponse.getStatusCode();
  }

  @Override
  public String getStatusText() {

    return asyncHttpResponse.getStatusText();
  }

  @Override
  public URI getUri() throws URISyntaxException {

    return asyncHttpResponse.getUri().toJavaNetURI();
  }

  @Override
  public String getUrl() {

    return asyncHttpResponse.getUri().toUrl();
  }

  @Override
  public String getContentType() {

    return asyncHttpResponse.getContentType();
  }

  @Override
  public T getTypedBody() throws IOException {

    if (typedBody == null) {

      checkNotNull(marshallingStrategy, "unmarshallingStrategy may not be null");
      checkNotNull(type, "class type may not be null");

      typedBody = marshallingStrategy.unmarshall(type, this);
    }

    return typedBody;
  }

  @Override
  public String getResponseBody() throws IOException {

    return asyncHttpResponse.getResponseBody();
  }

  @Override
  public byte[] getResponseBodyAsBytes() throws IOException {

    return asyncHttpResponse.getResponseBodyAsBytes();
  }

  @Override
  public InputStream getResponseBodyAsStream() throws IOException {

    return asyncHttpResponse.getResponseBodyAsStream();
  }

  @Override
  public ByteBuffer getResponseBodyAsByteBuffer() throws IOException {

    return asyncHttpResponse.getResponseBodyAsByteBuffer();
  }

  @Override
  public List<Cookie> getCookies() {

    return transformNettyResponseCookies(asyncHttpResponse.getCookies());
  }

  @Override
  public String getHeader(final String name) {

    return asyncHttpResponse.getHeader(name);
  }

  @Override
  public List<String> getHeaders(final String name) {

    return asyncHttpResponse.getHeaders(name);
  }

  @Override
  public Map<String, List<String>> getHeaders() {

    final Map<String, List<String>> httpHeaders = new HashMap<>();
    asyncHttpResponse.getHeaders().forEach(e -> httpHeaders.merge(e.getKey(), Collections.singletonList(e.getValue()), (a, b) -> {
      List<String> merge = new ArrayList<>(a);
      merge.addAll(b);
      return ImmutableList.copyOf(merge);
    }));
    return httpHeaders;
  }

  @Override
  public boolean isRedirected() {

    return asyncHttpResponse.isRedirected();
  }

  @Override
  public boolean hasResponseBody() {

    return asyncHttpResponse.hasResponseBody();
  }

  @Override
  public boolean hasResponseStatus() {

    return asyncHttpResponse.hasResponseStatus();
  }

  @Override
  public boolean hasResponseHeaders() {

    return asyncHttpResponse.hasResponseHeaders();
  }

  private List<Cookie> transformNettyResponseCookies(final List<org.asynchttpclient.cookie.Cookie> cookies) {

    final Function<org.asynchttpclient.cookie.Cookie, Cookie> transformer = nettyCookie ->
      new Cookie(nettyCookie.getName(), nettyCookie.getValue(), nettyCookie.getDomain(),
        nettyCookie.getPath(), nettyCookie.getMaxAge(),
        nettyCookie.isSecure(), nettyCookie.isHttpOnly());

    return transform(cookies, transformer);
  }

  @Override
  public String toString() {
    final StringBuilder response = new StringBuilder("Response(statusCode=[");

    response.append(getStatusCode());
    response.append("],");
    response.append("headers=[");
    response.append(getHeaders());
    response.append("],responseBody=[");
    response.append(Try.apply(this::getResponseBody));
    response.append("]");

    return response.toString();
  }
}
