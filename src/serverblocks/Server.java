package serverblocks;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server {
	public InetAddress ipAddress;
	public Integer portNumber;
	
	/**
	 * Constructor when objects are pased to create a server 
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
	 * To String Method gives the IpAddress and the port number is this form
	 * ipAddress:portNumber
	 */
	public String toString() {
		return ipAddress.getHostAddress() + ":" + portNumber;
	}
	
	public boolean equals(Server s) {
		if(ipAddress == s.ipAddress && portNumber == s.portNumber){
			return true;
		}
		return false;
	}
	
}
