package net.quaa.jfs;

import java.io.File;

public class javaFileSync {
	private static final int PORT_NUMBER = 17555;
	private static String localName;
	private static String fullPathName;
	
	public static void main(String[] args) {
		try {
			if(args.length > 0) {
				if (args[0].equalsIgnoreCase("-s")) {
					server();
				} if (args[0].equals("-t")) {
					System.out.println("You have found the secret testing function!");
					fullPathName = args[1];
					localName = cleanUpInput(args[1]);
					testing(new File(localName), args[1]);
				} else if (args[0].equalsIgnoreCase("-c") && args.length > 1 && args.length < 4) {
					localName = cleanUpInput(args[2]);
					client(localName, args[2], args[1]);
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
	
	public static void client(String dirName, String fullDirName, String serverIP) throws Exception {
		Client c = new Client(dirName, fullDirName, serverIP, PORT_NUMBER);
		c.runClient();
	}
	
	public static void testing(File dirName, String fullPathName) throws Exception {
		visitAllDirsAndFiles(dirName);

	}
	
	public static String cleanUpInput(String userInput) throws Exception {
		
		File f = new File(userInput);
		
		if(!f.isDirectory()) {
			System.out.println("Please input a directory instead of a file!");
			Thread.sleep(10);
			System.exit(0);
		}
		
		String localDirName = userInput; //cleaning up the users input
		if(userInput.contains("/")){
			if(userInput.lastIndexOf("/") != (userInput.length() - 1)) {
				localDirName = userInput.substring(userInput.lastIndexOf("/"));
			} else {
				localDirName = userInput.substring(0, (userInput.length() - 1));
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
		return localDirName;
	}
	
	// Process all files and directories under dir
	public static void visitAllDirsAndFiles(File dir) {
	    //process(dir);
		System.out.println("Name: " + dir.getName() + " Modified: " + dir.lastModified() + " Size: " + dir.length());
		System.out.println(fullPathName + dir.getAbsolutePath().substring((dir.getAbsolutePath().indexOf(fullPathName)  + fullPathName.length())) + " Directory? " + dir.isDirectory());
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
