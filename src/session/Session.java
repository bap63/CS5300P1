package session;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Hashtable;

public class Session {
	private String sessionID;
	private Integer versionNumber;
	private Timestamp expires;
	private Hashtable sessionTable = new Hashtable();
	
	public Session(String data, String clientIP){
		Date date = new Date();
		Timestamp stamp = new Timestamp(date.getTime());
		
		expires.setTime(stamp.getTime() + 600);
		
		//Creating UniqueID
		setSessionID(clientIP + stamp.toString());
		
		//Initialize Version Number
		versionNumber = 0;
		
		//Add This Session Object To The Hashtable
		storeSessionObject(data);
	}
	
	
	public String getSessionID() {
		return sessionID;
	}
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
	public int getVersionNumber() {
		return versionNumber;
	}
	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}
	
	public Timestamp getExpires() {
		return expires;
	}
	
	private void storeSessionObject(String data){
		String[] temp = {data, versionNumber.toString(), expires.toString()};
		sessionTable.put(sessionID, temp);
	}
}
