package serverblocks;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server {
	public InetAddress ipAddress;
	public Integer portNumber;
	
	/**
	 * Constructor when objects are passed to create a server 
	 */
	public Server(InetAddress serverIP, int serverPort){
		ipAddress = serverIP;
		portNumber = serverPort;
	}
	
	/**
	 * Constructor when strings are passed. Converts to an IP Address and port number.
	 */
	public Server(String serverIP, String serverPort){
		try{
			ipAddress = InetAddress.getByName(serverIP);
		}catch (UnknownHostException e){
			System.out.println("Server does not exist!");
			e.printStackTrace();
		}
		//Set the port
		portNumber = Integer.parseInt(serverPort);
	}
	
	/**
	 * Constructor when a single string combining IP and port is passed
	 */
	public Server (String serverIPPort) {
		String[] pieces = serverIPPort.split(":");
		try{
			ipAddress = InetAddress.getByName(pieces[0]);
			portNumber = Integer.parseInt(pieces[1]);
		}catch (UnknownHostException e){
			System.out.println("Server does not exist!");
			e.printStackTrace();
		}
	}
	
	/**
	 * To String Method gives the IpAddress and the port number is this form
	 * ipAddress:portNumber
	 */
	public String toString() {
		return ipAddress.getHostAddress() + ":" + portNumber;
	}
	
	/**
	 * Determine if 2 server objects are equivalent (same IP and port)
	 */
	public boolean equals(Server s) {
		if (this.toString().equals(s.toString())) {
			return true;
		}
		return false;
	}
	
}
