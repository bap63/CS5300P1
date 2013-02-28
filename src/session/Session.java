package session;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.*;

// Manage the user's session data, which is stored in a ConcurrentHashMap structure keyed on session ID
public class Session {
	private String sessionID;
	private Integer versionNumber;
	private Timestamp expires = new Timestamp(0);
	protected static ConcurrentHashMap<String, String[]> sessionTable = new ConcurrentHashMap<String, String[]>();
	private static int expTime = 600;  // session expiration time in seconds, i.e. 10 min
	
	/**Creates session object*/
	public Session() {
		// set the default session expiration and version #
		setExpires();
		versionNumber = 0;
	}
	
	/**Create new session*/
	public void getSession(String data, String clientIP) {
		//Creating UniqueID
		String sID = (clientIP + this.expires.toString()).replaceAll("[^0-9]","");
		setSessionID(sID);
		// reset version #
		versionNumber = 0;	
	}
	
	/**Initializes an existing session*/
	public void fetchSession(String sID) {
		setExpires();
		setSessionID(sID);
	}
	
	protected String getSessionID() {
		return sessionID;
	}
	
	protected void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
	
	protected int getVersionNumber() {
		return versionNumber;
	}

	// set the expiration timestamp
	protected void setExpires() {
		Date date = new Date();
		Timestamp stamp = new Timestamp(date.getTime());
		// set the expiration - note expTime is in seconds, we need microseconds
		expires.setTime(stamp.getTime() + (expTime * 1000));
	}

	protected Timestamp getExpires() {
		return expires;
	}
	
	protected int getExpTime() {
		return expTime;
	}
	
	protected void writeData(String data){
		versionNumber++;
		String[] temp = {data, versionNumber.toString(), expires.toString(), String.valueOf(expires.getTime())};
		sessionTable.put(sessionID, temp);
	}
	
	protected String readData(){
		try
		{
			return sessionTable.get(sessionID)[0];
		}
		catch(NullPointerException e){
			return "Error: Session ID not found";
		}
	}
	
	// parse the data string stored in the cookie to extract the session ID
	// TODO: do we need anything else from the cookie? version #?
	protected void parseCookieData(String data) {
		String[] cookiePieces = data.split("#");
		this.setSessionID(cookiePieces[0]);
		//this.setVersionNumber(cookiePieces[1]); 
	}
	
	// create the data string to be stored in the cookie
	protected String createCookieData(String[] locations) {
		String cookieData = this.getSessionID() + "#" + this.getVersionNumber()
		+ "#" + locations[0];
		return cookieData;
	}
}
