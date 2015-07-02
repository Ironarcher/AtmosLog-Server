package com.atmoslog;

import com.mongodb.MongoClient;
import com.mongodb.DB;

import java.util.Scanner;
import java.lang.String;

public class Start {

	public static void main(String []args){
		System.out.println("Server start");
		
		AtmoServer server = new AtmoServer(8191, startServer());
		new Thread(server).start();

		while(true){
			try {
			    Thread.sleep(20 * 1000);
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}
			System.out.println("STATUS: server ON");
		}
	}
	
	private static DB startServer(){
		//Collect database credentials
		Scanner in = new Scanner(System.in);
		System.out.println("Enter username");
		String username = in.nextLine();
		System.out.println("Enter password");
		String password = in.nextLine();
		in.close();

		try {
			//Connect to server
			MongoClient mongoClient = new MongoClient("localhost", 27017);
			//Connect to the database
			DB db = mongoClient.getDB("atmos_final");
			@SuppressWarnings("deprecation")
			boolean auth = db.authenticate(username, password.toCharArray());
			
			//For security purposes, delete username and password from memory after typed in.
			username = "";
			password = "";
			System.out.println("Authentication: "+auth);
			return db;
		} catch (Exception e) {
			 System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		}
		
		System.out.println("fatal error");
		return null;
		
	}
}
