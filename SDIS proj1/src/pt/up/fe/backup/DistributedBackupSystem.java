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
						//fManager.backupFile(commands.get(1), repDeg);
						tManager.executeTask(TaskManager.TaskTypes.BACKUPFILE,commands.get(1), repDeg);
						System.out.println("done...");
					} catch (Exception e) {
						e.printStackTrace();
					}
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
