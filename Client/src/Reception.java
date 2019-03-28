package src;

import java.io.BufferedReader;
import java.io.IOException;

public class Reception extends Thread {
	BufferedReader inChan = null;
	String line = null;

	public Reception(BufferedReader b) {
		inChan = b;
	}

	@Override
	public void run() {
		while (true) {
			try {
				line = inChan.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (line == null) {
				System.out.print("Connexion terminee");
				break;
			}

			System.out.println(line);
		}
	}

}
