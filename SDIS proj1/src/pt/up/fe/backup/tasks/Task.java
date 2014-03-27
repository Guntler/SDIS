package pt.up.fe.backup.tasks;

import java.util.ArrayList;

import pt.up.fe.backup.CommunicationManager;
import pt.up.fe.backup.FileManager;
import pt.up.fe.backup.Packet;

public abstract class Task implements Runnable {
	protected FileManager fManager;
	protected ArrayList<Packet> messages;
	
	public Task(FileManager fManager) {
		this.fManager = fManager;
		this.messages = new ArrayList<Packet>();
	}
	
	public synchronized void sendMessage(Packet p) {
		messages.add(p);
	}
	
	public abstract void run();
}
