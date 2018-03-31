public class MainCrawler {
    public static void main(String args[]) throws Exception{
        WebCrawler wc=new WebCrawler(20);
        System.out.println("Crawling.......");
        wc.Crawler();
    }
}
