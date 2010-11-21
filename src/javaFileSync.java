import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class javaFileSync {
	private static final int PORT_NUMBER = 17555;
	
	public static void main(String[] args) {
		try {
			if(args.length > 0) {
				if (args[0].equalsIgnoreCase("-s")) {
					server();
				} else if (args[0].equalsIgnoreCase("-c") && args.length > 1 && args.length < 4) {
					client(args[2], args[1]);
				} else {
					System.out.println("Invalid entry. Useage: java javaFileSync [-s] [-c [server IP] [dir to sync]]");
				}
			} else {
				System.out.println("Invalid entry. Useage: java javaFileSync [-s] [-c [server IP] [dir to sync]]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void server() throws Exception {
		Server s = new Server(PORT_NUMBER);
		s.startServer();
	}
	
	public static void client(String dirName, String serverIP) throws Exception {
		System.out.println("Client Selected!");
		System.out.println("Dir to sync: " + dirName);
		System.out.println("Server IP: " + serverIP);
		
		String localDirName = dirName; //cleaning up the users input
		if(dirName.contains("/")){
			if(dirName.lastIndexOf("/") != (dirName.length() - 1)) {
				localDirName = dirName.substring(dirName.lastIndexOf("/"));
			} else {
				localDirName = dirName.substring(0, (dirName.length() - 1));
				if(localDirName.contains("/"))
					localDirName = localDirName.substring(localDirName.lastIndexOf("/"));
			}
		}
		
		if(localDirName.equals(".")){
			System.out.println("Please input a dir name instead of ./ or .");
			Thread.sleep(10);
			System.exit(0);
		}
		
		if(!localDirName.startsWith("./")){ //still cleaning up their input
			if(localDirName.startsWith("/"))
				localDirName = "." + localDirName;
			else
				localDirName = "./" + localDirName;
		}
		
		Socket sock = new Socket(serverIP, PORT_NUMBER);
		ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
		oos.writeObject(localDirName);
		oos.flush();
		
/*		//check to see if file exists on server
		ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
		String fExists = (String) ois.readObject();

		if(fExists.equals("1")){
			System.out.print("Receiving file: ");
			byte[] mybytearray = new byte[1024];
			InputStream is = sock.getInputStream();
			FileOutputStream fos = new FileOutputStream(fileName);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			int bytesRead = 0;
			int current = 0;
			while((bytesRead = is.read(mybytearray, 0, mybytearray.length)) != -1){
				bos.write(mybytearray, 0, bytesRead);
				current = current + bytesRead;
				System.out.print(".");
			}
			System.out.println();
			System.out.println("Done!");
			oos.close();
			ois.close();
			bos.close();
		}else{
			System.out.println("Server replied that " + fileName + " does not exist, closing connection.");
		}
*/		oos.close();
		sock.close();
	}
	
	// Process all files and directories under dir
	public static void visitAllDirsAndFiles(File dir) {
	    //process(dir);
		System.out.println("Name: " + dir.getName() + " Modified: " + dir.lastModified() + " Size: " + dir.length());
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            visitAllDirsAndFiles(new File(dir, children[i]));
	        }
	    }
	}

	// Process only directories under dir
	public static void visitAllDirs(File dir) {
	    if (dir.isDirectory()) {
		    //process(dir);
			System.out.println("Name: " + dir.getName() + " Modified: " + dir.lastModified() + " Size: " + dir.length());

	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            visitAllDirs(new File(dir, children[i]));
	        }
	    }
	}

	// Process only files under dir
	public static void visitAllFiles(File dir) {
	    if (dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i=0; i<children.length; i++) {
	            visitAllFiles(new File(dir, children[i]));
	        }
	    } else {
		    //process(dir);
			System.out.println("Name: " + dir.getName() + " Modified: " + dir.lastModified() + " Size: " + dir.length());
	    }
	}
	
}
