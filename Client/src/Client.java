package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class Client {

	protected final static int PORT = 2019;
	protected final static String HOST = "ppti-14-509-07";

	public static void main(String[] args) {
		Socket s = null;
		try {
			System.out.println(HOST);
			s = new Socket(HOST, PORT);

			BufferedReader inChan = new BufferedReader(new InputStreamReader(s.getInputStream()));
			PrintStream outChan = new PrintStream(s.getOutputStream());

			System.out.println("Connexion etablie : " + s.getInetAddress() + " port : " + s.getPort());

			new Reception(inChan).start();
			new Envoi(outChan).start();
		} catch (IOException e) {
			System.err.println(e);

		} finally {
			if ((s != null)) {
//					s.close();
			}
		}

	}

}
