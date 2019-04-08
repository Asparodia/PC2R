package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import gameobjects.Vaisseau;
import ihmexemple.AsteroidsApp;
import ihmexemple.IHMclient;
import javafx.application.Application;

public class Reception extends Thread {
	BufferedReader inChan = null;
	String line = null;
	HashMap<String, Vaisseau> vehicules;
	private String nomJoueur;
	Object ihm;

	public Reception(BufferedReader b, HashMap<String, Vaisseau> vehicules, Object IHMCLINET) {
		inChan = b;
		this.vehicules = vehicules;
		ihm = IHMCLINET;
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
				System.out.println("bye bye");
				break;
			}
			System.out.println(line);
			String[] separation = line.split("/");
			if (separation[0].equals("TICK")) {
				if (separation.length > 1) {
					String[] coordonnees = separation[1].split("\\|");
					for (String s : coordonnees) {
						String[] coordonnee = s.split(":");
						System.out.print(coordonnee[0]);
						System.out.print(coordonnee[1]);
					}
				}
			}
			if (separation[0].equals("WELCOME")) {
				nomJoueur = line.split("/")[2].split(":")[0];

			} else if (separation[0].equals("SESSION")) {
				String[] listeVehicules = separation[1].split("\\|");
				for (String s : listeVehicules) {
					String[] individu = s.split(":");
					System.out.println(individu[1]);
					System.out.println(individu[2]);
					vehicules.put(individu[0], new Vaisseau(individu[0], individu[1], individu[2]));
				
				}
				synchronized (ihm) {
System.out.println("LE LOCK");
					ihm.notifyAll();
				}
			} else {
				System.out.println("CAS NON TRAITE");
			}
		}
	}

}
