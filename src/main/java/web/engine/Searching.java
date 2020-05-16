package web.engine;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import textprocessing.BoyerMoore;
import textprocessing.KMP;

public class Searching {
	Searching() {
		
	}
	
	public static void letssearch(String word) throws IOException {
		File[] files = new File("websites\\").listFiles();
		for (File file : files) {
			if (file.isFile()) {
				Document doc = Jsoup.parse(file, "UTF-8");
				String text = doc.text();
				int offset = 0;
				int numFound = 0;
				String pat = word;
				boolean found = false;
				do {
					KMP kmp1 = new KMP(pat);
					int tmp = 0;
					try { 
						tmp = kmp1.search(text.substring(offset));
					}
				    catch(Exception e) {}
					if (tmp < text.substring(offset).length()) {
						found = true;
						numFound++;
					}
					offset = offset + tmp + pat.length();
				} while (offset < text.length());
				System.out.println(numFound + " :found in" + file.getName());
			}
	    }
	}
	
	public static void main(String args[]) throws IOException {
		letssearch("computer");
	}
}
