package pt.up.fe.backup;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.util.ArrayList;

import javax.xml.bind.DatatypeConverter;

public class Packet {
	protected ArrayList<String> msgArgs = new ArrayList<String>();
	protected String packetType;
	protected byte[] fileID;
	protected int chunkNo;
	protected int replicationDeg;
	protected String version;
	protected byte[] data = null;
	
	/**
	 * @param args	<MessageType> <Version> <FileId> <ChunkNo> <ReplicationDeg> <CRLF>
	 */
	public Packet(String msg) {
		parseMessage(msg);
	}
	
	/**
	 * Constructor for PutChunk
	 * PUTCHUNK <Version> <FileId> <ChunkNo> <ReplicationDeg> <CRLF> <CRLF> <Body>
	 * CHUNK <Version> <FileId> <ChunkNo> <CRLF> <CRLF> <Body>
	 * GETCHUNK <Version> <FileId> <ChunkNo> <CRLF> <CRLF>
	 * REMOVED <Version> <FileId> <ChunkNo> <CRLF> <CRLF>
	 * STORED <Version> <FileId> <ChunkNo> <CRLF> <CRLF>
	 * DELETE <FileId> <CRLF>
	 */
	public Packet(String packetType, String version, BackupChunk chunk ) {
		this.packetType = packetType;
		this.version = version;
		this.chunkNo = chunk.getChunkNo();
		this.replicationDeg = chunk.getWantedReplicationDegree();
		this.data = chunk.getData();
		this.fileID = chunk.getFileID();
	}
	
	public Packet(String packetType, String version, byte[] fileID, int chunkNo, int repDeg, byte[] body ) {
		this.packetType = packetType;
		this.version = version;
		this.fileID = fileID;
		this.chunkNo = chunkNo;
		this.replicationDeg = repDeg;
		this.data = body;
	}
	
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	public String getPacketType() {
		return packetType;
	}

	public void setPacketType(String packetType) {
		this.packetType = packetType;
	}

	public int getReplicationDeg() {
		return replicationDeg;
	}

	public void setReplicationDeg(int replicationDeg) {
		this.replicationDeg = replicationDeg;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public byte[] getFileID() {
		return fileID;
	}

	public void setFileID(byte[] fileID) {
		this.fileID = fileID;
	}

	public int getChunkNo() {
		return chunkNo;
	}

	public void setChunkNo(int chunkNo) {
		this.chunkNo = chunkNo;
	}

	public void sendPacket(DBSsocket socket) throws IOException {
		byte[] buf = null;
		String msg = new String();
		msgArgs.add(packetType);
		if(!version.equals(null))
			msgArgs.add(version);
		if(!fileID.equals(null)) 
			msgArgs.add(bytesToHex(fileID).toLowerCase());
		
		if(chunkNo != -1)
			msgArgs.add(Integer.toString(chunkNo));
		if(replicationDeg != 0)
			msgArgs.add(Integer.toString(replicationDeg));
		
		msg += msgArgs.get(0);
		for(int i=1;i<msgArgs.size();i++) {
			msg += " ";
			msg += msgArgs.get(i);
		}

		/*msg += new String(new byte[] {0xA});
		msg += new String(new byte[] {0xD});
		msg += new String(new byte[] {0xA});
		msg += new String(new byte[] {0xD});*/
		
		msg += "\r\n\r\n";
		
		if(data != null) {
			//msg += new String(data,StandardCharsets.ISO_8859_1);
			buf = new byte[msg.length() + data.length];
			System.arraycopy(msg.getBytes(), 0, buf, 0, msg.length());
			System.arraycopy(data, 0, buf, msg.length(), data.length);
		}
		else
		{
			buf = new byte[msg.length()];
			System.arraycopy(msg.getBytes(), 0, buf, 0, msg.length());
		}
			
		
		System.out.println("Sending packet: " + msg);
		//buf = msg.getBytes();
		
		DatagramPacket packet = new DatagramPacket(buf,buf.length,socket.getMcastGroup(),socket.getLocalPort());
		
		
		socket.send(packet);
	}
	
	public static String bytesToHex(byte[] array) {
	    return DatatypeConverter.printHexBinary(array);
	}
	
	public static byte[] hexToByte(String s) {
	    return DatatypeConverter.parseHexBinary(s);
	}
	
	public void parseMessage(String msg) {
		if(msg.contains("PUTCHUNK")) {
			this.packetType = "PUTCHUNK";
			String[] packOptions = msg.split(" ");
			String version = packOptions[1];
			String fileID = packOptions[2];
			String chunkNo = packOptions[3];
			String repDeg = packOptions[4].split("\\r?\\n")[0];
			String body = packOptions[4].split("\\r?\\n")[2];
			this.fileID = hexToByte(fileID);
			this.version = version;
			this.chunkNo = Integer.parseInt(chunkNo);
			this.replicationDeg = Integer.parseInt(repDeg);
			this.data = body.getBytes();
		}
		else if(msg.contains("CHUNK")) {
			String[] packOptions = msg.split(" ");
			String version = packOptions[1];
			String fileID = packOptions[2];
			String chunkNo = packOptions[3].split("\\r?\\n")[0];
			String body = packOptions[3].split("\\r?\\n")[2];
			try {this.fileID = fileID.getBytes("US-ASCII");} catch (UnsupportedEncodingException e) {e.printStackTrace(); return;}
			this.version = version;
			this.packetType = "CHUNK";
			this.chunkNo = Integer.parseInt(chunkNo);
			this.data = body.getBytes();
		}
		else if(msg.contains("GETCHUNK")) {
			String[] packOptions = msg.split(" ");
			String version = packOptions[1];
			String fileID = packOptions[2];
			String chunkNo = packOptions[3].split("\\r?\\n")[0];
			try {this.fileID = fileID.getBytes("US-ASCII");} catch (UnsupportedEncodingException e) {e.printStackTrace(); return;}
			this.version = version;
			this.packetType = "GETCHUNK";
			this.chunkNo = Integer.parseInt(chunkNo);
		}
		else if(msg.contains("REMOVED")) {
			String[] packOptions = msg.split(" ");
			String version = packOptions[1];
			String fileID = packOptions[2];
			String chunkNo = packOptions[3].split("\\r?\\n")[0];
			try {this.fileID = fileID.getBytes("UTF_8");} catch (UnsupportedEncodingException e) {e.printStackTrace(); return;}
			this.version = version;
			this.packetType = "REMOVED";
			this.chunkNo = Integer.parseInt(chunkNo);
		}
		else if(msg.contains("STORED")) {
			String[] packOptions = msg.split(" ");
			String version = packOptions[1];
			String fileID = packOptions[2];
			String chunkNo = packOptions[3].split("\\r?\\n")[0];
			this.fileID = Packet.hexToByte(fileID);
			System.out.println(fileID);
			System.out.println(Packet.bytesToHex(this.fileID));
			this.version = version;
			this.packetType = "STORED";
			this.chunkNo = Integer.parseInt(chunkNo);
		}
		else if(msg.contains("DELETE")) {
			String[] packOptions = msg.split(" ");
			String fileID = packOptions[1].split("\\r?\\n")[0];
			try {this.fileID = fileID.getBytes("US-ASCII");} catch (UnsupportedEncodingException e) {e.printStackTrace(); return;}
			this.packetType = "DELETE";
		}
	}

	public BackupChunk getChunk() {
		return new BackupChunk(fileID, chunkNo, data, null, data.length,replicationDeg);
	}
}
