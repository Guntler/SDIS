package pt.up.fe.backup.tasks;

import pt.up.fe.backup.FileManager;

public class test extends Task {
	String s;
	public test(FileManager fManager, String s) {
		super(fManager);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println(s);
	}

}
