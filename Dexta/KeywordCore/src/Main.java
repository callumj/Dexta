package com.dexta.coreservices.models;

import com.mongodb.Mongo;

public class Main
{ 
	public static void main(String [] args) throws Exception
	{
		Mongo m = new Mongo("localhost", 27019);		
		
		Keyword newWord = new Keyword("Callum Jones");
		System.out.println(newWord);
		System.out.println(newWord.isImportant());
		newWord.commit(m.getDB( "dexta" ));
		
	}

}