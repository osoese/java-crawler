package com.ftriantos.vantage.demo.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
        
public class Tester {

  public static void main(String[] args) throws IOException {
	  
	  //using http client per spec but its easier to just use the jsoup call below
	  String url = "https://www.cochranelibrary.com/cdsr/reviews/topics";

		//HttpClient client = HttpClientBuilder.create().build();
	  	CookieStore cookieStore = new BasicCookieStore();
		HttpClient client = HttpClientBuilder.create()
        .setDefaultCookieStore(cookieStore)
        .build();
		HttpGet request = new HttpGet(url);

		// add request header user agent is mandatory
		request.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
		HttpResponse response = client.execute(request);
		
		
		// get Cookies
		List<Cookie> cookies = cookieStore.getCookies();
		// process...
		System.out.println("Cookies Are: "+cookies);

		System.out.println("Response Code : "+ response.getStatusLine().getStatusCode());

		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line1 = "";
		while ((line1 = rd.readLine()) != null) {
			result.append(line1);
		}
	  
	  System.out.println("THE HTML IS "+result.toString());
	  
	  String doc = result.toString();
    
    Elements topics;
    
    try {
    	/***
    	URL myURL = new URL("https://www.cochranelibrary.com/cdsr/reviews/topics");
    	URLConnection myURLConnection = myURL.openConnection();
        myURLConnection.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(
        		myURLConnection.getInputStream()));
		String inputLine;
		
		while ((inputLine = in.readLine()) != null) 
			System.out.println(inputLine);
		in.close();
		***/
		
		Document document;
		//the easier way to call the source is the function on the next line but using the spec request method instead
		//document = Jsoup.connect("https://www.cochranelibrary.com/cdsr/reviews/topics").get();
		document = Jsoup.parse(doc);
		
		topics = document.select("button.btn-link"); //get topics
		
		//btn-link browse-by-list-item-link
		
		for (int i=0; i < topics.size(); i++) {
			System.out.println("["+i+"]"+"	" + topics.get(i).text());
		}

		//String title = document.title(); //Get title
		//System.out.println("  Title: " + title); //Print title.
		
		
		BufferedReader buffer=new BufferedReader(new InputStreamReader(System.in));
	    String line = buffer.readLine();
	    int intnumber=0;//default
	    intnumber = Integer.parseInt(line);
	    
	    System.out.println("selected ["+intnumber+"]: "+topics.get(intnumber).text());
	    //the href link we are calling to get the reviews and it nees to show max number
	    String linkToReviews = topics.get(intnumber).parent().attr("href");
	    System.out.println(linkToReviews);
	    
	    
	    int intTopRecord = 26;
	    int currentSet = 25;
	    
	    Elements nothing = null;
	    getRecords(cookies,currentSet,intTopRecord,linkToReviews,nothing,linkToReviews,0);
	    
	    /***
	    
	    while(intTopRecord > currentSet) {
	    	
	    	Document document2;
			document2 = Jsoup.connect(linkToReviews).get();
			
			int myNumReviews = Integer.parseInt(document2.select("input.select-all-checkbox").attr("data-total-records"));
			String myNumReviews2 = document2.select("input.select-all-checkbox").toString();
			System.out.println(myNumReviews+" "+myNumReviews2);
			intTopRecord = myNumReviews;
			System.out.println("just displaying nm records "+myNumReviews);
			//intTopRecord = currentSet - 1;//escape during testing
			String myPaginationList = document2.select("ul.pagination-page-list").toString();
			System.out.println(myPaginationList);
			
			//document.select("select#resultPerPage");
			
			String submitPaginationLink = document2.select("ul.pagination-page-list").toString();
			
			Elements myReviews = document2.select("div.search-results-item-body");
		    
			for (int j=0; j < myReviews.size(); j++) {
				System.out.println("\n");
				System.out.println("[ "+j+" ]");
				System.out.println("href "+"https://www.cochranelibrary.com"+myReviews.get(j).select("a").get(0).attr("href")+" | ");
				System.out.println("text "+myReviews.get(j).text()+" | ");
				System.out.println("Authors "+myReviews.get(j).select("div.search-result-authors").select("div").text()+" | ");
				System.out.println("Date "+myReviews.get(j).select("div.search-result-date").select("div").text()+" | ");

			}
	    }
	    
	    ***/
	    
		
    } catch (IOException e) {
    	System.out.println("Connection Failed");
    }
    
    
    
  }
  
