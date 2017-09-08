package ca.polymtl.inf4410.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
	public String create(String file_name) throws RemoteException;
	public String list() throws RemoteException;
	public int generateclientid() throws RemoteException;
}
