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
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private int PORT_NUMBER;
	private static final String DONE = "DONE";
	
	public Server(int port) {
		PORT_NUMBER = port;
	}
	
	public void startServer() throws Exception {
		System.out.println("Starting File Sync Server!");
		
		ServerSocket servsock = new ServerSocket(PORT_NUMBER);
		
		while (true) {
			Socket sock = servsock.accept();

			ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
			String baseDir = (String) ois.readObject();
			
			File fBaseDir = new File(baseDir);
			
			Boolean baseDirExists = fBaseDir.exists();
			
			if(!baseDirExists)
				fBaseDir.mkdir();
			
			ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
			oos.writeBoolean(new Boolean(baseDirExists));
			oos.flush();
			
			Boolean isClientDone = false;
			
			while (!isClientDone) {
				String fName = (String) ois.readObject();
				
				if(fName.equals(DONE)) { // check if we are done
					isClientDone = true;
					break;
				}
				
				oos.writeBoolean(new Boolean(true));
				oos.flush();
				
				Boolean isDirectory = ois.readBoolean();
				
				if(isDirectory) {
					oos.writeBoolean(new Boolean(true));
					oos.flush();
					
					String path = (String) ois.readObject();
					
					File newDir = new File(baseDir, path);
					if (!newDir.exists())
						newDir.mkdir();
					
					oos.writeBoolean(new Boolean(true));
					oos.flush();
				} else {
					oos.writeBoolean(new Boolean(true));
					oos.flush();
					
					String path = (String) ois.readObject();
					
					oos.writeBoolean(new Boolean(true));
					oos.flush();
					
					Long lastModified = ois.readLong();
					
					File newFile = new File(baseDir, path);
					Boolean updateFromClient = !newFile.exists() && (newFile.lastModified() <= lastModified);
					
					if(updateFromClient) { // If true receive file from client
						newFile.delete();
						
						oos.writeBoolean(new Boolean(updateFromClient));
						oos.flush();
						
						receiveFile(newFile, sock);
						
						newFile.setLastModified(lastModified);
						
						oos.writeBoolean(new Boolean(true));
					} else { // if false send file to client
						oos.writeBoolean(new Boolean(updateFromClient));
						oos.flush();
						
						ois.readBoolean();
						
						sendFile(newFile, sock);
						
						ois.readBoolean();
						
						oos.writeLong(new Long(newFile.lastModified()));
						oos.flush();
					}
				}
			}
			
			if(baseDirExists){
				
			}
			
/*			if(baseDirExists)
				System.out.println(baseDir + " is a directory and exists!");
				System.out.print("Sending file '" + fileName + "' to: " + sock.getInetAddress() + "...");
				ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
				oos.writeObject("1");
				byte[] mybytearray = new byte[(int) f.length()];
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
				bis.read(mybytearray, 0, mybytearray.length);
				OutputStream os = sock.getOutputStream();
				os.write(mybytearray, 0, mybytearray.length);
				os.flush();
				System.out.println(" DONE!");	
			}else{
				System.out.println(baseDir + " Exists?: " + fBaseDir.exists() + " IsDirectory?: " + fBaseDir.isDirectory());
				System.out.println(sock.getInetAddress() + " requested " + fileName + ", but it does not exist!");
				ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
				oos.writeObject("0");
				oos.flush();
			} */
			oos.close();
			ois.close();
			sock.close();
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
}
