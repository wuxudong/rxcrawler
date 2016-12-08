package com.mrkid.ecommerce.crawler.httpasyncclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.ecommerce.crawler.dto.JDCategoryDTO;
import com.mrkid.ecommerce.crawler.dto.JDSkuDTO;
import io.reactivex.Flowable;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

/**
 * User: xudong
 * Date: 07/11/2016
 * Time: 2:27 PM
 */
@Component
@SpringBootApplication(scanBasePackages = "com.mrkid.ecommerce.crawler")
public class JDCrawler {


    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private CloseableHttpAsyncClient httpAsyncClient;

    @Autowired
    private ScriptEngine scriptEngine;

    public Flowable<JDCategoryDTO> getAllCategories() {
        return topCategories().flatMap(top ->
                Flowable.merge(
                        Flowable.just(top),
                        getSubCategories(top.getCid())
                                .flatMap(mid ->
                                        Flowable.merge(Flowable.just(mid), Flowable.fromArray(mid.getCatelogyList()))
                                )));
    }

    public Flowable<JDSkuDTO> getSku(JDCategoryDTO subCategory) {
        return getSkuPageGreatThan(subCategory, 1);
    }

    private Flowable<JDSkuDTO> getSkuPageGreatThan(JDCategoryDTO subCategory, int page) {
        return getSkuPageEq(subCategory, page).flatMap(list -> {
                    if (list.isEmpty()) return Flowable.empty();
                    else {
                        return Flowable.merge(Flowable.fromIterable(list)
                                , getSkuPageGreatThan(subCategory, page + 1));
                    }
                }
        );
    }

    private Flowable<List<JDSkuDTO>> getSkuPageEq(JDCategoryDTO subCategory, int page) {
        return getListPageResponse(subCategory, page).map(res -> {
            final JsonNode value = objectMapper.readTree(objectMapper.readTree(res).get("value").asText());

            List<JDSkuDTO> result = new ArrayList<>();
            for (JsonNode node : value.get("wareList")) {
                JDSkuDTO sku = new JDSkuDTO();
                sku.setId(node.get("wareId").asLong());
                sku.setName(node.get("wname").asText());
                sku.setPrice(BigDecimal.valueOf(node.get("jdPrice").asDouble()));

                if (sku.getId() != 0) {
                    result.add(sku);
                }
            }

            return result;
        });


    }

    private Flowable<Long> getSkuCount(final JDCategoryDTO subCategory) {
        return getListPageResponse(subCategory, 1).map(res -> {
            final JsonNode value = objectMapper.readTree(objectMapper.readTree(res).get("value").asText());
            return value.get("wareCount").asLong();
        });
    }


    private Flowable<String> getListPageResponse(JDCategoryDTO subCategory, int page) {
        HttpPost post = new HttpPost("http://so.m.jd.com/ware/searchList.action");
        List<NameValuePair> form = new ArrayList<>();

        form.add(new BasicNameValuePair("_format_", "json"));
        form.add(new BasicNameValuePair("stock", "1"));

        String path = subCategory.getPath();
        final String[] tokens = path.split("_");
        form.add(new BasicNameValuePair("categoryId", tokens[2]));
        form.add(new BasicNameValuePair("c1", tokens[0]));
        form.add(new BasicNameValuePair("c2", tokens[1]));
        form.add(new BasicNameValuePair("page", String.valueOf(page)));


        try {
            post.setEntity(new UrlEncodedFormEntity(form, "utf-8"));
        } catch (UnsupportedEncodingException e) {
        }

        return HttpAsyncClientUtils.execute(httpAsyncClient, post);
    }

    private Flowable<JDCategoryDTO> topCategories() {
        String url = "http://so.m.jd.com/category/all.html";
        return HttpAsyncClientUtils.execute(httpAsyncClient, new HttpGet(url)).flatMap(content -> extractTopCategories
                (content));
    }

    private Flowable<JDCategoryDTO> getSubCategories(long topCid) {
        String url = "http://so.m.jd.com/category/list.action?_format_=json&catelogyId=" + topCid;
        return HttpAsyncClientUtils.execute(httpAsyncClient, new HttpGet(url)).flatMap(content -> {
            final JsonNode jsonNode = objectMapper.readTree(objectMapper.readTree(content).get("catalogBranch")
                    .asText());
            final JDCategoryDTO[] categories = objectMapper.treeToValue
                    (jsonNode.get("data"), JDCategoryDTO[].class);
            return Flowable.fromArray(categories);
        }).onErrorResumeNext(Flowable.empty());

    }

    private Flowable<JDCategoryDTO> extractTopCategories(String content) throws ScriptException, IOException {
        final Document document = Jsoup.parse(content);
        final Elements elements = document.getElementsByTag("script");

        for (Element element : elements) {
            for (DataNode node : element.dataNodes()) {


                final String wholeData = node.getWholeData();
                if (StringUtils.isNotBlank(wholeData)) {

                    final String key = "jsArgs['category']";
                    int start = wholeData.indexOf(key);
                    if (start >= 0) {
                        int end = wholeData.indexOf(';', start);

                        String jsAssignment = wholeData.substring(start, end + 1);

                        final Object o = scriptEngine.eval("var jsArgs = {};" + jsAssignment + "jsArgs;");
                        final JsonNode list = objectMapper.readTree(objectMapper.writeValueAsString(o))
                                .get("category").get("roorList").get("catelogyList");

                        final Iterator<JsonNode> iterator = list.elements();
                        List<JDCategoryDTO> categories = new ArrayList<>();
                        while (iterator.hasNext()) {
                            categories.add(objectMapper.treeToValue(iterator.next(), JDCategoryDTO.class));
                        }

                        return Flowable.fromIterable(categories);
                    }
                }
            }

        }

        return Flowable.empty();
    }

    public static void main(String[] args) throws InterruptedException {
        final ConfigurableApplicationContext run = SpringApplication.run(JDCrawler.class);
        final JDCrawler jdCrawler = run.getBean(JDCrawler.class);

        ConcurrentHashMap<JDCategoryDTO, Long> countMap = new ConcurrentHashMap<>();
        jdCrawler.getAllCategories()
                .filter(category -> RequestHelper.shouldFetchItems(category))
                .flatMap(
                        category ->
                                jdCrawler.getSkuCount(category).map(count -> new ImmutablePair<>(category, count)))
                .blockingSubscribe(pair -> countMap.put(pair.left, pair.right));

        final Long total = countMap.values().stream().reduce(0l, (l1, l2) -> l1 + l2);
        System.out.println("total " + total);
    }

}
