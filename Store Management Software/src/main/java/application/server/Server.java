package application.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import application.common.Logger;

public class Server implements Runnable {

	private ServerSocket server;
	private ExecutorService executor;

	public void startServer() {
		try {
			Logger.getInstance().setLogger("Server");
			server = new ServerSocket(8000);
			executor = Executors.newCachedThreadPool();
			Thread t = new Thread(this);
			t.start();
			DatabaseHandler.getInstance();
		} catch (Exception e) {
			Logger.getInstance().captureException(e);
			return;
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				System.out.println("In attesa di connessione...");
				Socket socket = server.accept();
				System.out.println("Client collegato!");
				ManagerHandler m = new ManagerHandler(socket);
				executor.submit(m);
			} catch (Exception e) {
				Logger.getInstance().captureException(e);
				return;
			}
		}

	}
}
