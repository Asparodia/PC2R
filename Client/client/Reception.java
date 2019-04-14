package client;

import gameobjects.Objectif;
import gameobjects.Vaisseau;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

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
				// System.out.println("WELCOME(a print sur ihm)");
				System.out.println(line);
				break;
			case "PLAYERLEFT":
				synchronized (vehicules) {
					vehicules.get(separation[1]).setAEnlever(true);
					; // pb ici
				}
				break;
			case "SESSION":
				String[] listeVehicules = separation[1].split("\\|");
				synchronized (vehicules) {
					for (String s : listeVehicules) {
						String[] individu = s.split(":");
						vehicules.put(individu[0], new Vaisseau(null,
								individu[0], individu[1], individu[2]));
					}
				}
				String[] objectifSplit = separation[2].substring(1,
						separation[2].length() - 1).split("Y");
				this.objectif.setPosX(new Double(objectifSplit[0]));
				this.objectif.setPosY(new Double(objectifSplit[1]));

				synchronized (listener) {
					listener.notifyAll();
				}
				// System.out.println("SESSION a print sur ihm");
				System.out.println(line);
				break;
			case "TICK":
				if (separation.length > 1) {
					String[] coordonnees = separation[1].split("\\|");
					for (String s : coordonnees) {
						String[] coordonnee = s.split(":");
						Vaisseau courant = vehicules.get(coordonnee[0]);
						if (courant == null) {
							System.out.println("PB dans TICK courant est null");
							continue;
						}
						// System.out.println(s);
						String[] c = leVraiSplit(coordonnee[1]);
						courant.MaJPos(new Double(c[0]), new Double(c[1]),
								new Double(c[2]), new Double(c[3]), new Double(
										c[4]));
					}
				}
				break;
			case "NEWOBJ":
				String[] newObj = separation[1].split("Y");
				String x1 = newObj[0].substring(1, newObj[0].length() - 1);
				synchronized (objectif) {
					objectif.setPosX(new Double(x1));
					objectif.setPosY(new Double(newObj[1]));
					objectif.setAJour(false);
				}
				// objectif.majPos();
				// String[] scores = separation[2].split("\\|");
				System.out.println(line);
				break;
			case "NEWPLAYER":
				// System.out.println(vehicules);
				// System.out.println(nouveauVehicule);
				synchronized (vehicules) {
					vehicules.put(separation[1], new Vaisseau(null,
							separation[1], Double.MAX_VALUE, -10000));
				}
				System.out.println(line);
				break;
			case "NEWPOS":
				synchronized (vehicules) {
					Vaisseau moi = vehicules.get(separation[1]);
					String[] coordonnees = separation[2].split("Y");
					// System.out.println("MaJ coordonnees");
					moi.setPosX(new Double(coordonnees[0].substring(1,
							coordonnees[0].length() - 1)));
					moi.setPosY(new Double(coordonnees[1]));
				}
				// System.out.println(line);
			case "WINNER":
				synchronized (vehicules) {
					Iterator<Entry<String, Vaisseau>> iterateur = vehicules
							.entrySet().iterator();
					while (iterateur.hasNext()) {
						Entry<String, Vaisseau> courant = iterateur.next();
						// Besoin nécessairement de mutex ?
						courant.getValue().setFinJeu(true);
					}
				}
				System.out.println(line);
				break;
			default:
				System.out.println("CAS NON TRAITEE");
				break;
			}

		}
	}

	public String[] leVraiSplit(String chaine) {
		String[] premier = chaine.split("VX");
		String[] coordonnees = premier[0].substring(1, premier[0].length() - 1)
				.split("Y");
		String[] deuxieme = premier[1].split("VY");
		String[] troisieme = deuxieme[1].split("T");

		String x = coordonnees[0];
		String y = coordonnees[1];
		String vx = deuxieme[0];
		String vy = troisieme[0];
		String d = troisieme[1];
		String[] res = { x, y, vx, vy, d };
		return res;
	}
}
