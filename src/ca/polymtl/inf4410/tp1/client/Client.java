package ca.polymtl.inf4410.tp1.client;

import ca.polymtl.inf4410.tp1.shared.ServerInterface;
import java.io.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        case "get":
          get(fichier);
          break;
        case "push":
          push(fichier);
          break;
			  case "lock":
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
        System.out.println("Files created successfully...\n");
			} catch (IOException e) {
			  System.out.println("Erreur: " + e.getMessage());
			}
		}catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}


	}


	private void lock (String file_name)
	{
		String response = "";
		if (file_name!= null)
		{
			File fichier = new File("./src/ca/polymtl/inf4410/tp1/client/Client_Storage/"+file_name);
			if (fichier.exists())
			{
				try
				{
 					response=ServerStub.lock(file_name,id,FileToChecksum("./src/ca/polymtl/inf4410/tp1/client/Client_Storage/"+file_name));
 				}catch (RemoteException e)
				{
					System.out.println("Erreur: " + e.getMessage());
				}
			}
			else /*si le client ne possède pas le fichier*/
			{
				try
				{
 					response=ServerStub.lock(file_name,id,"-1");
				}catch (RemoteException e)
				{
					System.out.println("Erreur: " + e.getMessage());
				}
			}
 			if (response.equals("locked"))
 			{
 				System.out.println("file already locked...\n");

 			}else if (response.equals("-1"))
 			{
				System.out.println("File doesn't exist on the server...\n");
 			}else if (response.equals("0"))
 			{
 				System.out.println("file locked...\n");
 			}else
 			{
				try
				{
					BufferedWriter file_writer = new BufferedWriter(new FileWriter(fichier));
           			file_writer.write(response);
            		file_writer.close();
					System.out.println("File downloaded and locked successfully...\n");
				}catch(IOException e)
				{
       				 System.out.println("Erreur: " + e.getMessage());
      			}
 			}
		}

	}


	private void get(String file_name) {
		try {
        File new_file = new File("./src/ca/polymtl/inf4410/tp1/client/Client_Storage/"+file_name);

        if (!new_file.exists()) {
          new_file.createNewFile();
          String file_content_buffer = ServerStub.get(file_name, "-1");
          BufferedWriter file_writer = new BufferedWriter(new FileWriter(new_file));
          file_writer.write(file_content_buffer);
          file_writer.close();
          System.out.println("File created successfully...\n");
        } else {
          String file_content_buffer = ServerStub.get(file_name, FileToChecksum("./src/ca/polymtl/inf4410/tp1/client/Client_Storage/"+file_name));
          if (file_content_buffer.equals("0")) {
            System.out.println("Error : File already up to date...\n");
          } else if (file_content_buffer.equals("-2")){
            System.out.println("Error : File missing from server...\n");
          } else {
            BufferedWriter file_writer = new BufferedWriter(new FileWriter(new_file));
            file_writer.write(file_content_buffer);
            file_writer.close();
            System.out.println("File updated successfully...\n");
          }
        }
      } catch (IOException e) {
        System.out.println("Erreur: " + e.getMessage());
      }
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

  private void push(String file_name) {
    int state = 0;
    try {
      if((new File("./src/ca/polymtl/inf4410/tp1/client/Client_Storage/"+file_name)).exists()) {
        if ((state = ServerStub.push(file_name, FileToString("./src/ca/polymtl/inf4410/tp1/client/Client_Storage/" + file_name), this.id)) == 0 ) {
          System.out.println(file_name + " sent to the server...\n");
        } else {
          System.out.println(file_name + " cannot be sent... \n...Please lock the file first\n");
        }
      }
      System.out.println(file_name + " is now unlocked... \n");;
    } catch (RemoteException e) {
      System.out.println("Erreur: " + e.getMessage());
    }
  }

  private String FileToString(String filePath) {
    String result = "";
    try {
        result = new String (Files.readAllBytes(Paths.get(filePath)));
    } catch (IOException e) {
        System.out.println("Erreur: " + e.getMessage());
    }
    return result;
  }

}
