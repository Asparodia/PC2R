package ihmexemple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import client.Envoi;
import client.Reception;
import gameobjects.GameObject;
import gameobjects.Objectif;
import gameobjects.Vaisseau;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class IHMclient extends Application {

	// INFO A CONFIG POUR SE CO
	protected final static int PORT = 2018;
	protected final static String HOST = "ppti";

	public final static int LARGEUR = 500;
	public final static int HAUTEUR = 500;
	private GridPane root;
	Stage primaryStage;
	// private int refreshTickRate = 3;
	int tick = 0;

	private Objectif objectif;
	private Vaisseau player;
	String name = "";
	HashMap<String, Vaisseau> vehicules = new HashMap<>();
	private static Timer timer = new Timer();

	private Envoi e;
	private Object listener = new Object();
	private Random random = new Random();

	public IHMclient() {

		Socket s = null;
		try {
			s = new Socket(HOST, PORT);
			System.out.println("host : " + HOST + " port : " + PORT);

			BufferedReader inChan = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
			PrintStream outChan = new PrintStream(s.getOutputStream());

			Circle cercle = new Circle(20);
			cercle.setFill(Color.LAWNGREEN);
			objectif = new Objectif(cercle, 0, 0);

			Reception r = new Reception(inChan, vehicules, listener, objectif);
			e = new Envoi(outChan, vehicules);

			r.start();
		} catch (IOException e) {
			System.err.println(e);
		}

	}

	@Override
	public void start(Stage stage) throws Exception {
		primaryStage = stage;
		primaryStage.setTitle("Astroid");
		changeScene(Ecran.ACCUEIL);
		primaryStage.show();
	}

	public void changeScene(Ecran ecran) {
		switch (ecran) {
		case JEU:
			primaryStage.setScene(new Scene(jeu()));
			primaryStage.getScene().setOnKeyPressed(e -> {
				if (e.getCode() == KeyCode.LEFT) {
					player.rotateLeft();
				}
				if (e.getCode() == KeyCode.RIGHT) {
					player.rotateRight();
				}
				if (e.getCode() == KeyCode.UP) {
					player.move();

				}
			});
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					rafraichir();
				}
			}, 0, 100);
			break;

		case ACCUEIL:
			primaryStage.setScene(new Scene(connexion()));
			primaryStage.setResizable(false);
			break;
		case ATTENTE:

			break;
		}
	}

	private void addGameObject(GameObject object, double x, double y) {
		object.getView().setTranslateX(x);
		object.getView().setTranslateY(y);
		root.getChildren().add(object.getView());
	}

	private void removeGameObject(GameObject object) {
		root.getChildren().remove(object.getView());
	}

	private void onUpdate() {
		// root.getChildren().clear();
		// for (Vaisseau v :vehicules.values()) {
		// addGameObject(v, v.getPosX() + root.getPrefWidth() / 2,
		// v.getPosY() + root.getPrefHeight() / 2);
		// }
		// System.out.println("JIUER : " + player.getPosX());
		// System.out.println("JIUER : " + player.getPosY());
		// System.out.println("OBJ " + objectif.getX());
		// System.out.println("OBJ " + objectif.getY());
		// tick++;
		// if (tick % refreshTickRate == 0) {

		// tick = 0;
		// }
		// synchronized (nouveauVehicule) {
		// if (nouveauVehicule) {
		// synchronized (vehicules) {
		// Iterator<Entry<String, Vaisseau>> iterateur =
		// vehicules.entrySet().iterator();
		// while (iterateur.hasNext()) {
		// Vaisseau v = iterateur.next().getValue();
		// if (!v.getDejaAjoute()) {
		// Polygon p = new Polygon(0.0, 20.0, 40.0, 10.0, 0.0, 0.0);
		// p.setRotate(-90);
		// p.setFill(Color.color(random.nextDouble(), random.nextDouble(),
		// random.nextDouble()));
		// v.setNode(p);
		// v.setLimits(root.getPrefWidth(), root.getPrefHeight());
		// v.setVelocity(new Point2D(0, 0));
		// v.setDejaAjoute(true);
		// addGameObject(v, v.getPosX() + root.getPrefWidth() / 2,
		// v.getPosY() + root.getPrefHeight() / 2);
		// }
		// }
		// }
		// }
		// nouveauVehicule = false;
		// }
		synchronized (player) {
			if (player.partieTerminee()) {
				player.setFinJeu(false);

			}
		}
		synchronized (vehicules) {
			Iterator<Entry<String, Vaisseau>> iterateur = vehicules.entrySet()
					.iterator();
			while (iterateur.hasNext()) {
				Entry<String, Vaisseau> courant = iterateur.next();
				Vaisseau v = courant.getValue();
				if (!(v.getName().equals(player.getName()))) {
					// v = courant.getValue();
					// System.out.println("AVANT X : " +
					// v.getView().getTranslateX());
					// System.out.println("AVANT Y : " +
					// v.getView().getTranslateY());
					if (!v.hasNode() && v.getPosX() != Double.MAX_VALUE) {
						Polygon p = new Polygon(0.0, 20.0, 40.0, 10.0, 0.0, 0.0);
						p.setRotate(-90);
						p.setFill(Color.color(random.nextDouble(),
								random.nextDouble(), random.nextDouble()));
						v = courant.getValue();
						v.setNode(p);
						v.setLimits(root.getPrefWidth(), root.getPrefHeight());
						v.setVelocity(new Point2D(0, 0));
						v.setDejaAjoute(true);
						addGameObject(
								v,
								courant.getValue().getPosX()
										+ root.getPrefWidth() / 2,
								courant.getValue().getPosY()
										+ root.getPrefHeight() / 2);
					} else if (v.getAEnlever()) {
						removeGameObject(v);
						vehicules.remove(v);
					}
					v.moveAutreJoueur();
				}
				v.update();
				// System.out.println("APRES X : " +
				// v.getView().getTranslateX());
				// System.out.println("APRES Y : " +
				// v.getView().getTranslateY());
			}
		}

		synchronized (objectif) {
			if (!objectif.getAJour()) {
				System.out.println("NEW X : " + objectif.getPosX());
				System.out.println("NEW Y : " + objectif.getPosY());
				removeGameObject(objectif);
				objectif.update();
				addGameObject(objectif,
						objectif.getPosX() + root.getPrefWidth() / 2,
						objectif.getPosY() + root.getPrefHeight() / 2);
				objectif.setAJour(true);
			}
		}
	}

	public void rafraichir() {
		double x = player.getPosX();
		double y = player.getPosY();
		double angle = player.getAngleAEnvoyer();
		double thrust = player.getThrustAEnvoyer();
		if (e != null) {
			e.newCom(angle, thrust);
			e.newPos(x, y);
			player.resetValeurs();
		} else {
			System.out.println("NO ENTRA AQUI");
		}
	}

	private Parent jeu() {

		root = new GridPane();
		root.setPrefSize(LARGEUR, HAUTEUR);
		Button btnExit = new Button("Exit");
		Label lblTexte = new Label("Chat");
		TextField texte = new TextField();
		Button btnEnvoyer = new Button("Envoyer");
		root.add(btnExit, 0, 0);
		root.add(lblTexte, LARGEUR, HAUTEUR);
		root.add(texte, 0, HAUTEUR);
		root.add(btnEnvoyer, LARGEUR, 0);
		btnEnvoyer.setOnAction((event) -> {
			e.envoi(texte.getText());
			texte.clear();

		});
		

		btnExit.setOnAction((event) -> {
			e.exit(name);
			changeScene(Ecran.ACCUEIL);
		});
		synchronized (objectif) {
			addGameObject(objectif, objectif.getPosX() + root.getPrefWidth()
					/ 2, objectif.getPosY() + root.getPrefHeight() / 2);
//			System.out.println("NEW X : " + objectif.getPosX());
//			System.out.println("NEW Y : " + objectif.getPosY());
		}
		Polygon p;
		Vaisseau vaisseauAAjouter;
		synchronized (vehicules) {
			Iterator<Entry<String, Vaisseau>> iterateur = vehicules.entrySet()
					.iterator();
			while (iterateur.hasNext()) {
				Entry<String, Vaisseau> courant = iterateur.next();

				if (courant.getKey().equals(name)) {
					System.out.println("PLAYER : " + name);
					p = new Polygon(0.0, 20.0, 40.0, 10.0, 0.0, 0.0);
					p.setRotate(-90);
					p.setFill(Color.BLACK);
					player = courant.getValue();
					player.setNode(p);
					player.setLimits(root.getPrefWidth(), root.getPrefHeight());
					player.setVelocity(new Point2D(0, 0));
					player.setDejaAjoute(true);
					addGameObject(player,
							courant.getValue().getPosX() + root.getPrefWidth()
									/ 2,
							courant.getValue().getPosY() + root.getPrefHeight()
									/ 2);

				} else {
					System.out.println("VAISSEAU : " + courant.getKey());
					p = new Polygon(0.0, 20.0, 40.0, 10.0, 0.0, 0.0);
					p.setRotate(-90);
					p.setFill(Color.color(random.nextDouble(),
							random.nextDouble(), random.nextDouble()));
					vaisseauAAjouter = courant.getValue();
					vaisseauAAjouter.setNode(p);
					vaisseauAAjouter.setLimits(root.getPrefWidth(),
							root.getPrefHeight());
					vaisseauAAjouter.setVelocity(new Point2D(0, 0));
					vaisseauAAjouter.setDejaAjoute(true);
					addGameObject(vaisseauAAjouter, courant.getValue()
							.getPosX() + root.getPrefWidth() / 2, courant
							.getValue().getPosY() + root.getPrefHeight() / 2);
				}
			}
		}
		AnimationTimer timer = new AnimationTimer() {
			@Override
			public void handle(long now) {
				onUpdate();
			}
		};
		timer.start();

		return root;
	}

	private Parent connexion() {
		BorderPane bp = new BorderPane();
		bp.setPadding(new Insets(10, 50, 50, 50));
		// Adding HBox

		HBox hb = new HBox();
		hb.setPadding(new Insets(20, 20, 20, 30));

		// Adding GridPane
		GridPane gridPane = new GridPane();
		gridPane.setPadding(new Insets(20, 20, 20, 20));
		gridPane.setHgap(5);
		gridPane.setVgap(5);

		// Implementing Nodes for GridPane
		Label lblUserName = new Label("Username");
		TextField txtUserName = new TextField();
		Button btnLogin = new Button("Login");

		Label lblMessage = new Label();
		lblMessage.setTextFill(Color.RED);
		gridPane.add(lblUserName, 0, 0);
		gridPane.add(txtUserName, 1, 0);
		gridPane.add(btnLogin, 1, 1);
		gridPane.add(lblMessage, 1, 2);
		Text text = new Text("Astroid connection");
		text.setFont(Font.font("Times New Roman", 30));
		hb.getChildren().add(text);
		bp.setTop(hb);
		bp.setCenter(gridPane);
		btnLogin.setOnAction((event) -> {
			e.connexion(txtUserName.getText());
			name = txtUserName.getText();
			try {
				synchronized (listener) {
					listener.wait();
					changeScene(Ecran.JEU);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		});
		return bp;
	}

	public static void main(String[] args) {
		launch();
	}
}
