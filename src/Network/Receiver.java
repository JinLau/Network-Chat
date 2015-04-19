package Network;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import GUI.Group;
import GUI.Hauptfenster;

public class Receiver extends Thread {
	
	private DatagramSocket sc;
	private DatagramPacket dpp;
	private static ArrayList<Peer> peers;
	private byte[] data;
	//DEBUG 
	private static boolean selfScan = false,
						   scannable = true;
	private static int UDPReceived = 0;
	
	public Receiver(int port, ArrayList<Peer> pPeers) throws SocketException {
		peers = pPeers;
		sc = new DatagramSocket(port);
	}
	
	public void run() {
		Hauptfenster.appendNewLine("Receiver Started");
			try {
				for(;;) {
					data = new byte[50];
					dpp =new DatagramPacket(data, data.length);
					Hauptfenster.appendNewLine("Warte auf port " + sc.getLocalPort());
					sc.receive(dpp);
					String daten = new String(dpp.getData());
					Hauptfenster.appendNewLine("UDP Packet erhalten!, Inhalt: " + daten);
					Hauptfenster.updatTable().setValueAt(++UDPReceived, 4, 1);
					if(scannable) {
						if(InetAddress.getLocalHost().getHostAddress().equals(dpp.getAddress().getHostAddress()) && !selfScan) {
							Hauptfenster.appendNewLine("Ist eigenes Paket. Eigene Packete werden nicht angenommen!");
						} else {
							if(daten.trim().split(",")[0].equals("reply")) {
								addToList(dpp, daten);
							} else if(daten.trim().equals("scan") && scannable) {
								Hauptfenster.appendNewLine("Replying...");
								new Broadcaster()
								.setMessage("reply," + System.getProperty("user.name"))
								.run();
							}
							else if(daten.trim().split(",")[0].equals("request")) {
								Hauptfenster.appendNewLine("Request erhalten");
								final String[] data = daten.trim().split(",");
								Broadcaster bc = new Broadcaster();
								if(data[3].equals(System.getProperty("user.name"))) {
									System.out.println("Richtiger peer");
									if(JOptionPane.showConfirmDialog(null, data[1] + " möchte dich zum chatten einladen", "Chatanfrage", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
										bc.setMessage("accepted");
										Hauptfenster.appendNewLine("Erstelle Socket für Verbindung");
										final Socket client = new Socket(dpp.getAddress(), Integer.valueOf(data[2]));
										Hauptfenster.appendNewLine("Socket erstellt");
										EventQueue.invokeLater(new Runnable() {
	
											@Override
											public void run() {
												//Hauptfenster.addChatroom(new Chatroom(client, System.getProperty("user.name"), data[1]).setTitleAndReturn("Chat mit " + data[1]));
											}
											
										});
									}
									else {
										bc.setMessage("refused");
									}
									bc.run();
								}
							}
							else if(daten.trim().split(",")[0].equals("requestGroup")) {
								Hauptfenster.appendNewLine("Gruppen-Request erhalten");
								//UDP-Befehl, Sender-Name, ServerSocket-port, an wen, GruppenName
								final String[] data = daten.trim().split(",");
								final InetAddress sA = dpp.getAddress();
								if(data[3].equals(System.getProperty("user.name"))) {
									Hauptfenster.appendNewLine(this.getClass(), " UDP BRODCAST HAT DEN RICHTIGEN PEER ERREICHT");
									/*if(JOptionPane.showConfirmDialog(null, data[1] + " möchte dich in die Gruppe " + data[4] + " hinzufügen", "Chatanfrage", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
										final Socket datenSocket = new Socket(dpp.getAddress(), Integer.valueOf(data[2]));
										Hauptfenster.appendNewLine("Verbindung zu vorläufigem daten-Socket hergestellt, PORT: " + datenSocket.getLocalPort());
										new Thread() {
											public void run() {
												try {
													new ObjectOutputStream(datenSocket.getOutputStream()).writeObject(new NetDataObject(NetDataObject.REQUESTINFOSINGLE));
													Hauptfenster.appendNewLine("REQUESTINFO VON RECEIVER");
													//OK?
													//datenSocket.close();
												} catch (IOException e) {
													Hauptfenster.appendNewLine("NetDataObject konnte nicht gesendet werden " + e.toString());
												}
											}
										}.start();
										for(;;) {
											Object o = new ObjectInputStream(datenSocket.getInputStream()).readObject();
											Hauptfenster.appendNewLine("OBJEKT ERHALTEN");
											NetDataObject ndo = (NetDataObject) o;
											Hauptfenster.appendNewLine("EFOLGREICHER CAST");
											Hauptfenster.appendNewLine("Objekt gesendet von: " + ndo.sender);
											Hauptfenster.appendNewLine("PEER ARRAY" + ndo.peerArray);
											if(ndo.type == NetDataObject.GETINFO) {
												for(Peer p : ndo.peerArray)
												System.out.println(p.getIa() + ":" + p.getPeerRemotePort());
												Hauptfenster.addGroup(new Group(ndo.peerArray, data[4]));
												Hauptfenster.appendNewLine("Daten erhalten. Neue Gruppe erstellt. SOCKET CLOSED");
												datenSocket.close();
												break;
											}
										}
									}*/
									/*Hauptfenster.addRequest(data[1] + " möchte dich in die Gruppe " + data[4] + " hinzufügen", new Runnable() {
										Socket datenSocket = null;
										@Override
										public void run() {
											System.out.println("pressed yes");
											try {
												System.out.println("waiting for socket");
												System.out.println(sA  + ":" + data[2]);
												datenSocket = new Socket(sA, Integer.valueOf(data[2]));
												System.out.println("Socket generated");
											} catch (NumberFormatException e1) {
												// TODO Auto-generated catch block
												e1.printStackTrace();
											} catch (IOException e1) {
												// TODO Auto-generated catch block
												e1.printStackTrace();
											}
											Hauptfenster.appendNewLine("Verbindung zu vorläufigem daten-Socket hergestellt, PORT: " + datenSocket.getLocalPort());
											new Thread() {
												public void run() {
													try {
														new ObjectOutputStream(datenSocket.getOutputStream()).writeObject(new NetDataObject(NetDataObject.REQUESTINFOSINGLE));
														Hauptfenster.appendNewLine("REQUESTINFO VON RECEIVER");
														//OK?
														//datenSocket.close();
													} catch (IOException e) {
														Hauptfenster.appendNewLine("NetDataObject konnte nicht gesendet werden " + e.toString());
													}
												}
											}.start();
											System.out.println("thread started");
											for(;;) {
												Object o = null;
												try {
													o = new ObjectInputStream(datenSocket.getInputStream()).readObject();
												} catch (IOException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												} catch (ClassNotFoundException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												}
												Hauptfenster.appendNewLine("OBJEKT ERHALTEN");
												NetDataObject ndo = (NetDataObject) o;
												Hauptfenster.appendNewLine("EFOLGREICHER CAST");
												Hauptfenster.appendNewLine("Objekt gesendet von: " + ndo.sender);
												Hauptfenster.appendNewLine("PEER ARRAY" + ndo.peerArray);
												if(ndo.type == NetDataObject.GETINFO) {
													for(Peer p : ndo.peerArray)
													System.out.println(p.getIa() + ":" + p.getPeerRemotePort());
													Hauptfenster.addGroup(new Group(ndo.peerArray, data[4]));
													Hauptfenster.appendNewLine("Daten erhalten. Neue Gruppe erstellt. SOCKET CLOSED");
													try {
														datenSocket.close();
													} catch (IOException e1) {
														// TODO Auto-generated catch block
														e1.printStackTrace();
													}
													break;
													
												}
											}
											System.out.println("end of al");
										}
									}, new ActionListener() { public void actionPerformed(ActionEvent e) { }});
									System.out.println("addedRequest");*/
									Hauptfenster.addRequest(data[1] + " möchte dich in die Gruppe " + data[4] + " hinzufügen", new Runnable() {
										Socket datenSocket = null;
										@Override
										public void run() {
											try {
											datenSocket = new Socket(sA, Integer.valueOf(data[2]));
											System.out.println("Socket generated");
											Hauptfenster.append("Verbindung zu vorläufigem daten-Socket hergestellt, PORT: " + datenSocket.getLocalPort(), true);
											new ObjectOutputStream(datenSocket.getOutputStream()).writeObject(new NetDataObject(NetDataObject.REQUESTINFOSINGLE));
											Hauptfenster.append("REQUESTINFO VON RECEIVER", true);
											for(;;) {
												Hauptfenster.append("Warte auf Daten", true);
												NetDataObject ndo = (NetDataObject) new ObjectInputStream(datenSocket.getInputStream()).readObject();
												Hauptfenster.append(ndo.sender + " schickt " + ndo.peerArray, true);
												if(ndo.type == NetDataObject.GETINFO) {
													Hauptfenster.addGroup(new Group(ndo.peerArray, data[4]));
													Hauptfenster.append("Peers enthalten. Neue Gruppe erstellt. SOCKET CLOSED", true);
													datenSocket.close();
													break;
												}
											}
										} catch(IOException e) {
											//MANCHMAL STREAMCORRUPTEDEXCEPTION, VERUSUCHEN MIT THREADING ZU LÖSEN
											Hauptfenster.append("Fehler beim erstellen der Gruppe " + e.toString(), true);
										} catch (ClassNotFoundException e) {
											Hauptfenster.append("Fehlerhafte Informationen geschickt oder veraltete Version benutzt", true);;
										}
										}
									}, null);
								}
							}
							else if(daten.trim().split(",").equals("triggerScan")) {
								new Broadcaster()
								.setMessage("scan")
								.run();
							}
						}
					} 
					else {
						Hauptfenster.append("Scan-Befehl blockiert", true);
					}
				}
			} catch (IOException e) {
				Hauptfenster.appendNewLine("Fehler mit der Verbindung");
				Hauptfenster.appendNewLine(e.getMessage());
				sc.close();
			}
	}
	
	private static void addToList(DatagramPacket dpp, String daten) {
		//Concurrency vom feinsten
		Peer peer = new Peer(dpp.getAddress(), dpp.getPort(), daten.trim().split(",")[1]);
		synchronized(peers) {
			if(peers.size() == 0) {
				peers.add(peer);
				Hauptfenster.addToList(peer);
				Hauptfenster.appendNewLine("Peer wurde der Liste hinzugefügt, IP: " + dpp.getAddress());
			}
			else {
				if(isAlreadyListed(dpp)) {
					Hauptfenster.appendNewLine("Peer wurde schon in die Liste eingefügt");
				} else {
					peers.add(peer);
					Hauptfenster.addToList(peer);
					Hauptfenster.appendNewLine("Peer wurde der Liste hinzugefügt, IP: " + dpp.getAddress());
				}
			}		
		}
	}
	
	private static boolean isAlreadyListed(DatagramPacket dpp) {
		for(Peer p : peers)
			if(p.getIa().equals(dpp.getAddress()))
				return true;
		
		return false;
	}
	
	public static void toggleScannable() {
		scannable = !scannable;
		Hauptfenster.updatTable().setValueAt(scannable, 5, 1);
	}
	
	//DEBUG
	public static void toggleSelfScan() {
		selfScan = !selfScan;
		Hauptfenster.updatTable().setValueAt(selfScan, 6, 1);
	}
}
