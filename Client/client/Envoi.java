package client;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

import gameobjects.Vaisseau;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;

public class Envoi extends Thread {
	PrintStream outChan = null;
	HashMap<String, Vaisseau> vehicules;

	public Envoi(PrintStream s, HashMap<String, Vaisseau> vehicules) {
		outChan = s;
		this.vehicules = vehicules;
	}

	@Override
	public void run() {
		String line = new String();
		char c;
		// while (true) {
		// line = "";
		// try {
		// while ((c = (char) System.in.read()) != '\n') {
		// // client ecris
		// line = line + c;
		// }
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// // envoie au serv
		// outChan.print(line);
		// outChan.flush();
		// if (line.contains("EXIT/")) {
		// break;
		// }
		// }
	}

	public void connexion(String texte) {
		String line = texte;
		outChan.print("CONNECT/" + line + "\n");
		outChan.flush();
	}
}
