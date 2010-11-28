package net.quaa.jfs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

		ois = new ObjectInputStream(sock.getInputStream()); 
		if(dirName.equalsIgnoreCase("-ls")) {
			Vector<String> directories = (Vector<String>) ois.readObject();
			System.out.println("The directories available are: ");
			for (int x = 0; x < directories.size(); x++) {
				System.out.print(directories.elementAt(x) + " ");
			}
			System.out.println();
			System.out.println();
			System.out.println("To sync one of these directories please create the one you are wanting to sync and run the client again with that directory.");
		} else {
			System.out.print("Syncing");
			// receive if this directory exists
			Boolean fExists = (Boolean) ois.readObject();

			File baseDir = new File(fullDirName); // skipping the base dir as it already should be set up on the server
			String[] children = baseDir.list();

			for (int i=0; i < children.length; i++) {
				visitAllDirsAndFiles(new File(baseDir, children[i]));
			}
			Vector<String> vecDONE = new Vector<String>();
			vecDONE.add(DONE);
			oos.writeObject(vecDONE);
			oos.flush();
			reinitConn();

			if(fExists)
				updateFromServer();

			System.out.println();
			System.out.println("Finished sync");
		}
		oos.close();
		ois.close();
		sock.close();
	}

	// Process all files and directories under dir
	private static void visitAllDirsAndFiles(File dir) throws Exception{
		System.out.print(".");
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

		//		printDebug(true, dir);
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

		//		printDebug(false, dir);
	}

	private static void updateFromServer() throws Exception {
		Boolean isDone = false;
		Boolean nAll = false;
		while(!isDone) {
			System.out.print(".");
			String path = (String) ois.readObject();

			if(path.equals(DONE)) {
				isDone = true;
				break;
			}

			oos.writeObject(new Boolean(true));
			oos.flush();

			File newFile = new File(fullDirName + path);

			Boolean isDirectory = (Boolean) ois.readObject();

			oos.writeObject(new Boolean(newFile.exists()));
			oos.flush();
			if (!newFile.exists()) {
				ois.readObject();
				String userInput = null;
				if (!nAll) {
					if (isDirectory) {
						System.out.println("CONFLICT with " + fullDirName + path + "! The directory exists on the server but not this client.");
						System.out.println("Would you like to delete the server's directory (if no we would create the directory on this client)?");
						System.out.println("No to all would always accept the server's copy onto this client");
					} else {
						System.out.println("CONFLICT with " + fullDirName + path + "! The file exists on the server but not this client.");
						System.out.println("Would you like to delete the server's file (if no we would create the file on this client)?");						
						System.out.println("No to all would always accept the server's copy onto this client");
					}
					System.out.println("Type 'y' for yes, 'n' for no, 'a' for no to all, 'd' to do nothing");
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					try {
						userInput = br.readLine();
					} catch (IOException ioe) {
						System.out.println("You did not input a correct value, will do nothing.");
					}
				} else // if n to all then just set input to n!
					userInput = "n";
				if (userInput.equalsIgnoreCase("a") || userInput.equalsIgnoreCase("'a'")) {
					nAll = true;
					userInput = "n";
				}
				if (userInput.equalsIgnoreCase("y") || userInput.equalsIgnoreCase("'y'")) {
					if (isDirectory) {
						oos.writeObject(new Boolean(true)); // reply with yes to delete the server's copy
						oos.flush();
					} else {
						oos.writeObject(new Integer(1));
						oos.flush();
					}
				} else if (userInput.equalsIgnoreCase("n") || userInput.equalsIgnoreCase("'n'")) {
					if (isDirectory) {
						newFile.mkdir();
						oos.writeObject(new Boolean(false));
						oos.flush();
					} else {
						oos.writeObject(new Integer(0));
						oos.flush();
						receiveFile(newFile);

						oos.writeObject(new Boolean(true));
						oos.flush();

						Long lastModified = (Long) ois.readObject();
						newFile.setLastModified(lastModified);

						oos.writeObject(new Boolean(true));
						oos.flush();
					}
				} else {
					if (isDirectory) {
						oos.writeObject(new Boolean(false));
						oos.flush();
					} else {
						oos.writeObject(new Integer(2));
						oos.flush();
					}
				}
			}
		}
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
