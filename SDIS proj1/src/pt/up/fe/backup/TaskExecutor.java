package pt.up.fe.backup;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pt.up.fe.backup.tasks.Task;

public class TaskExecutor extends ThreadPoolExecutor {

	ArrayList<Task> activeTasks;
	
	public TaskExecutor(int poolSize) {
		super(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		activeTasks = new ArrayList<Task>();
		
		
	}
	
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		if(r instanceof Task)
			activeTasks.remove((Task) r);
		
		super.afterExecute(r, t);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		if(r instanceof Task)
			activeTasks.add((Task) r);
		
		super.beforeExecute(t, r);
	}
	
	@Override
	public Future<?> submit(Runnable task) {
		if(task instanceof Task) {
			return super.submit(task);
		}
		else {
			return null;
		}
	}

	public void messageActiveTasks(Packet p) {
		for(Task task : activeTasks) {
			task.sendMessage(p);
		}
	}
}
