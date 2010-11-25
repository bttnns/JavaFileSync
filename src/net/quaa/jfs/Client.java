package net.quaa.jfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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
	    
		oos.writeObject(new String(DONE));
		oos.flush();
		
		if(fExists) 
			updateFromServer(sock, fullDirName);
		
		oos.close();
		ois.close();
		sock.close();
	}
	
	// Process all files and directories under dir
	public static void visitAllDirsAndFiles(File dir) throws Exception{
		oos.writeObject(new String(dir.getName()));
		oos.flush();
		
		ois.readObject();
		
		oos.writeObject(new Boolean(dir.isDirectory()));
		oos.flush();
		
		ois.readObject();
		
		oos.writeObject(new String(dir.getAbsolutePath().substring((dir.getAbsolutePath().indexOf(fullDirName) + fullDirName.length()))));
		oos.flush();
		
		ois.readObject();
		
		if(!dir.isDirectory()) {
			oos.writeObject(new Long(dir.lastModified()));
			oos.flush();
			
			// receive SEND or RECEIVE
			Boolean updateToServer = (Boolean) ois.readObject(); //if true update server, else update from server

			if (updateToServer) {  // send file to server
				sendFile(dir);

				ois.readObject(); // make sure server got the file
				
			} else { // update file from server.  
				dir.delete(); // first delete the current file
				
				oos.writeObject(new Boolean(true)); // send "Ready"
				oos.flush();
				
				receiveFile(dir);
				
				oos.writeObject(new Boolean(true)); // send back ok
				oos.flush();
				
				Long updateLastModified = (Long) ois.readObject(); // update the last modified date for this file from the server
				dir.setLastModified(updateLastModified);
				
				oos.close();
				ois.close();
			}
		}
		
/* debug */		System.out.println("Name: " + dir.getName() + " Dir: " + dir.isDirectory() + " Modified: " + dir.lastModified() + " Size: " + dir.length());
	    
		if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            visitAllDirsAndFiles(new File(dir, children[i]));	            	
	        }
	    }
	}
	
	public static void sendFile(File dir) throws Exception {
		byte[] buff = new byte[sock.getSendBufferSize()];
		int bytesRead = 0;
		
		InputStream in = new FileInputStream(dir);

		while((bytesRead = in.read(buff))>0)
		{
			oos.write(buff,0,bytesRead);
		}
		oos.flush();
		in.close();
		// after sending a file you need to close the socket and reopen one.
		oos.flush();
		oos.close();
		ois.close();
		sock.close();
		sock = new Socket(serverIP, PORT_NUMBER);
		oos = new ObjectOutputStream(sock.getOutputStream());
		ois = new ObjectInputStream(sock.getInputStream());
	}
	
	public static void receiveFile(File dir) throws Exception {
		FileOutputStream wr = new FileOutputStream(dir);
		byte[] outBuffer = new byte[sock.getReceiveBufferSize()];
		int bytesReceived = 0;
		while((bytesReceived = ois.read(outBuffer))>0)
		{
			wr.write(outBuffer,0,bytesReceived);
		}
		wr.flush();
		wr.close();
		
		ois.close();
		oos.close();
		sock.close();
		sock = new Socket(serverIP, PORT_NUMBER);
		ois = new ObjectInputStream(sock.getInputStream());
		oos = new ObjectOutputStream(sock.getOutputStream());
	}
	
	public static void updateFromServer(Socket sock, String fullDirName) throws Exception {
		//oos = new ObjectOutputStream(sock.getOutputStream()); // send fileName LastModified to server
		//ois = new ObjectInputStream(sock.getInputStream()); // receive SEND or RECEIVE
		
		//File f = new File(fullDirName);
		
// need to implement this part		
				
	}
}
