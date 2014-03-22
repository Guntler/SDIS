package pt.up.fe.backup;

import java.util.ArrayList;
import pt.up.fe.backup.tasks.Task;

public class TaskManager {
	public enum TaskTypes {STORECHUNK, SENDCHUNK, RECEIVECHUNK, UPDATE_STORED, HANDLE_REMOVE, DELETE, RESTORE};
	
	private ArrayList<Task> tasks;
	
	public TaskManager() {
		tasks = new ArrayList<Task>();
	}
}
