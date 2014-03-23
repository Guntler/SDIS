package pt.up.fe.backup;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class SocketListener implements Runnable {
	protected MulticastSocket socket = null;
	protected boolean finished = false;
	protected CommunicationManager manager;
	
	public SocketListener(MulticastSocket socket, CommunicationManager m) {
		this.socket = socket;
		this.manager = m;
	}
	
	@Override
	public void run() {
		while(!finished) {
			byte[] buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {e.printStackTrace();}
		}
	}
	
	public void toggleFinished() {
		if(finished)
			finished = false;
		else
			finished = true;
	}
}
