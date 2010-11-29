package com.dexta.processors;

import com.dexta.coreservices.models.documents.Processor;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;

public class XlsProcessor implements Processor {
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
		String returnContent = null;
		
		if ("xlsx".equals(extension)) {
            XSSFWorkbook doc = new XSSFWorkbook(ioStream);
            XSSFExcelExtractor extractor = new XSSFExcelExtractor(doc);

            extractor.setFormulasNotResults(true);
            extractor.setIncludeSheetNames(false);
			
            returnContent = extractor.getText();
        } else if ("xls".equals(extension)) {
            POIFSFileSystem fs = new POIFSFileSystem(ioStream);

            HSSFWorkbook hsWorkBook = new HSSFWorkbook(fs);
            ExcelExtractor extractor = new ExcelExtractor(hsWorkBook);

            extractor.setFormulasNotResults(true);
            extractor.setIncludeSheetNames(false);
			
            returnContent = extractor.getText();
        }
		
		ioStream.close();
		return returnContent;
	}
}