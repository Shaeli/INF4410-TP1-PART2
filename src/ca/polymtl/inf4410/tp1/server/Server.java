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
				chain=file_name+" created";
				hashm.put(file_name,"unlock");
			}catch(IOException e)
			{
				System.err.println();
				System.err.println("Erreur: " + e.getMessage());
				chain="Error during creation";
			}
		}
		else
		{
			chain="Error: file already exists";
		}

		System.out.println(chain);
		return chain;
	}

	public String list() throws RemoteException
	{
		int cpt = 0;
		String result = "";
		if (hashm.isEmpty())
		{
			return "0 files\n";

		}
		else
		{
			Set set = hashm.entrySet();
	      	Iterator it = set.iterator();
	      	while(it.hasNext())
	      	{
	      		cpt++;
	        	Map.Entry entry = (Map.Entry)it.next();
	        	String file_name = (String)entry.getKey();
	        	if(hashm.get(file_name).equals("unlock"))
	        	{
	        		result = result + "\n" + file_name + " : unlocked";
	        	}
	        	else
	        	{
	        		result = result + "\n" + file_name + " : locked by Client " + hashm.get(file_name);
	        	}
	      	}
	      	result=result+"\n"+cpt+" Files\n";
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

		/**
		* Cette méthode permet de synchroniser le répertoire du client avec le serveur à distance.
		* Aucune vérification du checksum, les fichiers coté serveur sont recopiés coté client.
		*	Coté serveur, vérification de l'existence du fichier.
		*
		* @param : Void
		* @return : HashMap<String, String>
		*/
  	public HashMap<String, String> syncLocalDir() throws RemoteException
  	{
    	HashMap<String, String> files = new HashMap<String, String>();
    	for (String file_name : hashm.keySet())
    	{
      		files.put(file_name, FileToString("./src/ca/polymtl/inf4410/tp1/server/server_stockage/" + file_name));
    	}
    	return files;
  	}


		/**
		* Cette méthode permet de convertir un fichier en string afin de pouvoir l'envoyer sur le réseau.
		*
		* @param : String représentant le nom du fichier à convertir
		* @return : String -> String du fichier
		*/
  	private String FileToString(String filePath)
  	{
    	String result = "";
    	try
			{
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
				System.out.println("ok");
				String unlocked = "unlock";
				if(unlocked.equals(hashm.get(file_name)))
				{
					hashm.put(file_name,Integer.toString(clientid));
					return get(file_name,checksum);

				}
				else
				{
					return ("locked by Client " + hashm.get(file_name));
				}
			}
			else{
				return "-2";
			}
	}

	/**
	* Cet méthode permet de récupérer un fichier coté serveur et de le copié dans le repertoire courant du client.
	* Si le client ne possède pas le fichier, il envoie -1 au serveur pour forcer l'envoie.
	* Le serveur peut renvoyer certaines informations comme la non présence du fichier sur le serveur ou encore le fait que le fichier soit déja à jour coté client.
	*
	* @param : String représentant le nom du fichier à convertir.
	* @param : String représentant le checksum du fichier à convertir.
	* @return : String contenant le fichier.
	*/
  public String get(String name, String checksum) throws RemoteException
	{
		String file_content_buffer = "";
		File f1 = new File("./src/ca/polymtl/inf4410/tp1/server/server_stockage/"+name);
		if (!f1.exists())
		{
			file_content_buffer = "-2";
		}
		else if (checksum.equals(FileToChecksum("./src/ca/polymtl/inf4410/tp1/server/server_stockage/" + name)))
		{
			file_content_buffer = "0";
		}
		else if (checksum.equals("-1"))
		{
			file_content_buffer = FileToString("./src/ca/polymtl/inf4410/tp1/server/server_stockage/"+name);
		}
		else
		{
			file_content_buffer = FileToString("./src/ca/polymtl/inf4410/tp1/server/server_stockage/"+name);
		}
		return file_content_buffer;
  }


	/**
	* Cette méthode permet de convertir un fichier en un checksum via l'appel à
	* la librairie MessageDigest et l'utilisation de la fonction de hachage SHA1.
	*
	* @param : String représentant le nom du fichier à convertir
	* @return : String -> checksum du fichier
	*/
	private String FileToChecksum(String name)
	{
		int i = 0;
		byte [] file_content_buffer = new byte[1024];
    StringBuffer sb = new StringBuffer("");
    String checksum = "";
    try
		{
      MessageDigest md = MessageDigest.getInstance("SHA1");
      FileInputStream file_reader = new FileInputStream(name);

      while ((i=file_reader.read(file_content_buffer)) != -1)
			{
        md.update(file_content_buffer, 0, i);
      }

      byte[] mdbytes = md.digest();

      for (int k = 0; k < mdbytes.length; k++)
			{
        sb.append(Integer.toString((mdbytes[k] & 0xff) + 0x100, 16).substring(1));
      }
    }
		catch (FileNotFoundException e)
		{
      System.out.println("Erreur: " + e.getMessage());
    }
		catch (NoSuchAlgorithmException e)
		{
      System.out.println("Erreur: " + e.getMessage());
    }
		catch (IOException e)
		{
      System.out.println("Erreur: " + e.getMessage());
    }
    return checksum = sb.toString();
	}


	/**
	* Cette méthode permet de convertir en String le fichier dont le nom est passé en paramètres.
	* Avant de valider l'envoie des données, plusieurs aspects sont vérifiés :
	*		Fichier doit être lock via l'id du client
	*		Le fichier existe coté serveur
	*
	* @param : String représentant le nom du fichier à convertir
	* @param : String représentant le contenu du fichier envoyé au serveur.
	* @param : int correspondant à l'id de l'utilisateur souhaitant envoyer le fichier.
	* @return : int représentant l'état de la requête
	*/
	public int push(String file_name, String file_content, int client_id) throws RemoteException
	{
		int state = 0;
		if (hashm.containsKey(file_name))
		{
			if(hashm.get(file_name).equals("unlock"))
			{
				state = -1;
			}
			else if(hashm.get(file_name).equals(Integer.toString(client_id)))
			{
				try
				{
					File f1 = new File("./src/ca/polymtl/inf4410/tp1/server/server_stockage/"+file_name);
					BufferedWriter file_writer = new BufferedWriter(new FileWriter(f1));
					file_writer.write(file_content);
					file_writer.close();
					hashm.put(file_name, "unlock");
				}
				catch (IOException e)
				{
	        System.out.println("Erreur: " + e.getMessage());
	      }
			}
			else
			{
				state = 1;
			}
		}
		return state;
	}

}
