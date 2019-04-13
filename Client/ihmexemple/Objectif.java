package ihmexemple;

import javafx.scene.Node;

public class Objectif extends GameObject {

	private double x;
	private double y;
	private boolean aJour = true;

	public Objectif(Node view, double x, double y) {
		super(view);
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public void setX(double x1) {
		x = x1;
	}

	public void setY(double y1) {
		y = y1;
	}

	public void majPos(double limiteX, double limiteY) {
		this.view.setTranslateX(x + limiteX); // a modif
		this.view.setTranslateY(y + limiteY);
	}

	public boolean getAJour() {
		return aJour;
	}

	public boolean setAJour(boolean etat) {
		return aJour = etat;
	}
}
