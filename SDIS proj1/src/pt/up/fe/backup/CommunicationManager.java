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
	
	boolean done;
	
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
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(!done) {
			
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
}
