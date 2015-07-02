package com.atmoslog;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mongodb.DB;

public class AtmoServer implements Runnable{

	protected int serverPort = 8191;
	protected ServerSocket socket = null;
	protected boolean running = true;
	protected Thread runningThread = null;
	protected ExecutorService threadPool = Executors.newFixedThreadPool(10);
	protected DB db = null;
	
	public AtmoServer(int port, DB db){
		this.serverPort = port;
		this.db = db;
	}

	public void run(){
		synchronized(this){
			this.runningThread = Thread.currentThread();
		}
		openServerSocket();
		while(running){
			Socket client = null;
			try{
				client = this.socket.accept();
			} catch (IOException e){
				if(!running){
					System.out.println("Server stopped.");
					return;
				}
				throw new RuntimeException(
						"Error accepting client connection", e);
			}
			this.threadPool.execute(new WorkerRunnable(client, db));
		}
		this.threadPool.shutdown();
		System.out.println("Server stopped.");
	}
	
	@SuppressWarnings("unused")
	private synchronized boolean running(){
		return this.running;
	}
	
	public synchronized void stop(){
		this.running = false;
		try{
			this.socket.close();
		} catch (IOException e){
			throw new RuntimeException("Error closing server", e);
		}
	}
	
	public void openServerSocket(){
		try{
			this.socket = new ServerSocket(this.serverPort);
		} catch (IOException e){
			throw new RuntimeException("Cannot open port " + this.serverPort, e);
		}
	}
}
