package Network;

import java.io.Serializable;
import java.net.InetAddress;

public  class Peer implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private InetAddress ia;
	private transient int port;
	private int remotePort;
	private String name;
	private /*transient*/boolean chatting = false;
	
	public Peer(InetAddress ia, int port, String name) {
		this.ia = ia;
		this.port = port;
		this.name = name;
	}
	
	public Peer(InetAddress ia, int port,int remotePort, String name) {
		this.ia = ia;
		this.port = port;
		this.remotePort = remotePort;
		this.name = name;
	}
	
	public Peer() {
		
	}
	
	public InetAddress getIa() {
		return ia;
	}
	
	public int getPeerPort() {
		return port;
	}
	
	public void setPeerRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}
	
	public int getPeerRemotePort() {
		return remotePort;
	}
	
	public String getName() {
		return name;
	}
	
	public void setChat(boolean b) {
		chatting = b;
	}
	
	public boolean isChatting() {
		return chatting;
	}
}
