import java.io.*;
import org.jsoup.*;
import java.util.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.lang.Exception;

public class WebCrawler {
    public static int counter=0;
    public LinkedList<String> q = new LinkedList<String>();
    public HashMap<String,Boolean> visited=new HashMap<String,Boolean>();

    public void DownloadPage(String url) throws Exception
    {
        System.out.println(counter);
        try {
            Document doc=Jsoup.connect(url).get();
            org.jsoup.select.Elements links =doc.select("a");
            BufferedWriter writer = new BufferedWriter(new FileWriter("pages/"+counter+".html"));
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
    public void Crawler() throws Exception {
        String first_url="https://jsoup.org";

        q.add(first_url);
        this.DownloadPage(first_url);
        visited.put(first_url,true);
        counter++;
        while (q.size()!=0)
        {
            String url=q.poll();
            if (!visited.containsKey(url)) {
                System.out.println(url);
                this.DownloadPage(url);
                counter++;
                visited.put(url, true);
            }
            if (counter>=100)
                break;
        }

    }


}
