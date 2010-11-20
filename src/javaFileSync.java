import java.io.File;


public class javaFileSync {

	public static void main(String[] args) {
		if(args.length > 0) {
			if (args[0].equalsIgnoreCase("-s")) {
				System.out.println("Server selected!");
				server();
			} else if (args[0].equalsIgnoreCase("-c") && args.length > 1 && args.length < 3) {
					System.out.println("Client Selected!");
					System.out.println("Dir to sync: " + args[2]);
					System.out.println("Server IP: " + args[1]);
					client(args[2], args[1]);
			} else if (args[0].equalsIgnoreCase("-t") && args.length > 1 && args.length < 4) {
				System.out.println("Secret Testing function!");
				testing(args[2], args[1]);
			} else {
				System.out.println("Invalid entry. Useage: java javaFileSync [-s] [-c [server IP] [dir to sync]]");
			}
		} else {
			System.out.println("Invalid entry. Useage: java javaFileSync [-s] [-c [server IP] [dir to sync]]");
		}
	}

	public static void server(){
		
	}
	
	public static void client(String dirName, String serverIP){
		
	}
	
	public static void testing(String dirName, String serverIP){
		File f = new File(dirName);
		visitAllFiles(f);
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
