package net.quaa.jfs;

import java.io.File;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	private int PORT_NUMBER;
	
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
			
			if(fBaseDir.exists() && fBaseDir.isDirectory()){
				System.out.println(baseDir + " is a directory and exists!");
/*				System.out.print("Sending file '" + fileName + "' to: " + sock.getInetAddress() + "...");
				ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
				oos.writeObject("1");
				byte[] mybytearray = new byte[(int) f.length()];
				BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
				bis.read(mybytearray, 0, mybytearray.length);
				OutputStream os = sock.getOutputStream();
				os.write(mybytearray, 0, mybytearray.length);
				os.flush();
				System.out.println(" DONE!");
*/			}else{
				System.out.println(baseDir + " Exists?: " + fBaseDir.exists() + " IsDirectory?: " + fBaseDir.isDirectory());
/*				System.out.println(sock.getInetAddress() + " requested " + fileName + ", but it does not exist!");
				ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
				oos.writeObject("0");
				oos.flush();
*/			}
			sock.close();
		}
	}
}
