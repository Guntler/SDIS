package pt.up.fe.backup;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Packet {
	String[] msgArgs;
	
	/**
	 * @param args	<MessageType> <Version> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
	 */
	public Packet(String[] args) {
		msgArgs = args;
	}
	
	public void sendPacket(DatagramSocket socket, String mcastAddr, String mcastPort) throws IOException {
		byte[] buf = new byte[256];
		String msg = new String();
		
		for(int i=0;i<msgArgs.length;i++)
			msg += msgArgs[i];
		
		buf = msg.getBytes();
		InetAddress address = InetAddress.getByName(mcastAddr);
		DatagramPacket packet = new DatagramPacket(buf,buf.length,address,Integer.parseInt(mcastPort));
		
		socket.send(packet);
	}
}
