package session;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Vector;
import serverblocks.*;
import rpc.rpcClient;

// Manage the user's session data, which is stored in a ConcurrentHashMap structure keyed on session ID
public class Session {
	private String sessionID;
	private Integer versionNumber;
	private Timestamp expires = new Timestamp(0);
	private String message = "";
	protected static ConcurrentHashMap<String, String[]> sessionTable = new ConcurrentHashMap<String, String[]>();
	private static int expTime = 600;  // session expiration time in seconds, i.e. 10 min
	//private Vector<Server> locations = new Vector<Server>();
	private List<Server> locations = new CopyOnWriteArrayList<Server>();
	
	/**Creates session object*/
	public Session() {
		// set the default session expiration and version #
		setExpires();
		versionNumber = 0;
	}
	
	/**Create new session*/
	public void createSession(String clientIP) {
		//Creating UniqueID
		String sID = (clientIP + this.expires.toString()).replaceAll("[^0-9]","");
		setSessionID(sID);
		// reset version #
		versionNumber = 0;
	}
	
	/**Initializes an existing session*/
	public void getSessionById(String sID, int v) {
		setExpires();
		setSessionID(sID);
		setVersionNumber(v);
		String data = null;
		// if the local server is one of the servers, just read the data from the local session store
		//System.out.println(this.locationsToString());
		//System.out.println(Controller.localserver.toString());
		if (ServerManager.inServerList(Controller.localserver, this.getLocations())) {
			System.out.println("getSessionById: reading data for sID " + sID + " from localhost");
			data = readData();
		}
		// use the rpcClient get function to get the data from one of the locations that was
		// stored in the cookie
		if (data == null) {
			System.out.println("getSessionById: reading data for sID " + sID + " from rpcClient get");
			Session tmpSession = rpcClient.get(this);
			if (tmpSession != null) {
				data = tmpSession.getMessage();
			}
		}
		setMessage(data);
	}
	
	// return sessionID
	public String getSessionID() {
		return sessionID;
	}
	
	// set sessionID to given value
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
	
	// return current version #
	public int getVersionNumber() {
		return versionNumber;
	}
	
	// set version #
	public void setVersionNumber(int v) {
		this.versionNumber = v;
	}

	// set the expiration timestamp
	public void setExpires() {
		Date date = new Date();
		Timestamp stamp = new Timestamp(date.getTime());
		// set the expiration - note expTime is in seconds, we need microseconds
		expires.setTime(stamp.getTime() + (expTime * 1000));
	}

	// return the current expiration timestamp
	public Timestamp getExpires() {
		return expires;
	}
	
	// return the expiration time window
	public int getExpTime() {
		return expTime;
	}
	
	// set the session message
	public void setMessage(String message) {
		this.message = message;
	}
	
	// get the session message
	public String getMessage() {
		return message;
	}
	
	// increment version number and store current data in session table
	public void writeData(String data){
		versionNumber++;
		if(data != null && data.length() > 256){
			data = data.substring(0,256);
		}
		this.setMessage(data);
		String[] temp = {data, versionNumber.toString(), expires.toString(), String.valueOf(expires.getTime())};
		
		try{
			sessionTable.put(sessionID, temp);
		}catch(NullPointerException e){
			e.printStackTrace();
			System.out.println("Error in writing to sessionTable:");
			System.out.println(sessionID + " " + data);
		}
		// reset the location list to contain only this server
		this.clearLocations();
		//System.out.println(session.Controller.localserver);
		this.addLocation(session.Controller.localserver);

		// now we also try to write the data to a backup server using the RPC client
		// TODO: make sure this still works after we have multiple servers 
		//       - right now it is using the same sever as primary and backup
		Session tmpSession = rpcClient.put(this);
		if (tmpSession != null) {
			System.out.println("tmpSession sid:" + tmpSession.getSessionID() + " v:" + tmpSession.getVersionNumber());
			this.setLocations(tmpSession.getLocations());
		}
	}
	/*Static Write method*/
	public static void injectData(String sID,int vr,String data){
	   //Sets expiration time stamp
	   Timestamp exp = new Timestamp(0);
	   Timestamp stamp = new Timestamp(new Date().getTime());
	   exp.setTime(stamp.getTime() + (expTime * 1000));
	   //Constructs string data , version number, expiration (string), expiration (timestamp)
	   String[] temp = {data, Integer.toString(vr), exp.toString(), String.valueOf(exp.getTime())};
	   try{
			sessionTable.put(sID, temp);
		}catch(NullPointerException e){
			e.printStackTrace();
			System.out.println("Error in injecting to sessionTable:");
			System.out.println(sID + " " + data);
		}
		
	}
	
	// retrieve the message data associated with the current session from the session table
	public String readData(){
		try
		{
			String[] temp = sessionTable.get(sessionID);
			// make sure version matches - if not, return null
			if (versionNumber.toString().equals(temp[1])) {
				return temp[0];
			} else {
				return null;
			}
		}
		catch(NullPointerException e){
			return null;
		}
	}
	
	// parse the data string stored in the cookie to extract and set the session ID
	// plus the version number and server locations
	public void parseCookieData(String data) {
		String[] cookiePieces = data.split("#");
		this.setSessionID(cookiePieces[0]);
		this.setVersionNumber(Integer.parseInt(cookiePieces[1]));
		String[] servers = cookiePieces[2].split("_");
		Vector<Server> serverList = new Vector<Server>();
		for (int i=0; i<servers.length; i++) {
			Server s = new Server(servers[i]);
			serverList.add(s);
			// also make sure this server is in the master list
			ServerManager.addServer(s);
		}
		this.setLocations(serverList);
	}
	
	// create the data string to be stored in the cookie
	public String createCookieData() {
		// create cookie data by concatenating session id, version and location string, delimited by "#"
		String cookieData = this.getSessionID() + "#" + this.getVersionNumber() + "#" + this.locationsToString();
		//System.out.println("cookieData: " + cookieData);
		return cookieData;
	}
	
	// set the list of server locations to be used for this session
	public void setLocations(List<Server> list) {
		locations = list;
	}
	
	// clear the list of locations
	public void clearLocations() {
		locations.clear();
	}
	
	// add a single server to the list of session locations
	public void addLocation(Server s) {
		locations.add(s);
	}
	 
	// get the list of server locations used to store this session
	public List<Server> getLocations() {
		return locations;
	}
	
	public String locationsToString() {
		String tmpLocations = "";
		// convert the servers into a string delimited by "_"
		for (Server s : this.getLocations()) {
			tmpLocations += s.toString() + "_";
		}
		// remove trailing "_"
		if (tmpLocations != "") {
			tmpLocations = tmpLocations.substring(0, tmpLocations.length()-1);
		}
		return tmpLocations;
	}
}
