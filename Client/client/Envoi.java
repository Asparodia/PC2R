package client;

import java.io.PrintStream;
import java.util.HashMap;

import gameobjects.Vaisseau;

// POUR TESTER LA VERSION TEXTE REMETTRE THREAD ET RUN

public class Envoi /* extends Thread */ {
	PrintStream outChan = null;
	HashMap<String, Vaisseau> vehicules;

	public Envoi(PrintStream s, HashMap<String, Vaisseau> vehicules) {
		outChan = s;
		this.vehicules = vehicules;
	}

	// @Override
	// public void run() {
	// String line = new String();
	// char c;
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
	// }

	public void connexion(String texte) {
		String line = texte;
		outChan.print("CONNECT/" + line);
		outChan.flush();
	}

	public void newCom(double angle, double thrust) {
		String line = "A" + angle + "T" + thrust;
		outChan.print("NEWCOM/" + line);
		outChan.flush();
	}

	public void newPos(double x, double y) {
		String line = "X" + x + "Y" + y;
		outChan.print("NEWPOS/" + line );
		outChan.flush();
	}
	
	public void exit(String name) {
		outChan.print("EXIT/" + name);
		outChan.flush();
	}
}
