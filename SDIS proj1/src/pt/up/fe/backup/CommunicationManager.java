package pt.up.fe.backup;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

public class CommunicationManager implements Runnable {
	static public enum CommandTypes {GETCHUNK, PUTCHUNK, RESTORE, REMOVED, STORED, CHUNK, DELETE};
	static public enum Channels {MC, MDB, MDR};
	protected ArrayList<String> mcastArgs;		//<IPMC> <portMC> <ipMDB> <portMDB> <ipMDR> <portMDR>
	protected MulticastSocket socketMC = null;
	protected MulticastSocket socketMDB = null;
	protected MulticastSocket socketMDR = null;
	protected SocketHandler handlerMC = null;
	protected SocketHandler handlerMDB = null;
	protected SocketHandler handlerMDR = null;
	private boolean done;
	private ArrayList<Packet> receivedQueue;
	
	private DistributedBackupSystem dbs;

	public CommunicationManager(ArrayList<String> mcastArgs, DistributedBackupSystem dbs) throws IOException {
		this.mcastArgs = mcastArgs;
		this.dbs = dbs;
		
		final int mCastPort = Integer.parseInt(this.mcastArgs.get(1));
		final int mBackupPort = Integer.parseInt(this.mcastArgs.get(3));
		final int mRecoverPort = Integer.parseInt(this.mcastArgs.get(5));
		
		socketMC = new MulticastSocket(mCastPort);
		socketMDB = new MulticastSocket(mBackupPort);
		socketMDR = new MulticastSocket(mRecoverPort);
		InetAddress groupMC = InetAddress.getByName(this.mcastArgs.get(0));
		InetAddress groupMDB = InetAddress.getByName(this.mcastArgs.get(2));
		InetAddress groupMDR = InetAddress.getByName(this.mcastArgs.get(4));
		socketMC.joinGroup(groupMC);
		socketMDB.joinGroup(groupMDB);
		socketMDR.joinGroup(groupMDR);
		
		done = false;
		receivedQueue = new ArrayList<Packet>();
	}

	@Override
	public void run() {
		handlerMC = new SocketHandler(socketMC, this);
		handlerMDB = new SocketHandler(socketMDB, this);
		handlerMDR = new SocketHandler(socketMDR, this);
		Thread threadMC = new Thread(handlerMC);
		Thread threadMDB = new Thread(handlerMDB);
		Thread threadMDR = new Thread(handlerMDR);
		threadMC.run();threadMDB.run();threadMDR.run();
		
		while(!done) {
			if(receivedQueue.size() != 0) {
				dbs.getTManager().executeTask(receivedQueue.get(0));
				receivedQueue.remove(0);
			}
		}
	};
	
	public void sendPacket(Packet p, Channels channel) throws IOException {
		if(channel == Channels.MC) {
			p.sendPacket(socketMC);
		}
		else if(channel == Channels.MDB) {
			p.sendPacket(socketMDB);
		}
		else if(channel == Channels.MDR) {
			p.sendPacket(socketMDR);
		}
	}
	
	public void addPacketToReceived(Packet p) {
		this.receivedQueue.add(p);
	}
}
