package Network;

import java.net.SocketException;
import java.util.ArrayList;

import GUI.Hauptfenster;

public class Connection {
	
	private Broadcaster broadcaster = new Broadcaster();
	private ArrayList<Peer> peers = new ArrayList<Peer>();
	private int ports = (1 << 16) -1;
	
	public Connection() {
		Hauptfenster.append("Connection Class Initialized", false);
		for(int i = 0; i < 10; i++) {
			try {
				new Receiver(ports - i, peers).start();
				Hauptfenster.appendNewLine("Neuer Receiver erstellt");
				break;
			} catch(SocketException se) {
				Hauptfenster.appendNewLine("Port " + (ports -i) + " ist bereits belegt");
			}
		}
	}
	
	public Connection searchPeers() {
		Hauptfenster.appendNewLine("Searching Peers...");
		broadcaster
		.setMessage("scan")
		.run();
		return this;
	}
	
	public ArrayList<Peer> getPeerList() {
		synchronized(peers) {
			return peers;
		}
	}

}
