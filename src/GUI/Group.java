package GUI;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Vector;

import javax.swing.JFrame;

import Network.Broadcaster;
import Network.NetDataObject;
import Network.Peer;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Font;

import jleu.convert.Bytes;

public class Group extends JFrame {
	
	//Performanter, siehe Hauptfenster
	private static final long serialVersionUID = 6743861205681081128L;
	//Klassenvariablen, Für jede instanz dieser Klasse gleich
	private static SystemTray st = SystemTray.getSystemTray();
	//ANDERES ICON??
	private static TrayIcon ti = new TrayIcon(new ImageIcon(Hauptfenster.class.getResource("/javax/swing/plaf/basic/icons/JavaCup16.png")).getImage());
	private static Group grMitNeuerNachricht;
	private static int notificationTime = 1000,
					   bytesReceived,
					   bytesSent;
	
	//Instanzvariablen, für jedes Objekt dieser Klasse anders
	private String host = System.getProperty("user.name"),
			       groupName;
	
	//GUI
	private JList members;
	private JLabel lblMembers;
	private JButton btnSync;
	private DefaultListModel mModel = new DefaultListModel();
	private JEditorPane txtrChatfenster = new JEditorPane(),
					  txtrEingabe = new JEditorPane();
	private HTMLDocument htd;
	private HTMLEditorKit hek;
	private JScrollBar jsb;
	private JButton btnAddPic = new JButton("Daten senden");
	private ButtonGroup bg = new ButtonGroup();
	private JRadioButton rbAlle = new JRadioButton("An Alle"),
				 		 rbEinzeln = new JRadioButton("Einzelner aus Liste");
	//ServerSocket für ObjectTransfer
	private ServerSocket ssO = null;
	private Vector<Peer> peerList = new Vector<Peer>();
	private Vector<Socket> connections = new Vector<Socket>();
	private int idSelected;
	
	//Fürs erstamlige erstellen der Gruppe
	/**
	 * @wbp.parser.constructor
	 */
	public Group(String groupName) {
		setTitle(this.groupName = groupName);
	}
	
	public Group(final Peer[] peer, String groupName) {
		setTitle(this.groupName = groupName);
		Hauptfenster.append("Gruppe über RECEIVER erstellt\n"
				+ "Peer ARRAY: " + Arrays.deepToString(peer), true);
		//Wow... eine lokale klasse
		class peerAddHelper extends Thread {
			
			private InetAddress address;
			private int port;
			private String peerName;
			
			public peerAddHelper(InetAddress address, int port, String peerName) {
				this.address = address;
				this.port = port;
				this.peerName = peerName;
			}
			
			public void run() {
					try {
						Hauptfenster.append("Bau Verbindung zu " + peerName + ", " + address + ":" + port, true);
						Socket newPeer = new Socket(address, port);
						connections.add(newPeer);
						warteAufDaten(newPeer);
						Hauptfenster.append(" Verbindung mit " + peerName + ", " + address + ":" + port + " aufgebaut \nund DatenListener hinzugefügt", true);
					} catch (IOException e) {
						Hauptfenster.append("Fehler beim Verbindungsaufbau " + e.toString(), true);
					}
			}
		}
		for(Peer p : peer) {
			new peerAddHelper(p.getIa(), p.getPeerRemotePort(), p.getName()).start();
			peerList.add(p);
			mModel.addElement(p.getName());
		}
	}
	
