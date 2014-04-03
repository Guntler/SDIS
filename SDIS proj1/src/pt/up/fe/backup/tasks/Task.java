package pt.up.fe.backup.tasks;

import java.util.ArrayList;
import pt.up.fe.backup.Packet;

public abstract class Task implements Runnable {
	protected ArrayList<Packet> messages;
	
	public Task() {
		this.messages = new ArrayList<Packet>();
	}
	
	public synchronized void sendMessage(Packet p) {
		messages.add(p);
	}
	
	public abstract void run();
}
