package ca.polymtl.inf4410.tp1.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import ca.polymtl.inf4410.tp1.shared.ServerInterface;

public class Client {

	private int id;
	private ServerInterface ServerStub = null;


	public static void main(String[] args) {
		String Hostname = "127.0.0.1";
		String commande=null;
		String fichier=null;

		if (args.length > 0) {
			commande= args[0];
		}
		else
		{
			System.out.println("Veuillez entrer une commande");
		}
		if(args.length == 2)
		{
			fichier = args[1];
		}

		Client client = new Client(Hostname);
		if (commande != null)
		{
			client.run(commande,fichier);
		}
	}


	public Client(String Hostame) {
		super();

		if (System.getSecurityManager() == null)
		{
			System.setSecurityManager(new SecurityManager());
		}

		ServerStub = loadServerStub(Hostame);
		File id_cli = new File("id.txt");
		if (!id_cli.exists())
		{

            try
            {
            	id=generateclientid();
				id_cli.createNewFile();
				FileWriter writer = new FileWriter(id_cli);
                writer.write(Integer .toString(id));
                writer.close();
            }catch (Exception e)
            {
            	System.err.println("Erreur: " + e.getMessage());
        	}
		}
		else
		{
			try
			{
				Scanner sc = new Scanner(id_cli);
				id = sc.nextInt();
				sc.close();
			}catch(FileNotFoundException err)
			{
				System.err.println("Erreur: " + err.getMessage());
			}

		}


	}

	private void run(String commande, String fichier) {


		switch(commande)
		{
  			case "create" :
  			 	create(fichier);
     		 	break;
     		case "list" :
     		 	list();
     		 	break;
			case "syncLocalDir":
				syncLocalDir();
				break;
			case "lock" :
				lock(fichier);
				break;
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
		if (name!= null)
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

	}

	private void list()
	{
		try
		{
			String result = ServerStub.list();
			System.out.print(result);
		}
		catch (RemoteException e)
		{
			System.out.println("Erreur: " + e.getMessage());
		}
	}

	private int generateclientid()
	{
		int id=0;
		try
		{
			id=ServerStub.generateclientid();

		}
		catch (RemoteException e)
		{
			System.out.println("Erreur: " + e.getMessage());
		}
		return id;
	}


	private void syncLocalDir() {
		try {
			HashMap<String, String> files = new HashMap<String, String>();
			files = ServerStub.syncLocalDir();
			try {
				for (String file_name : files.keySet()) {
					File new_file = new File("./src/ca/polymtl/inf4410/tp1/client/Client_Storage/"+file_name);
					if (!new_file.exists()) {
						new_file.createNewFile();
					}
					BufferedWriter file_writer = new BufferedWriter(new FileWriter(new_file));
					file_writer.write(files.get(file_name));
					file_writer.close();
				}
			} catch (IOException e) {
			  System.out.println("Erreur: " + e.getMessage());
			}
		}catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}


	}

	private void lock (String file_name)
	{
		if (file_name!= null)
		{	
			byte[] hash=null;
			File fichier = new File("./src/ca/polymtl/inf4410/tp1/client/Client_Storage/"+file_name);
			String content="";

			if (fichier.exists()) 
			{
				try 
				{
					content= new String (Files.readAllBytes(Paths.get("./src/ca/polymtl/inf4410/tp1/client/Client_Storage/"+file_name)));
 					MessageDigest md = MessageDigest.getInstance("SHA1");
 					byte[] octetx = content.getBytes(); 
 					hash = md.digest(octetx);
 					System.out.println(hash);
 					try{
 						ServerStub.lock(file_name,id,hash);
 					}catch (RemoteException e)
					{
						System.out.println("Erreur: " + e.getMessage());
					}	

				}catch (IOException e) 
    			{
    				e.printStackTrace();
    			}catch (NoSuchAlgorithmException e) 
    			{
    				e.printStackTrace();
    			}
			}
			else
			{
					System.out.println("Le fichier n'existe pas localement");
			}

			//try
			//{
				//String result = ServerStub.lock(file_name,id,hash);
				System.out.println("bjr");
			//}
			
		}

	}


}
