package gameobjects;

import javafx.geometry.Point2D;
import javafx.scene.Node;

public class GameObject {

	protected Node view;
	protected Point2D velocity = new Point2D(0, 0);
	protected double posX;
	protected double posY;
	protected boolean aJour = true;

	protected boolean aEnlever = true;

	public GameObject(Node view) {
		this.view = view;
	}

	public void update() {
		view.setTranslateX(view.getTranslateX() + velocity.getX());
		view.setTranslateY(view.getTranslateY() + velocity.getY());
	}

	public void setVelocity(Point2D velocity) {
		this.velocity = velocity;
	}

	public Point2D getVelocity() {
		return velocity;
	}

	public Node getView() {
		return view;
	}

	public boolean getAEnlever() {
		return aEnlever;
	}

	public void setAEnlever(boolean etat) {
		this.aEnlever = etat;
	}

	public double getRotate() {
		return view.getRotate();
	}

	public void rotateRight() {
		view.setRotate(view.getRotate() + 5);
		setVelocity(new Point2D(Math.cos(Math.toRadians(getRotate())),
				Math.sin(Math.toRadians(getRotate()))));
	}

	public void rotateLeft() {
		view.setRotate(view.getRotate() - 5);
		setVelocity(new Point2D(Math.cos(Math.toRadians(getRotate())),
				Math.sin(Math.toRadians(getRotate()))));
	}

	public boolean isColliding(GameObject other) {
		return getView().getBoundsInParent().intersects(
				other.getView().getBoundsInParent());
	}

	public double getPosX() {
		return posX;
	}

	public void setPosX(double posX) {

		this.posX = posX;
		// System.out.println("APRES : " + this.posX);
	}

	public double getPosY() {
		return posY;
	}

	public void setPosY(double posY) {
		this.posY = posY;
	}
	
	public boolean getAJour() {
		return aJour;
	}

	public boolean setAJour(boolean etat) {
		return aJour = etat;
	}

}