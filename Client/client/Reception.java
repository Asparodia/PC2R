package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import gameobjects.Vaisseau;

public class Reception extends Thread {
	BufferedReader inChan = null;
	String line = null;
	HashMap<String, Vaisseau> vehicules;

	public Reception(BufferedReader b, HashMap<String, Vaisseau> vehicules) {
		inChan = b;
		this.vehicules = vehicules;
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
				String[] listeVehicules = separation[1].split("\\|");
				for (String s : listeVehicules) {
					String[] individu = s.split(":");
					vehicules.put(individu[0], new Vaisseau(individu[0], individu[1], individu[2]));
				}
			} else {
				System.out.println(line);
			}
		}
	}

}
