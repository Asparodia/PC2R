package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import gameobjects.Vaisseau;
import ihmexemple.AsteroidsApp;

public class Reception extends Thread {
	BufferedReader inChan = null;
	String line = null;
	HashMap<String, Vaisseau> vehicules;
	Lock verrouListeVehicules;
	Condition conditionListeVehicules; // Inutile ?
	private String nomJoueur;

	public Reception(BufferedReader b, HashMap<String, Vaisseau> vehicules, Lock listeVehicules,
			Condition listeVehiculesCondition) {
		inChan = b;
		this.vehicules = vehicules;
		this.verrouListeVehicules = listeVehicules;
		this.conditionListeVehicules = listeVehiculesCondition;
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
			} else if (separation[0].equals("SESSION")) {
				try {
					verrouListeVehicules.lock();
					String[] listeVehicules = separation[1].split("\\|");
					for (String s : listeVehicules) {
						String[] individu = s.split(":");
						vehicules.put(individu[0], new Vaisseau(individu[0], individu[1], individu[2]));
						System.out.println(vehicules);
					}
				} finally {
					AsteroidsApp app = new AsteroidsApp();
					app.init(vehicules, verrouListeVehicules, conditionListeVehicules, nomJoueur);
					new Thread(app).start();
					verrouListeVehicules.unlock();
				}
			} else if (separation[0].equals("WELCOME")) {
				nomJoueur = line.split("/")[2].split(":")[0];
			} else {
				System.out.println(line);
			}
		}
	}

}
