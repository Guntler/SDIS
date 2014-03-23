package pt.up.fe.backup;

import java.io.IOException;
import java.util.ArrayList;

public class DistributedBackupSystem {
	private FileManager fManager;
	private TaskManager tManager;
	private CommunicationManager listener;
	
	protected static ArrayList<String> mcastArgs;
	
	/**
	 * @param args	<IPMC> <portMC> <ipMDB> <portMDB> <ipMDR> <portMDR> 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 6) {
			System.out.println("Usage: java DistributedBackupSystem <IPMC> <portMC> <ipMDB> <portMDB> <ipMDR> <portMDR>");
			return;
		}
		for(int i=0;i<args.length;i++)
			mcastArgs.add(args[i]);
		
		DistributedBackupSystem system = new DistributedBackupSystem();
		system.start();
	}
	
	public void start() throws IOException {
		tManager = new TaskManager(null);
		fManager = new FileManager(tManager);
		tManager.setfManager(fManager);
		listener = new CommunicationManager(tManager,mcastArgs);
		listener.run();
	}
}
