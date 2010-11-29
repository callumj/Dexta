package com.dexta.processors;

import com.dexta.coreservices.models.documents.Processor;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TxtProcessor implements Processor {
	byte[] data;
	String extension;
	
	public void setFileExtension(String ext) {
		extension = ext;
	}
	
	public void setData(byte[] array) {
		data = array;
	}
	
	
	public String getText() throws Exception {
		ByteArrayInputStream ioStream = new ByteArrayInputStream(data);
		
		StringBuilder contents = new StringBuilder();

		BufferedReader input =  new BufferedReader(new InputStreamReader(ioStream));
		String line = null;

		while (( line = input.readLine()) != null){
		  contents.append(line);
		  contents.append(System.getProperty("line.separator"));
		}

		return contents.toString();
	}
}