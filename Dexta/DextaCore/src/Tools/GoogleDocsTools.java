package com.dexta.tools;

import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.media.MediaSource;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.util.ServiceException;

import java.net.MalformedURLException;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

public class GoogleDocsTools {
	
	public static String determineExtension(DocumentListEntry entry) throws Exception {
		if (entry.getType().equals("document"))
			return "doc";
		else if(entry.getType().equals("spreadsheet"))
			return "xls";
		else if (entry.getType().equals("presentation"))
			return "ppt";
		else if (entry.getType().equals("pdf"))
			return "pdf";
		else if (entry.getType().equals("file")) {
			//google docs permits external files, these don't get converted so it gets harder here
			MediaContent content = (MediaContent) entry.getContent();
			String subType = content.getMimeType().getSubType();
			if (subType.indexOf("msword") != -1)
				return "doc";
			else if(subType.indexOf("excel") != -1)
				return "xls";
			else if(subType.indexOf("powerpoint") != -1)
				return "ppt";
			else if(subType.indexOf("pdf") != -1)
				return "pdf";
			else if(subType.indexOf("plain") != -1)
				return "txt";
			else if(subType.indexOf("rtf") != -1)
				return "rtf";
			else if(subType.indexOf("openxmlformats-officedocument.wordprocessingml") != -1) //thanks Microsoft for those lovely names
				return "docx";
			else if(subType.indexOf("openxmlformats-officedocument.presentationml") != -1) //thanks Microsoft for those lovely names
				return "pptx";
			else if(subType.indexOf("openxmlformats-officedocument.spreadsheetml") != -1) //thanks Microsoft for those lovely names
				return "xlsx";
			else
				throw new Exception("type is not supported");

		}

		return "txt"; //worse case
	}

	public static byte[] downloadDocument(DocsService client, DocumentListEntry entry, String format) throws IOException, MalformedURLException, Exception, ServiceException {
		if (entry == null)
		  throw new Exception("null passed in for required parameters");

		MediaContent mc = new MediaContent();
			if (format == null)
				mc.setUri(((MediaContent)entry.getContent()).getUri());
			else
				mc.setUri(((MediaContent)entry.getContent()).getUri() + "&format=" + format + "&exportFormat=" + format);
		MediaSource ms = client.getMedia(mc);

		InputStream inStream = ms.getInputStream();
		ByteArrayOutputStream rawBuffer = new ByteArrayOutputStream();

		int read=0;
		byte[] bytes = new byte[1024];

		while((read = inStream.read(bytes))!= -1)
				rawBuffer.write(bytes, 0, read);

		inStream.close();
		rawBuffer.flush();
		rawBuffer.close();
		return rawBuffer.toByteArray();
  }
}