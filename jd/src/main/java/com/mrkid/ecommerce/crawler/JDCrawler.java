package com.mrkid.ecommerce.crawler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * User: xudong
 * Date: 07/11/2016
 * Time: 2:27 PM
 */
@Component
public class JDCrawler {


    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private CloseableHttpAsyncClient httpAsyncClient;

    @Autowired
    private ChromeDriver chromeDriver;

    public CompletableFuture<List<Category>> getAllCategories() {
        return topCategories().thenCompose(topCategories -> {
                    List<CompletableFuture<Category>> list = new ArrayList<>();

                    for (Category top : topCategories) {
                        list.add(completeCategory(top));
                    }

                    return CompletableFuture.allOf(list.toArray(new CompletableFuture[list.size()])).thenApply(v -> list
                            .stream().map(CompletableFuture::join).collect(Collectors.toList()));
                }
        );
    }

    public CompletableFuture<List<Sku>> getSku(Category subCategory) {
        AtomicInteger page = new AtomicInteger(1);

        List<Sku> result = new ArrayList<>();

        Function<List<Sku>, CompletionStage<List<Sku>>> nextPage = new Function<
                List<Sku>, CompletionStage<List<Sku>>>() {
            @Override
            public CompletionStage<List<Sku>> apply(List<Sku> list) {
                System.out.println("finish page " + page.get() + " of category " + subCategory.getCid());
                if (list.isEmpty()) {
                    return CompletableFuture.completedFuture(result);
                } else {
                    result.addAll(list);

                    return getSku(subCategory, page.incrementAndGet()).thenCompose(this);
                }
            }
        };

        return getSku(subCategory, page.get()).thenCompose(nextPage);


    }

    private CompletableFuture<List<Sku>> getSku(Category subCategory, int page) {

        HttpPost post = new HttpPost("http://so.m.jd.com/ware/searchList.action");
        List<NameValuePair> form = new ArrayList<>();

        form.add(new BasicNameValuePair("_format_", "json"));
        form.add(new BasicNameValuePair("stock", "1"));

        String path = subCategory.getPath();
        final String[] tokens = path.split("_");
        form.add(new BasicNameValuePair("categoryId", tokens[0]));
        form.add(new BasicNameValuePair("c1", tokens[1]));
        form.add(new BasicNameValuePair("c2", tokens[2]));
        form.add(new BasicNameValuePair("page", String.valueOf(page)));


        try {
            post.setEntity(new UrlEncodedFormEntity(form, "utf-8"));
        } catch (UnsupportedEncodingException e) {
        }

        return HttpAsyncClientUtils.execute(httpAsyncClient, post).thenApply(res -> {
            try {
                final JsonNode value = objectMapper.readTree(objectMapper.readTree(res).get("value").asText());

                List<Sku> result = new ArrayList<>();
                for (JsonNode node : value.get("wareList")) {
                    Sku sku = new Sku();
                    sku.setId(node.get("wareId").asLong());
                    sku.setName(node.get("wname").asText());
                    sku.setPrice(BigDecimal.valueOf(node.get("jdPrice").asDouble()));

                    if (sku.getId() != 0) {
                        result.add(sku);
                    } else {
                        // TODO
                    }
                }

                return result;
            } catch (IOException e) {
                // TODO
                return new ArrayList<Sku>();
            }
        });


    }

    private CompletableFuture<Category> completeCategory(Category top) {
        return
                getSubCategories(top.getCid()).thenApply(subCategories -> {
                    top.setCatelogyList(subCategories);
                    return top;
                });

    }


    private CompletableFuture<Category[]> topCategories() {

        String url = "http://so.m.jd.com/category/all.html";
        return HttpAsyncClientUtils.execute(httpAsyncClient, new HttpGet(url)).thenApply(content -> extractTopCategories
                (content));
    }

    private CompletableFuture<Category[]> getSubCategories(int topCid) {
        String url = "http://so.m.jd.com/category/list.action?_format_=json&catelogyId=" + topCid;
        return HttpAsyncClientUtils.execute(httpAsyncClient, new HttpGet(url)).thenApply(content -> {
            try {
                final JsonNode jsonNode = objectMapper.readTree(objectMapper.readTree(content).get("catalogBranch")
                        .asText());
                final Category[] datas = objectMapper.treeToValue
                        (jsonNode.get("data"), Category[].class);
                return datas;
            } catch (IOException e) {
                // TODO
                return new Category[0];
            }
        });

    }

    private Category[] extractTopCategories(String content) {
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

                        String categories = wholeData.substring(start, end + 1);
                        final Object o = chromeDriver.executeScript("var jsArgs = {};" + categories +
                                "return jsArgs;");

                        try {
                            final JsonNode list = objectMapper.readTree(objectMapper.writeValueAsString
                                    (o)).get("category").get("roorList").get
                                    ("catelogyList");

                            return objectMapper.treeToValue(list, Category[].class);
                        } catch (IOException e) {
                            // TODO
                        }
                    }


                }
            }

        }

        // TODO
        return new Category[0];
    }


}
