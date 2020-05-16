package web.engine;

import static spark.Spark.get;
import static spark.Spark.post;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.google.gson.Gson;

import algorithmDesign.Sequences;

public class SpellChecker {
	
	ArrayList<String> wordList;
	SpellChecker() throws IOException {
		loadSpellChecker();
		getSuggestion();
	}

	public void loadSpellChecker() throws IOException {
		this.wordList = new ArrayList<String>();
		// load from text document
		File file = new File("input\\SpellChecker.txt");

		BufferedReader br = new BufferedReader(new FileReader(file));

		String st;
		while ((st = br.readLine()) != null) {
			this.wordList.add(st);
		}
			
		br.close();
	}
	
	public Object getSuggestionsWithEditDist(String word) {
		ArrayList<Map<String, String>> list = new ArrayList<>(); 
		for (int itr = 0; itr < this.wordList.size(); itr++) {
			int dist = Sequences.editDistance(word, this.wordList.get(itr));
			if (dist <= 2 ) {
				Map<String, String> map = new HashMap<>();
				map.put("word", this.wordList.get(itr));
				map.put("distance", String.valueOf(dist));
				list.add(map);
			}
		}
		return list;
	}
	
	public boolean isValidWord(String word) {
		return this.wordList.contains(word);
	}
	
	public void getSuggestion() {
		Gson gson = new Gson();
		// get request
		get("/getsuggestions", (req, res) -> {
			Response response = new Response(false, "");
			String word = req.queryParams("key");
			if (this.isValidWord(word)) {
				response.setSuccess(true);
				response.setResult(word);
			} else {
				// return json with edit distance
				response.setSuccess(true);
				response.setResult(this.getSuggestionsWithEditDist(word));
			}
			return response;
		}, gson::toJson);
	}
	
	private static Map<String, String> toMap(List<NameValuePair> pairs){
        Map<String, String> map = new HashMap<>();
        for(int i=0; i<pairs.size(); i++){
            NameValuePair pair = pairs.get(i);
            System.out.print(pairs.get(i).getValue());
            map.put(pair.getName(), pair.getValue());
        }
        return map;
    }
	
	
	
//	public static void main(String args[]) throws IOException {
//		loadSpellChecker();
//	}
}
