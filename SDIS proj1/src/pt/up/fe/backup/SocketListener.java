package pt.up.fe.backup;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
		try {
			socket.setSoTimeout(1000);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
		
		while(!finished) {
			byte[] buf = new byte[65536];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				socket.receive(packet);
				byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
				
				String packetString = new String(data, StandardCharsets.ISO_8859_1);
				System.out.println("Received the following packet: \n" + packetString.split("\\r\\n\\r\\n")[0]);
				manager.addPacketToReceived(new Packet(data, packet.getAddress()));
			} catch(SocketTimeoutException e1) {
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void toggleFinished() {
		if(finished)
			finished = false;
		else
			finished = true;
	}
}
