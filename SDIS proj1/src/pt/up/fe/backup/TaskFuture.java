package pt.up.fe.backup;

import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import pt.up.fe.backup.tasks.Task;

public class TaskFuture extends FutureTask<Object> implements RunnableFuture<Object> {
	private Task task;

	public TaskFuture(Runnable arg0, Object arg1) {
		super(arg0, arg1);
		if(arg0 instanceof Task)
			task = (Task) arg0;
	}

	public Task getTask() {
		return task;
	}
}