  public static void getRecords(List<Cookie> cookies, int intTopRecord, int currentSet, String linkToReviews, Elements linkToReviewsSet, String origLink, int lastRecord) throws IOException {
  	
	File file = new File("./vanatageresults.txt");
	//Write Content
	FileWriter writer = new FileWriter(file,true);
	//Create the file
	if (file.createNewFile())
	{
	    System.out.println("File is created!");
	    writer.write("RESULTS FOR RECORD SET SEARCH");
	} else {
	    System.out.println("File already exists.");
	}
	  
	System.out.println("INCOMING: TOP RECORD "+intTopRecord+" CURRENT SET "+currentSet+" SUBMIT PAGINATION LINK "+linkToReviews);
	  
	/*****adding in apache client*
	String url = linkToReviews;

	HttpClient client2 = HttpClientBuilder.create().build();
	HttpGet request = new HttpGet(url);

	// add request header user agent is mandatory
	request.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
	HttpResponse response = client2.execute(request);

	System.out.println("Response Code : " 
                + response.getStatusLine().getStatusCode());

	BufferedReader rd = new BufferedReader(
		new InputStreamReader(response.getEntity().getContent()));

	StringBuffer result = new StringBuffer();
	String line1 = "";
	while ((line1 = rd.readLine()) != null) {
		result.append(line1);
	}
  
	System.out.println("THE HTML IS "+result.toString());
  
	String doc3 = result.toString();
	System.out.println("-------------------<<><><><><><>><>----------------------------");
	/*****end adding apache client*************/
	
	//Mapping cookies for jsoup use
	@SuppressWarnings("unchecked")
	Map<String, String> cookies2 = new HashMap<>();
	for(Cookie ck : cookies) {
		cookies2.put(ck.getName(),ck.getValue());
	}
  	Document document2;
  	
  	document2 = Jsoup.connect(linkToReviews).cookies(cookies2).get();
  	
	int myNumReviews;
	String myNumReviews2 = "";
	Elements myPaginationList;
	String originalLink;
	int setPointer = 1;
	
	if(intTopRecord == 25) {
		myNumReviews = Integer.parseInt(document2.select("input.select-all-checkbox").attr("data-total-records"));
		myNumReviews2 = document2.select("input.select-all-checkbox").toString();
		myPaginationList = document2.select("ul.pagination-page-list");
		//System.out.println(myPaginationList.toString());
		originalLink = linkToReviews;
	}else{
		myNumReviews = intTopRecord;
		myNumReviews2 = document2.select("input.select-all-checkbox").toString();
		myPaginationList = document2.select("ul.pagination-page-list");
		//myPaginationList = linkToReviewsSet;
		//System.out.println(myPaginationList.toString());
		setPointer = ((currentSet-1)/25);
		System.out.println("set pointer is "+setPointer);
		originalLink = origLink;
	}
	
	System.out.println(myNumReviews+" "+myNumReviews2);
	intTopRecord = myNumReviews;
	System.out.println("just displaying nm records "+myNumReviews);
	//intTopRecord = currentSet - 1;//escape during testing
	
	
	//document.select("select#resultPerPage");
	
	//System.out.println(document2.select("ul").toString());
	
	
	
	Elements myReviews = document2.select("div.search-results-item-body");
	
	//System.out.println("body is ..."+myReviews);
	int k = (currentSet-26);
	for (int j=0; j < myReviews.size(); j++) {
		int m = j+1;
		int n = k+1;
		//output for screen
		System.out.println("\n");
		System.out.println("[ "+m+" <> "+n+" <> "+intTopRecord+" ]");
		//link to review
		System.out.println("href "+"https://www.cochranelibrary.com"+myReviews.get(j).select("a").get(0).attr("href")+" | ");
		writer.write("https://www.cochranelibrary.com"+myReviews.get(j).select("a").get(0).attr("href")+" | ");
		//Should only be the title
		System.out.println("text "+myReviews.get(j).select("a").get(0).text()+" | ");
		writer.write(myReviews.get(j).select("a").get(0).text()+" | ");
		//Authors
		System.out.println("Authors "+myReviews.get(j).select("div.search-result-authors").select("div").text()+" | ");
		writer.write(myReviews.get(j).select("div.search-result-authors").select("div").text()+" | ");
		//Date
		System.out.println("Date "+myReviews.get(j).select("div.search-result-date").select("div").text()+" | ");
		writer.write(myReviews.get(j).select("div.search-result-date").select("div").text());
		k++;
		lastRecord = n;
		//line seperator
		writer.write(System.lineSeparator());
		writer.write(System.lineSeparator());
	}
	System.out.println("Last Record = "+lastRecord+" Top Record: "+intTopRecord);
	writer.close();
	
	String submitPaginationLink;
	String submitPaginationLinkSet;
	
	if(lastRecord < intTopRecord) {
		submitPaginationLink = myPaginationList.select("li.pagination-page-list-item").get(setPointer).select("a").attr("href").toString();
		submitPaginationLinkSet = myPaginationList.select("li.pagination-page-list-item").toString();
	}else{
		submitPaginationLink = "";
		submitPaginationLinkSet = "";
	}
	
	if(lastRecord < intTopRecord) {
		System.out.println("OUTGOING: TOP RECORD "+intTopRecord+" CURRENT SET "+currentSet+" SUBMIT PAGINATION LINK "+submitPaginationLink);
		
		currentSet+=25;
		if(currentSet < intTopRecord && (intTopRecord-currentSet) > 25) {
			System.out.println("current set = "+currentSet);
			getRecords(cookies,intTopRecord,currentSet,submitPaginationLink,myPaginationList,originalLink,0);
		}else if((intTopRecord - (currentSet-25)) > 0) {
			System.out.println("current set last page = "+currentSet);
			getRecords(cookies,intTopRecord,currentSet,submitPaginationLink,myPaginationList,originalLink,1);	
		}
	}else{
		System.out.println("YOUR FILE IS WRITTEN AND SAVED");
	}
		
  }
  
  

}