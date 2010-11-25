package com.dexta.coreservices.models.services;

import com.dexta.coreservices.models.base.DBAbstract;
import com.dexta.tools.Tools;
import com.dexta.tools.StorageWrapper;

import net.spy.memcached.MemcachedClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.util.Calendar;
import com.mongodb.DB;
import org.apache.http.HttpResponse;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

public class AWSS3 extends StorageService {
	InputStream tempStream;
	
	public AWSS3(InputStream ioStream) {
		super();
		tempStream = ioStream;
	}
	
	private void setIndentifier(String name) {
		this.put("reference", name);
	}
	
	public void commit(StorageWrapper wrapper) {
		//save the ioStream to the service
		DB systemDB = wrapper.mongoDatabase;
		MemcachedClient memcacheProvider = wrapper.memcachedServer;
		ServiceProvider amazonS3 = new ServiceProvider("awss3");
		if (amazonS3.find(systemDB)) {
			Random r = new Random();
			StringBuffer randomIdentifier = new StringBuffer(r.nextInt(25000) + ":" + Calendar.getInstance().getTimeInMillis() + ":" + r.nextInt(25000));
			int maxReplace = r.nextInt(20);
			for (int i = 0; i < maxReplace; i++) {
				randomIdentifier.setCharAt(r.nextInt(randomIdentifier.length() - 1), (char) (33 + r.nextInt(93)));
			}
			try {
				String randomIdentifier_hash = Tools.SHA512(randomIdentifier.toString());
				//write the inputStream to a FileOutputStream
		        File tmpWrite = new File("/tmp/aws_save" + randomIdentifier_hash);
		        OutputStream fileOut = new FileOutputStream(tmpWrite);
				ByteArrayOutputStream rawBuffer = new ByteArrayOutputStream();

		        int read=0;
		        byte[] bytes = new byte[1024];

		        while((read = tempStream.read(bytes))!= -1){
		                fileOut.write(bytes, 0, read);
						rawBuffer.write(bytes, 0, read);
		        }
				
				tempStream.close();
				rawBuffer.flush();
				fileOut.flush();
				fileOut.close();
				
				//construct the AWS S3 client
				BasicAWSCredentials awsCred = new BasicAWSCredentials((String) amazonS3.get("consumer_key"), (String) amazonS3.get("consumer_secret"));
	            AmazonS3Client s3Client = new AmazonS3Client(awsCred);
				PutObjectResult putResult = s3Client.putObject((String) amazonS3.get("bucket_name"), randomIdentifier_hash, tmpWrite);
				if (putResult != null) {
					this.setIndentifier(randomIdentifier_hash);
					this.put("created", Calendar.getInstance().getTimeInMillis() / 1000);
					super.commit(systemDB);
				}
				//save in memcached
				if (memcacheProvider != null)
					memcacheProvider.set("aws_" + randomIdentifier_hash, wrapper.MAX_MEMCACHED_STORE_TIME, rawBuffer.toByteArray());
				
			} catch (Exception genErr) {
				System.out.println(genErr);
			}
		}
	}
}