package client;

import java.io.PrintStream;
import java.util.HashMap;

import gameobjects.Vaisseau;

public class Envoi {
	PrintStream outChan = null;
	HashMap<String, Vaisseau> vehicules;

	public Envoi(PrintStream s, HashMap<String, Vaisseau> vehicules) {
		outChan = s;
		this.vehicules = vehicules;
	}

	public void connexion(String texte) {
		String line = texte;
		outChan.print("CONNECT/" + line+ "/");
		outChan.flush();
	}

	public void newCom(double angle, double thrust) {
		String line = "A" + angle + "T" + thrust;
		outChan.print("NEWCOM/" + line + "/");
		outChan.flush();
	}

	public void newPos(double x, double y) {
		String line = "X" + x + "Y" + y;
		outChan.print("NEWPOS/" + line+ "/" );
		outChan.flush();
	}
	
	public void exit(String name) {
		outChan.print("EXIT/" + name+ "/");
		outChan.flush();
	}
	
	public void envoi(String msg) {
		outChan.print("ENVOI/" + msg+ "/");
		outChan.flush();
	}
	
	public void envoiPrivee(String name,String msg) {
		outChan.print("PENVOI/" +name+"/"+ msg+ "/");
		outChan.flush();
	}
}
