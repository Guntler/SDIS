package pt.up.fe.backup;

import java.util.ArrayList;
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
		// TODO Auto-generated method stub
		//return super.newTaskFor(arg0, arg1)
		return (RunnableFuture<T>) new TaskFuture (arg0, arg1);
	}

	ArrayList<Task> activeTasks;
	
	public TaskExecutor(int poolSize) {
		super(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
		activeTasks = new ArrayList<Task>();
		
		
	}
	
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		if(r instanceof TaskFuture) {
			activeTasks.remove(((TaskFuture) r).getTask());
			//System.out.println("Removed task from active tasks, active tasks size is now " + activeTasks.size());
		}
		
		super.afterExecute(r, t);
	}

	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		if(r instanceof TaskFuture) {
			activeTasks.add(((TaskFuture) r).getTask());
			//System.out.println("Added task to active tasks, active tasks size is now " + activeTasks.size());
		}
		else
			//System.out.println("Class is not instance of class it's " + r.getClass().getName() + " extended from " + r.getClass().getSuperclass());
		
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
		//System.out.println(" with ID " + Packet.bytesToHex(p.getFileID()));
		//System.out.println("Number of active tasks is " + activeTasks.size());
		for(Task task : activeTasks) {
			task.sendMessage(p);
		}
	}
}
