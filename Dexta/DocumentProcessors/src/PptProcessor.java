package com.dexta.processors;

import com.dexta.coreservices.models.documents.Processor;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.poi.hslf.extractor.QuickButCruddyTextExtractor;

public class PptProcessor implements Processor {
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

		QuickButCruddyTextExtractor quickExtractor = new QuickButCruddyTextExtractor(ioStream);
		contents.append(quickExtractor.getTextAsString());

		ioStream.close();
		return contents.toString();
	}
}