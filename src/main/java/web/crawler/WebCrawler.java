package web.crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {
	
	public static String getDomainName(String url) throws URISyntaxException {
		try {
			URI uri = new URI(url);
		    String domain = uri.getHost();
		    return domain.startsWith("www.") ? domain.substring(4) : domain;
		}
	    catch(Exception e) {return "error";}
	}
	
	public static void crawlAndSave(String url, ArrayList<String> visitedLinks, PrintWriter webLinksStream) throws IOException, URISyntaxException {
		if (visitedLinks.size() > 500) {
			return;
		}
		// save to folder
		String folderName = "websites";
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
			String text = doc.html();
			String outputFileName = folderName + "/" + doc.title().replaceAll(" ","") + ".html";
			PrintWriter outputStream = new PrintWriter(new FileWriter(outputFileName));
			System.out.println("Created " + outputFileName);
			visitedLinks.add(getDomainName(url));
			webLinksStream.println(outputFileName + " = " + url);
			outputStream.print(text);
			outputStream.close();
		}
		catch(Exception e) {
			return;
		}
		Elements links = doc.select("a[href]");
		for (Element link : links) {
			String childUrl = link.attr("href");
			if (!getDomainName(childUrl).equals("error") && !visitedLinks.contains(getDomainName(childUrl)) && !childUrl.toLowerCase().contains("google")) {
				crawlAndSave(childUrl, visitedLinks, webLinksStream);
			}
		}
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		File outputDir = new File("websites");
		outputDir.mkdir();
		PrintWriter outputStream = new PrintWriter(new FileWriter("websites/weblinks.txt"));
		String feeder = "https://www.google.com/search?q=programming";
		ArrayList<String> visitedLinks = new ArrayList<String>();
		crawlAndSave(feeder, visitedLinks, outputStream);
		outputStream.close();
	}

}
