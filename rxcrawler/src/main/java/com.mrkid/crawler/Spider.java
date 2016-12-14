package com.mrkid.crawler;

import com.mrkid.crawler.downloader.Downloader;
import com.mrkid.crawler.pipeline.Pipeline;
import com.mrkid.crawler.processor.PageProcessor;
import com.mrkid.crawler.scheduler.Scheduler;
import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import lombok.Data;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: xudong
 * Date: 08/12/2016
 * Time: 4:44 PM
 */
@Data
public class Spider {
    private Downloader downloader;

    private Scheduler scheduler;

    private Site site;

    private PageProcessor pageProcessor;

    private Pipeline pipeline;

    private int maxConcurrency = 1;

    private AtomicInteger runningCount = new AtomicInteger(0);

    private Logger logger = LoggerFactory.getLogger(Spider.class);

    public void start() {
        Disposable disposable = Flowable.interval(10, 10, TimeUnit.SECONDS).subscribe(l -> logger.info("crawler " +
                "concurrency " + runningCount
                .get()));

        Flowable.generate(generator())
                .doOnNext(r -> runningCount.incrementAndGet())
                .flatMap(request -> download(request), maxConcurrency)
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

        logger.info("spider finished");

        disposable.dispose();
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
        scheduler.offer(request);
    }

}
