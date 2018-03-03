import java.io.*;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WebCrawler {

    public static void main(String args[]) {

        try {
            Document doc=Jsoup.connect("https://jsoup.org/bugs").get();
            org.jsoup.select.Elements links =doc.select("a");
            for(Element e :links) {
                System.out.println(e.attr("abs:href"));
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
