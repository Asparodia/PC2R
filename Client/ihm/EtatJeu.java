package ihm;

public class EtatJeu {
	private boolean connexionReussi = false;
	private boolean partieTerminee = false;
	private boolean newPos = false;
	
	public EtatJeu() {
	super();
	}
	
	public void setConnexionReussi(boolean etat) {
		connexionReussi = etat;
	}
	
	public boolean getConnexionReussi() {
		return connexionReussi;
	}
	
	public void setPartieTerminee(boolean etat) {
		partieTerminee = etat;
	}
	
	public boolean getPartieTerminee() {
		return partieTerminee;
	}

	public boolean getNewPos() {
		return newPos;
	}

	public void setNewPos(boolean newPos) {
		this.newPos = newPos;
	}
}
