package com.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.daemonservices.TweetMapExecutor;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.tasks.FetchTweetsTask.Location;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

@Path("/tweets")
public class TweetsResource {
	private static String indexName = TweetMapExecutor.getPropertiesFile("AwsCredentials.properties")
			.getProperty("index-name");
	private static String mappingName = TweetMapExecutor.getPropertiesFile("AwsCredentials.properties")
			.getProperty("mapping-name");
	
	@GET
	@Path("{query_term}/{from}/{size}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Location> getTweets(
			@PathParam("query_term") String queryTerm,
			@PathParam("size") int size,
			@PathParam("from") int from) {
		JestClient client = TweetMapExecutor.getESClient();
		System.out.println("QUERIES--------------------");
                System.out.println(queryTerm);
		System.out.println(from);
		System.out.println(size);
		String query = "{\n" +
	            "    \"query\": {\n" +
	            "                \"query_string\" : {\n" +
	            "                    \"query\" : \" "+ queryTerm +"\",\n" +
	            					  "\"default_field\" : \"tweet\"" + 
	            "                }\n" +
	            "    },\n" +
	            "	 \"from\" : "+ from + ",\n" +
	            "     \"size\" : "+ size + "\n" +
	            "}";

		Search search = new Search.Builder(query)
                // multiple index or types can be added.
                .addIndex(indexName)
                .addType(mappingName)
                .build();
                System.out.println("After search---------------------------");
		SearchResult sr = null;
		try {
			sr = client.execute(search);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(sr != null) {
                         System.out.println("Returning Nul ##################################");
			JsonObject results = sr.getJsonObject();
			return parseJsonToTweet(results);
		}
		return null;
	}

	private List<Location> parseJsonToTweet(JsonObject results) {
		List<Location> allTweets = new ArrayList<Location>();
		if(results != null && !results.isJsonNull() && results.isJsonObject()) {
			JsonObject obj = results.getAsJsonObject("hits");
			JsonArray arr = obj.getAsJsonArray("hits");
			for(int i = 0; i < arr.size(); i++) {
				JsonObject o = arr.get(i).getAsJsonObject();
				JsonObject oo = o.getAsJsonObject("_source");
				Location temp = new Location();
				temp.latitude = oo.getAsJsonPrimitive("latitude").getAsDouble();
				temp.longitude = oo.getAsJsonPrimitive("longitude").getAsDouble();
				allTweets.add(temp);
			}
		}
		return allTweets;
	}
}
