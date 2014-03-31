package pt.up.fe.backup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DistributedBackupSystem {
	static public FileManager fManager = null;
	static public TaskManager tManager = null;
	static public CommunicationManager cManager = null;
	
	protected static ArrayList<String> mcastArgs = new ArrayList<String>();
	
	/**
	 * @param args	<IPMC> <portMC> <ipMDB> <portMDB> <ipMDR> <portMDR> 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 6) {
			System.out.println("Usage: java DistributedBackupSystem <IPMC> <portMC> <ipMDB> <portMDB> <ipMDR> <portMDR>");
			return;
		}
		
		for(int i=0;i<args.length;i++)
			mcastArgs.add(args[i]);

		DistributedBackupSystem system = new DistributedBackupSystem();
		system.start();
	}

	public void start() throws IOException {
		tManager = new TaskManager(this);
		fManager = new FileManager(this);
		cManager = new CommunicationManager(mcastArgs,this);
		new Thread(cManager).start();

		boolean done = false;
		Scanner console=new Scanner(System.in);
		
		System.out.print("\n\n\n\n");
		System.out.println("DBS v.1.0.0");
		System.out.println("Type 'help' for a list of commands.");
		System.out.println("Type 'quit' to exit.");

		while(!done) {

			String input = console.nextLine();

			if (input.equals("quit")) {
				done = true;
				cManager.finish();
				tManager.finish();
			}
			else if(input.equals("help")) {
				System.out.println("'backup \"filename\" repDegree' to backup a file.");
				System.out.println("'restore \"filename\" ' to restore a file.");
				System.out.println("'delete \"filename\" ' to delete a file.");
				System.out.println("'setAllocatedMemory numberOfBytes' to delete a file.");
				System.out.println("'allocatedMemory' to print the current value for allocated memory.");
				System.out.println("'usedMemory' to print the current value of used up memory.");
				System.out.println("'filesStored' to print the list of files currently in storage.");
				System.out.println("'quit' to exit the program.");
			}
			else {
				
				
				ArrayList<String> commands = new ArrayList<String>();
				Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(input);
				while (m.find())
				    commands.add(m.group(1).replace("\"", "")); // Add .replace("\"", "") to remove surrounding quotes

				if(commands.size() == 3 && commands.get(0).equals("backup")) {
					try {
						int repDeg = Integer.parseInt(commands.get(2));

						System.out.println("Starting backup...");
						tManager.executeTask(TaskManager.TaskTypes.BACKUPFILE,commands.get(1), repDeg).get();
						System.out.println("done...");
					} catch (Exception e) {e.printStackTrace();}
				}
				else if(commands.size() == 2 && commands.get(0).equals("restore")) {
					try {
						System.out.println("Starting restore...");
						tManager.executeTask(TaskManager.TaskTypes.RESTOREFILE,commands.get(1), 0).get();
						System.out.println("done...");
					} catch (Exception e) {e.printStackTrace();}
				}
				else if(commands.size() == 2 && commands.get(0).equals("delete")) {
					try {
						System.out.println("Starting deletion...");
						tManager.executeTask(TaskManager.TaskTypes.DELETEFILE,commands.get(1), 0).get();
						System.out.println("done...");
					} catch (Exception e) {e.printStackTrace();}
				}
				else if(commands.size() == 2 && commands.get(0).equals("setAllocatedMemory")) {
					try {
						long bytes = Long.parseLong(commands.get(1));
						System.out.println("Starting space reclaiming...");
						tManager.executeTask(TaskManager.TaskTypes.SETMEMORY,null, bytes).get();
						System.out.println("done...");
					} catch (Exception e) {e.printStackTrace();}
				}
				else if(commands.size() == 1 && commands.get(0).equals("allocatedMemory")) {
					System.out.println("The current value for allocated memory is " + fManager.getMaxSize() + " bytes.");
				}
				else if(commands.size() == 1 && commands.get(0).equals("usedMemory")) {
					System.out.println("The current value for used up memory is " + fManager.getCurrSize() + " bytes.");
				}
				else if(commands.size() == 1 && commands.get(0).equals("filesStored")) {
					System.out.println("Files currently in storage:");
					System.out.println("Filename      Replication Degree      Number of Chunks");
					DistributedBackupSystem.fManager.printAllFiles();
				}
				else {
					System.out.println("Unknown command. Type 'help' for a list of commands.");
				}
			}
		}
		console.close();
	}

	
	public FileManager getFManager() {
		return fManager;
	}
	
	public TaskManager getTManager() {
		return tManager;
	}
	
	public CommunicationManager getCManager() {
		return cManager;
	}
}
