package pt.up.fe.backup;

import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class SocketHandler implements Runnable {

	CommunicationManager manager;
	MulticastSocket sock;
	boolean done;
	
	public SocketHandler(MulticastSocket s, CommunicationManager m) {
		this.sock = s;
		this.manager = m;
		done = false;
	}
	
	@Override
	public void run() {
		
		while(!done) {
		}
	}
}
