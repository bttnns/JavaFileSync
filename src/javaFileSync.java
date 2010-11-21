import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class javaFileSync {

	public static void main(String[] args) {
		try {
			if(args.length > 0) {
				if (args[0].equalsIgnoreCase("-s")) {
					server();
				} else if (args[0].equalsIgnoreCase("-c") && args.length > 1 && args.length < 3) {
					client(args[2], args[1]);
				} else {
					System.out.println("Invalid entry. Useage: java javaFileSync [-s] [-c [server IP] [dir to sync]]");
				}
			} else {
				System.out.println("Invalid entry. Useage: java javaFileSync [-s] [-c [server IP] [dir to sync]]");
			}
		} catch (Exception e) {
			System.out.print(e.getMessage());
		}
	}

	public static void server() throws Exception {
		System.out.println("Starting File Sync Server!");
		
		ServerSocket servsock = new ServerSocket(17555);
		
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
	
	public static void client(String dirName, String serverIP) {
		System.out.println("Client Selected!");
		System.out.println("Dir to sync: " + dirName);
		System.out.println("Server IP: " + serverIP);
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
