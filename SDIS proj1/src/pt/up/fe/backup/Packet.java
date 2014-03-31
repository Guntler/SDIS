package pt.up.fe.backup;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

public class Packet {
	protected ArrayList<String> msgArgs = new ArrayList<String>();
	protected String packetType;
	protected byte[] fileID;
	protected int chunkNo;
	protected int replicationDeg;
	protected String version;
	protected byte[] data = null;
	protected InetAddress addr;
	
	
	public Packet(byte[] buf, InetAddress addr) {
		this.addr = addr;
		boolean crFound = false;
		byte[] header = null;
		byte[] data = null;

		for(int i = 0; i < buf.length; i++) {
			if(buf[i] == (byte) 0x0D) {
				crFound = true;

				if(i + 3 < buf.length) {
					if (buf[i+1] == (byte) 0x0A && buf[i+2] == (byte) 0x0D && buf[i+3] == (byte) 0x0A) {
						header = Arrays.copyOfRange(buf, 0, i+4);
						if(i+4 < buf.length)
							data = Arrays.copyOfRange(buf, i+4, buf.length);
						else
							data = null;

						break;
					}
				}
			}
		}
		
		if(!crFound) {
			System.out.println("Error, missing crlf");
		}
		else {
		
			this.data = data;
			String msg = new String(header, StandardCharsets.ISO_8859_1);
			
			if(msg.contains("PUTCHUNK")) {
				this.packetType = "PUTCHUNK";
				String[] packOptions = msg.split(" ");
				String version = packOptions[1];
				String fileID = packOptions[2];
				String chunkNo = packOptions[3];
				String repDeg = packOptions[4];
				this.fileID = hexToByte(fileID);
				this.version = version;
				this.chunkNo = Integer.parseInt(chunkNo);
				this.replicationDeg = Integer.parseInt(repDeg.split("\\r\\n")[0]);
			}
			else if(msg.contains("CHUNK")) {
				this.packetType = "CHUNK";
				String[] packOptions = msg.split(" ");
				String version = packOptions[1];
				String fileID = packOptions[2];
				String chunkNo = packOptions[3];

				this.fileID = hexToByte(fileID);
				this.version = version;
				this.chunkNo = Integer.parseInt(chunkNo.split("\\r\\n")[0]);
			}
			else if(msg.contains("GETCHUNK")) {
				this.packetType = "GETCHUNK";
				String[] packOptions = msg.split(" ");
				String version = packOptions[1];
				String fileID = packOptions[2];
				String chunkNo = packOptions[3];
				this.fileID = hexToByte(fileID);
				this.version = version;
				this.chunkNo = Integer.parseInt(chunkNo.split("\\r\\n")[0]);
			}
			else if(msg.contains("REMOVED")) {
				this.packetType = "REMOVED";
				String[] packOptions = msg.split(" ");
				String version = packOptions[1];
				String fileID = packOptions[2];
				String chunkNo = packOptions[3];
				
				this.fileID = hexToByte(fileID);
				this.version = version;
				this.chunkNo = Integer.parseInt(chunkNo.split("\\r\\n")[0]);
			}
			else if(msg.contains("STORED")) {
				String[] packOptions = msg.split(" ");
				String version = packOptions[1];
				String fileID = packOptions[2];
				String chunkNo = packOptions[3];
				this.fileID = Packet.hexToByte(fileID);
				this.version = version;
				this.packetType = "STORED";
				this.chunkNo = Integer.parseInt(chunkNo.split("\\r\\n")[0]);
			}
			else if(msg.contains("DELETE")) {
				this.packetType = "DELETE";
				String[] packOptions = msg.split(" ");
				String fileID = packOptions[1].split("\\r\\n")[0];
				this.fileID = hexToByte(fileID);
			}
		}
	}

	
	public Packet(String packetType, String version, BackupChunk chunk, InetAddress addr) {
		this.packetType = packetType;
		this.version = version;
		this.chunkNo = chunk.getChunkNo();
		this.replicationDeg = chunk.getWantedReplicationDegree();
		this.data = chunk.getData();
		this.fileID = chunk.getFileID();
		this.addr = addr;
	}
	
	public Packet(String packetType, String version, byte[] fileID, int chunkNo, int repDeg, byte[] body, InetAddress addr) {
		this.packetType = packetType;
		this.version = version;
		this.fileID = fileID;
		this.chunkNo = chunkNo;
		this.replicationDeg = repDeg;
		this.data = body;
		this.addr = addr;
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
		
		System.out.println("Sending packet: " + msg);
		
		msg += "\r\n\r\n";
		
		if(data != null) {
			buf = new byte[msg.length() + data.length];
			System.arraycopy(msg.getBytes(), 0, buf, 0, msg.length());
			System.arraycopy(data, 0, buf, msg.length(), data.length);
		}
		else
		{
			buf = new byte[msg.length()];
			System.arraycopy(msg.getBytes(), 0, buf, 0, msg.length());
		}
			
		DatagramPacket packet = new DatagramPacket(buf,buf.length,socket.getMcastGroup(),socket.getLocalPort());
		
		socket.send(packet);
	}
	
	public static String bytesToHex(byte[] array) {
	    return DatatypeConverter.printHexBinary(array);
	}
	
	public static byte[] hexToByte(String s) {
	    return DatatypeConverter.parseHexBinary(s);
	}
	
	public BackupChunk getChunk() {
		return new BackupChunk(fileID, chunkNo, data, null, data.length,replicationDeg, 1, null);
	}
	
	public InetAddress getSenderAddress() {
		return addr;
	}
}
