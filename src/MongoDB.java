import java.util.*;
import java.io.*;
import com.mongodb.*;
import com.mongodb.client.model.DBCollectionUpdateOptions;
import org.jsoup.nodes.Document;
//import com.mongodb.client.model.UpdateOptions;
import org.tartarus.snowball.ext.englishStemmer;

public class MongoDB {
	MongoClient mongoClient;
	DB database;
	DBCollection byWord;
	DBCollection byUrl;
	englishStemmer stemmer;
	public MongoDB() {// ok
		mongoClient = new MongoClient( "localhost" , 27017 );
		database = mongoClient.getDB("mydb");
		byWord = database.getCollection("byWord");
		byUrl = database.getCollection("byUrl");
		stemmer = new englishStemmer();
		
	}
	public void clear() {// ok
		byWord.drop();
		byUrl.drop();
	}
/*	public void addRandomData() {
		word a = new word();
		a.url="facebook";
		a.importantWords.add("friend");a.importantWords.add("like");a.importantWords.add("photo");
		a.tags.add("p");a.tags.add("h");a.tags.add("t");
		word b = new word();
		b.url="twitter";
		b.importantWords.add("tweet");b.importantWords.add("follower");b.importantWords.add("retweet");
		b.tags.add("t");b.tags.add("p");b.tags.add("h");
		word c = new word();
		c.url="instagram";
		c.importantWords.add("follower");c.importantWords.add("photo");c.importantWords.add("like");
		c.tags.add("h");c.tags.add("t");c.tags.add("p");
		addUrl(a);
		addUrl(b);
		addUrl(c);
	}*/
	public void buildIndexes() {// ok
		DBObject criteria1 = new BasicDBObject("word",1);
		byWord.createIndex(criteria1);

		DBObject criteria2 = new BasicDBObject("url",1);
		byUrl.createIndex(criteria2);
	}
	public boolean contains(String url) {
		if(byUrl.findOne(url)!=null)
			return true;
		return false;
	}
	public String getStem(String word) {
		stemmer.setCurrent(word);
		stemmer.stem();
		return stemmer.getCurrent();
	}
	public void remove(String url) {
		DBObject result=byUrl.findOne(new BasicDBObject("url",url));
		if(result==null)return;
		//System.out.println("HERE");
		BasicDBList list =(BasicDBList) result.get("list");
		for(Object ob : list) {
				String word=(String)(ob);
				String stem=getStem(word);
			//	System.out.println(word);
			//	System.out.println(stem);
				DBObject findQuery = new BasicDBObject("stem",stem).append("list.word",word);
				DBObject updateQuery = new BasicDBObject("$pull",new BasicDBObject("list.$.urls",
						new BasicDBObject("url",url)));
				byWord.update(findQuery, updateQuery);
				byWord.update(new BasicDBObject("stem",stem),new BasicDBObject("$inc", new BasicDBObject("cnt", -1)));
		}
		byUrl.remove(new BasicDBObject("url",url));
	}
	public void addUrl(word doc) { // add url or update it if it already exists -- removes old version and adds new one
		remove(doc.URL);// removes old version of document if this url already exists
		HashMap<String,Integer> M=new HashMap<String,Integer>();
		ArrayList<ArrayList<Integer>> indices = new ArrayList<>();
		ArrayList<Integer> titleCount = new ArrayList<>();
		ArrayList<Integer> headerCount = new ArrayList<>();
		ArrayList<Integer> bodyCount = new ArrayList<>();
		ArrayList<String> uniqueWords = new ArrayList<>();
		ArrayList<String> stems = new ArrayList<>();
		int sz=0;
		for(int i=0;i<doc.importantWords.size();i++) {
			if(!M.containsKey(doc.importantWords.get(i))) {
				M.put(doc.importantWords.get(i),sz);
				indices.add(new ArrayList<>());
				titleCount.add(0);
				headerCount.add(0);
				bodyCount.add(0);
				uniqueWords.add(doc.importantWords.get(i));
				stems.add(getStem(doc.importantWords.get(i)));
				sz++;
			}
			int idx=M.get(doc.importantWords.get(i));
			if(doc.priorityClass.get(i)==1)titleCount.set(idx, titleCount.get(idx)+1);      // assumes (t/h/body)
			else if(doc.priorityClass.get(i)==2)headerCount.set(idx,headerCount.get(idx)+1);
			else bodyCount.set(idx,bodyCount.get(idx)+1);
		}

		for(int i=0;i<doc.importantWordsOrder.size();i++) {
//			System.out.println(doc.importantWordsOrder.get(i));
//			System.out.println(i);
			if(M.get(doc.importantWordsOrder.get(i))!=null) {
				int idx = M.get(doc.importantWordsOrder.get(i));
				indices.get(idx).add(doc.position.get(i));
			}
		}
		
		ArrayList<String> mainList= new ArrayList<>();
		for(int i=0;i<sz;i++) {
			mainList.add(uniqueWords.get(i));
			addWord(uniqueWords.get(i),stems.get(i),doc.URL,titleCount.get(i),headerCount.get(i),bodyCount.get(i),indices.get(i));
		}
		DBObject mainObject = new BasicDBObject("url",doc.URL).append("list",mainList);
		byUrl.insert(mainObject);
		
	}
	public void addWord(String word,String stem,String url,int titleCount, int headerCount,int bodyCount,ArrayList<Integer> indices) {// ok
		//db.words.update({"name":"omar"},{$push:{"list":"cc"}},{upsert:1})
		DBObject findQuery = new BasicDBObject("stem",stem).append("list.word",word);
		DBObject innerObject = new BasicDBObject("url",url).append("titleCount",titleCount).append("headerCount", headerCount)
				.append("bodyCount", bodyCount).append("indices", indices);
		if(byWord.findOne(findQuery)!=null) {
			DBObject updateQuery = new BasicDBObject("$push",new BasicDBObject("list.$.urls",innerObject));
			byWord.update(findQuery,updateQuery);
		}
		//
		else {
			ArrayList<DBObject> innerlist=new ArrayList<>();
			innerlist.add(innerObject);
			DBObject myObj= new BasicDBObject("word",word).append("urls",innerlist);
			DBObject updateQuery = new BasicDBObject("$push",new BasicDBObject("list",myObj));
			DBCollectionUpdateOptions options= new DBCollectionUpdateOptions().upsert(true);
			byWord.update(new BasicDBObject("stem",stem),updateQuery,options);
		}
		byWord.update(new BasicDBObject("stem",stem),new BasicDBObject("$inc", new BasicDBObject("cnt", 1)));
	}
	public void getUrls(String word) {
		DBObject Query=new BasicDBObject("stem",word);
		DBObject result= byWord.findOne(Query);
		if(result!=null)
			System.out.println(result.get("list"));
	}
	public void getWords(String url) {
		DBObject Query=new BasicDBObject("url",url);
		DBObject result= byUrl.findOne(Query);
		if(result!=null)
			System.out.println(result.get("list"));
	}

}