package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;

import gameobjects.Vaisseau;

public class ClientMain {

	protected final static int PORT = 2019;
	protected final static String HOST = "ppti-14-502-14";

	public static void main(String[] args) {
		Socket s = null;
		HashMap<String, Vaisseau> vehicules = new HashMap<>();
		try {
			System.out.println(HOST);
			s = new Socket(HOST, PORT);

			BufferedReader inChan = new BufferedReader(new InputStreamReader(s.getInputStream()));
			PrintStream outChan = new PrintStream(s.getOutputStream());

			System.out.println("Connexion etablie : " + s.getInetAddress() + " port : " + s.getPort());

			Reception r = new Reception(inChan,vehicules);
			Envoi e = new Envoi(outChan,vehicules);

			r.start();
			e.start();
			try {
				r.join();
				e.join();
				s.close();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (IOException e) {
			System.err.println(e);

		} finally {
			if ((s != null)) {
				/// JOIN LES R ET E

				// s.close();
			}
		}

	}

}
