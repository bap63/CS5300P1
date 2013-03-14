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
	private static int bufferSize = 512;
	private static final int numServers = 1;
	//private static final double lamba = 1.0;
	//private static final double ro = 2.0;

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

			rpcPacket = new DatagramPacket(encodedByte, encodedByte.length,
					s.ipAddress, s.portNumber);
			rpcSocket.send(rpcPacket);
			System.out.println("Sent packet: " + rpcPacket.toString());

			// !------------------RECEIVE PACKET---------------//
			byte[] recBuffer = new byte[bufferSize];
			byte[] tempByte = new byte[bufferSize];
			DatagramPacket receivingPacket = new DatagramPacket(recBuffer,
					recBuffer.length);

			// Test that the uniqueID's are equal ergo, this is the same packet
			// and the server lives
			try {
				do {
					rpcSocket.receive(receivingPacket);
					tempByte = receivingPacket.getData();
				} while (!(byteDecoder(tempByte).split("_")[0].equals(uniqueID)));
			} catch (SocketException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error in sending the packet in checker!");
			return false;
		}
		System.out.println("Server:" + s + " is Online!");
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

			// For loop sends the packet to the list of all the servers
			for (Server sNode : s.getLocations()) {
				DatagramPacket sendPkt = new DatagramPacket(encodedByte,
						encodedByte.length, sNode.ipAddress, sNode.portNumber);
				try {
					rpcSocket.send(sendPkt);
					System.out.println("Sent packet: " + sendPkt.toString()
							+ "@ Server: " + sNode.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// rpcPacket = new DatagramPacket(encodedByte, encodedByte.length,
			// s.ipAddress, s.portNumber);
			// rpcSocket.send(rpcPacket);
			// System.out.println("Sent packet: " + rpcPacket.toString());

			// !------------------RECEIVE PACKET---------------//
			byte[] recBuffer = new byte[bufferSize];
			byte[] tempByte = new byte[bufferSize];
			DatagramPacket receivingPacket = new DatagramPacket(recBuffer,
					recBuffer.length);

			// Test that the uniqueID's are equal ergo, this is the same packet
			try {
				do {
					rpcSocket.receive(receivingPacket);
					tempByte = receivingPacket.getData();
				} while (!(byteDecoder(tempByte).split("_")[0].equals(uniqueID))
						|| tempByte == null);
			} catch (SocketException e) {
				e.printStackTrace();
			}
			System.out.println("Client received response: "
					+ byteDecoder(tempByte).split("_")[0]);
			String[] response = byteDecoder(tempByte).split("_");
			//TODO: make sure we really want to do this
			s.setMessage(response[2]); // Not sure this is correct
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error in sending the packet in checker!");
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
		    
		    //For loop sends the packet to the list of all the servers
		    List<Server> allServers = serverblocks.ServerManager.getServerList();
		    for (Server sNode : allServers) {
		        DatagramPacket sendPkt = new DatagramPacket(encodedByte, encodedByte.length, sNode.ipAddress, sNode.portNumber);
		        try {
		          rpcSocket.send(sendPkt);
		          System.out.println("Sent packet: " + sendPkt.toString() + "@ Server: " + sNode.toString());
		        } catch (IOException e) {
		          e.printStackTrace();
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
		          //System.out.println("Put client received:" + response);
		          if (data.split("_")[0].equals(uniqueID)) {
		        	  receiveCount++;
		        	  // add the responding server to the session
		        	  //TODO: is this correct based on 3.8a in the assignment?
		        	  s.addLocation(new Server(recPacket.getAddress(), recPacket.getPort()));
		          }
		        } catch (IOException e) {
		          e.printStackTrace();
		          rpcSocket.close();
		          return null;

		        }
		 	} while (receiveCount < numServers);
		 	rpcSocket.close();
		}catch (IOException e){
			e.printStackTrace();
		}
		System.out.println("Client finished put");
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
