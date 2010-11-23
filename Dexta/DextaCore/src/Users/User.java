package com.dexta.coreservices.models.users;

import com.dexta.coreservices.models.base.DBAbstract;

import com.dexta.tools.Tools;
import com.mongodb.DB;

public class User extends DBAbstract {
	
	public User() {
		
	}
	
	public User(String firstName, String lastName, String email) {
		super();
		this.setFirstName(firstName);
		this.setLastName(lastName);
		this.setEmail(email);
	}
	
	public void setFirstName(String name) {
		this.put("first_name", name);
	}
	
	public String getFirstName() {
		return (String) this.get("first_name");
	}
	
	public void setLastName(String name) {
		this.put("last_name", name);
	}
	
	public String getLastName() {
		return (String) this.get("last_name");
	}
	
	public void setEmail(String email) {
		this.put("email", email);
	}
	
	public String getEmail() {
		return (String) this.get("email");
	}
	
	public void setPassword(String password) throws Exception {
		this.put("password", Tools.SHA1(password));
	}
	
	public void commit(DB systemDB) {
		//check if a user exists
		User searchUser = new User();
		searchUser.setEmail(this.getEmail());
		if (!searchUser.find(systemDB))
			super.commit(systemDB);
		else {
			System.out.println(searchUser.getID());
			this.put("_id", searchUser.getID()); //perform update
			super.commit(systemDB);
		}
	}
}