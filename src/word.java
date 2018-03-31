import java.util.ArrayList;

public class word {
	// word - stem - tag - tf 
	ArrayList<String> importantWords = new ArrayList<String>();
	ArrayList<Double> tf = new ArrayList<Double>();  // el frequency bta3t kol kelma fe el file
	ArrayList<Integer> priorityClass = new ArrayList<Integer>();  // Title/Heading/Body
	String URL = "";
	
	// word - stem - index
	ArrayList<String> importantWordsOrder = new ArrayList<String>();
	ArrayList<Integer> position = new ArrayList<Integer>();

}
