package org.example;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

import java.util.*;

public class OschinaBlogPageProcessor implements PageProcessor {

    private final Site site = Site.me()
            .setRetryTimes(3)
            .setSleepTime(1000)
            .setTimeOut(10000)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

    @Override
    public void process(Page page) {
        try {
            Html html = page.getHtml();
            if (html == null) {
                page.setSkip(true);
                return;
            }

            // 获取当前页的所有篮球卡片链接
            List<String> links = html.links().regex(".*/Cards/Basketball/[^,]*").all();
            if (links != null && !links.isEmpty()) {
                links = removeDuplicateWithOrder(links);
                page.addTargetRequests(links);
            }

            // 提取卡片信息
            String title = html.$("#img1", "alt").toString();
            String img = html.$("#img1", "src").toString();
            String imgBak = html.$("#img1", "data-otherside").toString();
            String price = html.xpath("//span[@class='listprice']/text()").toString();

            // 只有当页面包含卡片信息时才保存
            if (title != null || img != null || price != null) {
                page.putField("title", title);
                page.putField("img", img);
                page.putField("img-bak", imgBak);
                page.putField("price", price);
                System.out.println("get page: " + page.getUrl().toString());
                System.out.println("title: " + title);
                System.out.println("img: " + img);
                System.out.println("img-bak: " + imgBak);
                System.out.println("price: " + price);
            }

            // 获取下一页链接
            String nextPageUrl = html.$("#ctl00_ContentPlaceHolder1_cmdNext_Bottom", "href").toString();
            if (nextPageUrl != null && !nextPageUrl.isEmpty()) {
                page.addTargetRequest(nextPageUrl);
            }
        } catch (Exception e) {
            System.err.println("Error processing page: " + page.getUrl());
            e.printStackTrace();
            page.setSkip(true);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    // 删除ArrayList中重复元素，去除带逗号的URL
    public static List<String> removeDuplicateWithOrder(List<String> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> set = new HashSet<>();
        List<String> newList = new ArrayList<>();
        for (String element : list) {
            if (element != null && !element.contains(",") && set.add(element)) {
                newList.add(element);
            }
        }
        return newList;
    }

    public static void main(String[] args) {
        Spider.create(new OschinaBlogPageProcessor())
                .addUrl("https://www.comc.com/Cards/Basketball")
                .thread(1)
                .addPipeline(new ConsolePipeline())
                .run();
    }
}