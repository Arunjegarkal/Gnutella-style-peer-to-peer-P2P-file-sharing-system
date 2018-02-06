import java.net.Socket;
import java.io.IOException;
import java.io.*;

public class ClientThread extends Thread {

int portofconnection;
int peertoconnect;
String filetodownload;
Socket socket=null;
int[] peersArray;
MessageFormat MF=new MessageFormat();
String msgid;
int frompeer_id;
int TTL_value;

	public ClientThread(int portofconnection,int peertoconnect,String filetodownload,String msgid,int frompeer_id,int TTL_value)
	{
		this.portofconnection=portofconnection;
		this.peertoconnect=peertoconnect;
		this.filetodownload=filetodownload;
		this.msgid=msgid;
		this.frompeer_id=frompeer_id;
		this.TTL_value=TTL_value;
	}

	public void run()
	{
		try{
				//System.out.println("got the request ");
				socket=new Socket("localhost",portofconnection);
				//socket=new Socket("localhost",4001);
				OutputStream os=socket.getOutputStream();
				ObjectOutputStream oos=new ObjectOutputStream(os);
				InputStream is=socket.getInputStream();
				ObjectInputStream ois=new ObjectInputStream(is);
				MF.fname=filetodownload;							//writing the data to be serialized and send to the server thread 
				MF.msgId=msgid;
				MF.fromPeerId=frompeer_id;
				MF.TTL_value=TTL_value;
				oos.writeObject(MF);
		
				peersArray=(int[])ois.readObject();
			}
			catch(IOException io)
			{
				io.printStackTrace();
			}
			catch(ClassNotFoundException cp)
			{
				cp.printStackTrace();
			}
	}
	
	public int[] getarray()
	{
		return peersArray;
	}
}

