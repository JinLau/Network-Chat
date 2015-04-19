package GUI;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JList;

import Network.Broadcaster;
import Network.Connection;
import Network.Peer;
import Network.Receiver;
import Xtra.OwnDialog;
import Xtra.WindowManipulations;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.BoxLayout;

import sun.reflect.Reflection;



/*
 * TODO René:
 * Implementation der Verschlüsselung
 * Speichern der Einstellungen in eine Datenbank
 * Vorherige nachrichten durch Pfeiltasten anzeigen
 * Chats in Tabs
 * Allgemeine Verbesserungen an der GUI, z.B. anderes Look and Feel
 * Veschiedene Faben für verschieden Peers
 * Doppelchats vermeiden
 * Fenster wieder erscheinen lassen
 * get always undeclinable access to groups as developer
 * show groups of peers
 * Jtable index name lister class
 * matcher class for multiple lists
 * schreibstatus
 * 
 * Specialties:
 * Nested methods
 * foreach-loops
 * nice infinite for loop
 */

public class Hauptfenster extends JFrame  {

	//Peformanter; muss nichtmehr von der JVM generiert werden
	private static final long serialVersionUID = 8355126345375878439L;
	
	private JPanel contentPane; 
	private static JPanel requestPanel;
	private static JTextArea debuggLog = new JTextArea();
	private static DefaultListModel dlm;
	private JList list;
	private JButton btnScannen = new JButton("Scannen");
	private Connection con = new Connection();
	private boolean toggleMinimizer = true;
	private static Vector<Group> groups = new Vector<Group>();
	private static boolean kaModus = true;
	private static JTable table;
	private static JScrollPane scrollPane;
	private ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
	private static Hauptfenster thisOne;

