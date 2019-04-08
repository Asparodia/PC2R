package ihmexemple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import client.Envoi;
import client.Reception;
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
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class IHMclient extends Application {
	protected final static int PORT = 2018;
	protected final static String HOST = "ppti-14-406-01";
	HashMap<String, Vaisseau> vehicules = new HashMap<>();
	private Envoi e;
	private Reception r;
	PrintStream outChan;
	private Pane root;
	private Ship player;
	Stage primaryStage;
	ReentrantLock verrou = new ReentrantLock();
	public Condition changeLaScene = verrou.newCondition();
	private Object listener = new Object();
	String name = "";
	public final static int LARGEUR = 800;
	public final static int HAUTEUR = 800;

	public IHMclient() {
		Socket s = null;
		try {
			System.out.println(HOST);
			s = new Socket(HOST, PORT);

			BufferedReader inChan = new BufferedReader(new InputStreamReader(s.getInputStream()));
			outChan = new PrintStream(s.getOutputStream());

			Reception r = new Reception(inChan, vehicules, listener);
			Envoi e = new Envoi(outChan, vehicules);

			r.start();
			e.start();

		} catch (IOException e) {
			System.err.println(e);

		}

	}

	@Override
	public void start(Stage stage) throws Exception {
		primaryStage = stage;
		primaryStage.setTitle("Astroid");
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
		Scene connexion = new Scene(bp);
		primaryStage.setScene(connexion);
		primaryStage.setResizable(false);
		btnLogin.setOnAction((event) -> {
			outChan.print("CONNECT/" + txtUserName.getText());
			outChan.flush();
			name = txtUserName.getText();
			try {
				synchronized (listener) {
					System.out.println("QFQSF");
					listener.wait();
					System.out.println("qshhgf");
					changeScene();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
		primaryStage.show();

	}

	public void changeScene() {
		primaryStage.setScene(new Scene(createContent()));

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

	}

	private void addGameObject(GameObject object, double x, double y) {
		object.getView().setTranslateX(x);
		object.getView().setTranslateY(y);
		root.getChildren().add(object.getView());
	}

	private void onUpdate() {
		player.update();
	}

	private Parent createContent() {
		root = new Pane();

		root.setPrefSize(LARGEUR, HAUTEUR);
		Polygon p;
		Ship vaisseauAAjouter;
		Iterator<Entry<String, Vaisseau>> iterateur = vehicules.entrySet().iterator();
		while (iterateur.hasNext()) {
			Entry<String, Vaisseau> courant = iterateur.next();
			if (courant.getKey().equals(name)) {
				p = new Polygon(0.0, 20.0, 40.0, 10.0, 0.0, 0.0);
				p.setRotate(-90);
				player = new Ship(p, root.getPrefWidth(), root.getPrefHeight());
				player.setVelocity(new Point2D(0, 0));
				addGameObject(player, courant.getValue().getPosX() + LARGEUR / 2,
						courant.getValue().getPosY() + HAUTEUR / 2);
			} else {
				p = new Polygon(0.0, 20.0, 40.0, 10.0, 0.0, 0.0);
				p.setRotate(-90);
				vaisseauAAjouter = new Ship(p, root.getPrefWidth(), root.getPrefHeight());
				vaisseauAAjouter.setVelocity(new Point2D(0, 0));
				addGameObject(vaisseauAAjouter, courant.getValue().getPosX(), courant.getValue().getPosY());
			}
		}
		// p = new Polygon(0.0, 20.0, 40.0, 10.0, 0.0, 0.0);
		// p.setRotate(-90);
		// player = new Ship(p, root.getPrefWidth(), root.getPrefHeight());
		// player.setVelocity(new Point2D(0, 0));
		// addGameObject(player, 0, 0);

		AnimationTimer timer = new AnimationTimer() {
			@Override
			public void handle(long now) {

				onUpdate();
			}
		};
		timer.start();

		return root;
	}

	public static void main(String[] args) {
		launch();
	}

	public void setChangeListener(ChangeListener listener) {
		this.listener = listener;
	}
}
