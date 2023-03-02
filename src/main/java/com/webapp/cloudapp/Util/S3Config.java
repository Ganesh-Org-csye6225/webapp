package com.webapp.cloudapp.Util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
public class S3Config {
	// private String region = "us-east-1";

	@Bean
	public AmazonS3 amazonS3Client() {
		// AWSCredentials credentials = new BasicAWSCredentials("AKIARLZIICNXMPN5ER5Y", "T5eBxj6Xi3abnY0ZkKjkdILmDi4/kkHTgJyxB7xt");
        return AmazonS3ClientBuilder.standard().build();
                // .withCredentials(new AWSStaticCredentialsProvider(credentials))
                // .withRegion(region).build();
	}
}
