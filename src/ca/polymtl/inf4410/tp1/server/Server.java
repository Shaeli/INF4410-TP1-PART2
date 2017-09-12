package ca.polymtl.inf4410.tp1.server;

import ca.polymtl.inf4410.tp1.shared.ServerInterface;
import java.io.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ca.polymtl.inf4410.tp1.shared.ServerInterface;

public class Server implements ServerInterface {

	private int nb_client=0;
	HashMap<String, String> hashm = new HashMap<String, String>();
	private Object mutex_id=new Object();
	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
		super();
	}

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("server", stub);
			System.out.println("Server ready.");
		} catch (ConnectException e) {
			System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	public String create(String file_name) throws RemoteException
	{
		File new_file = new File("./src/ca/polymtl/inf4410/tp1/server/server_stockage/"+file_name);
		String chain;
		if (!new_file.exists())
		{
			try
			{
				new_file.createNewFile();
				chain=file_name+" ajouté";
				hashm.put(file_name,"unlock");
			}catch(IOException e)
			{
				System.err.println();
				System.err.println("Erreur: " + e.getMessage());
				chain="la creation du fichier a echoue";
			}
		}
		else
		{
			chain="Erreur le fichier existe deja";
		}

		System.out.println(chain);
		return chain;
	}

	public String list() throws RemoteException
	{
		String result = "";
		if (hashm.isEmpty())
		{
			return "aucun fichier\n";

		}
		else
		{
			Set set = hashm.entrySet();
	      	Iterator it = set.iterator();
	      	while(it.hasNext())
	      	{
	        	Map.Entry entry = (Map.Entry)it.next();
	        	result = result + "\n" + entry.getKey();
	      	}

	      	result=result+"\n";
	      	return result;
		}

	}

	public int generateclientid() throws RemoteException
	{
		synchronized(mutex_id)
		{
			return ++nb_client;
		}
	}


  	public HashMap<String, String> syncLocalDir() throws RemoteException
  	{
    	HashMap<String, String> files = new HashMap<String, String>();
    	for (String file_name : hashm.keySet())
    	{
      		files.put(file_name, FileToString("./src/ca/polymtl/inf4410/tp1/server/server_stockage/" + file_name));
    	}
    	return files;
  	}

  	private String FileToString(String filePath)
  	{
    	String result = "";
    	try {
        	result = new String (Files.readAllBytes(Paths.get(filePath)));
    	}
    	catch (IOException e)
    	{
    		e.printStackTrace();
    	}
    	return result;
  	}

	public String lock(String file_name, int clientid, String checksum) throws RemoteException
	{

			if(hashm.containsKey(file_name))
			{
				String unlocked = "unlock";
				if(unlocked.equals(hashm.get(file_name)))
				{
					hashm.put(file_name,Integer.toString(clientid));
					return get(file_name,checksum);

				}
				else
				{
					return "locked";
				}
			}
			else{

				return "-1";
			}
	}

  public String get(String name, String checksum) throws RemoteException {
		String file_content_buffer = "";
		File f1 = new File("./src/ca/polymtl/inf4410/tp1/server/server_stockage/"+name);
		if (!f1.exists()) {
			file_content_buffer = "-2";
		} else if (checksum.equals(FileToChecksum("./src/ca/polymtl/inf4410/tp1/server/server_stockage/" + name))) {
			file_content_buffer = "0";
		} else if (checksum.equals("-1")) {
			file_content_buffer = FileToString("./src/ca/polymtl/inf4410/tp1/server/server_stockage/"+name);
		} else {
			file_content_buffer = FileToString("./src/ca/polymtl/inf4410/tp1/server/server_stockage/"+name);
		}
		return file_content_buffer;
  }

	private String FileToChecksum(String name) {
		int i = 0;
		byte [] file_content_buffer = new byte[1024];
    StringBuffer sb = new StringBuffer("");
    String checksum = "";
    try {
      MessageDigest md = MessageDigest.getInstance("SHA1");
      FileInputStream file_reader = new FileInputStream(name);

      while ((i=file_reader.read(file_content_buffer)) != -1) {
        md.update(file_content_buffer, 0, i);
      }

      byte[] mdbytes = md.digest();

      for (int k = 0; k < mdbytes.length; k++) {
        sb.append(Integer.toString((mdbytes[k] & 0xff) + 0x100, 16).substring(1));
      }
    } catch (FileNotFoundException e) {
      System.out.println("Erreur: " + e.getMessage());
    } catch (NoSuchAlgorithmException e) {
      System.out.println("Erreur: " + e.getMessage());
    } catch (IOException e) {
      System.out.println("Erreur: " + e.getMessage());
    }
    return checksum = sb.toString();
	}

	public int push(String file_name, String file_content, int client_id) throws RemoteException {
		int state = 0;
		if (hashm.containsKey(file_name)) {
			if(hashm.get(file_name).equals("unlock")) {
				state = -1;
			} else if(hashm.get(file_name).equals(Integer.toString(client_id))) {
				try {
					File f1 = new File("./src/ca/polymtl/inf4410/tp1/server/server_stockage/"+file_name);
					BufferedWriter file_writer = new BufferedWriter(new FileWriter(f1));
					file_writer.write(file_content);
					file_writer.close();
					hashm.put(file_name, "unlock");
				}catch (IOException e) {
	        System.out.println("Erreur: " + e.getMessage());
	      }
			}
		}
		return state;
	}

}
