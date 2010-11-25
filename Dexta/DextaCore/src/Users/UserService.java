package com.dexta.coreservices.models.users;

import com.dexta.coreservices.models.base.DBAbstract;
import com.dexta.coreservices.models.services.Service;

import com.mongodb.DB;
import com.mongodb.DBCursor;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.ArrayList;


public class UserService extends DBAbstract {
	
	private UserService() {
		
	}
	
	public UserService(User userObj, Service serviceObj) {
		super();
		this.setUser(userObj);
		this.setService(serviceObj);	
	}
	
	public void setUser(User userObj) {
		this.put("user", userObj.getID());
	}
	
	public void setService(Service serviceObj) {
		this.put("service", serviceObj.getID());
	}
	
	public void commit(DB systemDB) {
		if (!this.find(systemDB))
			super.commit(systemDB);
	}
	
	public static List<Service> getServicesForUser(DB systemDB, User user, Service getType) {
		UserService searchParam = new UserService();
		searchParam.setUser(user);
		UserService classGet = new UserService();
		DBCursor cur = systemDB.getCollection(DBAbstract.classToCollectionName(classGet.getClass())).find(searchParam);
		
		ArrayList<Service> returningCollection = new ArrayList<Service>();
		
		while(cur.hasNext()) {
			Service findService = new Service();
			findService.put("_id", (ObjectId) cur.next().get("service"));
			if (findService.find(systemDB)) {
				if ((findService != null && findService.getType().equals(getType.createClassType())) || getType == null)
					returningCollection.add(findService);
			} else {
				System.out.println("Could not reverse lookup");
			}
		}
		
		return returningCollection;
	}
}