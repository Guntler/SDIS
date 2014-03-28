package pt.up.fe.backup;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import pt.up.fe.backup.tasks.test;

public class CommunicationManager implements Runnable {
	static public enum CommandTypes {GETCHUNK, PUTCHUNK, RESTORE, REMOVED, STORED, CHUNK, DELETE};
	static public enum Channels {MC, MDB, MDR};
	protected ArrayList<String> mcastArgs;		//<IPMC> <portMC> <ipMDB> <portMDB> <ipMDR> <portMDR>
	protected DBSsocket socketMC = null;
	protected DBSsocket socketMDB = null;
	protected DBSsocket socketMDR = null;
	protected SocketListener listenerMC = null;
	protected SocketListener listenerMDB = null;
	protected SocketListener listenerMDR = null;
	private boolean done;
	private ArrayList<Packet> receivedQueue;
	
	private DistributedBackupSystem dbs;

	public CommunicationManager(ArrayList<String> mcastArgs, DistributedBackupSystem dbs) throws IOException {
		this.mcastArgs = mcastArgs;
		this.dbs = dbs;
		
		final int mCastPort = Integer.parseInt(this.mcastArgs.get(1));
		final int mBackupPort = Integer.parseInt(this.mcastArgs.get(3));
		final int mRecoverPort = Integer.parseInt(this.mcastArgs.get(5));
		socketMC = new DBSsocket(mCastPort);
		//socketMC.setLoopbackMode(true);
		socketMDB = new DBSsocket(mBackupPort);
		socketMDB.setLoopbackMode(true);
		socketMDR = new DBSsocket(mRecoverPort);
		socketMDR.setLoopbackMode(true);
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
		listenerMC = new SocketListener(socketMC, this);
		listenerMDB = new SocketListener(socketMDB, this);
		listenerMDR = new SocketListener(socketMDR, this);
		Thread threadMC = new Thread(listenerMC);
		Thread threadMDB = new Thread(listenerMDB);
		Thread threadMDR = new Thread(listenerMDR);
		threadMC.start();threadMDB.start();threadMDR.start();

		while(!done) {
			if(receivedQueue.size() != 0) {
				//System.out.print("I received a packet!");
				//DistributedBackupSystem.tManager.executor.execute(new test(DistributedBackupSystem.fManager, "wtf"));
				//System.out.println(receivedQueue.get(0).getPacketType());
				synchronized(this) {
					DistributedBackupSystem.tManager.handlePacket(receivedQueue.get(0));
					receivedQueue.remove(0);
				}
			}
		}
	};
	
	synchronized public void sendPacket(Packet p, Channels channel) throws IOException {
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
	
	synchronized public void addPacketToReceived(Packet p) {
		this.receivedQueue.add(p);
		System.out.println(Packet.bytesToHex(p.getFileID()));
	}
	
	public void finish() {
		done = true;
		this.listenerMC.toggleFinished();
		this.listenerMDB.toggleFinished();
		this.listenerMDR.toggleFinished();
	}
}
