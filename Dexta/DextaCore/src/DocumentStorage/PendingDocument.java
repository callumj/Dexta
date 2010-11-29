package com.dexta.coreservices.models.documents;

import com.dexta.coreservices.models.base.DBAbstract;
import com.dexta.coreservices.models.services.Service;
import com.dexta.tools.StorageWrapper;
import com.dexta.coreservices.models.services.StorageService;

import com.mongodb.DB;
import org.bson.types.ObjectId;

import java.util.regex.Pattern;

public class PendingDocument extends DBAbstract {
	
	public PendingDocument() {
		super();
	}
	
	public PendingDocument(String fileName, Service parentService) {
		super();
		this.setfileName(fileName);
		this.setParentService(parentService);
	}
	
	public void setFileExtension(String extension) {
		this.put("file_extension", extension.toLowerCase());
	}
	
	public String getFileExtension() {
		return (String) this.get("file_extension");
	}
	
	public void setfileName(String fileName) {
		this.put("file_name", fileName);
		if (fileName.lastIndexOf(".") != -1)
			this.setFileExtension(fileName.substring(fileName.lastIndexOf(".") + 1));
	}
	
	public String getfileName() {
		return (String) this.get("file_name");
	}
	
	public void setParentService(Service service) {
		this.put("service", service.getID());
	}
	
	public ObjectId getParentServiceId() {
		return (ObjectId) this.get("service");
	}
	
	public void setStorageReference(ObjectId storageID) {
		this.put("storage", storageID);
	}
	
	public ObjectId getStorageReference() {
		return (ObjectId) this.get("storage");
	}
	
	public void setMimeType(String mime) {
		this.put("mime_type", mime);
	}
	
	public String getMimeType() {
		return (String) this.get("mime_type");
	}
	
	public void setLocked(boolean status) {
		this.put("locked", status);
	}
	
	public Boolean isLocked() {
		Boolean returnDecision = (Boolean) this.get("locked");
		if (returnDecision != null && returnDecision)
			return true;
		else
			return false;
	}
	
	public Document toDocument() {
		Document returnDoc = new Document();
		for (String key : this.keySet()) {
			if (!(key.equals("_id")))
				returnDoc.put(key, this.get(key));
		}
		return returnDoc;
	}
	
	public Document transposeToDocument(StorageWrapper storageWrapper, Processor documentProcessor) throws Exception {
		DB systemDB = storageWrapper.mongoDatabase;
		Document returnObj = this.toDocument();
		StorageService stor = returnObj.getStorage(systemDB);
		byte[] data = stor.getData(storageWrapper);
		documentProcessor.setData(data);
		documentProcessor.setFileExtension(this.getFileExtension());
		returnObj.addDataToContent(documentProcessor.getText());
		returnObj.buildKeywords(10);
		returnObj.compact(2);
		return returnObj;
	}
	
	public static PendingDocument getPendingDocumentInQueue(DB systemDB, String file_ext) {
		PendingDocument filter = new PendingDocument();
		filter.setLocked(false);
		filter.put("file_extension", Pattern.compile("(" + file_ext + ")", Pattern.CASE_INSENSITIVE));
		if (filter.find(systemDB))
			return filter;
		else
			return null;
	}
}