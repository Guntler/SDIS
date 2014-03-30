package pt.up.fe.backup;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import pt.up.fe.backup.tasks.Task;

public class TaskExecutor extends ThreadPoolExecutor {

	@SuppressWarnings("unchecked")
	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable arg0, T arg1) {
		return (RunnableFuture<T>) new TaskFuture (arg0, arg1);
	}

	CopyOnWriteArrayList<Task> activeTasks;
	
	public TaskExecutor(int poolSize) {
		super(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		activeTasks = new CopyOnWriteArrayList<Task>();
		
		
	}
	
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		if(r instanceof TaskFuture) {
			activeTasks.remove(((TaskFuture) r).getTask());
		}
		
		super.afterExecute(r, t);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		if(r instanceof TaskFuture) {
			activeTasks.add(((TaskFuture) r).getTask());
		}
		
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
