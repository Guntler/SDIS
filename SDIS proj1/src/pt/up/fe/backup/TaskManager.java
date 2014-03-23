package pt.up.fe.backup;

import java.util.ArrayList;
import pt.up.fe.backup.tasks.Task;

public class TaskManager {
	public enum TaskTypes {STORECHUNK, SENDCHUNK, RECEIVECHUNK, UPDATE_STORED, HANDLE_REMOVE, DELETE, RESTORE};

	private ArrayList<Task> tasks;
	private FileManager fManager;
	
	public TaskManager(FileManager fManager) {
		tasks = new ArrayList<Task>();
		this.fManager = fManager;
	}

	public void setfManager(FileManager fManager) {
		this.fManager = fManager;
	}
	
	public void addTask(Task task) {
		tasks.add(task);
	}
}
