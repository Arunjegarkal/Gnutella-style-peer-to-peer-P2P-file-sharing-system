import java.net.*;
import java.util.Properties;
import java.io.*;
import java.util.*;

public class Main {
	static String fileName;
	public static void main(String[] args)  {
		int id;
		//int c = 1;
		int ports;
		int portserver;
		Scanner scan = new Scanner(System.in);
		String serverName = "localhost";
		int peer;
		int count=0;
		int TTL_Value;
		String msgid;
		String sharedDir;
		ArrayList<Thread> thread=new ArrayList<Thread>();				
		ArrayList<ClientThread> peers=new ArrayList<ClientThread>();		//To store all client threads
		try {
			
			/*System.out.println("Enter the peer_id");
			int peer_id=scan.nextInt();
			scan.nextLine();
			System.out.println("Enter the shared directory"); 		//Local Directory of the Peer
			sharedDir=scan.nextLine();*/
			int peer_id=Integer.parseInt(args[1]);
			//int peer_id=Integer.parseInt(args[0]);
			sharedDir=args[2];
			System.out.println("Peer "+peer_id+" stated with shared directory "+ sharedDir);
			Properties prop = new Properties();						//Properties class to read the configuration file
		    fileName = args[0];
		    System.out.println("Selected the "+fileName);
		    InputStream is = new FileInputStream(fileName);
		    prop.load(is);
		    //System.out.println("peer"+peer_id+".serverport");
		    ports=Integer.parseInt(prop.getProperty("peer"+peer_id+".serverport"));
		    ServerDownload sd=new ServerDownload(ports,sharedDir);
		    sd.start();
		    portserver=Integer.parseInt(prop.getProperty("peer"+peer_id+".port"));
		    //System.out.println(portserver+" "+sharedDir+" "+peer_id);
			ServerThread cs=new ServerThread(portserver,sharedDir,peer_id);
			cs.start();
			//System.out.println("started "+portserver);
			System.out.println("\n     Enter \n\t1 To download a file\n \t2 to Broadcast a invalied message\n");
			int ch=scan.nextInt();
			scan.nextLine();
			if(ch==1)
			{
					System.out.println("Enter the file to be downloaded");
			}
			else if(ch==2)
			{
				System.out.println("Enter the file name you want to Broadcast as Invalied file");
			}
					String f_name=scan.nextLine();
					//long startTime = System.currentTimeMillis();
					++count;
					msgid=peer_id+"."+count;
				    String[] neighbours=prop.getProperty("peer"+peer_id+".next").split(","); 	//Creating a client thread for every neighbouring peer
				    TTL_Value=neighbours.length;
				    for(int i=0;i<neighbours.length;i++)
				    {
				    	int connectingport=Integer.parseInt(prop.getProperty("peer"+neighbours[i]+".port"));
				    	int neighbouringpeer=Integer.parseInt(neighbours[i]);
				    	//System.out.println("sending requests to"+neighbours[i]);
				    	ClientThread cp=new ClientThread(connectingport,neighbouringpeer,f_name,msgid,peer_id,TTL_Value);
				    	//System.out.println(" "+connectingport+" "+neighbouringpeer+" "+f_name+" "+msgid+" "+peer_id);
				    	Thread t=new Thread(cp);
				    	t.start();
				    	thread.add(t);
				    	peers.add(cp);
				    }
				    for(int i=0;i<thread.size();i++)
				    {
				    	try {
				    		//Wait until all the client threads are done executing
							((Thread) thread.get(i)).join();		
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    }
				    int[] peerswithfiles;//part on how to send data from the ConnectingPeer
				    if(ch==1)
				    {
				    	System.out.println("Peers containing the file are: ");
					    for(int i=0;i<peers.size();i++)
					    {
					    	//Reading the list of client threads which contains files
					    	peerswithfiles=((ClientThread)peers.get(i)).getarray();			
					    	for(int j=0;j<peerswithfiles.length;j++)
					    	{	if(peerswithfiles[j]==0)
					    		break;
					    		System.out.println(peerswithfiles[j]);
					    	}
					    }
					    System.out.println("Enter the peer from where to download the file: ");
					    int peerfromdownload=scan.nextInt();
					    int porttodownload=Integer.parseInt(prop.getProperty("peer"+peerfromdownload+".serverport"));
					    ClientasServer(peerfromdownload,porttodownload,f_name,sharedDir);
					    System.out.println("File: "+f_name+" downloaded from Peer "+peerfromdownload+" to Peer "+peer_id);
				    }
			    if(ch==2)
			    {
			    	System.out.println("File Modification Message Broadcasted to ");
				    for(int i=0;i<peers.size();i++)
				    {
				    	//Reading the list of client threads which contains files
				    	peerswithfiles=((ClientThread)peers.get(i)).getarray();			
				    	for(int j=0;j<peerswithfiles.length;j++)
				    	{	if(peerswithfiles[j]==0)
				    		break;
				    		System.out.println("Peer "+peerswithfiles[j]);
				    		//System.out.println("Enter the peer from where to download the file: ");
						    int peerfromdownload=peerswithfiles[j];
						    int porttodownload=Integer.parseInt(prop.getProperty("peer"+peerfromdownload+".serverport"));
						    BroadcastInvaliedMsg(peerfromdownload,porttodownload,f_name);
				    	}
				    }	
			    }
			
			}catch(IOException io)
				{
						io.printStackTrace();
				}
			}
	//Establish connection to Serverdownload thread to download the file
	public static void ClientasServer(int cspeerid,int csportno,String filename,String sharedDir)
	{																															
		try{
			Socket clientasserversocket=new Socket("localhost",csportno);
			ObjectOutputStream ooos=new ObjectOutputStream(clientasserversocket.getOutputStream());
			ooos.flush();
			ObjectInputStream oois=new ObjectInputStream(clientasserversocket.getInputStream());
			ooos.writeObject(filename);
			int readbytes=(int)oois.readObject();
			System.out.println("bytes transferred: "+readbytes);
			byte[] b=new byte[readbytes];
			oois.readFully(b);
			OutputStream fileos=new FileOutputStream(sharedDir+"//"+filename);
			BufferedOutputStream bos=new BufferedOutputStream(fileos);
			bos.write(b, 0,(int) readbytes);
	        System.out.println(filename+" file has be downloaded to your directory "+sharedDir);
	        bos.flush();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public static void BroadcastInvaliedMsg(int cspeerid,int csportno,String filename)
	{
		try{
			Socket clientasserversocket=new Socket("localhost",csportno);
			ObjectOutputStream ooos=new ObjectOutputStream(clientasserversocket.getOutputStream());
			ooos.flush();
			ooos.writeObject("Invalied File "+filename);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
