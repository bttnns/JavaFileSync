import java.io.File;


public class javaFileSync {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		File f = new File(args[0]);
		
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
