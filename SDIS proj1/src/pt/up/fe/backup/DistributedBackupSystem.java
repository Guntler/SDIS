package pt.up.fe.backup;

public class DistributedBackupSystem {
	private FileManager fManager;
	private TaskManager tManager;
	private PacketListener listener;
	
	/**
	 * @param args	<IPMC> <portMC> <ipMDB> <portMDB> <ipMDR> <portMDR> 
	 */
	public static void main(String[] args) {
		if (args.length != 6) {
			System.out.println("Usage: java DistributedBackupSystem <IPMC> <portMC> <ipMDB> <portMDB> <ipMDR> <portMDR>");
			return;
		}
		
		DistributedBackupSystem system = new DistributedBackupSystem();
		system.start();
	}
	
	public void start() {
		tManager = new TaskManager(null);
		fManager = new FileManager(tManager);
		tManager.setfManager(fManager);
		listener = new PacketListener(tManager);
		listener.run();
	}
}
