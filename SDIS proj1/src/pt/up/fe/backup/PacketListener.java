package pt.up.fe.backup;

public class PacketListener implements Runnable {
	static public enum CommandTypes {GETCHUNK, PUTCHUNK, RESTORE, REMOVED, STORED, CHUNK, DELETE};

	TaskManager tManager;

	public PacketListener(TaskManager tManager) {
		this.tManager = tManager;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	};
}
