package serverblocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rpc.rpcClient;

public class ServerManager extends Thread {
	public static final int checkRate = 10; // How often should we probe servers
	private Random x = new Random();										// in seconds
	public static boolean simulateCrashOff = true;
	private static List<Server> servers = new ArrayList<Server>();
	private Server currentServer;

	// Insert a server\
	// Remove a server
	// Checking/probing for status of server
	// Return a list of servers
	// Return the number of servers

	public ServerManager(Server s) throws IOException {
		currentServer = s;
		servers.add(currentServer);
	}
	
	public void crashServer() {
		simulateCrashOff = false;
	}

	public void run() {
		while (simulateCrashOff) {
			try {
				Thread.sleep(checkRate * 1000);
				pingAllServers();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void pingAllServers(){
		System.out.println("Ping All Servers, Starting with: " + currentServer.toString());
		
		//Probe A Server To Check If It Is Alive
		int y = x.nextInt(servers.size());
		Server test = servers.get(y);
		//Checking server 'test'
		//If checker returns false then kill it
		boolean flag = rpcClient.checker(test);
		if (flag == false){
			servers.remove(y);
		}
	}
	
	public static List<Server> getServerList(){
		return servers;
	}
	
	public static void addServer(Server s){
		if(!servers.contains(s.toString())){
			servers.add(s);
		}
	}
	
	public static int numServers() {
		int size = servers.size();
		return size;
	}

}
