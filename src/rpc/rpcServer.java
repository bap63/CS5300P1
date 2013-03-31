package rpc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import session.Session;
import serverblocks.*;

public class rpcServer extends Thread {
	protected static boolean simulateCrashOff = true;
	private static int bufferSize = 4096;
	private static int rpcSenderServerPort;

	DatagramSocket rpcSocket;
	int portNumberServer = 0;

	public rpcServer() {
		try {
			rpcSocket = new DatagramSocket();
			portNumberServer = rpcSocket.getLocalPort();
			System.out.println("rpc server running on port " + portNumberServer);
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
	private byte[] responseBuilder(byte[] data) {
		Session retreivedSession;
		byte[] returnedData = null;
		String requestString = rpcClient.byteDecoder(data);
		System.out.println("rpc server : received request " + requestString);
		String[] splitString = requestString.split("_");
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
		rpcSenderServerPort = Integer.parseInt(splitString[4]);
		String message = "";
		try{
			message = splitString[5];
		}catch(ArrayIndexOutOfBoundsException e){}
		String response = null;
		
		//Manage the response from the String
		if (actionType == rpcClient.OPCODE_PROBE){
			//Probe
			response = uniqueID;
		}else if(actionType == rpcClient.OPCODE_GET){
			//GET
			//get session via uniqueID and version
			retreivedSession = new session.Session();
			retreivedSession.getSessionById(sessionID, sessionVersion);
			if(retreivedSession.getMessage() == null){
				response = "";
				System.out.println("rpcServer GET - session " + sessionID + " was not found");
			}else{
				response = uniqueID;
				//try{
					// pass the data back from the retrieved session
					String rData = retreivedSession.getMessage();
					String rVersion = Integer.toString(retreivedSession.getVersionNumber());
					//TODO: do we really need/want to urlencode these?
					//response = response + "_" + URLEncoder.encode(rVersion,"UTF-8"); //Get Version Number
					//response = response + "_" + URLEncoder.encode(rData,"UTF-8"); //Get Data 'Message'
					response = response + "_" + rVersion; //Get Version Number
					response = response + "_" + rData; //Get Data 'Message'
					System.out.println("rpcServer GET - returning data " + response);
				//} catch (UnsupportedEncodingException e){
				//	e.printStackTrace();
				//	System.out.println("rpcServer Response Builder GET");
				//}
			}
		}else if(actionType == rpcClient.OPCODE_PUT){
			//PUT
			session.Session.injectData(sessionID, sessionVersion, message);
			response = uniqueID;
		}
		System.out.println("rpc server : returning response " + response);
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
			DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
			try {
				rpcSocket.receive(recPacket);
				InetAddress returnAddress = recPacket.getAddress();
				int returnPort = recPacket.getPort();
				// add this server to the list of known servers
				// TODO: which one is correct per 3.8a in the assignment?
				//Server s = new Server(returnAddress, returnPort);
				//ServerManager.addServer(s);
				Server s = new Server(returnAddress, rpcSenderServerPort);
				ServerManager.addServer(s);
				byte[] tempByte = responseBuilder(recPacket.getData());
				DatagramPacket sendPacket = new DatagramPacket(tempByte,
						tempByte.length, returnAddress, returnPort);
				rpcSocket.send(sendPacket);
			} catch (IOException e) {
				//e.printStackTrace();
				System.out.println("rpcServer Run Method of Thread failed");
			}
		}
	}

}
