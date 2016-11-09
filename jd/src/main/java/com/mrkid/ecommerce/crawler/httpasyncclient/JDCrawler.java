package com.mrkid.ecommerce.crawler.httpasyncclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mrkid.ecommerce.crawler.dto.JDCategoryDTO;
import com.mrkid.ecommerce.crawler.dto.JDSkuDTO;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    private ScriptEngine scriptEngine;

    public CompletableFuture<List<JDCategoryDTO>> getAllCategories() {
        return topCategories().thenCompose(topCategories -> {
                    List<CompletableFuture<JDCategoryDTO>> list = new ArrayList<>();

                    for (JDCategoryDTO top : topCategories) {
                        list.add(completeCategory(top));
                    }

                    return CompletableFuture.allOf(list.toArray(new CompletableFuture[list.size()])).thenApply(v -> list
                            .stream().map(CompletableFuture::join).collect(Collectors.toList()));
                }
        );
    }

    public CompletableFuture<List<JDSkuDTO>> getSku(JDCategoryDTO subCategory) {
        AtomicInteger page = new AtomicInteger(1);

        List<JDSkuDTO> result = new ArrayList<>();

        Function<List<JDSkuDTO>, CompletionStage<List<JDSkuDTO>>> nextPage = new Function<
                List<JDSkuDTO>, CompletionStage<List<JDSkuDTO>>>() {
            @Override
            public CompletionStage<List<JDSkuDTO>> apply(List<JDSkuDTO> list) {
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

    private CompletableFuture<List<JDSkuDTO>> getSku(JDCategoryDTO subCategory, int page) {

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

                List<JDSkuDTO> result = new ArrayList<>();
                for (JsonNode node : value.get("wareList")) {
                    JDSkuDTO sku = new JDSkuDTO();
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
                return new ArrayList<JDSkuDTO>();
            }
        });


    }

    private CompletableFuture<JDCategoryDTO> completeCategory(JDCategoryDTO top) {
        return
                getSubCategories(top.getCid()).thenApply(subCategories -> {
                    top.setCatelogyList(subCategories);
                    return top;
                });

    }

    private CompletableFuture<JDCategoryDTO[]> topCategories() {

        String url = "http://so.m.jd.com/category/all.html";
        return HttpAsyncClientUtils.execute(httpAsyncClient, new HttpGet(url)).thenApply(content -> extractTopCategories
                (content));
    }

    private CompletableFuture<JDCategoryDTO[]> getSubCategories(long topCid) {
        String url = "http://so.m.jd.com/category/list.action?_format_=json&catelogyId=" + topCid;
        return HttpAsyncClientUtils.execute(httpAsyncClient, new HttpGet(url)).thenApply(content -> {
            try {
                final JsonNode jsonNode = objectMapper.readTree(objectMapper.readTree(content).get("catalogBranch")
                        .asText());
                final JDCategoryDTO[] categories = objectMapper.treeToValue
                        (jsonNode.get("data"), JDCategoryDTO[].class);
                return categories;
            } catch (IOException e) {
                // TODO
                return new JDCategoryDTO[0];
            }
        });

    }

    private JDCategoryDTO[] extractTopCategories(String content) {
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

                        try {
                            final Object o = scriptEngine.eval("var jsArgs = {};" + jsAssignment + "jsArgs;");
                            final JsonNode list = objectMapper.readTree(objectMapper.writeValueAsString
                                    (o)).get("category").get("roorList").get
                                    ("catelogyList");

                            final Iterator<JsonNode> iterator = list.elements();
                            List<JDCategoryDTO> categories = new ArrayList<>();
                            while (iterator.hasNext()) {
                                categories.add(objectMapper.treeToValue(iterator.next(), JDCategoryDTO.class));
                            }

                            return categories.toArray(new JDCategoryDTO[0]);
                        } catch (Exception e) {
                            // TODO
                        }
                    }


                }
            }

        }

        // TODO
        return new JDCategoryDTO[0];
    }


}
