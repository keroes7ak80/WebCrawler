import java.io.*;
import org.jsoup.*;
import java.util.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.lang.Exception;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URL;

public class WebCrawler  implements Runnable  {
    public static int pages_counter = 0;
    public static LinkedList<String> q = new LinkedList<String>();
    public static HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
    public static HashMap<String, List<String>> disallowed = new HashMap<String, List<String>>();
    public static String regex = "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)";

    public static String getDomainName(String url) throws Exception {
        URL uri = new URL(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : uri.getProtocol()+"://"+domain;
    }

    private static boolean IsAllowed(String url) throws Exception {

        try {
            if(disallowed.containsKey(getDomainName(url))) {
                for (int i = 0; i < disallowed.get(getDomainName(url)).size(); i++) {
                    String disallowedURL=disallowed.get(getDomainName(url)).get(i);
                    String urlRegex=generateRegex (disallowedURL);
                    if(url.contains(disallowedURL)||IsMatch(url,urlRegex))
                        return false;
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            return false;
        }

        return true;

    }

    private static boolean IsMatch(String s, String pattern) {
        try {
            Pattern patt = Pattern.compile(pattern);
            Matcher matcher = patt.matcher(s);
            return matcher.matches();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public void DownloadPage(String url) throws Exception {
        if (url != null) {
            System.out.println(pages_counter);
            System.out.println(url);
            addDisallowedURL(url);
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
                    if (!q.contains(href) && IsMatch(href, regex) && !visited.containsKey(href)&&IsAllowed(href))
                        q.add(href);
                }
            } catch (IOException ioe) {
                System.out.println("Page not found");
            }

        }
    }
    public void run()  {
        try {
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
        catch (Exception e){
            System.out.println(e);
        }

    }

    public void addDisallowedURL(String url) throws Exception{

        String domainName=getDomainName(url);
        List<String> Disallowed=new ArrayList<String>();
        if(disallowed.containsKey(domainName))
        {
            return;
        }
        else
        {
            try(BufferedReader in = new BufferedReader(
                    new InputStreamReader(new URL(domainName+"/robots.txt").openStream()))) {
                boolean mykey=false;
                String line = null;
                while((line = in.readLine()) != null) {
                    String[] words = line.split(" ");
                    if (mykey)
                    {
                        if(words[0].equals("Disallow:"))
                        {
                            if (words[1]!="")
                                Disallowed.add(domainName+words[1]);
                        }
                    }
                    if (line=="")
                        mykey=false;
                    if(words[0].equals("User-agent:")&&words[1].equals("*"))
                        mykey=true;

                }
            } catch (IOException e) {
//                e.printStackTrace();
            }

            if(Disallowed.size()!=0)
                disallowed.put(domainName,Disallowed);
        }

    }
    public void feedCrawler() throws Exception {
        File file = new File("seeds.txt");
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            q.add(line);
        }
        fileReader.close();
    }
    public static String generateRegex(String url){
        String regex=url;
        regex = regex.replace("\\", "\\\\");
        regex = regex.replace(".", "\\.");
        regex = regex.replace("*", ".*");
        regex = regex.replace("?", "\\?");
        regex = regex.replace("^", "\\^");
        regex = regex.replace("$", "\\$");
        regex = regex.replace("|", "\\|");
        regex = regex.replace("+", "\\+");
        regex = regex.replace("(", "\\(");
        regex = regex.replace(")", "\\)");
        regex = regex.replace("[", "\\[");
        regex = regex.replace("]", "\\]");
        regex = regex.replace("{", "\\{");
        regex = regex.replace("}", "\\}");
        return regex;
    }
    public void Crawler() throws Exception {
////        List<String> l = Arrays.asList("https://github.com/*/features");
////        disallowed.put("https://github.com",l);
////        System.out.println(IsAllowed("https://github.com/kero/features"));
//        String url="https://github.com/*/features#/*";
//        System.out.println(IsMatch("https://github.com/kero/features#/kero",generateRegex(url)));
        feedCrawler();
        //Running Threads
        new Thread((new WebCrawler())).start();
        new Thread((new WebCrawler())).start();
        new Thread((new WebCrawler())).start();
        new Thread((new WebCrawler())).start();
        new Thread((new WebCrawler())).start();
//        addDisallowedURL("https://github.com/features");
    }
}
