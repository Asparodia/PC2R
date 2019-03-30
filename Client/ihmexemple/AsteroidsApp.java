package ihmexemple;


import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

//import java.util.ArrayList;
//import java.util.List;

public class AsteroidsApp extends Application {

	private Pane root;

	// private List<GameObject> bullets = new ArrayList<>();
	// private List<GameObject> enemies = new ArrayList<>();

	private Ship player;
	private int refreshTickRate = 10;
	private int nextDecelerate = 0;

	private Parent createContent() {
		root = new Pane();
		root.setPrefSize(600, 600);

		player = new Ship(new Polygon(0.0, 20.0, 40.0, 10.0, 0.0, 0.0), root.getPrefWidth(), root.getPrefHeight());
		player.setVelocity(new Point2D(0, 0));
		addGameObject(player, 300, 300);

		AnimationTimer timer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				if (nextDecelerate == 0) {
					player.decelerate();
					nextDecelerate = refreshTickRate;
				}
				nextDecelerate--;
				onUpdate();
			}
		};
		timer.start();

		return root;
	}

	// private void addBullet(GameObject bullet, double x, double y) {
	// bullets.add(bullet);
	// addGameObject(bullet, x, y);
	// }
	//
	// private void addEnemy(GameObject enemy, double x, double y) {
	// enemies.add(enemy);
	// addGameObject(enemy, x, y);
	// }

	private void addGameObject(GameObject object, double x, double y) {
		object.getView().setTranslateX(x);
		object.getView().setTranslateY(y);
		root.getChildren().add(object.getView());
	}

	private void onUpdate() {
		// for (GameObject bullet : bullets) {
		// for (GameObject enemy : enemies) {
		// if (bullet.isColliding(enemy)) {
		// bullet.setAlive(false);
		// enemy.setAlive(false);
		//
		// root.getChildren().removeAll(bullet.getView(), enemy.getView());
		// }
		// }
		// }
		//
		// bullets.removeIf(GameObject::isDead);
		// enemies.removeIf(GameObject::isDead);
		//
		// bullets.forEach(GameObject::update);
		// enemies.forEach(GameObject::update);

		player.update();

		// if (Math.random() < 0.02) {
		// addEnemy(new Enemy(), Math.random() * root.getPrefWidth(), Math.random() *
		// root.getPrefHeight());
		// }
	}

	// private static class Enemy extends GameObject {
	// Enemy() {
	// super(new Circle(15, 15, 15, Color.RED));
	// }
	// }
	//
	// private static class Bullet extends GameObject {
	// Bullet() {
	// super(new Circle(5, 5, 5, Color.BROWN));
	// }
	// }

	@Override
	public void start(Stage stage) throws Exception {
		stage.setScene(new Scene(createContent()));

		stage.getScene().setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.LEFT) {
				player.rotateLeft();
				// player.move();
			} else if (e.getCode() == KeyCode.RIGHT) {
				player.rotateRight();
				// player.move();
			} else if (e.getCode() == KeyCode.UP) {
				player.impulse();
				player.move();
				// Bullet bullet = new Bullet();
				// bullet.setVelocity(player.getVelocity().normalize().multiply(5));
				// addBullet(bullet, player.getView().getTranslateX(),
				// player.getView().getTranslateY());
			}
		});

		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}