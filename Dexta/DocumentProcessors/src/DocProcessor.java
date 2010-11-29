package com.dexta.processors;

import com.dexta.coreservices.models.documents.Processor;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class DocProcessor implements Processor {
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
		
        if ("docx".equals(extension)) {
			XWPFDocument doc = new XWPFDocument(ioStream);

			XWPFWordExtractor extractor = new XWPFWordExtractor(doc);

			ioStream.close();
			return extractor.getText();

        } else if ("doc".equals(extension)) {
			POIFSFileSystem fs = new POIFSFileSystem(ioStream);

			HWPFDocument doc = new HWPFDocument(fs);

			WordExtractor extractor = new WordExtractor(doc);

			ioStream.close();
			return extractor.getText();
        }

		ioStream.close();
        return null;
	}
}