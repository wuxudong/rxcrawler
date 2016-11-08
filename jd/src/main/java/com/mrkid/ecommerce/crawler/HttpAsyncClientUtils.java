package com.mrkid.ecommerce.crawler;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * User: xudong
 * Date: 07/11/2016
 * Time: 5:41 PM
 */
public class HttpAsyncClientUtils {
    public static CompletableFuture<String> execute(CloseableHttpAsyncClient client, HttpUriRequest request) {
        CompletableFuture<String> promise = new CompletableFuture<>();

        client.execute(request, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse) {
                final int status = httpResponse.getStatusLine().getStatusCode();
                if (status != HttpStatus.SC_OK) {
                    promise.completeExceptionally(new JDCrawlerException(request.getURI().getPath() + " return " + status));
                } else {
                    try {
                        final String value = IOUtils.toString(httpResponse.getEntity().getContent(), "utf-8");
                        promise.complete(value);
                    } catch (IOException e) {
                        promise.completeExceptionally(new JDCrawlerException(e));
                    }
                }

            }

            @Override
            public void failed(Exception e) {
                promise.completeExceptionally(new JDCrawlerException(e));
            }

            @Override
            public void cancelled() {
                promise.cancel(false);
            }
        });

        return promise;
    }

}
