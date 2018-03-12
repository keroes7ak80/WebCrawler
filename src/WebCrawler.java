import java.io.*;
import org.jsoup.*;
import java.util.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.lang.Exception;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class WebCrawler implements Runnable {
    public static int pages_counter = 0;
    public static LinkedList<String> q = new LinkedList<String>();
    public static HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
    public static String regex = "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)";

    private static boolean IsMatch(String s, String pattern) {
        try {
            Pattern patt = Pattern.compile(pattern);
            Matcher matcher = patt.matcher(s);
            return matcher.matches();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public void DownloadPage(String url) {
        if (url != null) {
            System.out.println(pages_counter);
            System.out.println(url);
            try {
                Document doc = Jsoup.connect(url).get();
                org.jsoup.select.Elements links = doc.select("a");
                BufferedWriter writer = new BufferedWriter(new FileWriter("pages/" + (pages_counter++) + ".html"));
                writer.write(doc.outerHtml());
                writer.close();
                visited.put(url, true);
                for (Element e : links) {
                    String href = e.attr("abs:href");
                    if (!href.isEmpty())
                        if (href.charAt(href.length() - 1) == '/'||href.charAt(href.length() - 1) == '#')
                            href = href.substring(0, href.length() - 1);
                    if (!q.contains(href) && IsMatch(href, regex) && !visited.containsKey(href))
                        q.add(href);
                }
            } catch (IOException ioe) {
                System.out.println("Page not found");
            }

        }
    }
    public void run() {
        while (q.size() != 0) {
            String url;
            synchronized (q) {
                url = q.poll();
                if (!visited.containsKey(url)) {
                    this.DownloadPage(url);
                }
                if (pages_counter >= 100)
                    break;
            }
        }

    }
    public void Crawler() throws Exception {
        //feeding Crawler
        File file = new File("seeds.txt");
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            q.add(line);
        }
        fileReader.close();
        //Running Threads
        new Thread((new WebCrawler())).start();
        new Thread((new WebCrawler())).start();
        new Thread((new WebCrawler())).start();
        new Thread((new WebCrawler())).start();
        new Thread((new WebCrawler())).start();

    }
}