	//Code der am ende des Konstruktor angehängt wird
	{
		getContentPane().setLayout(null);
		hek = new HTMLEditorKit();
		txtrChatfenster.setEditable(false);
		txtrChatfenster.setContentType("text/html");
		txtrChatfenster.setEditorKit(hek);
		htd = (HTMLDocument) txtrChatfenster.getDocument();
		
		final JScrollPane jspChatfenster = new JScrollPane(txtrChatfenster);
		jsb = jspChatfenster.getVerticalScrollBar();
		jspChatfenster.setBounds(129, 43, 388, 188);
		getContentPane().add(jspChatfenster);
		
		txtrEingabe = new JTextPane();
		txtrEingabe.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER && !arg0.isShiftDown()) {
					schreiben();
				} else if(arg0.isShiftDown() && arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						txtrEingabe.getDocument().insertString(txtrEingabe.getCaretPosition(),
															   "\n",
															   null);
					} catch (BadLocationException e) {
						Hauptfenster.append("An dieser Stelle kann keine Neue Zeile eingefügt werden " + e.toString(), true);
					}
				}
			}
		});
		final JScrollPane jspEingabe = new JScrollPane(txtrEingabe);
		jspEingabe.setBounds(10, 276, 408, 75);
		getContentPane().add(jspEingabe);
		
		final JButton btnAbschicken = new JButton("Abschicken");
		btnAbschicken.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnAbschicken.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				schreiben();
			}
		});
		btnAbschicken.setBounds(428, 276, 89, 75);
		getContentPane().add(btnAbschicken);
		
		JButton btnPeerEinladung = new JButton("Jemand zum Gruppenchat einladen");
		btnPeerEinladung.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnPeerEinladung.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//VERBESSERUNGSWÜRDIG
				peerGruppenEinladung(JOptionPane.showInputDialog(Group.this, "Peernamen eingeben"));
			}
		});
		btnPeerEinladung.setBounds(10, 11, 215, 20);
		getContentPane().add(btnPeerEinladung);
		btnAddPic.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				 JFileChooser chooser = new JFileChooser();
				    int returnVal = chooser.showOpenDialog(getContentPane());
				    if(returnVal == JFileChooser.APPROVE_OPTION) {
				    	byte[] data = null;
				    	File f = chooser.getSelectedFile();
				    	InputStream is = null;
						try {
							is = new FileInputStream(f);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
				    	data = new byte[(int)f.length()];
				    	int offset = 0;
				    	int numRead = 0;
				    	try {
				    		//VERBESSERN
							while (offset < data.length &&(numRead=is.read(data, offset, data.length-offset)) >= 0) {
							        offset += numRead;
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
				    	if(rbAlle.isSelected())
				    	sendeDatenAnAlle(new NetDataObject(data, f.getName(), host));
				    	else {
				    		int selected = members.getSelectedIndex();
				    		if(selected < 0)
				    			JOptionPane.showMessageDialog(getContentPane(), "Niemand ausgewählt");
				    		else
				    			sendeDaten(connections.get(selected), new NetDataObject(data, f.getName(), host), false); //MÖGLICHERWEISE FALSCHER INDEX WEGEN CONCURRENCY
				    	}
				    }
			}
		});
		
		btnAddPic.setBounds(10, 242, 114, 23);
		getContentPane().add(btnAddPic);
		
		rbAlle.setFont(new Font("Tahoma", Font.PLAIN, 11));
		rbEinzeln.setFont(new Font("Tahoma", Font.PLAIN, 11));
		rbEinzeln.setSelected(true);
		rbEinzeln.setBounds(190, 238, 114, 31);
		rbAlle.setBounds(129, 238, 59, 31);
		bg.add(rbAlle);
		bg.add(rbEinzeln);
		
		getContentPane().add(rbAlle);
		getContentPane().add(rbEinzeln);
		
		try {
			ssO = new ServerSocket(0);
			Peer peer = new Peer(InetAddress.getLocalHost(), 0, ssO.getLocalPort(), host);
			peerList.add(peer);
			//mModel.addElement(peer.getName());
			
		final JPopupMenu rPop = new JPopupMenu("Test");
		rPop.setBounds(0, 0, 59, 16);
		
		final JMenuItem mnmtVib = new JMenuItem("Vibrieren");
		mnmtVib.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(idSelected >= 0)
					sendeDaten(connections.get(idSelected), new NetDataObject(NetDataObject.COMMAND,NetDataObject.VIBRATE,host), false); //Könnte falsch sein
				else 
					JOptionPane.showMessageDialog(null, "Niemand ausgewählt");
				rPop.setVisible(false);
			}
			
		});
		rPop.add(mnmtVib);
		
		final JMenuItem mntmDau = new JMenuItem("DAU-Exception auslösen");
		mntmDau.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(idSelected >= 0)
					sendeDaten(connections.get(idSelected), new NetDataObject(NetDataObject.COMMAND,NetDataObject.DAUMODE,host), false); //Könnte falsch sein
				else 
					JOptionPane.showMessageDialog(null, "Niemand ausgewählt");
				rPop.setVisible(false);
			}
			
		});
		rPop.add(mntmDau);
		
		getContentPane().add(rPop);
			
		members = new JList(mModel);
		members.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON3) {
					idSelected = members.locationToIndex(e.getPoint());
					members.setSelectedIndex(idSelected);
					rPop.setLocation(e.getLocationOnScreen());
					rPop.setVisible(true);
					rPop.show(members, e.getX(), e.getY());
				}
			}
		});
		members.setBounds(10, 107, 109, 124);
		getContentPane().add(members);
		
		lblMembers = new JLabel("Teilnehmer:");
		lblMembers.setBounds(10, 82, 74, 14);
		lblMembers.setText("Teilnehmer");
		lblMembers.setToolTipText("Namen aller Teilnehmer");
		getContentPane().add(lblMembers);
		
		btnSync = new JButton("synchronisieren");
		btnSync.setFont(new Font("Tahoma", Font.PLAIN, 11));
		btnSync.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				sendeDatenAnAlle(new NetDataObject(NetDataObject.REQUESTINFO));
			}
		});
		btnSync.setBounds(10, 43, 109, 28);
		getContentPane().add(btnSync);
			new Thread() {
				public void run() {
					for(;;) {
						try {
							Socket newPeer = ssO.accept();
							Hauptfenster.append("Neuer Peer mit Guppe Verbunden", true);
							connections.add(newPeer);
							warteAufDaten(newPeer);
							sendeDaten(newPeer, new NetDataObject(NetDataObject.REQUESTINFO), false);
							Hauptfenster.append("Daten angefordert oder gesendet", true);
						} catch(IOException e) {
							Hauptfenster.append("IOEXCEPTION " + e.toString(), true);
						}
					}
				}
			}.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setSize(543,400);
		setVisible(true);
		setLocationRelativeTo(null);
		getContentPane().addComponentListener(new ComponentAdapter() {
			//Achtung Shadowing
			Dimension ds = getSize(),
					  dsAbs = btnAbschicken.getSize(),
					  dsTxtpEingabe = jspEingabe.getSize(),
					  dsChat = jspChatfenster.getSize();
			Point pAbs = btnAbschicken.getLocation(),
				  pTxtpEingabe = jspEingabe.getLocation(),
				  pTxtpChatfenster = jspChatfenster.getLocation();
			int btnAbsWidth = dsAbs.width,
				btnAbsAbstandRechts = ds.width - (btnAbsWidth + pAbs.x),
				btnAbsHeight = btnAbschicken.getSize().height,
				btnAbstandUnten = ds.height - (btnAbsHeight + pAbs.y),
				txtpEingabeWidth = dsTxtpEingabe.width,
				txtpEingabeHeight = dsTxtpEingabe.height,
				txtpEingabeAbstandUnten = ds.height - (txtpEingabeHeight + pTxtpEingabe.y),
				txtpUndAbsAbstand = pAbs.x - pTxtpEingabe.x - txtpEingabeWidth,
				chatAbstandRechts = ds.width - dsChat.width;
			public void componentResized(ComponentEvent arg0) {
				//Shadowing	
				Dimension ds = getSize();
				btnAbschicken.setLocation(ds.width - (btnAbsAbstandRechts + btnAbsWidth), ds.height - (btnAbsHeight + btnAbstandUnten));
				jspEingabe.setLocation(pTxtpEingabe.x, ds.height - txtpEingabeAbstandUnten - txtpEingabeHeight);
				jspEingabe.setSize(ds.width - btnAbsWidth - btnAbsAbstandRechts - txtpUndAbsAbstand - pTxtpEingabe.x,jspEingabe.getSize().height);
				jspChatfenster.setSize(ds.width - 10 - chatAbstandRechts, ds.height - txtpEingabeHeight - txtpEingabeAbstandUnten - pTxtpChatfenster.y - 50);
				btnAddPic.setLocation(btnAddPic.getLocation().x, jspEingabe.getLocation().y - 10 - btnAddPic.getSize().height);
				jspEingabe.revalidate();
				jspChatfenster.revalidate();
			}
		});
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				sendeDatenAnAlle(new NetDataObject(NetDataObject.MESSAGE, host + " hat den Chat verlassen", "&lt;System&gt;", null));
			}
		});
	}
	
	//Static Initializer: Egal wie oft ein Objekt dieser Klasse erstellt wird, dieser Block wird nur einmal ausgeführt
	static {
		ti.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Hauptfenster.appendNewLine("<Group> [User] Benachrichtigung geklickt");
				grMitNeuerNachricht.setState(JFrame.NORMAL);
				grMitNeuerNachricht.toFront();
			}
		});
	}
	
	public Group peerGruppenEinladung(final String peerName) {
		new Broadcaster()
		.setMessage("requestGroup"
				  + ","
		          + System.getProperty("user.name") 
		          + "," 
		          + ssO.getLocalPort()
		          + ","
		          + peerName
		          + ","
		          + groupName)
		.run();
		return this;
	}
	
	public void addToChat(final String s) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					hek.insertHTML(htd, htd.getLength(), s, 0, 0, null);
				} catch(Exception e) {
					Hauptfenster.appendNewLine(Group.this.getClass(), "Fehler beim Einfügen des Textes");
				}
			}
			
		});
	}
	
	public void warteAufDaten(final Socket s) {
		new Thread() {
			public void run() {
				for(;!Thread.currentThread().isInterrupted();) {
					try {
						NetDataObject ndo = (NetDataObject) new ObjectInputStream(s.getInputStream()).readObject();
						Hauptfenster.appendNewLine(Group.this.getClass(), "NEUE DATEN ERHALTEN");
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						ObjectOutputStream oos = new ObjectOutputStream(baos);
						oos.writeObject(ndo);
						oos.flush();
						oos.close();
						Hauptfenster.updatTable().setValueAt(Bytes.convertBytes(bytesReceived += baos.toByteArray().length), 9, 1);
						if(ndo.type == NetDataObject.MESSAGE) {
							Calendar c = Calendar.getInstance();
							String received = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);
							addToChat(ndo.sentTime + ";" + received + ndo.sender + ":" + replacer(ndo.message/*.replace("\n", "<br>")*/ ));
							jsb.setValue(jsb.getMaximum());
							Hauptfenster.appendNewLine(Group.this.getClass(), "DATENTYP: Nachricht");
							if(!Group.this.isActive()) {
								benachrichtigen();
								grMitNeuerNachricht = Group.this;
							}
						} else if(ndo.type == NetDataObject.GETINFO) {
							for(Peer peer : ndo.peerArray) {
								if(!isAlreadyListed(peer.getIa(), peer.getPeerRemotePort())) {
									peerList.add(peer);
									mModel.addElement(peer.getName());
								}
								else 
									Hauptfenster.appendNewLine(Group.this.getClass(), "ALREADY LISTED");
							}
							Hauptfenster.appendNewLine(Group.this.getClass(), "DATENTYP: erhalte Verbindungsdaten zu Peers");
						} else if(ndo.type == NetDataObject.REQUESTINFO) {
							int listSize = peerList.size();
							sendeDaten(s, new NetDataObject(NetDataObject.GETINFO, peerList.toArray(new Peer[listSize])),false);
							Hauptfenster.appendNewLine(Group.this.getClass(), "Sende " + listSize + " PeerObjekte " + Arrays.deepToString(peerList.toArray(new Peer[listSize])));
							Hauptfenster.appendNewLine(Group.this.getClass(), "DATENTYP: REQUESTINFO");
						} else if(ndo.type == NetDataObject.REQUESTINFOSINGLE) {
							int listSize = peerList.size();
							sendeDaten(s, new NetDataObject(NetDataObject.GETINFO, peerList.toArray(new Peer[listSize])), true);
							Hauptfenster.appendNewLine(Group.this.getClass(), "Sende " + listSize + " PeerObjekte " + Arrays.deepToString(peerList.toArray(new Peer[listSize])));
							Hauptfenster.appendNewLine(Group.this.getClass(), "DATENTYP: REQUESTINFOSINGLE");
							Hauptfenster.appendNewLine(Group.this.getClass(), "DATEN AUSGETAUSCHT");
							Hauptfenster.appendNewLine(Group.this.getClass(), "Interrupt Thread");
							Thread.currentThread().interrupt();
						} else if(ndo.type == NetDataObject.FILE) {
							int ans = JOptionPane.showConfirmDialog(getContentPane(), ndo.sender + " möchte dir die datei: " + ndo.filename + " senden", "Datentransfer", JOptionPane.YES_NO_CANCEL_OPTION);
							if(ans == JOptionPane.YES_OPTION) {
							    JFileChooser saver = new JFileChooser();
							    saver.setSelectedFile(new File(ndo.filename));
							    saver.showSaveDialog(getContentPane());
							    try {
									FileOutputStream fos = new FileOutputStream(saver.getSelectedFile());
									fos.write(ndo.data);
									fos.close();
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						} else if(ndo.type == NetDataObject.COMMAND) {
							switch(ndo.command) {
							case NetDataObject.VIBRATE:
								Hauptfenster.callVibrate();
								break;
							case NetDataObject.DAUMODE:
								Hauptfenster.callDauMode();
								break;
							}
						}
						else {
							Hauptfenster.appendNewLine(Group.this.getClass(), "DATENTYP: Unbekannt");
						}
					} catch (IOException e) {
						Hauptfenster.appendNewLine(Group.this.getClass(), "'warteAufDaten()' " + e.toString() + " caused by " 
								+ s.getInetAddress() + ":" + s.getPort());
						e.printStackTrace();
						Hauptfenster.appendNewLine(Group.this.getClass(), "PORT DES BÖSEWICHTS " + s.getPort());
						try {
							s.close();
							Hauptfenster.appendNewLine(Group.this.getClass(), "Interrupting Thread");
							Thread.currentThread().interrupt();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					} catch (ClassNotFoundException e) {
						Hauptfenster.appendNewLine(Group.this.getClass(), "'warteAufDaten()' " + e.toString());
					}
				}
			}
		}.start();
	}
	
	public void sendeDaten(final Socket s, final NetDataObject ndo, final boolean closeAfterOperation) {
		new Thread() {
			public void run() {
				try {
					new ObjectOutputStream(s.getOutputStream()).writeObject(ndo);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(baos);
					oos.writeObject(ndo);
					oos.flush();
					oos.close();
					Hauptfenster.updatTable().setValueAt(Bytes.convertBytes(bytesSent += baos.toByteArray().length), 8, 1);
					if(closeAfterOperation) {
						s.close();
						Hauptfenster.appendNewLine(Group.this.getClass(), "Socket durch das Programm geschlossen und aus liste entfernt? " + connections.remove(s));
					}
				} catch (IOException e) {
					Hauptfenster.appendNewLine(Group.this.getClass(), "'sendeDaten()' " + e.toString());
				}
			}
		}.start();
	}
	
	public synchronized void sendeDatenAnAlle(NetDataObject ndo) {
		//Verursacht manchmal ConcurrentModificationException
		//mModel.clear();
		//peerList.clear();
		for(Socket s : connections)
			sendeDaten(s, ndo, false);
	}
	
	
	public synchronized boolean isAlreadyListed(InetAddress ia, int port) {
		for(Peer p: peerList)
			if(p.getIa().equals(ia) && p.getPeerRemotePort() == port)
				return true;
		
		return false;
	}
	
	public void schreiben() {
		Hauptfenster.appendNewLine(getClass(), "Abschicken geklickt. Nachricht: " + txtrEingabe.getText());
		Calendar c = Calendar.getInstance();
		String sent = c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);
		addToChat(sent + ", " + host + ": " /*+ "<pre>"*/ + replacer(txtrEingabe.getText()/*.replace("\n", "<br>")*/) /*+ "</pre>"*/);
		sendeDatenAnAlle(new NetDataObject(NetDataObject.MESSAGE, /*"<pre>" + */txtrEingabe.getText()/*.replace("\n", "<br>")*/ /*+ "</pre>"*/, host, sent));
		txtrEingabe.setText("");
	}
	
	public void benachrichtigen() {
		Hauptfenster.appendNewLine(getClass(), "Füge Benachrichtigung hinzu");
		try {
			if(st.getTrayIcons().length == 0) {
				st.add(ti);
				Hauptfenster.appendNewLine("ADDED");
			}
		} catch (AWTException e) {
			Hauptfenster.appendNewLine(getClass(), "Fehler beim hinzufügen eines TrayIcons zum anzeigen von Nachrichten");
		}
		ti.displayMessage("Java Chat", "Neue Nachricht", TrayIcon.MessageType.INFO);
		Hauptfenster.appendNewLine(getClass(), "DISPLAYED");
		Thread waitThread = new Thread() {
			public void run() {
				try {
					Thread.sleep(notificationTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		waitThread.start();
		Hauptfenster.appendNewLine(getClass(), "TSTART");
		try {
			waitThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Hauptfenster.appendNewLine(getClass(), "TJOINED");
		st.remove(ti);
		Hauptfenster.appendNewLine(getClass(), "REMOVED");
	}
	
	public static void setNotificationTime(int time) {
		notificationTime = time * 1000;
	}
	
	public int getSocketCount() {
		return connections.size();
	}
	
	public String replacer(String s) {
		return s.replace("[[yds]]", "<img src=\"" + getClass().getResource("/images/yds.png").toString()+"\" >")
				.replace("[[bps]]", "<img src=\"" + getClass().getResource("/images/bps.png").toString()+"\" >")
				.replace("[[fkm]]", "<img src=\"" + getClass().getResource("/images/fkm.jpg").toString()+"\" >")
				.replace("[[fp]]", "<img src=\"" + getClass().getResource("/images/fp.png").toString()+"\" >")
				.replace("[[trf]]", "<img src=\"" + getClass().getResource("/images/trf.png").toString()+"\" >")
				.replace("[[umt]]", "<img src=\"" + getClass().getResource("/images/umt.gif").toString()+"\" >");
	}
}
