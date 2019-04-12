package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import gameobjects.Vaisseau;
import ihmexemple.Objectif;

public class Reception extends Thread {

	BufferedReader inChan;
	String line;
	HashMap<String, Vaisseau> vehicules;

	Object listener;
	Objectif objectif;

	public Reception(BufferedReader b, HashMap<String, Vaisseau> vehicules,
			Object list, Objectif objectif) {
		inChan = b;
		this.vehicules = vehicules;
		this.listener = list;
		this.objectif = objectif;
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
			switch (separation[0]) {
			case "WELCOME":
				System.out.println("WELCOME a print sur ihm");
				break;
			case "SESSION":
				String[] listeVehicules = separation[1].split("\\|");
				for (String s : listeVehicules) {
					String[] individu = s.split(":");
					vehicules.put(individu[0], new Vaisseau(null, individu[0],
							individu[1], individu[2]));
				}
				String[] objectifSplit = separation[2].substring(1,
						separation[2].length() - 1).split("Y");
				this.objectif.setX(new Double(objectifSplit[0]));
				this.objectif.setY(new Double(objectifSplit[1]));
				synchronized (listener) {
					listener.notifyAll();
				}
				System.out.println("SESSION a print sur ihm");
				break;
			case "TICK":
				if (separation.length > 1) {
					String[] coordonnees = separation[1].split("\\|");
					for (String s : coordonnees) {
						String[] coordonnee = s.split(":");
					}
					System.out.println(line);
				}
				break;
			case "NEWOBJ" :
				String[] newObj = separation[1].split("Y");
				String x1 = newObj[0].substring(1, newObj[0].length() - 1);
				objectif.setX(new Double(x1));
				objectif.setY(new Double(newObj[1]));
				objectif.majPos();
				System.out.println("Newobjectif");
				String[] scores = separation[2].split("\\|");
				System.out.println(scores[0]);
				break;
			default:
				System.out.println("CAS NON TRAITEE");
				break;
			}

		}
	}

}
