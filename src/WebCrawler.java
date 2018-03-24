import java.io.*;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import org.jsoup.*;
import java.util.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.lang.Exception;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URL;


public class WebCrawler  extends Thread  {
    private static MongoClient mc = new MongoClient("localhost", 27017);
    private static DB db = mc.getDB("mydb");
    private static DBCollection visited_table = db.getCollection("visited");
    private static DBCollection queue_table = db.getCollection("queue");
    public  static int pages_counter = 0;
    public static LinkedList<String> q = new LinkedList<String>();
    public static HashMap<String, Boolean> visited = new HashMap<String, Boolean>();
    public static HashMap<String, List<String>> disallowed = new HashMap<String, List<String>>();
    public static HashMap<String, List<String>> allowed = new HashMap<String, List<String>>();
    public static String regex = "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)";

    public static String getDomainName(String url) throws Exception {
        URL uri = new URL(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : uri.getProtocol()+"://"+domain;
    }

    private static boolean IsAllowed(String url) throws Exception {

        try {
            if(allowed.containsKey(getDomainName(url))) {
                for (int i = 0; i < allowed.get(getDomainName(url)).size(); i++) {
                    String allowedURL=allowed.get(getDomainName(url)).get(i);
                    String urlRegex=generateRegex (allowedURL);
                    if(url.contains(allowedURL)||IsMatch(url,urlRegex))
                        return true;
                }
            }

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
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("index.txt", true)));
                    out.append(pages_counter + " " + url + "\n");
                    out.close();
                BasicDBObject obj = new BasicDBObject();
                obj.put("url",url);
                visited_table.insert(obj);
                visited.put(url, true);
                for (Element e : links) {
                    String href = e.attr("abs:href");
                    if (!href.isEmpty())
                        if (href.charAt(href.length() - 1) == '/'||href.charAt(href.length() - 1) == '#')
                            href = href.substring(0, href.length() - 1);
                    if (!q.contains(href) && IsMatch(href, regex) && !visited.containsKey(href)&&IsAllowed(href)&&!(href.contains("#")))
                        q.add(href);
                }
            } catch (IOException ioe) {
                System.out.println("Page not found");
            }

        }
    }
    public void run()  {
        try {
//            System.out.println(pages_counter++);
            while (q.size() != 0) {
                String url;
                synchronized (q) {
                    url = q.poll();
                    if (!visited.containsKey(url)) {
                        this.DownloadPage(url);
                    }
                }
                        if (pages_counter >= 100) {
                            for (int i = 0; i < q.size(); i++) {
                                BasicDBObject obj = new BasicDBObject();
                                obj.put("url", q.poll());
                                queue_table.insert(obj);
                            }
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
        List<String> Allowed=new ArrayList<String>();
        if(disallowed.containsKey(domainName)||allowed.containsKey(domainName))
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
                        if(words[0].equals("Allow:"))
                        {
                            if (words[1]!="")
                                Allowed.add(domainName+words[1]);
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

            if(Disallowed.size()!=0){
                disallowed.put(domainName,Disallowed);
                allowed.put(domainName,Allowed);
            }
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
        if(!regex.endsWith(".*")||!regex.endsWith(".*/"))
            regex=regex+".*";
        return regex;
    }
    public void Crawler() throws Exception {
        DBCursor curs_visited = visited_table.find();
        DBCursor curs_queue = queue_table.find();
        for (int i = 0; i < visited_table.count() ; i++) {
            if(curs_visited.hasNext()) {
                DBObject o = curs_visited.next();
                visited.put((String)o.get("url"),true);
            }
        }
        for (int i = 0; i < queue_table.count() ; i++) {
            if(curs_queue.hasNext()) {
                DBObject o = curs_queue.next();
                q.add((String)o.get("url"));
            }
        }
        pages_counter=(int)visited_table.count()+1;
        feedCrawler();
        //Running Threads
        for (int i = 0; i < 50; i++) {
            new Thread((new WebCrawler())).start();
        }


        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run()
            {
                System.out.println("Adding urls to Database");
                for (int i = 0; i < q.size(); i++) {
                    BasicDBObject obj = new BasicDBObject();
                    obj.put("url", q.poll());
                    queue_table.insert(obj);
                }
            }
        });
    }
}
