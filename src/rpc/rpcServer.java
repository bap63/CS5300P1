package rpc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import session.Session;
import serverblocks.*;

public class rpcServer extends Thread {
	protected static boolean simulateCrashOff = true;
	private static int bufferSize = 512;

	DatagramSocket rpcSocket;
	int portNumberServer = 0;

	public rpcServer() {
		try {
			rpcSocket = new DatagramSocket();
			portNumberServer = rpcSocket.getLocalPort();
		} catch (SocketException e) {
			e.printStackTrace();
			System.out.println("rpcServer Default Constructor");
		}
	}

	/**
	 * Returns -1 if the constructor fails for some weird reason.
	 */
	public int getPort() {
		if (portNumberServer != 0) {
			return portNumberServer;
		} else {
			return -1;
		}
	}

	/**
	 * Crashes the server/simulates a server not responding
	 */
	public void crashServer() {
		simulateCrashOff = false;
	}

	/**
	 * CODES 0: Probe 1: Get 2: Put
	 * 
	 * This function
	 */
	private byte[] responseBuilder(byte[] data, int packetLength){
		Session retreivedSession;
		byte[] returnedData = null;
		String requestedString = rpcClient.byteDecoder(data);
		String[] splitString = requestedString.split(",");
		if (splitString.length < 4){
			return null;
		}
		/* REFERENCE
		String encodeString = (uniqueID + "," + opCodeGET + ","
		+ s.getSessionID() + "," + s.getVersionNumber());*/
		String uniqueID = splitString[0];
		int actionType = Integer.parseInt(splitString[1]);
		String sessionID = splitString[2];
		int sessionVersion = Integer.parseInt(splitString[3]);
		String message = "";
		try{
			message = splitString[4];
		}catch(ArrayIndexOutOfBoundsException e){}
		String response = null;
		
		//Manage the response from the String
		if (actionType == rpcClient.OPCODE_PROBE){
			//Probe
			response = uniqueID;
		}else if(actionType == rpcClient.OPCODE_GET){
			//GET
			//get session via uniqueID and version
			String sID = sessionID;
			retreivedSession = new session.Session();
			retreivedSession.getSessionById(sID, sessionVersion);
			if(retreivedSession.getMessage() == null){
				response = "";
			}else{
				response = uniqueID;
				try{
					//Need a way to pass the data from the retrieved session
					String rData = retreivedSession.getMessage();
					String rVersion = Integer.toString(retreivedSession.getVersionNumber());
					response = response + "," + URLEncoder.encode(rVersion,"UTF-8"); //Get Version Number
					response = response + "," + URLEncoder.encode(rData,"UTF-8"); //Get Data 'Message'
				} catch (UnsupportedEncodingException e){
					e.printStackTrace();
					System.out.println("rpcServer Response Builder GET");
				}
			}
		}else if(actionType == rpcClient.OPCODE_PUT){
			//PUT
			session.Session.injectData(sessionID, sessionVersion, message);
			response = uniqueID;
		}
		returnedData = rpcClient.byteEncoder(response);
		return returnedData;
	}

	/**
	 * Overrides the superclass run You can crash the server here by stopping
	 * the thread aka stopping the receiving via the while loop
	 * 
	 * This run method continuously checks what's coming in and then repackages
	 * it and sends it out to the LB to be sent to the client.
	 */
	public void run() {
		while (simulateCrashOff) {
			byte[] recBuffer = new byte[bufferSize];
			DatagramPacket receivingPacket = new DatagramPacket(recBuffer,
					recBuffer.length);
			try {
				rpcSocket.receive(receivingPacket);
				InetAddress ipAddressReturn = receivingPacket.getAddress();
				int receivingPort = receivingPacket.getPort();
				// add this server to the list of known servers
				// TODO: is this correct?
				Server s = new Server(ipAddressReturn, receivingPort);
				ServerManager.addServer(s);
				byte[] tempByte = responseBuilder(receivingPacket.getData(),
						receivingPacket.getLength());
				DatagramPacket sendingPacket = new DatagramPacket(tempByte,
						tempByte.length, ipAddressReturn, receivingPort);
				rpcSocket.send(sendingPacket);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("rpcServer Run Method of Thread");
			}
		}
	}

}
