package net.quaa.jfs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
	private static String dirName;
	private static String serverIP;
	private static String fullDirName;
	private static int PORT_NUMBER;
	private static final String DONE = "DONE";
	
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
		
		Socket sock = new Socket(serverIP, PORT_NUMBER);
		ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream()); // send directory name to server
		oos.writeChars(new String(dirName));
		oos.flush();
		
		ObjectInputStream ois = new ObjectInputStream(sock.getInputStream()); // receive if this directory exists
		Boolean fExists = (Boolean) ois.readObject();
		
		File baseDir = new File(fullDirName); // skipping the base dir as it already should be set up on the server
		String[] children = baseDir.list();
	    for (int i=0; i<children.length; i++) {
	    	visitAllDirsAndFiles(new File(baseDir, children[i]), sock);	            	
	    }
	    	
		oos.writeChars(new String(DONE));
		oos.flush();
		
		if(fExists) 
			updateFromServer(sock, fullDirName);
		
		oos.close();
		ois.close();
		sock.close();
	}
	
	// Process all files and directories under dir
	public static void visitAllDirsAndFiles(File dir, Socket sock) throws Exception{
		ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream()); // send pathName fileName LastModified to server
		oos.writeChars(new String(dir.getName()));
		oos.flush();
		
		ObjectInputStream ois = new ObjectInputStream(sock.getInputStream()); // wait for server to say ok
		ois.readBoolean();
		
		oos.writeBoolean(new Boolean(dir.isDirectory()));
		oos.flush();
		
		ois.readBoolean();
		
		oos.writeChars(new String(dir.getAbsolutePath().substring((dir.getAbsolutePath().indexOf(fullDirName) + fullDirName.length()))));
		oos.flush();
		
		ois.readBoolean();
		
		if(!dir.isDirectory()) {
			oos.writeLong(new Long(dir.lastModified()));
			oos.flush();
			
			// receive SEND or RECEIVE
			Boolean updateToServer = (Boolean) ois.readObject(); //if true update server, else update from server
			
			if (updateToServer) {  // send file to server
				sendFile(dir, sock);
				
				Boolean fileWasOk = (Boolean) ois.readObject(); // make sure server got the file
				
				oos.close();
				ois.close();
				
				if (!fileWasOk) // if the server replys true then continue, else repeat
					visitAllDirsAndFiles(dir, sock);
				
			} else { // update file from server.  
				dir.delete(); // first delete the current file
				
				oos.writeBoolean(new Boolean(true)); // send "Ready"
				oos.flush();
				
				receiveFile(dir, sock);
				
				oos.writeBoolean(new Boolean(true)); // send back ok
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
	            visitAllDirsAndFiles(new File(dir, children[i]), sock);	            	
	        }
	    }
	}
	
	public static void sendFile(File dir, Socket sock) throws Exception { 
		byte[] mybytearray = new byte[(int) dir.length()];
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(dir));
		bis.read(mybytearray, 0, mybytearray.length);
		OutputStream os = sock.getOutputStream();
		os.write(mybytearray, 0, mybytearray.length);
		os.flush();
		
		os.close();
		bis.close();
	}
	
	public static void receiveFile(File dir, Socket sock) throws Exception {
		byte[] mybytearray = new byte[1024]; // receive file from server
		InputStream is = sock.getInputStream();
		FileOutputStream fos = new FileOutputStream(dir);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		int bytesRead = 0;
		int current = 0;
		while((bytesRead = is.read(mybytearray, 0, mybytearray.length)) != -1){
			bos.write(mybytearray, 0, bytesRead);
			current = current + bytesRead;
		}
		
		bos.close();
		fos.close();
		is.close();
	}
	
	public static void updateFromServer(Socket sock, String fullDirName) throws Exception {
		ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream()); // send fileName LastModified to server
		ObjectInputStream ois = new ObjectInputStream(sock.getInputStream()); // receive SEND or RECEIVE
		
		File f = new File(fullDirName);
		
// need to implement this part		
				
	}
}
