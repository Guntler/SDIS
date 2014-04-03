package pt.up.fe.backup.tasks;

import pt.up.fe.backup.BackupFile;
import pt.up.fe.backup.DistributedBackupSystem;
import pt.up.fe.backup.Packet;
import pt.up.fe.backup.TaskManager;

public class BackupFileTask extends Task{
	private String filename;
	private int replicationDegree;
	
	public BackupFileTask( String filename, int replicationDegree) {
		super();
		this.filename = filename;
		this.replicationDegree = replicationDegree;
	}

	@Override
	public void run() {
		DistributedBackupSystem.fManager.backupFile(filename, replicationDegree);
		BackupFile file = DistributedBackupSystem.fManager.getFileByName(filename);
		if(file != null) {
			for(Packet p : messages) {
				if(p.getPacketType().equals("DELETE") && Packet.bytesToHex(p.getFileID()).equals(Packet.bytesToHex(file.getFileID()))) {
                    try {
                        DistributedBackupSystem.tManager.executeTask(TaskManager.TaskTypes.DELETE, file.getFileID(), 0).get();
                        DistributedBackupSystem.fManager.getFiles().remove(DistributedBackupSystem.fManager.getFileByName(filename));
                        DistributedBackupSystem.fManager.updateLog();
                    } catch (Exception e) {e.printStackTrace();};
                }
			}
		}
	}

	@Override
	public String toString() {
		return "backupfile";
	}
}
