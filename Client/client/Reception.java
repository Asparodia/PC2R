package client;

import gameobjects.Objectif;
import gameobjects.Vaisseau;
import ihm.DebutJeu;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class Reception extends Thread {

	BufferedReader inChan;
	String line;
	HashMap<String, Vaisseau> vehicules;

	DebutJeu listener;
	Objectif objectif;

	public Reception(BufferedReader b, HashMap<String, Vaisseau> vehicules,
			DebutJeu list, Objectif objectif) {
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
				System.out.println("--- bye bye ---");
				break;
			}
			String[] separation = line.split("/");
			switch (separation[0]) {

			case "WELCOME":
				System.out.println("------ WELCOME TO ASTEROID -----");
				System.out.println("[*] CURRENT PHASE : " + separation[1]);
				System.out.println("[*] SCORES : " + separation[2]);
				System.out.println("[*] NEXT OBJECTIVE IS AT : "
						+ separation[3]);
				System.out.println("--------------------------------");
				synchronized (listener) {
					listener.setConnexionReussi(true);
				}
				break;
			case "PLAYERLEFT":
				synchronized (vehicules) {
					vehicules.get(separation[1]).setAEnlever(true);
					;
				}
				System.out.println("[*] PLAYER LEFT : " + separation[1]);
				break;
			case "SESSION":
				System.out.println("[*] SESSION STARTED WITH :");
				String[] listeVehicules = separation[1].split("\\|");
				synchronized (vehicules) {
					for (String s : listeVehicules) {
						String[] individu = s.split(":");
						vehicules.put(individu[0], new Vaisseau(null,
								individu[0], individu[1], individu[2]));
						System.out.println("[*] >>>>> " + individu[0]);
					}
				}
				String[] objectifSplit = separation[2].substring(1,
						separation[2].length() - 1).split("Y");
				this.objectif.setPosX(new Double(objectifSplit[0]));
				this.objectif.setPosY(new Double(objectifSplit[1]));
				synchronized (listener) {
					listener.notifyAll();
				}
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
				System.out.println("[*] NEW OBJECTIVE IS AT : X " + x1
						+ " : Y " + newObj[1]);
				System.out.println("[*] SCORES : " + separation[2]);

				break;
			case "NEWPLAYER":
				System.out.println("[*] NEW PLAYER JOIN THE GAME : "
						+ separation[1]);
				synchronized (vehicules) {
					vehicules.put(separation[1], new Vaisseau(null,
							separation[1], Double.MAX_VALUE, -10000));
				}
				break;
			case "NEWPOS":
				synchronized (vehicules) {
					Vaisseau moi = vehicules.get(separation[1]);
					String[] coordonnees = separation[2].split("Y");
					moi.setPosX(new Double(coordonnees[0].substring(1,
							coordonnees[0].length() - 1)));
					moi.setPosY(new Double(coordonnees[1]));
				}
			case "WINNER":
				synchronized (vehicules) {
					Iterator<Entry<String, Vaisseau>> iterateur = vehicules
							.entrySet().iterator();
					while (iterateur.hasNext()) {
						Entry<String, Vaisseau> courant = iterateur.next();
						courant.getValue().setFinJeu(true);
					}
				}
				break;
			case "RECEPTION":
				System.out.println("[*] " + separation[1]);
				break;
			case "PRECEPTION":
				System.out.println("[*] >>>MP from " + separation[2] + " : "
						+ separation[1]);
				break;
			case "DENIED":
				System.out
						.println("[*] DENIED this pseudo is already used or the personne you are trying to contact doesnt exist");
				synchronized (listener) {
					listener.setConnexionReussi(false);
					listener.notifyAll();
				}
				break;
			default:
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
