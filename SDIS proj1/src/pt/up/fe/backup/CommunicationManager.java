package pt.up.fe.backup;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

public class CommunicationManager implements Runnable {
	static public enum CommandTypes {GETCHUNK, PUTCHUNK, RESTORE, REMOVED, STORED, CHUNK, DELETE};
	protected ArrayList<String> mcastArgs;		//<IPMC> <portMC> <ipMDB> <portMDB> <ipMDR> <portMDR>
	protected MulticastSocket socketMC = null;
	protected MulticastSocket socketMDB = null;
	protected MulticastSocket socketMDR = null;
	
	TaskManager tManager;

	public CommunicationManager(TaskManager tManager,ArrayList<String> mcastArgs) throws IOException {
		this.tManager = tManager;
		this.mcastArgs = mcastArgs;
		
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
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	};
}
