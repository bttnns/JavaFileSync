package net.quaa.jfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Vector;

public class Client {
	private static String dirName;
	private static String serverIP;
	private static String fullDirName;
	private static int PORT_NUMBER;
	private static final String DONE = "DONE";
	private static Socket sock;
	private static ObjectInputStream ois;
	private static ObjectOutputStream oos;

	public Client(String dirName, String fullDirName, String serverIP, int port) {
		Client.dirName = dirName;
		Client.serverIP = serverIP;
		Client.fullDirName = fullDirName;
		PORT_NUMBER = port;

		System.out.println("Client Selected!");
		System.out.println("Dir to sync: " + dirName);
		System.out.println("Server IP: " + serverIP);
	}

	public void runClient() throws Exception {

		sock = new Socket(serverIP, PORT_NUMBER);
		oos = new ObjectOutputStream(sock.getOutputStream()); // send directory name to server
		oos.writeObject(new String(dirName));
		oos.flush();

		ois = new ObjectInputStream(sock.getInputStream()); // receive if this directory exists
		Boolean fExists = (Boolean) ois.readObject();

		File baseDir = new File(fullDirName); // skipping the base dir as it already should be set up on the server
		String[] children = baseDir.list();

	    for (int i=0; i<children.length; i++) {
	    	visitAllDirsAndFiles(new File(baseDir, children[i]));
	    }
	    Vector<String> vecDONE = new Vector<String>();
	    vecDONE.add(DONE);
		oos.writeObject(vecDONE);
		oos.flush();
		reinitConn();
	
		if(fExists)
			updateFromServer(sock, fullDirName);

		oos.close();
		ois.close();
		sock.close();
	}

	// Process all files and directories under dir
	private static void visitAllDirsAndFiles(File dir) throws Exception{
			Vector<String> vec = new Vector<String>();
		vec.add(dir.getName());
		vec.add(dir.getAbsolutePath().substring((dir.getAbsolutePath().indexOf(fullDirName) + fullDirName.length())));

		if(dir.isDirectory()) {
			oos.writeObject(vec);
			oos.flush();
			reinitConn();

			ois.readObject();
		} else {
			vec.add(new Long(dir.lastModified()).toString());
			oos.writeObject(vec);
			oos.flush();
			reinitConn();
			// receive SEND or RECEIVE
			Integer updateToServer = (Integer) ois.readObject(); //if true update server, else update from server

			if (updateToServer == 1) {  // send file to server
				sendFile(dir);

				ois.readObject(); // make sure server got the file

			} else if (updateToServer == 0) { // update file from server.
				dir.delete(); // first delete the current file

				oos.writeObject(new Boolean(true)); // send "Ready"
				oos.flush();

				receiveFile(dir);

				oos.writeObject(new Boolean(true)); // send back ok
				oos.flush();

				Long updateLastModified = (Long) ois.readObject(); // update the last modified date for this file from the server
				dir.setLastModified(updateLastModified);

			} // no need to check if update to server == 2 because we do nothing here
		}
		if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            visitAllDirsAndFiles(new File(dir, children[i]));
	        }
	    }
	}

	private static void sendFile(File dir) throws Exception {
		byte[] buff = new byte[sock.getSendBufferSize()];
		int bytesRead = 0;

		InputStream in = new FileInputStream(dir);

		while((bytesRead = in.read(buff))>0) {
			oos.write(buff,0,bytesRead);
		}
		in.close();
		// after sending a file you need to close the socket and reopen one.
		oos.flush();
		reinitConn();

		printDebug(true, dir);
	}

	private static void receiveFile(File dir) throws Exception {
		FileOutputStream wr = new FileOutputStream(dir);
		byte[] outBuffer = new byte[sock.getReceiveBufferSize()];
		int bytesReceived = 0;
		while((bytesReceived = ois.read(outBuffer))>0) {
			wr.write(outBuffer,0,bytesReceived);
		}
		wr.flush();
		wr.close();

		reinitConn();

		printDebug(false, dir);
	}

	private static void updateFromServer(Socket sock, String fullDirName) throws Exception {
		//oos = new ObjectOutputStream(sock.getOutputStream()); // send fileName LastModified to server
		//ois = new ObjectInputStream(sock.getInputStream()); // receive SEND or RECEIVE
		//File f = new File(fullDirName);
// need to implement this part
	}

	private static void printDebug(Boolean sending, File dir){
		if(sending)
			System.out.println("SEND=Name: " + dir.getName() + " Dir: " + dir.isDirectory() + " Modified: " + dir.lastModified() + " Size: " + dir.length());
		else
			System.out.println("RECV=Name: " + dir.getName() + " Dir: " + dir.isDirectory() + " Modified: " + dir.lastModified() + " Size: " + dir.length());
	}

	private static void reinitConn() throws Exception {
		ois.close();
		oos.close();
		sock.close();
		sock = new Socket(serverIP, PORT_NUMBER);
		ois = new ObjectInputStream(sock.getInputStream());
		oos = new ObjectOutputStream(sock.getOutputStream());
	}
}
