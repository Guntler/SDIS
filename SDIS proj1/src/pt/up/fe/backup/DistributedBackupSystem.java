package pt.up.fe.backup;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class DistributedBackupSystem {
	private FileManager fManager;
	private TaskManager tManager;
	private PacketListener listener;
	
	protected static MulticastSocket socket = null;
	
	protected static String[] mcastArgs;
	
	/**
	 * @param args	<IPMC> <portMC> <ipMDB> <portMDB> <ipMDR> <portMDR> 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 6) {
			System.out.println("Usage: java DistributedBackupSystem <IPMC> <portMC> <ipMDB> <portMDB> <ipMDR> <portMDR>");
			return;
		}
		mcastArgs = args;
		
		DistributedBackupSystem system = new DistributedBackupSystem();
		system.start();
	}
	
	public void start() throws IOException {
		tManager = new TaskManager(null);
		fManager = new FileManager(tManager);
		tManager.setfManager(fManager);
		listener = new PacketListener(tManager);
		listener.run();
		
		final int mCastPort = Integer.parseInt(mcastArgs[1]);
		
		socket = new MulticastSocket(mCastPort);
		InetAddress group = InetAddress.getByName(mcastArgs[0]);
		socket.joinGroup(group);
	}
}
