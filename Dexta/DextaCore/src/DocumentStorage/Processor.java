package com.dexta.coreservices.models.documents;

import java.io.InputStream;

public interface Processor {	
	public void setData(byte[] dataArray);
	public void setFileExtension(String fileName);
	public String getText() throws Exception;
}