package pt.up.fe.backup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class DistributedBackupSystem {
	private FileManager fManager;
	private TaskManager tManager;
	private CommunicationManager cManager;
	
	protected static ArrayList<String> mcastArgs = new ArrayList<String>();
	private static enum Menus {MAIN, BACKUP};
	
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
		System.out.println("Starting system...");
		System.out.println("Initializing managers...");
		
		System.out.println("Initializing task manager...");
		tManager = new TaskManager(this);
		
		System.out.println("Initializing file manager...");
		fManager = new FileManager(this);
		
		System.out.println("Initializing communication manager...");
		cManager = new CommunicationManager(mcastArgs,this);
		new Thread(cManager).start();
		System.out.println("done...");
		
		boolean done = false;
		Menus menu = Menus.MAIN;
		Scanner console=new Scanner(System.in);
		
		while(!done) {
			if(menu == Menus.MAIN) {
				System.out.print("\n\n\n\n");
				System.out.println("DBS v.1.0.0");
				System.out.println("Main menu options:");
				System.out.println("1. Backup file into system");
				System.out.println("Type 'quit' to exit");
				
				String input = console.nextLine();
				
				if(input.equals("1")) {
					menu = Menus.BACKUP;
				}
				else if (input.equals("quit")) {
					done = true;
				}
			}
			else if (menu == Menus.BACKUP) {
				System.out.print("Enter the filename: ");
				
				String name = console.nextLine();
				
				System.out.print("Enter desired replication degree: ");
				
				int repDeg = console.nextInt();
				
				System.out.println("Starting backup...");
				fManager.backupFile(name, repDeg);
				System.out.println("done...");
				
				menu = Menus.MAIN;
			}
			
			
		}
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
