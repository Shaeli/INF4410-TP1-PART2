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
			ServerInterface stub = (ServerInterface) UnicastRemoteObject
					.exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("server", stub);
			System.out.println("Server ready.");
		} catch (ConnectException e) {
			System.err
					.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}
	// @Override
	// public int execute(int a, int b) throws RemoteException {
	// 	return a + b;
	// }

	@Override
	public String create(String file_name) throws RemoteException
	{

		File new_file = new File(file_name);
		String chain;
		try 
		{
			new_file.createNewFile();
				chain="fichier crée";
			
		}catch(IOException e)
		{	
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
			chain="la creation du fichier a echoue";
		}
		System.out.println(chain);
		return chain;
	}


	
}
