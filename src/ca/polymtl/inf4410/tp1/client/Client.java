package ca.polymtl.inf4410.tp1.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import ca.polymtl.inf4410.tp1.shared.ServerInterface;

public class Client {
	public static void main(String[] args) {
		String Hostname = null;

		if (args.length > 0) {
			Hostname = args[0];
		}
		else
		{
			Hostname="127.0.0.1";
		}

		Client client = new Client(Hostname);
		client.run();
	}

	
	private ServerInterface ServerStub = null;

	public Client(String distantServerHostname) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

			ServerStub = loadServerStub(distantServerHostname);

	}

	private void run() {

		Scanner sc = new Scanner(System.in);
		String entry[];

		entry = sc.nextLine().split(" ");
		switch(entry[0]) 
		{
  			case "create" :
  			 	create(entry[1]);
     		 	break; 
     		case "list" :
     		 	list();
   			default : 
   				System.out.println("Commande non reconnue");
   				break;
  
		}
	}

	private ServerInterface loadServerStub(String hostname) {
		ServerInterface stub = null;
		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (ServerInterface) registry.lookup("server");
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage()
					+ "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}

	private void create(String name)
	{
		try
		{
			String result = ServerStub.create(name);
			System.out.println(result);
		} 
		catch (RemoteException e) 
		{
			System.out.println("Erreur: " + e.getMessage());
		}

	}

	private void list()
	{
		try
		{
			String result = ServerStub.list();
			System.out.println(result);
		} 
		catch (RemoteException e) 
		{
			System.out.println("Erreur: " + e.getMessage());
		}
	}


}
