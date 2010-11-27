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
	
	
	public String getText() {
		ByteArrayInputStream ioStream = new ByteArrayInputStream(data);
		
        if ("docx".equals(extension)) {
            try {

                XWPFDocument doc = new XWPFDocument(ioStream);

                XWPFWordExtractor extractor = new XWPFWordExtractor(doc);
				
				ioStream.close();
                return extractor.getText();
            } catch (Exception ioErr) {
				System.out.println(ioErr);
				return null;
            }
        } else if ("doc".equals(extension)) {
            try {
                POIFSFileSystem fs = new POIFSFileSystem(ioStream);

                HWPFDocument doc = new HWPFDocument(fs);

                WordExtractor extractor = new WordExtractor(doc);
				
				ioStream.close();
               	return extractor.getText();
            } catch (Exception ioErr2) {
				System.out.println(ioErr2);
                return null;
            }
        }
		try {
			ioStream.close();
		} catch (IOException ioErr) {
			System.out.println(ioErr);
		}
        return null;
	}
}