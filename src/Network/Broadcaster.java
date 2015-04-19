package Network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import GUI.Hauptfenster;

public class Broadcaster implements Runnable {
	
	private DatagramSocket sendSocket;
	private int ports = (1 << 16) -1;
	private String msg;
	private static int packetCount = 0;
	
	public void run() {
		
		byte[] data = msg.getBytes();
		InetAddress ia = null;
		try {
			sendSocket = new DatagramSocket();
			sendSocket.setBroadcast(true);
			ia = InetAddress.getByName("255.255.255.255");
			StringBuilder sb = new StringBuilder();
			sb.append("Schicke Packet mit Inhalt: \n" + msg + ", an folgende Ports:\n");
			for(int i = ports; i > ports - 10; i--) {
				sendSocket.send(new DatagramPacket(data, data.length, ia, i));
				packetCount++;
				sb.append(i+" ");
			}
			Hauptfenster.updatTable().setValueAt(packetCount, 3, 1);
			Hauptfenster.appendNewLine(sb.toString());
			sendSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Broadcaster setMessage(String msg) {
		this.msg = msg;
		return this;
	}
}
