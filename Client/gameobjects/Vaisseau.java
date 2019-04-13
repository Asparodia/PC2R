package gameobjects;

import ihmexemple.GameObject;
import javafx.geometry.Point2D;
import javafx.scene.Node;

public class Vaisseau extends GameObject {

	private String name;
	private double posX;
	private double posY;
	private double direction;
	private double vX;
	private double vY;
	private int score;
	private boolean dejaAjoute = false;

	private double maxWidth;
	private double maxHeight;

	private final static double CAPVITESSE = 3.0;
	private final static double THRUST = 1;
	private final static double CLOCK = 6.0;
	private final static double ANTICLOCK = -6.0;
	private double angleAenvoyer = 0.0;
	private double thrustAEnvoyer = 0.0;

	public Vaisseau(Node view, String n, String px, String py) {
		super(view);
		this.name = n;
		this.posX = new Double(px.substring(1, px.length() - 1));
		this.posY = new Double(py.substring(1, py.length() - 1));
		this.direction = Math.toRadians(90.0);
		this.vX = 0.0;
		this.vY = 0.0;
		this.score = 0;
	}

	public Vaisseau(Node view, String n, double px, double py) {
		super(view);
		this.name = n;
		this.posX = px;
		this.posY = py;
		this.direction = Math.toRadians(90.0);
		this.vX = 0.0;
		this.vY = 0.0;
		this.score = 0;
	}

	@Override
	public void update() {
		// if (boom) {
		if (view != null) {
			posX += maxWidth / 2;
			posX = (velocity.getX() + posX + maxWidth) % (maxWidth);
			posX -= maxWidth / 2;
			posY += maxHeight / 2;
			posY = (velocity.getY() + posY + maxHeight) % (maxHeight);
			posY -= maxHeight / 2;
			view.setTranslateX((view.getTranslateX() + velocity.getX() + maxWidth) % maxWidth);
			view.setTranslateY((view.getTranslateY() + velocity.getY() + maxHeight) % maxHeight);
		}
	}

	public void rotateRight() {
		view.setRotate(view.getRotate() + CLOCK);
		angleAenvoyer += Math.toRadians(CLOCK);
	}

	public void rotateLeft() {
		view.setRotate(view.getRotate() + ANTICLOCK);
		angleAenvoyer += Math.toRadians(ANTICLOCK);
	}

	public void move() {
		thrustAEnvoyer = thrustAEnvoyer + 1;
		double x = velocity.getX() + THRUST * Math.cos(Math.toRadians(getRotate()));
		double y = velocity.getY() + THRUST * Math.sin(Math.toRadians(getRotate()));
		if (x > 0) {
			if (y > 0) {
				setVelocity(new Point2D(Math.min(x, CAPVITESSE), Math.min(y, CAPVITESSE)));
			} else {
				setVelocity(new Point2D(Math.min(x, CAPVITESSE), Math.max(y, -CAPVITESSE)));
			}
		} else {
			if (y > 0) {
				setVelocity(new Point2D(Math.max(x, -CAPVITESSE), Math.min(y, CAPVITESSE)));
			} else {
				setVelocity(new Point2D(Math.max(x, -CAPVITESSE), Math.max(y, -CAPVITESSE)));
			}
		}
	}

	public double getAngleAEnvoyer() {
		return angleAenvoyer;
	}

	public double getThrustAEnvoyer() {
		return thrustAEnvoyer;
	}

	public void moveAutreJoueur() {
		double x = vX;
		double y = vY;
		if (x > 0) {
			if (y > 0) {
				setVelocity(new Point2D(Math.min(x, CAPVITESSE), Math.min(y, CAPVITESSE)));
			} else {
				setVelocity(new Point2D(Math.min(x, CAPVITESSE), Math.max(y, -CAPVITESSE)));
			}
		} else {
			if (y > 0) {
				setVelocity(new Point2D(Math.max(x, -CAPVITESSE), Math.min(y, CAPVITESSE)));
			} else {
				setVelocity(new Point2D(Math.max(x, -CAPVITESSE), Math.max(y, -CAPVITESSE)));
			}
		}
	}

	public void MaJPos(double px, double py, double vx, double vy, double d) {
		setPosX(px);
		setPosY(py);
		setvX(vx);
		setvY(vy);
		setDirection(d);

	}

	public void resetValeurs() {
		angleAenvoyer = 0.0;
		thrustAEnvoyer = 0.0;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPosX() {
		return posX;
	}

	public void setPosX(double posX) {
		this.posX = posX;
	}

	public double getPosY() {
		return posY;
	}

	public void setPosY(double posY) {
		this.posY = posY;
	}

	public double getDirection() {
		return direction;
	}

	public void setDirection(double direction) {
		this.direction = direction;
	}

	public double getvX() {
		return vX;
	}

	public void setvX(double vX) {
		this.vX = vX;
	}

	public double getvY() {
		return vY;
	}

	public void setvY(double vY) {
		this.vY = vY;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void setNode(Node view) {
		this.view = view;
	}

	@Override
	public String toString() {
		return "Nom = " + name + "/" + " PosX : " + posX + "/PosY : " + posY + "/Direction : " + direction + "/vX : "
				+ vX + "/vY : " + vY + "/Score : " + score;
	}

	public void setLimits(double maxWidth, double maxHeight) {
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
	}

	public boolean getDejaAjoute() {
		return dejaAjoute;
	}

	public void setDejaAjoute(boolean etet) {
		this.dejaAjoute = etet;
	}

	public boolean hasNode() {
		return view != null;
	}
}
