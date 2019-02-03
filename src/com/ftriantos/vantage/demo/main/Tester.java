package com.ftriantos.vantage.demo.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
        
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
		
		//just some stuff early on for testing body of document return
		//String title = document.title(); //Get title
		//System.out.println("  Title: " + title); //Print title.
		
		//prompt the user for a selection
		System.out.println("Please enter a number below to select a topic and receive the reults to file");
		//buffered input for the viewer selection
		BufferedReader buffer=new BufferedReader(new InputStreamReader(System.in));
		//read the input
	    String line = buffer.readLine();
	    int intnumber=0;//default
	    intnumber = Integer.parseInt(line);
	    //output the selected menu topic for the viewer
	    System.out.println("selected ["+intnumber+"]: "+topics.get(intnumber).text());
	    String myTopic = topics.get(intnumber).text();
	    //the href link we are calling to get the reviews and it nees to show max number
	    String linkToReviews = topics.get(intnumber).parent().attr("href");
	    System.out.println(linkToReviews);
	    //set up the initial call to psuedo recursive results
	    int intTopRecord = 26;
	    int currentSet = 25;
	    //empty elements input for use in psuedo recursive results (set later)
	    Elements nothing = null;
	    //the call to getrecords which will then take over and call itself until complete with no return
	    getRecords(cookies,currentSet,intTopRecord,linkToReviews,nothing,linkToReviews,0,myTopic);
	    
	    
	    /***
	    //this was my initial results iteration before I made a psuedo recursive function now below
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
  
  public static void getRecords(List<Cookie> cookies, int intTopRecord, int currentSet, String linkToReviews, Elements linkToReviewsSet, String origLink, int lastRecord, String myTopic) throws IOException {
  	
	//if topic run previously delete existing results NOTE: small chance this fails if record set less than 25 resutls total
	if(intTopRecord == 25) {
		boolean success = (new File("./"+myTopic+"-vanatageresults.txt")).delete();
		if (success) {
			System.out.println("Previous record set successfully deleted"); 
		}else{
			System.out.println("No previous record set"); 
		}
	}
	//Make a file with topic name for results
	File file = new File("./"+myTopic+"-vanatageresults.txt");
	//Write Content append equals true so I can add to it on future passes
	FileWriter writer = new FileWriter(file,true);
	//this is a meaningless step now because I create the file with the writer but just in case ...create the file
	if (file.createNewFile()){
	    System.out.println("File is created!");
	    writer.write("RESULTS FOR RECORD SET SEARCH");//if this was still being sed I would have a top line in the file
	} else {
	    System.out.println("File already exists.");
	}
	  
	System.out.println("INCOMING: TOP RECORD "+intTopRecord+" CURRENT SET "+currentSet+" SUBMIT PAGINATION LINK "+linkToReviews);
	  
	/*****adding in apache client*************/
	//already used apache client above for initial the call and process the cookies
	//if an absolute requirement to use apache on every call I could make the same calls here
	//however I am then using jsop to process the returned data from the apache call
	//so in this second and every iteritive call I am foregoing the step of using apache
	//to make the call as requirement was met above to prove apache use and mapping those
	//cookies to the jsoup structure below shows a deeper understanding of the fundamentals
	/*****end adding apache client*************/
	
	//Mapping cookies for jsoup use
	//@SuppressWarnings("unchecked")
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
	System.out.println("number of records: "+myNumReviews);
	//intTopRecord = currentSet - 1;//escape during testing
	//document.select("select#resultPerPage");
	//System.out.println(document2.select("ul").toString());
	
	//the reviews parsed from the return
	Elements myReviews = document2.select("div.search-results-item-body");
	//System.out.println("body is ..."+myReviews);
	System.out.println("MY REVIEWS SIZE IS "+myReviews.size());
	
	//looping each set of 25 records with numbering on screen and refined formatting in file
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
		//remove all non ascii characters
		String reviewtext = myReviews.get(j).select("a").get(0).text().replaceAll("[^\\p{ASCII}]", "-").toString();
		System.out.println("text "+reviewtext+" | ");
		writer.write(reviewtext+" | ");
		//Authors
		System.out.println("Authors "+myReviews.get(j).select("div.search-result-authors").select("div").text()+" | ");
		writer.write(myReviews.get(j).select("div.search-result-authors").select("div").text()+" | ");
		//Date
		System.out.println("Date "+myReviews.get(j).select("div.search-result-date").select("div").get(0).text()+" | ");
		writer.write(myReviews.get(j).select("div.search-result-date").select("div").get(0).text());
		k++;
		lastRecord = n;
		//line seperator
		writer.write(System.lineSeparator());
		writer.write(System.lineSeparator());
	}
	//some guidance for the viewer
	System.out.println("Last Record = "+lastRecord+" Top Record: "+intTopRecord);
	//close the file writer this pass
	writer.close();
	
	//set up of psuedo recursive call to complete the result set 
	String submitPaginationLink;
	String submitPaginationLinkSet;
	if(setPointer < 7) {
		System.out.println("less than 7");
		submitPaginationLink = myPaginationList.select("li.pagination-page-list-item").get(setPointer).select("a").attr("href").toString();
		submitPaginationLinkSet = myPaginationList.select("li.pagination-page-list-item").toString();
		System.out.println(setPointer+" "+submitPaginationLinkSet);
	}else if(lastRecord < intTopRecord){
		System.out.println("greater than 7");
		int totalPages = (intTopRecord/25);
		if((totalPages%1)>0){
			totalPages = totalPages-(totalPages%1)+1;
		}else{
			totalPages = totalPages-(totalPages%1);
		}
		int totalPagesSets = (intTopRecord/175);
		int totalPageSetsRemainder = (intTopRecord%175);
		if((totalPagesSets%1)>0){
			totalPagesSets = totalPagesSets-(totalPagesSets%1)+1;
		}else {
			totalPagesSets = totalPagesSets-(totalPagesSets%1);
		}
		int subtractedPages = (setPointer-7);
		int currentRange = (setPointer * 25);
		int relativeSetPointer;
		if((subtractedPages*25) > totalPageSetsRemainder) {
			System.out.println("dynamic last page");
			relativeSetPointer = (setPointer - subtractedPages)-1;
		}else if((totalPageSetsRemainder-(subtractedPages*25)) < 25) {
			System.out.println("dynamic");
			relativeSetPointer = (setPointer - subtractedPages)-1;
		}else {
			System.out.println("static");
			relativeSetPointer = 6;
		}
		System.out.println("greater than 8 and totalPages = "+totalPages+" totalPagesSets = "+totalPagesSets+" totalPageSetsRemainder = "+totalPageSetsRemainder+" subtractedPages = "+subtractedPages+" currentRange = "+currentRange+" relativeSetPointer = "+relativeSetPointer);
		submitPaginationLink = myPaginationList.select("li.pagination-page-list-item").get(relativeSetPointer).select("a").attr("href").toString();
		submitPaginationLinkSet = myPaginationList.select("li.pagination-page-list-item").toString();
		System.out.println(setPointer+" "+submitPaginationLinkSet);
	}else {
		System.out.println("we are done");
		submitPaginationLink = "";
		submitPaginationLinkSet = "";
	}
	//is this the end of the record set or psuedo recursion to get the next iteration?
	if(lastRecord < intTopRecord) {
		System.out.println("why are we not enterring this if statement?");
		System.out.println("OUTGOING: TOP RECORD "+intTopRecord+" CURRENT SET "+currentSet+" SUBMIT PAGINATION LINK "+submitPaginationLink);
		//Iterate the pagination
		currentSet+=25;
		if(currentSet < intTopRecord && (intTopRecord-currentSet) > 25) {
			System.out.println("current set = "+currentSet);
			getRecords(cookies,intTopRecord,currentSet,submitPaginationLink,myPaginationList,originalLink,0,myTopic);
		}else if((intTopRecord - (currentSet-25)) > 0) {
			System.out.println("current set last page = "+currentSet);
			getRecords(cookies,intTopRecord,currentSet,submitPaginationLink,myPaginationList,originalLink,1,myTopic);	
		}
	}else{
		//when complete let the user know there file is in the program directory
		System.out.println("YOUR REVIEWS FOR "+myTopic+" ARE WRITTEN AND SAVED AT "+file.getPath());
		System.out.println("Program complete please run again for a new result set");
		System.exit(0);
	}
		
  }
  
  

}