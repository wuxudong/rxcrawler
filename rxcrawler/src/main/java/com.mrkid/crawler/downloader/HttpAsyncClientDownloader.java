package com.mrkid.crawler.downloader;

import com.mrkid.crawler.CrawlerException;
import com.mrkid.crawler.Page;
import com.mrkid.crawler.Request;
import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 08/12/2016
 * Time: 7:12 PM
 */
@Data
public class HttpAsyncClientDownloader implements Downloader {

    private Logger logger = LoggerFactory.getLogger(HttpAsyncClientDownloader.class);

    private final CloseableHttpAsyncClient client;

    private static <T> Flowable<T> toFlowable(CompletableFuture<T> future) {
        final BehaviorProcessor<T> processor = BehaviorProcessor.create();

        future.whenComplete((result, error) -> {
            if (error != null) {
                processor.onError(error);
            } else {
                processor.onNext(result);
                processor.onComplete();
            }
        });

        return processor;
    }


    @Override
    public Flowable<Page> download(Request request) {

        CompletableFuture<Page> promise = new CompletableFuture<>();

        HttpUriRequest httpUriRequest = null;
        switch (request.getMethod()) {
            case "GET":
                httpUriRequest = new HttpGet(request.getUrl());
                break;
            case "POST":
                final HttpPost httpPost = new HttpPost(request.getUrl());
                final List<BasicNameValuePair> nameValuePairs = request.getForm().entrySet().stream()
                        .map(e -> new BasicNameValuePair(e.getKey(), e.getValue()))
                        .collect(Collectors.toList());

                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    return Flowable.error(e);
                }
                httpUriRequest = httpPost;
                break;

            default:
                throw new UnsupportedOperationException(request.getMethod() + " is not supported");
        }

        logger.info("start downloading {} method {} form {}", request.getUrl(), request.getMethod(), request.getForm());
        client.execute(httpUriRequest, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse httpResponse) {
                final int status = httpResponse.getStatusLine().getStatusCode();
                logger.info("finish download {} method {} form {} status {}", request.getUrl(),
                        request.getMethod(), request.getForm(), status);

                if (status != HttpStatus.SC_OK) {
                    promise.completeExceptionally(new CrawlerException(request.getUrl() + " return " + status));
                } else {
                    try {
                        final String value = IOUtils.toString(httpResponse.getEntity().getContent(), "utf-8");

                        Page page = new Page(request);
                        page.setRawText(value);

                        promise.complete(page);
                    } catch (IOException e) {
                        promise.completeExceptionally(new CrawlerException(e));
                    }
                }

            }

            @Override
            public void failed(Exception e) {
                promise.completeExceptionally(new CrawlerException(e));
            }

            @Override
            public void cancelled() {
                promise.cancel(false);
            }
        });

        return toFlowable(promise);


    }
}
