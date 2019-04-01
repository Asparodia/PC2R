package ihmexemple;

import ihmexemple.GameObject;
import javafx.geometry.Point2D;
import javafx.scene.Node;

public class Ship extends GameObject {
	private double maxWidth;
	private double maxHeight;
	private boolean boom = false;

	public Ship(Node view, double d, double e) {
		super(view);
		this.maxWidth = d;
		this.maxHeight = e;
	}

	@Override
	public void update() {
		if (boom) {
			view.setTranslateX((view.getTranslateX() + (5 * velocity.getX())% 10 + maxWidth) % maxWidth);
			view.setTranslateY((view.getTranslateY() + (5 * velocity.getY())%10 + maxHeight) % maxHeight);
		} else {
			view.setTranslateX((view.getTranslateX() + velocity.getX() + maxWidth) % maxWidth);
			view.setTranslateY((view.getTranslateY() + velocity.getY() + maxHeight) % maxHeight);
		}
	}

	public void rotateRight() {
		view.setRotate(view.getRotate() + 5);
	}

	public void rotateLeft() {
		view.setRotate(view.getRotate() - 5);
	}

	public void move() {
		System.out.println("Truc : " + Math.cos(Math.toRadians(getRotate())));
		System.out.println("X : " + velocity.getX());
		setVelocity(new Point2D((velocity.getX() - Math.cos(Math.toRadians(getRotate()))),
				(velocity.getY() - Math.sin(Math.toRadians(getRotate())))));
	}

	public void impulse() {
		boom = true;
	}

	// public void decelerate() {
	// view.setTranslateX((view.getTranslateX() + velocity.getX() + maxWidth - 0.5)
	// % maxWidth);
	// view.setTranslateY((view.getTranslateY() + velocity.getY() + maxHeight - 0.5)
	// % maxHeight);
	// System.out.println(velocity.getX());
	// }
}
