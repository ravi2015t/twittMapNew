package com.daemonservices;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;

import com.tasks.FetchTweetsTask;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.mapping.PutMapping;

public class TweetMapExecutor implements ServletContextListener {

	private static final Properties awsCredentialsFile = TweetMapExecutor.
			loadPropertiesFile("AwsCredentials.properties");
	private static final Properties twitterConfFile = TweetMapExecutor.
			loadPropertiesFile("TwitterConfig.properties");
	private static final JestClient esClient = TweetMapExecutor.initializeESClient();
	private static final int numOfThreads = 6;
	
	private ExecutorService exeService;
	
	
	public TweetMapExecutor() {
		this.exeService = Executors.newFixedThreadPool(TweetMapExecutor.numOfThreads);
	}

	public static JestClient getESClient() {
		return TweetMapExecutor.esClient;
	}
	
	public static Properties getPropertiesFile(String name) {
		if(name.equals("TwitterConfig.properties")) {
			return TweetMapExecutor.twitterConfFile;
		} else if(name.equals("AwsCredentials.properties")) {
			return TweetMapExecutor.awsCredentialsFile;
		}
		
		return null;
	}

	private static Properties loadPropertiesFile(String propFilePath) {
		Properties prop = new Properties();
		try {
		InputStream is = FetchTweetsTask.class.getClassLoader().
				getResourceAsStream(propFilePath);
			prop.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;
	}

	private static JestClient initializeESClient() {
		// Important to make the client as multi-threaded since mulitple threads attempt to write
		// index to elastic-search
                System.out.println("Initializing Jest Client!!!");
		JestClientFactory factory = new JestClientFactory();
		 factory.setHttpClientConfig(new HttpClientConfig
		                        .Builder(awsCredentialsFile.getProperty("es-endPoint"))
		                        .multiThreaded(true)
		                        .build());
                 System.out.println("Intialization done");
		 return factory.getObject();

	}
	
	private void createESIndex() {
		try {
			Builder settingsBuilder = Settings.settingsBuilder();
			settingsBuilder.put("number_of_shards", 5);
			settingsBuilder.put("number_of_replicas", 1);
			TweetMapExecutor.esClient.execute(new CreateIndex.Builder(awsCredentialsFile.getProperty("index-name")).
					settings(settingsBuilder.build().getAsMap()).build());
	                System.out.println("Creating Indexxx------");	
			// Create the mapping/schema for documents
			PutMapping putMapping = new PutMapping.Builder(
					awsCredentialsFile.getProperty("index-name"),
					awsCredentialsFile.getProperty("mapping-name"),
			        "{ \""+ awsCredentialsFile.getProperty("mapping-name") +"\" : { \"properties\" : "
			        		+ "{ \"tweet\" : {\"type\" : \"string\","
			        + " \"store\" : \"true\", \"null_value\" : \"na\", \"index\" : \"analyzed\"},"
			        + " \"latitude\" : {\"type\" : \"double\","
			        + " \"store\" : \"true\"},"
			        + "\"longitude\" : {\"type\" : \"double\","
			        + " \"store\" : \"true\"} } } }"
			).build();
			
			esClient.execute(putMapping);
		System.out.println("After creating index----------------------");	
		} catch (IOException e) {
			e.printStackTrace();
                        System.out.println("Exception while creating index");
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// Shuts all tasks immediately
		System.out.println("Shutting Down!!");
		this.exeService.shutdownNow();
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// Create the index first
		this.createESIndex();
                System.out.println("After creating index--------------");
		// Get all topics and spawn threads, such that one thread should cover two topics
		String follow = TweetMapExecutor.twitterConfFile.getProperty("follow");
		String[] followList = follow.split(",");
		
		this.exeService.execute(new FetchTweetsTask("sample-client", followList));
		int counter = 0;
		while(counter < followList.length) {
			this.exeService.execute(new FetchTweetsTask("client-" + counter, followList[counter++],
					followList[counter++]));
		}
	}
	
	public static void main(String[] args) {
		TweetMapExecutor e = new TweetMapExecutor();
		e.contextInitialized(null);
	}
}
