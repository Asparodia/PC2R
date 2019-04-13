package ihmexemple;

import ihmexemple.GameObject;
import javafx.geometry.Point2D;
import javafx.scene.Node;

public class Ship extends GameObject {
	private double maxWidth;
	private double maxHeight;
	// private boolean boom = false;
	private final static double CAPVITESSE = 6.0;
	private double angleAenvoyer = 0.0;
	private double thrustAEnvoyer = 0.0;

	public Ship(Node view, double d, double e) {
		super(view);
		this.maxWidth = d;
		this.maxHeight = e;
	}

	@Override
	public void update() {
		// if (boom) {
		view.setTranslateX((view.getTranslateX() + velocity.getX() + maxWidth) % maxWidth);
		view.setTranslateY((view.getTranslateY() + velocity.getY() + maxHeight) % maxHeight);
	}

	public void rotateRight() {
		view.setRotate(view.getRotate() + 6);
		angleAenvoyer += Math.toRadians(6);
	}

	public void rotateLeft() {
		view.setRotate(view.getRotate() - 6);
		angleAenvoyer += Math.toRadians(-6);
	}

	public void move() {
		thrustAEnvoyer += 1;
		double x = (velocity.getX() >= CAPVITESSE) ? CAPVITESSE
				: (velocity.getX() <= -CAPVITESSE) ? -CAPVITESSE : velocity.getX();
		double y = (velocity.getY() >= CAPVITESSE) ? CAPVITESSE
				: (velocity.getY() <= -CAPVITESSE) ? -CAPVITESSE : velocity.getY();

		setVelocity(new Point2D(x + Math.cos(Math.toRadians(getRotate())), y + Math.sin(Math.toRadians(getRotate()))));
		// System.out.println("Cos : " + Math.cos(Math.toRadians(getRotate())));
		// System.out.println("VelocityX : " + velocity.getX());
		// System.out.println("Sin : " + Math.sin(Math.toRadians(getRotate())));
		// System.out.println("VelocityY : " + velocity.getY());
	}

	public double getPosX() {
		return view.getTranslateX();
	}

	public double getPosY() {
		return view.getTranslateY();
	}

	public double getAngleAEnvoyer() {
		return angleAenvoyer;
	}

	public double getThrustAEnvoyer() {
		return thrustAEnvoyer;
	}

	public void resetValeurs() {
		angleAenvoyer = 0.0;
		thrustAEnvoyer = 0.0;
	}

	// public void impulse() {
	// boom = true;
	// }

	// public void decelerate() {
	// view.setTranslateX((view.getTranslateX() + velocity.getX() + maxWidth - 0.5)
	// % maxWidth);
	// view.setTranslateY((view.getTranslateY() + velocity.getY() + maxHeight - 0.5)
	// % maxHeight);
	// System.out.println(velocity.getX());
	// }
}
