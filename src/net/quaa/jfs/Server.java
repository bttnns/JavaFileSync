package net.quaa.jfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
	private int PORT_NUMBER;
	private static final String DONE = "DONE";
	private static Socket sock;
	private static ObjectOutputStream oos;
	private static ObjectInputStream ois;
	private static ServerSocket servsock;
	
	public Server(int port) {
		PORT_NUMBER = port;
	}
	
	public void startServer() throws Exception {
		System.out.println("Starting File Sync Server!");
		
		servsock = new ServerSocket(PORT_NUMBER);
		
		while (true) {
			System.out.println("NEW CLIENT");
			sock = servsock.accept();

			ois = new ObjectInputStream(sock.getInputStream());
			String baseDir = (String) ois.readObject();
			
			File fBaseDir = new File(baseDir);
			
			Boolean baseDirExists = fBaseDir.exists();
			
			if(!baseDirExists)
				fBaseDir.mkdir();
			
			oos = new ObjectOutputStream(sock.getOutputStream());
			oos.writeObject(new Boolean(baseDirExists));
			oos.flush();
			
			Boolean isClientDone = false;
			
			while (!isClientDone) {
/*				String fName = (String) ois.readObject();
				
				if(fName.equals(DONE)) { // check if we are done
					isClientDone = true;
					break;
				}
				
				oos.writeObject(new Boolean(true));
				oos.flush();
				
				Boolean isDirectory = (Boolean) ois.readObject();
*/				
				Vector<String> vec = (Vector<String>) ois.readObject();
				
				oos.close();
				ois.close();
				sock.close();
				sock = servsock.accept();
				oos = new ObjectOutputStream(sock.getOutputStream());
				ois = new ObjectInputStream(sock.getInputStream());
				System.out.println(vec.elementAt(0));
				if(vec.elementAt(0).equals(DONE)) {
					System.out.println(DONE);
					isClientDone = true;
					break;
				}

				if(vec.size() == 2) { // if the size is 2 then this is a directory
/*					oos.writeObject(new Boolean(true));
					oos.flush();
					
					String path = (String) ois.readObject();
*/					
					File newDir = new File(baseDir, vec.elementAt(1));
					if (!newDir.exists())
						newDir.mkdir();
					
					oos.writeObject(new Boolean(true));
					oos.flush();
				} else {
/*					oos.writeObject(new Boolean(true));
					oos.flush();
					
					String path = (String) ois.readObject();
					
					oos.writeObject(new Boolean(true));
					oos.flush();
					
					Long lastModified = (Long) ois.readObject();
*/					
					File newFile = new File(baseDir, vec.elementAt(1));
					Integer updateFromClient = 2; // default = do nothing
					
					Long lastModified = new Long(vec.elementAt(2));
					if (!newFile.exists() || (newFile.lastModified() <= lastModified))
						updateFromClient = 1;
					else
						updateFromClient = 0;
					
					if(newFile.exists() && newFile.lastModified() == lastModified)
						updateFromClient = 2;
						
					if(updateFromClient == 1) { // If true receive file from client
						newFile.delete();
						
						oos.writeObject(new Integer(updateFromClient));
						oos.flush();
						
						receiveFile(newFile);
						
						newFile.setLastModified(lastModified);
						
						oos.writeObject(new Boolean(true));
					} else if (updateFromClient == 0) { // if false send file to client
						oos.writeObject(new Integer(updateFromClient));
						oos.flush();
						
						ois.readObject();
						
						sendFile(newFile);
						
						ois.readObject();
						
						oos.writeObject(new Long(newFile.lastModified()));
						oos.flush();
					} else { //updateFromClient == 2 // do nothing
						oos.writeObject(new Integer(updateFromClient));
						oos.flush();
					}
				}
			}
			
			if(baseDirExists){
// need to implement this section				
			}
			
			oos.close();
			ois.close();
			sock.close();
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
		in.close();
		// after sending a file you need to close the socket and reopen one.
		oos.flush();
		oos.close();
		ois.close();
		sock.close();
		sock = servsock.accept();
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
		sock = servsock.accept();
		ois = new ObjectInputStream(sock.getInputStream());
		oos = new ObjectOutputStream(sock.getOutputStream());
	}
}
