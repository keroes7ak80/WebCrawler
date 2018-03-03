import java.io.*;
import org.jsoup.*;
import java.util.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.lang.Exception;

public class WebCrawler extends Thread{
    public static int pages_counter=0;
    public LinkedList<String> q = new LinkedList<String>();
    public HashMap<String,Boolean> visited=new HashMap<String,Boolean>();

    public  void DownloadPage(String url)
    {
        System.out.println(pages_counter);
        try {
            Document doc=Jsoup.connect(url).get();
            org.jsoup.select.Elements links =doc.select("a");
            BufferedWriter writer = new BufferedWriter(new FileWriter("pages/"+(pages_counter++)+".html"));
            writer.write(doc.outerHtml());
            writer.close();
            for(Element e :links) {
                q.add(e.attr("abs:href"));
            }
        }
        catch (IOException ioe) {
            System.out.println("Page not found");
        }

    }
    public void run(){
        String first_url="https://jsoup.org";
        q.add(first_url);
        this.DownloadPage(first_url);
        visited.put(first_url,true);

        pages_counter++;
        while (q.size()!=0)
        {
            String url=q.poll();
            System.out.println(url);
            if (!visited.containsKey(url)) {
                this.DownloadPage(url);
                visited.put(url, true);
            }
            if (pages_counter>=100)
                break;
        }


    }

    public void Crawler() throws Exception {
        (new WebCrawler()).start();
        (new WebCrawler()).start();
        (new WebCrawler()).start();
        (new WebCrawler()).start();
        (new WebCrawler()).start();

    }
}
