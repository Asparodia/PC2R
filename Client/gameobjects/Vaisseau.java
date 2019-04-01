package gameobjects;

public class Vaisseau {

	private String name;
	private double posX;
	private double posY;
	private double direction;
	private double vX;
	private double vY;
	private int score;

	public Vaisseau(String n, String px, String py) {
		this.name = n;
		this.posX = px.codePointCount(1, px.length() - 1);
		this.posY = py.codePointCount(1, py.length() - 1);
		this.direction = 90.0;
		this.vX = 0.0;
		this.vY = 0.0;
		this.score = 0;
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

	@Override
	public String toString() {
		return "Nom = " + name + "/PosX = " + posX + "/PosY = " + posY + "/Direction = " + direction + "/vX = " + vX
				+ "/vY = " + vY + "/Score = " + score;
	}

}
