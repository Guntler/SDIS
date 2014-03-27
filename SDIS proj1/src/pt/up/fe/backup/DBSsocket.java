package pt.up.fe.backup;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class DBSsocket extends MulticastSocket {
	
	private InetAddress mcastGroup = null;

	public DBSsocket(int port) throws IOException {
		super(port);
	}

	public InetAddress getMcastGroup() {
		return mcastGroup;
	}

	@Override
	public void joinGroup(InetAddress mcastaddr) throws IOException {
		super.joinGroup(mcastaddr);
		mcastGroup = mcastaddr;
	}

	@Override
	public void leaveGroup(InetAddress mcastaddr) throws IOException {
		super.leaveGroup(mcastaddr);
		mcastGroup = null;
	}
}
