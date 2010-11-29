package com.dexta.processors;

import com.dexta.coreservices.models.documents.Processor;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

public class RtfProcessor implements Processor {
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

        RTFEditorKit rtfEditKit = new RTFEditorKit();
        Document doc = rtfEditKit.createDefaultDocument();

        rtfEditKit.read(ioStream, doc, 0);

        contents.append(doc.getText(0, doc.getLength()));

        ioStream.close();

        return contents.toString();
	}
}