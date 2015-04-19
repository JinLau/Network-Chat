package Network;

import java.io.Serializable;

import GUI.Hauptfenster;

public class NetDataObject implements Serializable {
	
	private static final long serialVersionUID = 3L;
	public final static transient int MESSAGE = 0,
									  GETINFO = 1,
									  REQUESTINFO = 2,
									  REQUESTINFOSINGLE = 3,
									  FILE = 4,
									  COMMAND = 5;
	public final static int VIBRATE = 0,
							DAUMODE = 1;
	
	public int type;
	public int command;
	public String message,
				  sender,
				  filename,
				  memename,
				  sentTime;
	public Peer[] peerArray;
	public byte[] data;
	
	public NetDataObject(int type, String message, String sender, String sentTime) {
		this.type = type;
		this.message = message;
		this.sender = sender;
		this.sentTime = sentTime;
	}
	
	public NetDataObject(int type, Peer[] peerArray) {
		this.type = type;
		this.peerArray = peerArray;
	}
	
	public NetDataObject(byte[] data, String filename, String sender) {
		type = FILE;
		this.data = data;
		this.filename = filename;
		this.sender = sender;
	}
	
	public NetDataObject(int type) {
		this.type = type;
	}
	
	public NetDataObject(int type, int command, String sender) {
		this.type = type;
		this.command = command;
		this.sender = sender;
	}
	
	{
		Hauptfenster.appendNewLine("Benutze Version " + serialVersionUID + " des NetDataObjects");
	}
}
