import java.io.ObjectOutputStream;
import java.net.Socket;


public class Client {
	private String dirName;
	private String serverIP;
	private int PORT_NUMBER;
	
	public Client(String dirName, String serverIP, int port) {
		this.dirName = dirName;
		this.serverIP = serverIP;
		PORT_NUMBER = port;
		
		System.out.println("Client Selected!");
		System.out.println("Dir to sync: " + dirName);
		System.out.println("Server IP: " + serverIP);
	}
	
	public void runClient() throws Exception {
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
}
