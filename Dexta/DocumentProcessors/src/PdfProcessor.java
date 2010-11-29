package com.dexta.processors;

import com.dexta.coreservices.models.documents.Processor;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.io.StringWriter;

import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;

public class PdfProcessor implements Processor {
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
        PDDocument pDocument = PDDocument.load(ioStream);

        StringWriter stringWriter = new StringWriter();

        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(1);
        stripper.setEndPage(Integer.MAX_VALUE);
        stripper.writeText(pDocument, stringWriter);

        pDocument.close();

        return stringWriter.toString();
	}
}