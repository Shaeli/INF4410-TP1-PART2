package ca.polymtl.inf4410.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.io.*;

 
import ca.polymtl.inf4410.tp1.shared.ServerInterface;

public class Server implements ServerInterface {

	private int nb_client=0;
	HashMap<String, String> hashm = new HashMap<String, String>();

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
			chain="Erreure le fichier existe deja";
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

	        // System.out.println(entry.getValue());
	      	}
	      	
	      	result=result+"\n";
	      	return result;
		}
	
	}



	public int generateclientid() throws RemoteException
	{
		return ++nb_client; // note a moi même :synchronized a faire
	}

	public int get(String filename, long checksum) throws RemoteException
	{

		
	}


	
	
}
