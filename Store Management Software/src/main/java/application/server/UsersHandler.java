package application.server;

import java.util.ArrayList;
import java.util.HashMap;

public class UsersHandler {

	/**
	 * tutti gli utenti online
	 */
	private static HashMap<String, ManagerHandler> users = new HashMap<String, ManagerHandler>();

	/*
	 * concurrent HashMap oppure mettere synchronized (per evitare che due utenti
	 * con lo stesso email provano a fare il login in contemporanea)
	 */
	public synchronized static boolean insertUser(String email, ManagerHandler handler) {

		if (users.containsKey(email) || email.equals("")) // se l'utente esiste, allora si è già loggato
			return false;
		users.put(email, handler);
		System.out.println("[SERVER] ho aggiunto " + email);
		return true;
	}

	public synchronized static void removeUser(String email) { // se un utente si disconnette lo dobbiamo rimuovere
		System.out.println("[SERVER] " + email + " rimosso");
		users.remove(email);
	}

	public synchronized static String allUsers() { // una stringa contenente tutti gli utenti connessi
		ArrayList<String> onlineUsers = new ArrayList<String>();
		for (String s : users.keySet()) // scorriamo l'insieme degli utenti connessi
			onlineUsers.add(s);
		return onlineUsers.toString();
	}

	public static boolean contains(String email) {
		return users.containsKey(email);
	}

	public synchronized static int getNumOnlineUsers() {
		return users.size();
	}

}
