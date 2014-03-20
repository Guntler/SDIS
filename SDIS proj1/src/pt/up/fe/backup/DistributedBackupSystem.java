package pt.up.fe.backup;

public class DistributedBackupSystem {
	private FileManager fManager;
	private PacketListener listener;
	
	public static void main() {
		DistributedBackupSystem system = new DistributedBackupSystem();
		system.start();
	}
	
	public void start() {
		
	}
}
