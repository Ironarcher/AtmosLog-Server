package com.atmoslog;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mongodb.DB;

public class AtmoServer implements Runnable{

	protected int serverPort = 8191;
	protected DatagramSocket socket = null;
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
		System.out.printf("Listening on udp:%s:%d%n",
                InetAddress.getLocalHost().getHostAddress(), serverPort);
		byte[] receiveData = new byte[4096];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		while(running){
			try{
				socket.receive(receivePacket);
			} catch (IOException e){
				if(!running){
					System.out.println("Server stopped.");
					return;
				}
				throw new RuntimeException(
						"Error accepting client connection", e);
			}
			this.threadPool.execute(new WorkerRunnable(socket, receivePacket, db));
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
		this.socket.close();
	}
	
	public void openServerSocket(){
		try{
			this.socket = new DatagramSocket(this.serverPort);
		} catch (IOException e){
			throw new RuntimeException("Cannot open port " + this.serverPort, e);
		}
	}
}
