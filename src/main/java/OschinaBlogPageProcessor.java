import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.ConsolePipeline;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.*;


public class OschinaBlogPageProcessor implements PageProcessor {

    private final Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setDomain("https://www.comc.com/");

    @Override
    public void process(Page page) {
        //获取当前页
        List<String> links = page.getHtml().links().regex("/Cards/Basketball/.*").all();

        links = removeDuplicateWithOrder(links);
        page.addTargetRequests(links);
        page.putField("title", page.getHtml().$("#img1", "alt"));
        page.putField("img", page.getHtml().$("#img1", "src"));
        page.putField("img-bak", page.getHtml().$("#img1", "data-otherside"));

        //获取下一页
        String nplinks = page.getHtml().$("#ctl00_ContentPlaceHolder1_cmdNext_Bottom", "href").toString();
        page.addTargetRequests(Collections.singletonList(nplinks));

    }

    @Override
    public Site getSite() {
        return site;

    }

    // 删除ArrayList中重复元素，去除带逗号的URL
    public static List removeDuplicateWithOrder(List list) {
        Set set = new HashSet();
        List newList = new ArrayList();
        for (Iterator iter = list.iterator(); iter.hasNext(); ) {
            Object element = iter.next();
            if (set.add(element) && !element.toString().contains(","))

                newList.add(element);
        }
        list.clear();
        list.addAll(newList);
        return list;
    }

    public static void main(String[] args) {
        Spider.create(new OschinaBlogPageProcessor()).addUrl("https://www.comc.com/Cards/Basketball,p1").thread(1)
                .addPipeline(new ConsolePipeline()).run();
    }
}