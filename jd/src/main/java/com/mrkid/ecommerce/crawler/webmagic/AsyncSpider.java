package com.mrkid.ecommerce.crawler.webmagic;

import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.schedulers.Schedulers;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: xudong
 * Date: 05/12/2016
 * Time: 2:51 PM
 */
public class AsyncSpider extends Spider {
    public AsyncSpider(PageProcessor pageProcessor) {
        super(pageProcessor);
    }

    private AsyncDownloader asyncDownloader;

    private AtomicInteger runningCount = new AtomicInteger(0);

    public AsyncSpider setAsyncDownloader(AsyncDownloader asyncDownloader) {
        this.asyncDownloader = asyncDownloader;
        return this;
    }

    private void dispatchMore(BehaviorProcessor processor) {


        while (runningCount.get() < threadNum) {

            Request request = scheduler.poll(this);

            if (request != null) {
                processor.onNext(request);
            } else {
                if (runningCount.get() == 0) {
                    processor.onComplete();
                }

                break;
            }
        }
    }


    @Override
    public void run() {
        initComponent();
        logger.info("AsyncSpider " + getUUID() + " started!");

        BehaviorProcessor<Request> processor = BehaviorProcessor.create();

        final Flowable<Page> flowable = processor
                .flatMap(requestFinal -> {
                    runningCount.incrementAndGet();
                    return asyncProcessRequest(requestFinal)
                            .subscribeOn(Schedulers.io())
                            .doOnError(e -> {
                                onError(requestFinal);
                                logger.error("process request " + requestFinal + " error", e);
                            })
                            .doOnComplete(() -> onSuccess(requestFinal))
                            .doFinally(() -> runningCount.decrementAndGet())
                            .doFinally(() -> dispatchMore(processor))
                            .onErrorResumeNext(Flowable.empty());
                }, threadNum);

        Request request = scheduler.poll(this);

        // trigger start
        if (request != null) {
            processor.onNext(request);
        }

        flowable.blockingSubscribe();


    }


    protected Flowable<Page> asyncProcessRequest(Request request) {
        return asyncDownloader.asyncDownload(request, this).doOnNext(page -> {
            if (page == null) {
                throw new RuntimeException("unaccpetable response status");
            }
            // for cycle retry
            if (page.isNeedCycleRetry()) {
                extractAndAddRequests(page, true);
                sleep(site.getRetrySleepTime());
                return;
            }
            pageProcessor.process(page);
            extractAndAddRequests(page, spawnUrl);
            if (!page.getResultItems().isSkip()) {
                for (Pipeline pipeline : pipelines) {
                    pipeline.process(page.getResultItems(), this);
                }
            }
            //for proxy status management
            request.putExtra(Request.STATUS_CODE, page.getStatusCode());
            sleep(site.getSleepTime());
        });
    }

}
