package serverblocks;

import java.io.IOException;
import java.util.List;
import java.util.Random;
//import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import rpc.rpcClient;

public class ServerManager extends Thread {
	public static final int checkRate = 10; // How often should we probe servers
	private Random x = new Random();										// in seconds
	public static boolean simulateCrashOff = true;
	//private static Vector<Server> servers = new Vector<Server>();
	private static List<Server> servers = new CopyOnWriteArrayList<Server>();
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
				pingRandomServer();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void pingRandomServer(){
		
		//Probe A Server To Check If It Is Alive
		int y = x.nextInt(servers.size());
		Server test = servers.get(y);
		System.out.println("Ping a random server " + test.toString());
		//Checking server 'test'
		//If checker returns false then kill it
		boolean flag = rpcClient.checker(test);
		if (flag == false){
			servers.remove(y);
		}
	}
	
	// Get the full list of servers
	public static List<Server> getServerList(){
		return servers;
	}

	// Get a limited number of servers from the full list
	public static List<Server> getServerList(int num) {
		if (num >= servers.size()) {
			return servers;
		} else {
			return servers.subList(0, num-1);
		}
	}
	
	// returns true if the given server already exists in the given list of servers
	public static boolean inServerList(Server s, List<Server> serverList) {
		for (Server tmpServer : serverList) {
			System.out.println("s:" + s.toString() + " tmpServer:" + tmpServer.toString());
			if (tmpServer.equals(s)) {
				System.out.println("Matched!");
				return true;
			}
		}
		return false;
	}
	
	// add a server to the list, assuming it isn't already there
	public static void addServer(Server s) {
		if (! inServerList(s, servers)) {
			servers.add(s);
		}
	}
	
	// return the number of servers in the list
	public static int numServers() {
		int size = servers.size();
		return size;
	}

}
