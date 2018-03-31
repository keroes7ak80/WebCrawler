import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tartarus.snowball.ext.englishStemmer;


public class Indexer {
	
	// Array of object - every object represents all info in one document 
	public static List<word> listObjects = new ArrayList<>();
	
	// Array of stop words any word of them will be removed from the document
    public static String[] stopWords = {"it's","a", "as", "able", "about", "above", "according", "accordingly", "across", "actually", "after", "afterwards", "again", "against", "aint", "all", "allow", "allows", "almost", "alone", "along", "already", "also", "although", "always", "am", "among", "amongst", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "aren't", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "both", "brief", "but", "by", "c'mon", "as", "came", "can", "cant", "cannot", "cant", "cause", "causes", "certain", "certainly", "changes", "clearly", "co", "com", "come", "comes", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldn't", "course", "currently", "definitely", "described", "despite", "did", "didn't", "different", "do", "does", "doesn't", "doing", "don't", "done", "down", "downwards", "during", "each", "edu", "e.g.", "eight", "either", "else", "elsewhere", "enough", "entirely", "especially", "et", "etc.", "even", "ever", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "far", "few", "off", "fifth", "first", "five", "followed", "following", "follows", "for", "former", "formerly", "forth", "four", "from", "further", "furthermore", "get", "gets", "getting", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "had", "hadn't", "happens", "hardly", "has", "hasn't", "have", "haven't", "having", "he", "he's", "hello", "help", "hence", "her", "here", "here's", "hereafter", "hereby", "herein", "hereupon", "hers", "herself", "hi", "him", "himself", "his", "hither", "hopefully", "how", "howbeit", "however", "i", "id", "ill", "i'm", "vie", "i.e.", "if", "ignored", "immediate", "in", "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates", "inner", "insofar", "instead", "into", "inward", "is", "isn't", "it", "it'd", "it'll", "its", "its", "itself", "just", "keep", "keeps", "kept", "know", "knows", "known", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "lets", "like", "liked", "likely", "little", "look", "looking", "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean", "meanwhile", "merely", "might", "more", "moreover", "most", "mostly", "much", "must", "my", "myself", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needs", "neither", "never", "nevertheless", "new", "next", "nine", "no", "nobody", "non", "none", "no one", "nor", "normally", "not", "nothing", "novel", "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "ones", "only", "onto", "or", "other", "others", "otherwise", "ought", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "particular", "particularly", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provides", "que", "quite", "qv", "rather", "rd.", "re", "really", "reasonably", "regarding", "regardless", "regards", "relatively", "respectively", "right", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "she", "should", "shouldn't", "since", "six", "so", "some", "somebody", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "to", "take", "taken", "tell", "tends", "the", "than", "thank", "thanks", "thanx", "that", "that's", "that's", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "there's", "thereafter", "thereby", "therefore", "therein", "there's", "thereupon", "these", "they", "they'd", "they'll", "they're", "they've", "think", "third", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "to", "together", "too", "took", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twice", "two", "un", "under", "unfortunately", "unless", "unlikely", "until", "unto", "up", "upon", "us", "use", "used", "useful", "uses", "using", "usually", "value", "various", "very", "via", "viz", "vs", "want", "wants", "was", "wasn't", "way", "we", "wed", "well", "were", "we've", "welcome", "well", "went", "were", "weren't", "what", "what's", "whatever", "when", "whence", "whenever", "where", "where's", "where after", "whereas", "whereby", "wherein", "whereupon", "wherever", "whether", "which", "while", "whither", "who", "who's", "whoever", "whole", "whom", "whose", "why", "will", "willing", "wish", "with", "within", "without", "wont", "wonder", "would", "would", "wouldn't", "yes", "yet", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves", "zero","awesome"};
    public static Set<String> stopWordsSet = new HashSet<String>(Arrays.asList(stopWords));
    
    // function that remove the stop words
	public static void removeStopWords(word w) {
	    ArrayList<String> importantWords = new ArrayList<String>();
		ArrayList<Integer> priorityClass = new ArrayList<Integer>();
	    for(int i =0;i<w.importantWords.size();i++)
	    {
	        String wordCompare = w.importantWords.get(i).toLowerCase();
	        // if the word in the stop words array it will not be added to my important list
	        if(!w.importantWords.get(i).trim().isEmpty()&&!stopWordsSet.contains(wordCompare))
	        {
	        	importantWords.add(wordCompare.replaceAll("'", ""));
	        	// if the word in title its priority will be 1 and if in headings its priority will be 2 other than it will be 3
				priorityClass.add(w.priorityClass.get(i));
	        }
	    }
	    w.importantWords = importantWords;
	    w.priorityClass = priorityClass;
	}
	
	// function get the current position for every word in the document
	public static void countPosition (word w , String line) {
		List<String> mywords = new ArrayList<String>(Arrays.asList(line.split(" ")));
		ArrayList<String> j = new ArrayList<String>();
		for(int i =0;i<mywords.size();i++)
		{
			String pCompare = mywords.get(i).toLowerCase();
		    if(!stopWordsSet.contains(pCompare))
		    {
		    	w.position.add(i);
			    w.importantWordsOrder.add(pCompare);
		    }
		}
	}
	// set the priority of the word
	public static Integer getPriority(String tag) {
       	 if(tag=="title")return 1;
       	 if(tag=="header"||tag=="h1"||tag=="h2"||tag=="h3"||tag=="h4"||tag=="h5"||tag=="h6")return 2;
       	 return 3;
	}
	
	// parse the document from html tags and store text only
	public static void WebScrapper(word w, Path filePath) throws IOException { 
		 String stringData = Files.lines(filePath, StandardCharsets.UTF_8).collect(Collectors.joining("\n"));
		 // remove special char from words
	     String file = stringData.replaceAll("\n", "").replaceAll("[>]", "> ").replaceAll("[<]", " <").replaceAll("[-+.^:,?()&�|� �;@]"," ");
	     Document doc = Jsoup.parse(file);
	     countPosition(w,doc.text());
	     String[] importantWords = null;
	     Elements elements = doc.select("*");
	     for (Element element : elements) {
	        importantWords = element.ownText().split(" ");
	        for (String e : importantWords) {
	        	String newE = e.replaceAll("/", " ").replaceAll("�", " ").replaceAll("�", " ").replaceAll("=", " ").replaceAll("�", " ").replaceAll("�", "'").replaceAll("!", " ").replace("\"", "").replaceAll("�", " ");
	        	w.importantWords.add(newE);
	        	w.priorityClass.add(getPriority(element.tagName()));	
	        }
	      }     
	}
	
	// read all html files from specific directory
	public static String[] listFilesForFolder(final File folder) {
		 ArrayList<String> htmlFiles = new ArrayList<String>();
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	        	htmlFiles.add(fileEntry.getPath());
	        }
	    }
	    String[] files = htmlFiles .toArray(new String[htmlFiles .size()]);
		return files;
	}
	
 
	public void MainIndexer() throws FileNotFoundException, IOException {
		
				MongoDB myDB = new MongoDB();
				myDB.clear();
				myDB.buildIndexes(); // build indices if they are not already built
				final File folder = new File("pages/");
				String[]  files = listFilesForFolder(folder);
				// looping on all files
		        for (int i = 0; i < files.length; i++) {
			       try {
		            Path filePath = Paths.get(files[i]);
		    	    word w = new word();
		            WebScrapper(w,filePath);
		            removeStopWords(w);
		            // store URL
		            w.URL = Files.readAllLines(Paths.get("index.txt")).get(i);
		    	    listObjects.add(w);
		    	    myDB.addUrl(w);

		        } catch (IOException e) {
		            e.printStackTrace();
		                  }
		    }
		        
	}

	// calculate Term Frequency
	public static void calcTF(word w, String term) {
		double result = 0;
		for (String word : w.importantWords) {
			if (term.equalsIgnoreCase(word))
				result++;
			}
		w.tf.add(result / w.importantWords.size());
	}
}
