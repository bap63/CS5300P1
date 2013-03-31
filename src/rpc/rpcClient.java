package rpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;
//import java.net.URLDecoder;
import java.util.UUID;

//import org.apache.catalina.Session;
import session.*;

import serverblocks.Server;

/*operation Codes:
 * 0: Probe
 * 1: Get
 * 2: Put
 * DatagramPacket(callID, operationSESSIONREAD, sessionID, sessionVersionNum)
 * */
public class rpcClient {
	private static final Integer TIMEOUT = 2000;
	public static int OPCODE_PROBE = 0;
	public static int OPCODE_GET = 1;
	public static int OPCODE_PUT = 2;
	private static int bufferSize = 4096;
	private static final int numServers = 1;

	// Converts a string to a byte array for UDP Packaging
	public static byte[] byteEncoder(String s) {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			ObjectOutput outObj = new ObjectOutputStream(stream);
			outObj.writeObject(s);
			return stream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	// Converts a byte[] to a string FROM a UDP packet
	public static String byteDecoder(byte[] b) {
		try {
			String output = "";
			ByteArrayInputStream stream = new ByteArrayInputStream(b);
			ObjectInput in = new ObjectInputStream(stream);
			output = (String) in.readObject();
			return output;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean checker(Server s) {
		System.out.println("Checking if Server is alive: " + s);
		DatagramSocket rpcSocket;
		DatagramPacket rpcPacket;
		try {
			rpcSocket = new DatagramSocket();
			rpcSocket.setSoTimeout(TIMEOUT);

			// Generate Unique ID
			String uniqueID = UUID.randomUUID().toString();

			// Encode string for packet sending
			//String encodeString = (uniqueID + "," + OPCODE_PROBE + ",0,0," + Controller.localport);
			String encodeString = encodeStringForPacket(uniqueID, OPCODE_PROBE, null);
			byte[] encodedByte = byteEncoder(encodeString);
			
			// set up receive packet
			byte[] recBuffer = new byte[bufferSize];
			byte[] tempByte = new byte[bufferSize];
			DatagramPacket recPacket = new DatagramPacket(recBuffer,
					recBuffer.length);

			// send the packet to the given server
			rpcPacket = new DatagramPacket(encodedByte, encodedByte.length,
					s.ipAddress, s.portNumber);
			rpcSocket.send(rpcPacket);
			System.out.println("rpcClient checker : Sent : " + encodeString);

			// receive a packet back; test that the uniqueID's are equal ergo, this is the same packet
			// and the server lives
			String recID = "";
			try {
				do {
					rpcSocket.receive(recPacket);
					tempByte = recPacket.getData();
					recID = byteDecoder(tempByte).split("_")[0];
					System.out.println("rpcClient checker : received " + recID);
				} while (! recID.equals(uniqueID));
			} catch (SocketException e) {
				//e.printStackTrace();
				System.out.println("rpcClient checker : SocketException on server " + s);
				return false;
			}
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("rpcClient checker : IOException on server " + s);
			return false;
		}
		System.out.println("rpcClient checker : Server:" + s + " is Online!");
		return true;
	}

	// Code 1: Get Function
	public static Session get(session.Session s) {
		System.out.println("GETTING from Session: " + s);
		DatagramSocket rpcSocket;
		//DatagramPacket rpcPacket;
		try {
			rpcSocket = new DatagramSocket();
			rpcSocket.setSoTimeout(TIMEOUT);

			// Generate Unique ID
			String uniqueID = UUID.randomUUID().toString();

			// Encode string for packet sending
			String encodeString = encodeStringForPacket(uniqueID, OPCODE_GET, s);
			//String encodeString = (uniqueID + "," + OPCODE_GET + ","
			//		+ s.getSessionID() + "," + s.getVersionNumber());
			byte[] encodedByte = byteEncoder(encodeString);
			System.out.println("rpc get : Sending string " + encodeString);

			// For loop sends the packet to the list of all the servers
			for (Server sNode : s.getLocations()) {
		        if (sNode.equals(Controller.localserver)) {
		        	continue;
		        }
				DatagramPacket sendPkt = new DatagramPacket(encodedByte,
						encodedByte.length, sNode.ipAddress, sNode.portNumber);
				System.out.println("rpcClient get : Sending to Server: " + sNode.toString());
				try {
					rpcSocket.send(sendPkt);
				} catch (IOException e) {
					//e.printStackTrace();
					System.out.println("rpcClient get : IOException Error in sending the packet to " + sNode.toString());
				}
			}

			// rpcPacket = new DatagramPacket(encodedByte, encodedByte.length,
			// s.ipAddress, s.portNumber);
			// rpcSocket.send(rpcPacket);
			// System.out.println("Sent packet: " + rpcPacket.toString());

			// !------------------RECEIVE PACKET---------------//
			byte[] recBuffer = new byte[bufferSize];
			byte[] tempByte = new byte[bufferSize];
			DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);

			// Test that the uniqueID's are equal ergo, this is the same packet
			try {
				do {
					rpcSocket.receive(recPacket);
					tempByte = recPacket.getData();
				} while (!(byteDecoder(tempByte).split("_")[0].equals(uniqueID))
						|| tempByte == null);
			} catch (SocketException e) {
				//e.printStackTrace();
				System.out.println("rpcClient get : SocketException Error in receiving the packet");
			}
			String[] response = byteDecoder(tempByte).split("_");
			System.out.println("rpcClient get : received data: " + response[2]);
			s.setMessage(response[2]);
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("rpcClient get : IOException Error in sending the packet");
			return null;
		}
		return s;
	}

	// Code 2: Put
	public static Session put(session.Session s){
		System.out.println("PUTTING to Session: " + s);
		DatagramSocket rpcSocket;
		//DatagramPacket rpcPacket;
		try{
			
			rpcSocket = new DatagramSocket();
			rpcSocket.setSoTimeout(TIMEOUT);
			
			//Generate Unique ID
			String uniqueID = UUID.randomUUID().toString();
			
			// Encode string for packet sending
			String encodeString = encodeStringForPacket(uniqueID, OPCODE_PUT, s);
			//String encodeString = (uniqueID + "," + OPCODE_PUT + "," + s.getSessionID() + "," + s.getVersionNumber() + "," + s.readData());
		    
			byte[] encodedByte = byteEncoder(encodeString);
			System.out.println("rpcClient put : Sending string " + encodeString);
		    
		    //For loop sends the packet to the list of all the servers
		    List<Server> allServers = serverblocks.ServerManager.getServerList();
		    for (Server sNode : allServers) {
		        if (sNode.equals(Controller.localserver)) {
		        	//System.out.println("rpcClient put : Skipping localserver");
		        	continue;
		        }
		    	DatagramPacket sendPkt = new DatagramPacket(encodedByte, encodedByte.length, sNode.ipAddress, sNode.portNumber);
		        System.out.println("rpcClient put : Sending to Server: " + sNode.toString());
		        try {
		          rpcSocket.send(sendPkt);
		        } catch (IOException e) {
		        	System.out.println("rpcClient put : IOException Error in sending the packet to " + sNode.toString());
		          //e.printStackTrace();
		        }
		    }
		    
		    int receiveCount = 0;
		    // !------------------RECEIVE PACKET---------------//
		    byte[] recBuffer = new byte[bufferSize];
		 	//byte[] tempByte = new byte[bufferSize];
		 	DatagramPacket recPacket = new DatagramPacket(recBuffer, recBuffer.length);
		 	
		 	// now we wait for a response from numServers servers before we consider it done
		 	do {
		        try {
		          rpcSocket.receive(recPacket);
		          String data = byteDecoder(recBuffer);
		          System.out.println("rpcClient put : received:" + data);
		          if (data.split("_")[0].equals(uniqueID)) {
		        	  receiveCount++;
		        	  // add the responding server to the session
		        	  //TODO: is this correct based on 3.8a in the assignment?
		        	  s.addLocation(new Server(recPacket.getAddress(), recPacket.getPort()));
		          }
		        } catch (IOException e) {
		          //e.printStackTrace();
		        	System.out.println("rpcClient put : IOException Error in receiving the packet");
		          rpcSocket.close();
		          return null;

		        }
		 	} while (receiveCount < numServers);
		 	rpcSocket.close();
		}catch (IOException e){
			//e.printStackTrace();
        	System.out.println("rpcClient put : IOException Error in sending the packet");
		}
		System.out.println("rpcClient finished put");
	    return s;
	}
	
	// construct the packet for the given opCode type
	public static String encodeStringForPacket(String uniqueID, int opCode, Session s) {
		String sessionID = "0";
		String sessionVersion = "0";
		String message = "";
		if (s != null) {
			sessionID = s.getSessionID();
			sessionVersion = Integer.toString(s.getVersionNumber());
			if (opCode == OPCODE_PUT) {
				message = s.getMessage();
			}
		}

		String encodeString = (uniqueID + "_" + opCode + "_" + sessionID + "_" + 
				sessionVersion + "_" + Controller.localport + "_" + message);
		return encodeString;
		
	}
}
