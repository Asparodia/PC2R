package gameobjects;

import javafx.scene.Node;

// ELLE SERT A RIEN CETTE CLASSE ?
public class Objectif extends GameObject {

	public Objectif(Node view, double x, double y) {
		super(view);
		this.posX = x;
		this.posY = y;
	}

	// public void majPos(double limiteX, double limiteY) {
	// System.out.println(x);
	// System.out.println(y);
	// this.view.setTranslateX(x + limiteX); // a modif
	// this.view.setTranslateY(y + limiteY);
	// }
}
