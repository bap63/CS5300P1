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
import java.net.URLDecoder;
import java.util.UUID;

import org.apache.catalina.Session;

import serverblocks.Server;

/*operationSESSIONREAD Codes:
 * 0: Probe
 * 1: Get
 * 2: Put
 * DatagramPacket(callID, operationSESSIONREAD, sessionID, sessionVersionNum)
 * */
public class rpcClient {
	private static final Integer TIMEOUT = 2000;
	private static int opCodeProbe = 0;
	private static int opCodeGET = 1;
	private static int opCodePUT = 2;
	private static int bufferSize = 512;
	private static final double lamba = 1.0;
	private static final double ro = 2.0;

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
			String encodeString = (uniqueID + "," + opCodeProbe + ",0,0");
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
				} while (!(byteDecoder(tempByte).split(",")[0].equals(uniqueID)));
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
		DatagramPacket rpcPacket;
		try {
			rpcSocket = new DatagramSocket();
			rpcSocket.setSoTimeout(TIMEOUT);

			// Generate Unique ID
			String uniqueID = UUID.randomUUID().toString();

			// Encode string for packet sending
			String encodeString = (uniqueID + "," + opCodeGET + ","
					+ s.getSessionID() + "," + s.getVersionNumber());
			byte[] encodedByte = byteEncoder(encodeString);

			// For loop sends the packet to the list of all the servers
			// NEED A LOCATION FUNCTION IN SESSION CLASS! CONTROLLER
			// CONSTRUCTIONS LOCATION
			// BUT DEPENDS ON THE GROUP SENSING FUNCTION!
			for (Server sNode : s.getLocations()) {
				DatagramPacket sendPkt = new DatagramPacket(encodedByte,
						encodedByte.length, sNode.ipAddress, sNode.portNumber);
				try {
					rpcSocket.send(sendPkt);
					System.out.println("Sent packet: " + rpcPacket.toString()
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
			// and the server lives
			try {
				do {
					rpcSocket.receive(receivingPacket);
					tempByte = receivingPacket.getData();
				} while (!(byteDecoder(tempByte).split(",")[0].equals(uniqueID))
						|| tempByte == null);
			} catch (SocketException e) {
				e.printStackTrace();
			}
			System.out.println("Client received response: "
					+ byteDecoder(tempByte).split(",")[0]);
			String[] response = byteDecoder(tempByte).split(",");
			s.writeData(response[2]); // Not sure this is correct
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error in sending the packet in checker!");
			return null;
		}
		System.out.println("Server:" + s + " is Online!");
		// return true;
	}

	// Code 2: Put
	public static Session put(session.Session s){
		System.out.println("PUTTING to Session: " + s);
		DatagramSocket rpcSocket;
		DatagramPacket rpcPacket;
		try{
			//NEED Number of servers
			
			rpcSocket = new DatagramSocket();
			rpcSocket.setSoTimeout(TIMEOUT);
			
			//Generate Unique ID
			String uniqueID = UUID.randomUUID().toString();
			
			//Encode string for packet sending
			String encodeString = (uniqueID + "," + opCodePUT + s.getSessionID() + s.getVersionNumber());
		    byte[] encodedByte = byteEncoder(encodeString);
		    
		    //For loop sends the packet to the list of all the servers
		    //NEED A LOCATION FUNCTION IN SESSION CLASS! CONTROLLER CONSTRUCTIONS LOCATION
		    //BUT DEPENDS ON THE GROUP SENSING FUNCTION!
		    for (Server sNode : 10/*Group Membership.getServers*/) {
		        DatagramPacket sendPkt = new DatagramPacket(encodedByte, encodedByte.length, sNode.ipAddress, sNode.portNumber);
		        try {
		          rpcSocket.send(sendPkt);
		          System.out.println("Sent packet: " + rpcPacket.toString() + "@ Server: " + sNode.toString());
		        } catch (IOException e) {
		          e.printStackTrace();
		        }
		    }
		    
		    int receiveCount = 0;
		    // !------------------RECEIVE PACKET---------------//
		    byte[] recBuffer = new byte[bufferSize];
		 	byte[] tempByte = new byte[bufferSize];
		 	DatagramPacket receivingPacket = new DatagramPacket(recBuffer, recBuffer.length);
		 	
		 	
		 	do {
		        try {
		          rpcSocket.receive(receivingPacket);
		          String data = byteEncoder(recBuffer);
		          //System.out.println("Put client received:" + response);
		          if (data.split(",")[0].equals(uniqueID)) {
		        	  receiveCount++;
		            //s.addLocation(new Server(recvPkt.getAddress(), recvPkt.getPort()));
		          }
		        } catch (IOException e) {
		          // TODO Auto-generated catch block
		          e.printStackTrace();
		          return null;

		        }
		      } while (receiveCount < (lamba * numServers));
		}catch (IOException e){
			 e.printStackTrace();
		}
	}
}
