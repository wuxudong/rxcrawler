package com.mrkid.crawler;

import com.mrkid.crawler.downloader.Downloader;
import com.mrkid.crawler.downloader.HttpAsyncClientDownloader;
import com.mrkid.crawler.pipeline.Pipeline;
import com.mrkid.crawler.processor.PageProcessor;
import com.mrkid.crawler.scheduler.MemoryScheduler;
import com.mrkid.crawler.scheduler.Scheduler;
import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.message.BasicHeader;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 08/12/2016
 * Time: 4:44 PM
 */
@Data
public class Spider implements Closeable {
    private Downloader downloader;

    private Scheduler scheduler = new MemoryScheduler();

    private Site site;

    private PageProcessor pageProcessor = page -> page;

    private Pipeline pipeline = resultItems -> {
    };


    private final AtomicInteger runningCount = new AtomicInteger(0);

    private final static Logger logger = LoggerFactory.getLogger(Spider.class);

    public Spider() {
    }

    public void start() {
        assert site != null;

        initComponent();

        Disposable stats = Flowable.interval(10, 10, TimeUnit.SECONDS)
                .subscribe(l ->
                        logger.info("crawler concurrency {}, {} requests are waiting scheduled"
                                , runningCount.get(), scheduler.size()));

        Flowable.generate(generator())
                .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
                .doOnNext(r -> runningCount.incrementAndGet())
                .flatMap(request -> download(request), site.getMaxConnTotal())
                .flatMap(optional -> {
                    if (optional.isPresent()) {
                        return Flowable.just(optional).subscribeOn(Schedulers.io())
                                .doOnNext(op -> pageProcessor.process(op.get()))
                                .doOnNext(op -> pipeline.process(op.get().getResultItems()))
                                .doOnNext(op -> op.get().getTargetRequests().forEach(req -> addRequest(req)))
                                .onErrorResumeNext(throwable -> {
                                    logger.error("abandon handling {} caused by {} ",
                                            optional.get().getRequest(), throwable.getMessage());

                                    return Flowable.just(Optional.empty());
                                });
                    } else {
                        return Flowable.just(optional);
                    }
                })
                .doOnNext(optional -> runningCount.decrementAndGet())
                .blockingSubscribe();

        stats.dispose();

        logger.info("spider finished");
    }

    private void initComponent() {
        assert site != null;

        if (downloader == null) {
            // reactor config
            IOReactorConfig reactorConfig = IOReactorConfig.custom()
                    .setConnectTimeout(site.getTimeOut())
                    .setSoTimeout(site.getTimeOut())
                    .build();

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(site.getTimeOut())
                    .setSocketTimeout(site.getTimeOut())
                    .setConnectionRequestTimeout(site.getTimeOut())
                    .build();


            HttpAsyncClientBuilder asyncClientBuilder = HttpAsyncClientBuilder.create()
                    .setDefaultIOReactorConfig(reactorConfig)
                    .setDefaultRequestConfig(requestConfig)
                    .setMaxConnPerRoute(site.getMaxConnPerRoute())
                    .setMaxConnTotal(site.getMaxConnTotal());

            if (site.getUserAgent() != null) {
                asyncClientBuilder.setUserAgent(site.getUserAgent());
            }

            if (site.getHttpProxy() != null) {
                asyncClientBuilder.setProxy(site.getHttpProxy());
            }

            if (site.getHeaders() != null) {
                asyncClientBuilder.setDefaultHeaders(site.getHeaders().entrySet()
                        .stream()
                        .map(e -> new BasicHeader(e.getKey(), e.getValue()))
                        .collect(Collectors.toList()));
            }


            final CloseableHttpAsyncClient asyncClient = asyncClientBuilder.build();
            asyncClient.start();

            downloader = new HttpAsyncClientDownloader(asyncClient);
        }

    }

    private Consumer<Emitter<Request>> generator() {
        return e -> {
            for (; ; ) {
                final Request poll = scheduler.poll();
                if (poll != null) {
                    e.onNext(poll);
                    break;
                } else {
                    if (runningCount.get() == 0) {
                        e.onComplete();
                        break;
                    } else {
                        Thread.sleep(100);
                    }
                }
            }
        };
    }

    private Publisher<Optional<Page>> download(Request request) {
        final int retryTimes = site.getRetryTimes();
        final int retrySleepTime = site.getRetrySleepTime();

        final Function<Flowable<Throwable>, Publisher<?>> retry =
                attempts -> attempts.zipWith(Flowable.range(1, retryTimes + 1), (e, i) -> new ImmutablePair<>(e, i))
                        .flatMap(pair ->
                        {
                            if (pair.getRight() > retryTimes) {
                                return Flowable.error(pair.getLeft());
                            }
                            return Flowable.timer(retrySleepTime, TimeUnit.MILLISECONDS);
                        });


        return Flowable.defer(() -> downloader.download(request))
                .map(page -> Optional.of(page))
                .delay(site.getSleepTime(), TimeUnit.MILLISECONDS)
                .retryWhen(retry)
                .onErrorResumeNext(throwable -> {
                    logger.error("abandon downloading {} caused by unable to not recover from {} ",
                            request, throwable.getMessage());
                    return Flowable.just(Optional.empty());
                });
    }


    public void addRequest(Request request) {
        logger.debug("add Request {} to scheduler", request);
        scheduler.offer(request);
    }

    @Override
    public void close() throws IOException {
        if (downloader != null) {
            downloader.close();
        }
    }
}
