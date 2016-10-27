package com.daemonservices;

import java.io.IOException;
import java.util.Properties;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;

import com.models.Tweet;
import com.tasks.FetchTweetsTask;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.mapping.PutMapping;


public class ElasticSearchHose {
	private static final Properties awsCredentialsFile = TweetMapExecutor.
			getPropertiesFile("AwsCredentials.properties");
	
	public void indexTweet(Tweet t) {
		JestClient esClient = TweetMapExecutor.getESClient();
		Index index = new Index.Builder(t).index(awsCredentialsFile.getProperty("index-name")).
				type(awsCredentialsFile.getProperty("mapping-name")).build();
		  System.out.println("Indexing twettttt---------------");
                  System.out.println(t.getTweet());
		try {
			esClient.execute(index);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
