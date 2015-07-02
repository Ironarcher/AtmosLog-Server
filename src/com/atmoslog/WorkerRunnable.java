package com.atmoslog;

import java.net.*;
import java.io.*;
import java.util.Date;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;


public class WorkerRunnable implements Runnable{
	
	protected Socket client = null;
	protected String serverText = null;
	protected DB db = null;
	
	public WorkerRunnable(Socket client, DB db){
		this.client = client;
		this.db = db;
	}
	
	public void run(){
		try{
			DataInputStream input = new DataInputStream(client.getInputStream());
			DataOutputStream output = new DataOutputStream(client.getOutputStream());
			output.writeUTF(receive(input.readUTF()));
			output.close();
			input.close();
			System.out.println("Request processed");
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	private String receive(String packet){
		//Banned character set: %s (replace with character code)
		String[] parts;
		if(packet.contains("%s")){
			parts = packet.split("%s");
		} else{
			return "Incorrect packet syntax";
		}
		int type = Integer.parseInt(parts[0]);
		switch(type){
		case 0:
			//Simple ping
			return "ping";
		case 1:
			//Logging (no name)
			String apikey = parts[1];
			String table = parts[2];
			String text = parts[3];
			boolean apicount = false;
			String username = "";
			
			try{
				BasicDBObject allQuery = new BasicDBObject();
				BasicDBObject fields = new BasicDBObject();
				fields.put("key", apikey);
				DBCollection api_secret = db.getCollection("api_secret");
				DBCursor cursor = api_secret.find(allQuery, fields);

				while(cursor.hasNext()){
					System.out.println(cursor.next());
					apicount = true;
					username = (String) cursor.next().get("username");
					System.out.println(username);
				}
			} catch (Exception e){
				System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			}
			if(apicount){
				String conc = username + "-" + table;
				if(conc != "api_secret" && conc != "api_public"){
					DBCollection tab;
					try{
						tab = db.getCollection(conc);
					} catch(Exception e){
						return "Table name not found";
					}
					try{
						BasicDBObject doc = new BasicDBObject("log", text).
								append("datetime", (new Date().getTime())/1000);
						tab.insert(doc);
						return "success";
					} catch(Exception e){
						System.err.println( e.getClass().getName() + ": " + e.getMessage() );
					}
				} else{
					return "Invalid table name";
				}
			} else{
				return "Incorrect API secret key";
			}
		}
		//Convert packet to string
		//Split string by '%s'
		//Take second part as API-secret key, second part as name, third part as value, and fourth part as table
		return null;
	}
}
