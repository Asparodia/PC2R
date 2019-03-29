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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String[] separation = line.split("/");
			if (separation.length > 1) {
				if (separation[0].equals("TICK")) {
					String[] coordonnees = separation[1].split("\\|");
					for (String s : coordonnees) {
						String[] coordonnee = s.split(":");
						System.out.print(coordonnee[0]);
						System.out.print(coordonnee[1]);
					}
				}
			} else {
				// System.out.println(separation[0]);
				// System.out.println(line);
			}
		}
	}

}
