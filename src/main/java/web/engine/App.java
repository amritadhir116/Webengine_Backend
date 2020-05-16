package web.engine;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import textprocessing.In;
import textprocessing.KMP;
import textprocessing.StdOut;
import textprocessing.TST;

public class App {
	static TST<Integer> websiteTrie;
	static int indexTable[][];
	static ArrayList<Map<String, String>> websitesNames;
	Logger logger = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws IOException {
		port(8083);
		long startTime, endTime;
		double totalTime = 0.0;
		startTime = System.currentTimeMillis();
		createTrie();
		endTime = System.currentTimeMillis();
		totalTime = (endTime - startTime);
		StdOut.println(String.format("Time taken for create is %s millis", totalTime / 100));
		updateUrlsinWebsiteNames();
		options("/*", (request, response) -> {

			String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
			if (accessControlRequestHeaders != null) {
				response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
			}

			String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
			if (accessControlRequestMethod != null) {
				response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
			}

			return "OK";
		});
		before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));
		new SpellChecker();
		registerSearchAPI();
	}

	private static void updateUrlsinWebsiteNames() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader("websites\\weblinks.txt"));
		Map<String, String> tmpMap = new HashMap<>();
		try {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	try {
		    	   String data[] = line.split("=");
			       String fileName = data[0].toString().split("websites/")[1].trim();
			       String url = data[1].toString().trim();
			       tmpMap.put(fileName, url);
		    	}
		    	catch(Exception e) {}
		    }
		    for (Map<String, String> hashMap : websitesNames)
		       {
		           for (Map.Entry<String, String> entry  : hashMap.entrySet())
		           {
		              String key = entry.getKey();
		              try {
		            	  if (tmpMap.get(key) != null) {
			            	  entry.setValue(tmpMap.get(key));
			              }
		              }
		              catch(Exception e) {}
		           }
		       }
		} finally {
		    br.close();
		}
	}

	private static void registerSearchAPI() {
		Gson gson = new Gson();
		get("/search", (req, res) -> {
			Response response = new Response(true, "");
			String word = req.queryParams("key");
			response.setResult(getResult(word));
			return response;
		}, gson::toJson);
	}

	public static void createTrie() throws IOException {
		int wordsInSpellChecker = 10;
		System.out.println("Started creating trie");
		websitesNames = new ArrayList<>();
		In inputFile = new In("input\\SpellChecker.txt");
		websiteTrie = new TST<Integer>();
		String text = inputFile.readAll();
		StringTokenizer tokenizer = new StringTokenizer(text);
		int itr = 0;
		File[] files = new File("websites\\").listFiles();
		indexTable = new int[files.length - 1][wordsInSpellChecker];
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			createIndexTable(token, itr, files);
			websiteTrie.put(token, itr);
			itr++;
		}
		System.out.println("Ended creating trie");
	}

	public static void createIndexTable(String word, int index, File[] files) throws IOException {
		int itr = 0;
		for (File file : files) {
			if (file.isFile() && !file.getName().equals("weblinks.txt")) {
				Document doc = Jsoup.parse(file, "UTF-8");
				String text = doc.text();
				int offset = 0;
				int numFound = 0;
				String pat = word;
				boolean found = false;
				boolean error = false;
				do {
					int tmp = 0;
					error = false;
					try {
						KMP kmp1 = new KMP(pat);
						tmp = kmp1.search(text.substring(offset));
					} catch (Exception e) {
						error = true;
					}
					if (tmp < text.substring(offset).length() && !error) {
						found = true;
						numFound++;
					}

					offset = offset + tmp + pat.length();
				} while (offset < text.length());
				indexTable[itr][index] = numFound;
				Map<String, String> map = new HashMap<>();
				map.put(file.getName(), "");
				websitesNames.add(map);
				itr++;
			}
		}
	}

	public static Object getResult(String word) {
		ArrayList<Map<String, Object>> result = new ArrayList<>();
		try {
			int wordIndex = websiteTrie.get(word.toLowerCase());
			for (int i = 0; i < indexTable.length; i++) {
				if (indexTable[i][wordIndex] > 0) {
					Map<String, Object> map = new HashMap<>();
					map.put("name", (String) websitesNames.get(i).keySet().toArray()[0]);
					map.put("url", (String) websitesNames.get(i).values().toArray()[0]);
					map.put("count", indexTable[i][wordIndex]);
					result.add(map);
				}
			}
		} catch (Exception e) {
			System.out.println("No results found.");
		}
		return result;
	}

}