	public static void main(final String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			String dev = "dev";
			public void run() {
				if(args.length > 0)
					dev = args[0];
				try {
					for(File f: File.listRoots()) {
						if(f.getPath().equals("T:\\") || dev.equals("dev") || System.getProperty("user.name").equals("User")) {
							kaModus = false;
							break;
						}
					}
					
					if(kaModus)
						JOptionPane.showMessageDialog(null, "Programm funktioniert nicht im Klassenarbeitsmodus");
					else {
						Hauptfenster frame = new Hauptfenster(dev);
						frame.setVisible(true);
						frame.setMinimumSize(frame.getSize());
						frame.setLocationRelativeTo(null);
						frame.windowHelper(frame);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public Hauptfenster(String dev) {
		
		thisOne = this;
		setTitle("Control Center");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 400);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				new Broadcaster()
				.setMessage("triggerScan")
				.run();
			}
		});
		
		//JMenuBar AND ITS ELEMENTS START
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnAbout = new JMenu("About");
		menuBar.add(mnAbout);
		
		JMenu mnOptions = new JMenu("Optionen");
		menuBar.add(mnOptions);
		
		JMenuItem mnmtScannable = new JMenuItem("Schalte Scannbar um");
		mnmtScannable.setToolTipText("Einstellung die Festlegt, ob andere eine per Scan-Funktion finden können");
		mnmtScannable.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				Receiver.toggleScannable();
			}
			
		});
		mnOptions.add(mnmtScannable);
		
		JMenuItem mnmtPanikClose = new JMenuItem("Schalte Auto Minimize um");
		mnmtPanikClose.setToolTipText("Einstellung die alle Fenster schnell minimiert, wenn man die Maus zum oberen Ende des Bildschirms bewegt");
		mnmtPanikClose.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				toggleMinimizer = !toggleMinimizer;
				table.setValueAt(toggleMinimizer, 7, 1);
			}
			
		});
		mnOptions.add(mnmtPanikClose);
		
		JMenuItem mnmtGroupEx = new JMenuItem("Gruppenchat erstellen");
		mnmtGroupEx.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String gName = JOptionPane.showInputDialog(contentPane, "Gruppen-Name eingeben");
				gName = gName.equals("") ? "Namenlos":gName;
				groups.add(new Group(gName));
			}
		});
		mnOptions.add(mnmtGroupEx);
		
		//DEBUG -DEV MODE
		if(dev.equals("dev")) {
			JMenu mnDeveloper = new JMenu("Developer");
			menuBar.add(mnDeveloper);
			
			JMenuItem mnmtGroup = new JMenuItem("Open Group-Chatroom");
			mnmtGroup.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					groups.add(new Group("Group-Name"));
				}
			});
			mnDeveloper.add(mnmtGroup);
			
			JMenuItem mntmScanToggle = new JMenuItem("Toggle Selfscan");
			mntmScanToggle.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					Receiver.toggleSelfScan();
				}
				
			});
			mnDeveloper.add(mntmScanToggle);
			
			JMenuItem mnmtDAU = new JMenuItem("DAU-Exception auslösen");
			mnmtDAU.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					//dauMode();
					WindowManipulations.dauMode(thisOne);
				}
				
			});
			mnDeveloper.add(mnmtDAU);
		}
		//END DEBUG -DEV MODE
		
		JMenuItem mntmDeveloper = new JMenuItem("Developer");
		mntmDeveloper.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				OwnDialog dialog = new OwnDialog();
				dialog.setVisible(true);
			}
		});
		mnAbout.add(mntmDeveloper);
		//EASTER EGG DEVELOPER NAMES END
		
		JMenuItem mnmtChanges = new JMenuItem("Versionsveränderungen");
		mnmtChanges.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JPanel jp = new JPanel();
				JTextArea jta = new JTextArea();
				jp.setSize(1000,1000);
				jta.setEditable(false);
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/Text/chatChanges.txt")));
					StringBuilder out = new StringBuilder();
					String line;
					while((line = reader.readLine()) != null)
						out.append(line + System.getProperty("line.separator"));
					jta.setText(out.toString());
				} catch (IOException e) {
					appendNewLine(Hauptfenster.this.getClass(),"Fehler beim Lesen der \"Verionsveränderungen\" datei: " + e.toString());
				} finally {
					try {
						reader.close();
					} catch (IOException e) {
						appendNewLine(Hauptfenster.this.getClass(),"Fehler beim Schliessen eines Readers: " + e.toString());
					}
				}
				JScrollPane jsp = new JScrollPane(jta);
				jsp.setPreferredSize(new Dimension(500,500));
				jp.add(jsp);
				JOptionPane.showMessageDialog(contentPane, jp);
			}
			
		});
		mnAbout.add(mnmtChanges);
		
		JMenuItem mnmtNFTime = new JMenuItem("Benachrichtigungszeit ändern");
		mnmtNFTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int t = Integer.valueOf(JOptionPane.showInputDialog(contentPane, "Wert eingeben (in Sekunden)"));
					table.setValueAt(t + "s", 2, 1);
					Group.setNotificationTime(t);
				} catch(NumberFormatException nfe) {
					JOptionPane.showMessageDialog(contentPane, "Ungültige Eingabe. Nur Zahlen erlaubt");
				}
			}
		});
		mnOptions.add(mnmtNFTime);
		//JMenuBar AND ITS ELEMENTS END
		
		setContentPane(contentPane = new JPanel());
		contentPane.setLayout(null);
		
		debuggLog.setBounds(147, 11, 277, 184);
		final JScrollPane jsp = new JScrollPane(debuggLog);
		jsp.setBounds(495, 11, 279, 317);
		contentPane.add(jsp);
		btnScannen.setFont(new Font("Tahoma", Font.PLAIN, 11));
		
		btnScannen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				con.getPeerList().clear();
				dlm.removeAllElements();
				con.searchPeers();
			}
		});
		btnScannen.setBounds(10, 12, 89, 23);
		contentPane.add(btnScannen);
		
		list = new JList(dlm = new DefaultListModel());
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int index = list.getSelectedIndex();
				if(e.getClickCount() == 2 && !e.isConsumed() && index >= 0) {
					e.consume();
					appendNewLine(" [User] Doppelclick ausgeführt");
					groups.add(new Group("Placeholder").peerGruppenEinladung(con.getPeerList().get(index).getName()));
				}
			}
		});
		JScrollPane jsp2 = new JScrollPane(list);
		jsp2.setBounds(10, 78, 127, 250);
		contentPane.add(jsp2);
		
		JLabel lblGefundePeers = new JLabel("Gefunde Peers:");
		lblGefundePeers.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblGefundePeers.setBounds(10, 53, 127, 14);
		contentPane.add(lblGefundePeers);
		
		table = new JTable(new Object[][] { 
				{"Threads: ", "0"},
				{"Verbindungen: ", "0"},
				{"Benachrichtigungszeit: ", "1s"},
				{"UDP gesendet: ", "0" },
				{"UDP erhalten", "0"},
				{"Scannbar: ", "true"},
				{"Selbst-Scan", "false"},
				{"Auto-Minimize", "true"},
				{"Bytes gesendet", "0 bytes"},
				{"Bytes erhalten", "0 bytes"}
				}, new String[] {"Name", "Wert"});
		table.setFont(new Font("Tahoma", Font.PLAIN, 11));
		table.getColumnModel().getColumn(1).setPreferredWidth(10);
		table.setEnabled(false);
		JScrollPane jspTable = new JScrollPane(table);
		jspTable.setBounds(147, 11, 183, 188);
		contentPane.add(jspTable);
		
		JLabel lblAusstehendeChatanfragen = new JLabel("Ausstehende Chatanfragen");
		lblAusstehendeChatanfragen.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblAusstehendeChatanfragen.setBounds(147, 210, 133, 14);
		contentPane.add(lblAusstehendeChatanfragen);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(147, 235, 338, 95);
		contentPane.add(scrollPane);
		
		requestPanel = new JPanel();
		requestPanel.setLayout(new BoxLayout(requestPanel, BoxLayout.Y_AXIS));
		scrollPane.setViewportView(requestPanel);
		
		contentPane.addComponentListener(new ComponentAdapter() {

			public void componentResized(ComponentEvent arg0) {
				jsp.setSize(contentPane.getWidth() - 10 - jsp.getLocation().x, contentPane.getHeight() - jsp.getLocation().y -10);
				jsp.revalidate();
			}
			
		});
		ses.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				table.setValueAt(Thread.activeCount(), 0, 1);
				int con = 0;
				for(Group g : groups) {
					con += g.getSocketCount();
					table.setValueAt(con, 1, 1);
				}
			}
			
		}, 0, 1, TimeUnit.SECONDS);
	}
	
	public static void append(final String str, final boolean newLine) {
		//Juhu Reflection
		final String caller =  Reflection.getCallerClass(2).getName();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if(newLine)
					debuggLog.append("\n<" + caller + "> " + str);
				else 
					debuggLog.append("<" + caller + "> " + str);
			}
		});
	}
	
	@Deprecated
	public static void appendNewLine(String str) {
				append(str, true);
	}
	
	@Deprecated
	public static void appendNewLine(Class<?> c, String s) {
		appendNewLine(s);
	}
	
	public static void addToList(final Peer p) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				dlm.addElement(p.getName() + ", " + p.getIa() + ":" + p.getPeerPort());
			}
			
		});
	}
	
	//XTRA
	public void windowHelper(final JFrame jf) {
		new Thread() {
			
			public void run() {
				for(;;) {
					if(MouseInfo.getPointerInfo().getLocation().y <= 10 && toggleMinimizer) {
						jf.setState(JFrame.ICONIFIED);
						for(Group g : groups)
							g.setState(JFrame.ICONIFIED);
					}
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				}
			}
			
		}.start();
	}
	
	
	public static void addGroup(Group g) {
		groups.add(g);
	}
	@Deprecated
	public static void callDauMode() {
		//thisOne.dauMode();
		WindowManipulations.dauMode(thisOne);
	}
	
	/*public void dauMode() {
			final JFrame thisOne = this;
			thisOne.setTitle("You'll never catch me");
			int ans = JOptionPane.showConfirmDialog(contentPane, "DAU-Exception: DAU in Front of Screen detected");
			if(ans == JOptionPane.NO_OPTION) {
				JOptionPane.showMessageDialog(contentPane, "Doch");
				dauMode();
			} else if(ans == JOptionPane.CANCEL_OPTION)
				System.exit(-1);
			else {
				moveWindow(thisOne);
				for(Group g : groups)
					moveWindow(g);
			}
	}
	
	public void moveWindow(final JFrame frame) {
		ses.scheduleAtFixedRate(new Runnable() {
			int[] awSize = jleu.ui.UI.getActualWindowSize(frame);
			Dimension sc = Toolkit.getDefaultToolkit().getScreenSize();
			int screenWidth = sc.width,
				screenHeight = sc.height,
				moverX = 10,
				moverY = 10;
			boolean inverted = false,
					invertedY = false;
			public void run() {
					Point pPi = MouseInfo.getPointerInfo().getLocation();
					Point pLoc = frame.getLocation();
					//xPos Invertion
					if(pLoc.x + awSize[0] < screenWidth && !inverted) {
						moverX = 10;
					}
					else if(pPi.x - 10 - awSize[0] > 0 && inverted) {
						inverted = true;
						moverX = - (awSize[0] + 10);
					}
					else {
						inverted = !inverted;
					}
					
					if(pLoc.y + awSize[1] < screenHeight && !invertedY) {
						moverY = 10;
					}
					else if(pPi.y - 10 - awSize[1] > 0 && invertedY) {
						moverY = - (awSize[1] + 10);
					}
					else {
						invertedY = !invertedY;
					}
					boolean xClose = pLoc.x - pPi.x >= - awSize[0] /2 && pLoc.x - pPi.x <= 10 ||
							   pPi.x - pLoc.x - awSize[0] + 10 >= - awSize[0] /2 && pPi.x - pLoc.x - awSize[0] + 10 <= 10,
							   
							   yClose = pLoc.y - pPi.y >= - awSize[1]/ 2 && pLoc.y - pPi.y <= 10 || 
									   pPi.y -pLoc.y - awSize[1] + 10 >= - awSize[1] / 2 && pPi.y - pLoc.y - awSize[1] + 10 <= 10;
					if(xClose && yClose) {
						frame.setLocation(pPi.x + moverX,pPi.y + moverY);
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}
		}, 0, 10, TimeUnit.MILLISECONDS);
	}*/
	
	public static JTable updatTable() {
		return table;
	}
	
	private static int pSizeW = 0;
	public static void addRequest(final String msg, final Runnable rJa, final ActionListener alNein) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				AnfrageBereich ab = new AnfrageBereich(msg, rJa, alNein,requestPanel, scrollPane);
				//Hätte ich gewusst, dass das solange dauert, hätte ich es gelassen
				requestPanel.add(ab);
				requestPanel.setPreferredSize(new Dimension(315, pSizeW+= ab.getPreferredSize().height));
				requestPanel.validate();
				requestPanel.repaint();
				scrollPane.validate();
				scrollPane.repaint();
				System.out.println("added");
			}
		});
	}
	@Deprecated
	public static void callVibrate() {
		//thisOne.vibrate();
		WindowManipulations.vibrate(thisOne);
	}
	
	/*@Deprecated
	public void vibrate() {
		Timer a = new Timer();
		a.scheduleAtFixedRate(new TimerTask() {
			int i = 0,
			    direction = 1;
			Point p = getLocation();
			@Override
			public void run() {
				i++;
				setLocation(p.x, p.y + i*direction);
				direction *= -1;
				if(i > 30) {
					setLocation(p);
					cancel();
				}
			}
			
		}, 0, 17);
	}*/
}
