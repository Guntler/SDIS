package pt.up.fe.backup;

import java.util.ArrayList;
import java.util.HashMap;

public class FileManager {
	private HashMap<byte[],BackupFile> files;
	private ArrayList<BackupChunk> backedUpChunks;
	
	public FileManager() {
		files = new HashMap<byte[],BackupFile>();
		backedUpChunks = new ArrayList<BackupChunk>();
	}
}
