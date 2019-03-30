package gameobjects;


public class Vaisseau {
	
	private String name;
    private double posX;
    private double posY;
    private double direction;
    private double vX;
    private double vY;
    private int score;
    
    public Vaisseau(String n, double px, double py, double dir, double vX, double vY) {
    	this.name = n;
    	this.posX = px;
    	this.posY = py;
    	this.direction = dir;
    	this.vX = vX;
    	this.vY = vY;
    	this.score =0;
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
	
}
