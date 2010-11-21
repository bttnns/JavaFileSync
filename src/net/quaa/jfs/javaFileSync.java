package net.quaa.jfs;

import java.io.File;

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
		Client c = new Client(dirName, serverIP, PORT_NUMBER);
		c.runClient();
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
