package client;

import java.io.IOException;
import java.io.PrintStream;

public class Envoi extends Thread {
	PrintStream outChan = null;

	public Envoi(PrintStream s) {
		outChan = s;
	}

	@Override
	public void run() {
		String line = new String();
		char c;
		while (true) {
			line = "";
			try {
				while ((c = (char) System.in.read()) != '\n') {
					// client ecris
					line = line + c;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			// envoie au serv
			outChan.print(line);
			outChan.flush();

		}

	}
}
