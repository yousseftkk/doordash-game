package game.engine.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import game.engine.Board;
import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.cards.Card;
import game.engine.cells.CardCell;
import game.engine.cells.Cell;
import game.engine.cells.ContaminationSock;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.DoorCell;
import game.engine.cells.MonsterCell;
import game.engine.dataloader.DataLoader;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.Dasher;
import game.engine.monsters.Monster;
import game.engine.monsters.MultiTasker;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.application.Platform;

public class GameGUI extends Application {

	private MediaPlayer mediaPlayer;
	private AudioClip hoverSoundEffect, clickSoundEffect;
	private StackPane playRoot, menuRoot;
	private GridPane currentGrid;
	private Game game;
	private int insPage = 1;
	private Role role;
	private Stage primaryStage;
	private Scene menuScene, playScene;
	private VBox playerBox, oppBox;
	private StackPane playerPanel, oppPanel;
	private ArrayList<Label> cellEnergyLabels;
	private ArrayList<StackPane> cellStack;
	private final int tileSize = 68;
	private Button endTurnBtn;
	private Label playerEnergy, playerCurrentRole, playerPosition,
			playerStatus, oppEnergy, oppCurrentRole, oppPosition, oppStatus;
	private ImageView playerOverlayView, opponentOverlayView;
	private Pane overlayPane;
	private StackPane diceOn;
	private Button rollBtn;
	private int previousPlayerPos = 0;
	private int previousOpponentPos = 0;
	private Label playerTagLabel;
	private Label opponentTagLabel;
	private Font font = Font.loadFont(
			getClass().getResourceAsStream("/AutumnVoyage-Regular.ttf"), 20);
	private Font minifont = Font.loadFont(
			getClass().getResourceAsStream("/AutumnVoyage-Regular.ttf"), 14);
	private Label cardsRemainingLabel;
	private ImageView cardsImageView;
	private boolean firstMove = true;
	private Image monsterCellImg;
	private Image conveyorCellImg;
	private Image sockCellImg;
	private Image cardBgCellImg;
	private Image normalCellImg;
	private Image cardCellImg;
	
	
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		this.primaryStage = stage;
		primaryStage.setTitle("DOORDASH");

		Media bgMusic = new Media(getClass().getResource(
				"/sounds/background_music.mp3").toExternalForm());
		mediaPlayer = new MediaPlayer(bgMusic);
		mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
		//mediaPlayer.setMute(true); // delete
		mediaPlayer.play();
		mediaPlayer.setVolume(0.3);
		clickSoundEffect = new AudioClip(getClass().getResource(
				"/sounds/clickedSound.mp3").toExternalForm());

		Image start_screen = new Image(getClass().getResourceAsStream(
				"/images/startScreen.png"));
		Image playHovered = new Image(getClass().getResourceAsStream(
				"/images/playHovered.png"));
		Image optionsHovered = new Image(getClass().getResourceAsStream(
				"/images/optionsHovered.png"));
		Image quitHovered = new Image(getClass().getResourceAsStream(
				"/images/quitHovered.png"));
		monsterCellImg = new Image(getClass().getResourceAsStream("/images/cells/monster.png"));
		conveyorCellImg = new Image(getClass().getResourceAsStream("/images/cells/conveyor.png"));
		sockCellImg = new Image(getClass().getResourceAsStream("/images/cells/sock.png"));
		cardBgCellImg = new Image(getClass().getResourceAsStream("/images/cells/cardbg.png"));
		normalCellImg = new Image(getClass().getResourceAsStream("/images/cells/normal.png"));
		cardCellImg = new Image(getClass().getResourceAsStream("/images/cells/cardCell.png"));
		ImageView start_screenView = new ImageView(start_screen);
		ImageView playHoveredView = new ImageView(playHovered);
		ImageView optionsHoveredView = new ImageView(optionsHovered);
		ImageView quitHoveredView = new ImageView(quitHovered);

		playHoveredView.setVisible(false);
		playHoveredView.setTranslateX(-4);

		optionsHoveredView.setVisible(false);
		optionsHoveredView.setTranslateY(-13);
		optionsHoveredView.setTranslateX(-5);

		quitHoveredView.setVisible(false);
		quitHoveredView.setTranslateY(-19.5);
		quitHoveredView.setTranslateX(2);

		Button playHitbox = new Button("play");
		playHitbox.setOpacity(0);
		playHitbox.setMinSize(280, 240);
		playHitbox.setMaxSize(280, 240);

		Button optionsHitbox = new Button("options");
		optionsHitbox.setOpacity(0);
		optionsHitbox.setTranslateY(-13);
		optionsHitbox.setMinSize(150, 45);
		optionsHitbox.setMaxSize(150, 45);

		Button quitHitbox = new Button("quit");
		quitHitbox.setOpacity(0);
		quitHitbox.setTranslateY(-19);
		quitHitbox.setMinSize(145, 43);
		quitHitbox.setMaxSize(145, 43);

		StackPane playStack = new StackPane(playHoveredView, playHitbox);
		StackPane optionsStack = new StackPane(optionsHoveredView,
				optionsHitbox);
		StackPane quitStack = new StackPane(quitHoveredView, quitHitbox);

		VBox menu = new VBox(0, playStack, optionsStack, quitStack);
		menu.setAlignment(Pos.CENTER);
		menu.setPadding(new Insets(265, 0, 0, 0));

		playHoverEffect(playHitbox, playHoveredView);
		playHoverEffect(optionsHitbox, optionsHoveredView);
		playHoverEffect(quitHitbox, quitHoveredView);

		menuRoot = new StackPane();
		menuRoot.setStyle("-fx-background-color: black;");
		menuRoot.getChildren().addAll(start_screenView, menu);
		this.menuScene = new Scene(menuRoot, 1280, 720);

		// Button test = new Button();
		// test.setOnMouseClicked(e ->{
		// displayFrozen();
		// });

		// menuRoot.getChildren().add(test);

		playHitbox.setOnMouseClicked(e -> {
		    clickSoundEffect.play();
		    boolean wasFullScreen = primaryStage.isFullScreen();
		    
		    ImageView mainGUI = new ImageView(new Image(getClass()
		            .getResourceAsStream("/images/mainGUI.png")));

		    playRoot = new StackPane();
		    playRoot.setStyle("-fx-background-color: black;");
		    playRoot.getChildren().addAll(mainGUI);

		    rollBtn = new Button();
		    rollBtn.setPrefSize(102, 44);
		    rollBtn.setOpacity(0);
		    StackPane.setAlignment(rollBtn, Pos.CENTER_RIGHT);
		    StackPane.setMargin(rollBtn, new Insets(137, 92, 0, 0));
		    rollBtn.setVisible(false);

		    rollBtn.setOnMouseClicked(s -> {
		        try {
		            roll();
		        } catch (InvalidMoveException inv) {
		        }
		    });

		    playScene = new Scene(playRoot);

		    primaryStage.setScene(playScene);
		    
		    if (wasFullScreen) {
		        primaryStage.setFullScreen(true);
		    }
		    
		    primaryStage.setOnCloseRequest(f -> {
		        f.consume();
		        clickSoundEffect.play();
		        displayQuit(playRoot);
		    });

		    showInstructions();
		});
		optionsHitbox.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			displayOptions();
		});

		quitHitbox.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			displayQuit(menuRoot);
		});
		// primaryStage.setOnCloseRequest(e -> {
		// e.consume();
		// clickSoundEffect.play();
		// displayQuit();
		// });

		start_screenView.setPreserveRatio(false);
		start_screenView.fitWidthProperty().bind(menuScene.widthProperty());
		start_screenView.fitHeightProperty().bind(menuScene.heightProperty());

		double baseWidth = 1280;
		double baseHeight = 720;

		Runnable updateScale = () -> {
			double scaleX = menuScene.getWidth() / baseWidth;
			double scaleY = menuScene.getHeight() / baseHeight;
			double scale = Math.min(scaleX, scaleY);

			menu.setScaleX(scale);
			menu.setScaleY(scale);
		};

		menuScene.widthProperty().addListener((obs, o, n) -> updateScale.run());
		menuScene.heightProperty()
				.addListener((obs, o, n) -> updateScale.run());
		menuScene.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ESCAPE
					|| (e.getCode() == KeyCode.F11 && primaryStage
							.isFullScreen())) {
				primaryStage.setFullScreen(false);
				menuRoot.getTransforms().clear();
				menuRoot.setManaged(true);
			} else if (e.getCode() == KeyCode.F11
					&& !primaryStage.isFullScreen()) {
				primaryStage.setFullScreen(true);
			}
		});

		primaryStage.setResizable(false);
		primaryStage.setScene(menuScene);
		primaryStage.show();

	}
	public void playHoverEffect(Button hitbox, ImageView iv) {
		hoverSoundEffect = new AudioClip(getClass().getResource(
				"/sounds/hoverSound.mp3").toExternalForm());
		hitbox.setOnMouseEntered(e -> {
			iv.setVisible(true);
			hoverSoundEffect.play();
		});
		hitbox.setOnMouseExited(e -> {
			iv.setVisible(false);
		});

	}

	public void playHoverEffect(ImageView hitbox, ImageView iv) {
		hoverSoundEffect = new AudioClip(getClass().getResource(
				"/sounds/hoverSound.mp3").toExternalForm());

		iv.setMouseTransparent(true);

		hitbox.setOnMouseEntered(e -> {
			hitbox.setOpacity(0);
			iv.setOpacity(1);
			hoverSoundEffect.play();
		});
		hitbox.setOnMouseExited(e -> {
			hitbox.setOpacity(1);
			iv.setOpacity(0);
		});

	}

	public void slide(ImageView iv, double targetY) {
		TranslateTransition tt = new TranslateTransition(Duration.seconds(0.2),
				iv);
		tt.setToY(targetY);
		tt.play();
	}

	private String getTexture(int index) {
		if (contains(Constants.MONSTER_CELL_INDICES, index))
			return "/images/cells/monster.png";
		if (contains(Constants.CONVEYOR_CELL_INDICES, index))
			return "/images/cells/conveyor.png";
		if (contains(Constants.SOCK_CELL_INDICES, index))
			return "/images/cells/sock.png";
		if (contains(Constants.CARD_CELL_INDICES, index))
			return "/images/cells/cardbg.png";

		return "/images/cells/normal.png";
	}

	private boolean contains(int[] array, int target) {
		for (int val : array) {
			if (val == target)
				return true;
		}
		return false;
	}

	private Cell getCellAtBoardIndex(int index) {
		int cols = Constants.BOARD_COLS;
		int placeRow = index / cols;
		int placeCol = index % cols;
		if (placeRow % 2 == 1)
			placeCol = cols - 1 - placeCol;
		return game.getBoard().getBoardCells()[placeRow][placeCol];
	}

	public void returnToMenu() {
	    if (primaryStage != null && menuScene != null) {
	        boolean wasFullScreen = primaryStage.isFullScreen();
	        primaryStage.setScene(menuScene);
	        if (wasFullScreen) {
	            primaryStage.setFullScreen(true);
	        }
	    }
	    primaryStage.setOnCloseRequest(f -> {
	        f.consume();
	        clickSoundEffect.play();
	        displayQuit(menuRoot);
	    });
	}

	private void displayAlertReturn(String title, String message) {
		Stage alertStage = new Stage();
		alertStage.setTitle(title);

		Label label = new Label(message);
		Button closeButton = new Button("Back");
		closeButton.setOnAction(event -> {
			alertStage.close();
			returnToMenu();
		});

		BorderPane pane = new BorderPane();
		pane.setTop(label);
		pane.setCenter(closeButton);

		Scene scene = new Scene(pane, 500, 100);
		alertStage.setScene(scene);
		alertStage.show();
	}

	private void displayFrozen() {
		AudioClip freeze = new AudioClip(getClass().getResource(
				"/sounds/freeze.mp3").toExternalForm());
		freeze.setVolume(0.6);
		freeze.play();

		Font font = Font.loadFont(
				getClass().getResourceAsStream("/FROZBITE.ttf"), 40);
		Font fontMini = Font.loadFont(
				getClass().getResourceAsStream("/FROZBITE.ttf"), 28);

		Rectangle dim = new Rectangle(1280, 720);
		dim.setFill(Color.rgb(0, 0, 0, 0.7));
		dim.widthProperty().bind(playRoot.widthProperty());
		dim.heightProperty().bind(playRoot.heightProperty());

		Label frozen = new Label("FROZEN");
		Label mini = new Label("You will not be able to play this round");

		frozen.setFont(font);
		mini.setFont(fontMini);
		frozen.setTextFill(Color.AQUA);
		mini.setTextFill(Color.WHITE);

		String iceGlow = "-fx-effect: dropshadow(three-pass-box, deepskyblue, 10, 0.5, 0, 0);";
		frozen.setStyle(iceGlow);
		mini.setStyle("-fx-effect: dropshadow(gaussian, black, 2, 1.0, 0, 0);");

		VBox textContainer = new VBox(10, frozen, mini);
		textContainer.setAlignment(Pos.CENTER);

		StackPane layoutLayer = new StackPane(dim, textContainer);
		layoutLayer.setAlignment(Pos.CENTER);

		playRoot.getChildren().add(layoutLayer);

		PauseTransition delay = new PauseTransition(Duration.seconds(3));
		delay.setOnFinished(event -> playRoot.getChildren().remove(layoutLayer));
		delay.play();
	}

	private void displayPowerUp(Monster m, String title, int oldEnergy,
			int newEnergy) {
		Font fontTitle = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 24);
		Font fontMin = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 18);

		String msg;

		if (m.getClass().getSimpleName().equals("Dynamo")) {
			msg = m.getName() + " froze their opponent for one round";
		} else if (m.getClass().getSimpleName().equals("MultiTasker")) {
			msg = m.getName()
					+ " activated Focus Mode! Normal speed for 2 turns!";
		} else if (m.getClass().getSimpleName().equals("Schemer")) {
			msg = m.getName() + " stole from -> ";
			for (Monster t : Board.getStationedMonsters()) {
				msg += "\n" + t.getName();
			}
			msg += "\nTotal Stolen = " + (newEnergy - oldEnergy);
		} else {
			msg = m.getName()
					+ " activated Momentum Rush! 3x speed for 3 turns!";
		}

		String stroke = "-fx-effect: dropshadow(gaussian, black, 2, 1.0, 0, 0);";
		Label top = new Label(title);
		Label message = new Label(msg);

		top.setTextFill(Color.WHITE);
		message.setTextFill(Color.WHITE);
		top.setStyle(stroke);
		message.setStyle(stroke);

		top.setFont(fontTitle);
		message.setFont(fontMin);
		message.setWrapText(true);
		message.setMaxWidth(460);
		message.setAlignment(Pos.CENTER);
		message.setTextAlignment(TextAlignment.CENTER);

		VBox textHolder = new VBox(10);
		textHolder.setAlignment(Pos.CENTER);
		textHolder.getChildren().addAll(top, message);
		textHolder.setPadding(new Insets(20));

		StackPane layoutLayer = new StackPane();
		layoutLayer.setStyle("-fx-background-color: rgba(40,40,40,0.9);"
				+ "-fx-background-radius: 15;" + "-fx-border-color: gold;"
				+ "-fx-border-width: 2;" + "-fx-border-radius: 15;");
		layoutLayer.getChildren().add(textHolder);
		layoutLayer.setTranslateY(-100);

		VBox effectContainer = new VBox();
		effectContainer.setAlignment(Pos.CENTER);
		layoutLayer.setMaxWidth(Region.USE_PREF_SIZE);
		layoutLayer.setMinWidth(Region.USE_PREF_SIZE);
		layoutLayer.setMaxHeight(Region.USE_PREF_SIZE);

		effectContainer.setMaxWidth(Region.USE_PREF_SIZE);
		effectContainer.setMaxHeight(Region.USE_PREF_SIZE);
		effectContainer.getChildren().add(layoutLayer);

		StackPane.setAlignment(effectContainer, Pos.CENTER);
		playRoot.getChildren().add(effectContainer);

		PauseTransition delay = new PauseTransition(Duration.seconds(3));
		delay.setOnFinished(event -> playRoot.getChildren().remove(
				effectContainer));
		delay.play();
	}

	private void displayQuit(StackPane root) {
		Rectangle dim = new Rectangle(1280, 720);
		dim.setFill(Color.rgb(0, 0, 0, 0.7));
		dim.widthProperty().bind(root.widthProperty());
		dim.heightProperty().bind(root.heightProperty());

		Image screen = new Image(getClass().getResourceAsStream(
				"/images/quitConfirm.png"));
		Image quit = new Image(getClass().getResourceAsStream(
				"/images/yesConfirm.png"));
		Image no = new Image(getClass().getResourceAsStream(
				"/images/noConfirm.png"));
		ImageView screenView = new ImageView(screen);
		ImageView quitHovered = new ImageView(quit);
		ImageView noHovered = new ImageView(no);

		quitHovered.setVisible(false);
		noHovered.setVisible(false);

		Pane popupUI = new Pane(screenView, quitHovered, noHovered);
		popupUI.setMaxSize(480, 240);

		Button quitBtn = new Button();
		quitBtn.setPrefSize(159, 75);
		quitBtn.setLayoutX(26);
		quitBtn.setLayoutY(130);
		quitBtn.setOpacity(0);

		Button noBtn = new Button();
		noBtn.setPrefSize(159, 75);
		noBtn.setLayoutX(300);
		noBtn.setLayoutY(130);
		noBtn.setOpacity(0);

		screenView.setVisible(true);
		playHoverEffect(quitBtn, quitHovered);
		playHoverEffect(noBtn, noHovered);

		popupUI.getChildren().addAll(quitBtn, noBtn);

		StackPane fullOverlay = new StackPane(dim, popupUI);
		fullOverlay.setPrefSize(1280, 720);
		StackPane.setAlignment(popupUI, Pos.CENTER);

		quitBtn.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			System.exit(0);
		});

		noBtn.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			root.getChildren().remove(fullOverlay);
		});

		fullOverlay.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				clickSoundEffect.play();
				System.exit(0);
			} else if (e.getCode() == KeyCode.ESCAPE) {
				clickSoundEffect.play();
				root.getChildren().remove(fullOverlay);
			}
		});

		playHoverEffect(quitBtn, quitHovered);
		playHoverEffect(noBtn, noHovered);

		root.getChildren().add(fullOverlay);
	}

	private void startMonsterRandomizer(StackPane overlay, Role roleH) {
		try {
			ArrayList<Monster> allMonsters = DataLoader.readMonsters();
			ArrayList<Monster> roleMatches = new ArrayList<>();
			ArrayList<Image> allImages = new ArrayList<>();

			for (Monster m : allMonsters) {
				if (m.getRole() == roleH) {
					String safeName = m.getName().toLowerCase()
							.replaceAll("[ .]", "");
					String path = "/monsters/" + safeName + ".png";
					java.net.URL imgUrl = getClass().getResource(path);

					if (imgUrl != null) {
						roleMatches.add(m);
						allImages.add(new Image(imgUrl.toExternalForm()));
					} else {
						System.err.println("Missing image: " + path);
					}
				}
			}

			ImageView displayView = new ImageView();
			displayView.setFitHeight(180);
			displayView.setPreserveRatio(true);

			Font fontMain = Font
					.loadFont(
							getClass().getResourceAsStream(
									"/AutumnVoyage-Regular.ttf"), 22);
			Font fontSub = Font
					.loadFont(
							getClass().getResourceAsStream(
									"/AutumnVoyage-Regular.ttf"), 18);

			Label nameLabel = new Label("SCANNING...");
			nameLabel.setFont(fontMain);
			nameLabel.setTextFill(Color.WHITE);

			Label descLabel = new Label();
			descLabel.setFont(fontSub);
			descLabel.setTextFill(Color.LIGHTGRAY);
			descLabel.setWrapText(true);
			descLabel.setMaxWidth(300);

			Label energyLabel = new Label();
			energyLabel.setFont(fontSub);
			energyLabel.setTextFill(Color.YELLOW);

			energyLabel.setMouseTransparent(true);
			descLabel.setMouseTransparent(true);
			nameLabel.setMouseTransparent(true);
			displayView.setMouseTransparent(true);

			VBox monsterBox = new VBox(2.5, displayView, nameLabel, descLabel,
					energyLabel);
			displayView.setFitWidth(80);
			displayView.setPreserveRatio(true);
			monsterBox.setAlignment(Pos.CENTER);
			if (roleH == game.getPlayer().getRole()) {
				monsterBox.setTranslateX(-140);
				VBox.setMargin(descLabel, new Insets(0, -150, 0, 0));
				monsterBox.setTranslateY(10);
			} else {
				monsterBox.setTranslateX(120);
				VBox.setMargin(descLabel, new Insets(0, -150, 0, 0));
				monsterBox.setTranslateY(10);
			}
			overlay.getChildren().add(monsterBox);
			monsterBox.setMouseTransparent(true);

			Random random = new Random();
			Timeline flicker = new Timeline(new KeyFrame(Duration.millis(100),
					e -> {
						if (!allImages.isEmpty()) {
							int i = random.nextInt(allImages.size());
							Monster m = roleMatches.get(i);

							displayView.setImage(allImages.get(i));
							nameLabel.setText(m.getName().toUpperCase());
							descLabel.setText(m.getDescription());
							energyLabel.setText("ENERGY: " + m.getEnergy());
						}
					}));

			flicker.setCycleCount(40);

			flicker.setOnFinished(e -> {
				Monster finalPlayer = roleH == game.getPlayer().getRole() ? game
						.getPlayer() : game.getOpponent();
				String finalSafeName = finalPlayer.getName().toLowerCase()
						.replaceAll("[ .]", "");
				String finalPath = "/monsters/" + finalSafeName + ".png";
				Image finalImage = new Image(getClass().getResourceAsStream(
						finalPath));

				displayView.setImage(finalImage);
				nameLabel.setText(finalPlayer.getName().toUpperCase());
				descLabel.setText(finalPlayer.getDescription());
				energyLabel.setText("ENERGY: " + finalPlayer.getEnergy());

			});

			flicker.play();

		} catch (IOException e) {
			System.err.println("Data loading failed.");
		}
	}

	private void startDiceRandomizer(StackPane overlay, int diceResult,
			Runnable onComplete) {
		ArrayList<Image> allImages = new ArrayList<>();
		for (int i = 1; i <= 6; i++) {
			allImages.add(new Image(getClass().getResourceAsStream(
					"/images/die/dice" + i + ".png")));
		}

		VBox diceBox = (VBox) overlay.getChildren().stream()
				.filter(node -> "persistent_dice".equals(node.getUserData()))
				.findFirst().orElse(null);

		ImageView displayView;

		if (diceBox == null) {
			displayView = new ImageView();
			displayView.setFitWidth(80);
			displayView.setPreserveRatio(true);

			diceBox = new VBox(displayView);
			diceBox.setUserData("persistent_dice");
			diceBox.setAlignment(Pos.CENTER_RIGHT);
			diceBox.setPadding(new Insets(0, 93, 0, 0));
			diceBox.setMouseTransparent(true);

			overlay.getChildren().add(diceBox);
		} else {
			displayView = (ImageView) diceBox.getChildren().get(0);
		}

		Random random = new Random();
		Timeline flicker = new Timeline(new KeyFrame(Duration.millis(65),
				e -> {
					displayView.setImage(allImages.get(random.nextInt(allImages
							.size())));
				}));

		flicker.setCycleCount(15);

		flicker.setOnFinished(e -> {
			String finalPath = "/images/die/dice" + diceResult + ".png";
			displayView.setImage(new Image(getClass().getResourceAsStream(
					finalPath)));
			if (onComplete != null)
				onComplete.run();
		});

		flicker.play();
	}

	public void createGrid() {
		String stroke = "-fx-effect: dropshadow(gaussian, black, 2, 1.0, 0, 0);";

		if (currentGrid != null) {
		    currentGrid.getChildren().clear();
		    playRoot.getChildren().remove(currentGrid);
		}
		if (cellStack != null) {
		    cellStack.clear();
		}
		if (cellEnergyLabels != null) {
		    cellEnergyLabels.clear();
		}

		currentGrid = new GridPane();
		currentGrid.setAlignment(Pos.CENTER);

		Cell[][] vBoard = game.getBoard().getBoardCells();
		cellEnergyLabels = new ArrayList<Label>();
		cellStack = new ArrayList<StackPane>();

		currentGrid.setHgap(1);
		currentGrid.setVgap(1);

		int rows = Constants.BOARD_ROWS;
		int cols = Constants.BOARD_COLS;

		for (int i = 0; i < Constants.BOARD_SIZE; i++) {
			cellEnergyLabels.add(null);
			cellStack.add(new StackPane());
			int r = i / cols;
			int c = i % cols;

			int row = (rows - 1) - r;

			int col = (r % 2 == 0) ? c : (cols - 1) - c;

			Image sharedCellImage;
			if (contains(Constants.MONSTER_CELL_INDICES, i)) sharedCellImage = monsterCellImg;
			else if (contains(Constants.CONVEYOR_CELL_INDICES, i)) sharedCellImage = conveyorCellImg;
			else if (contains(Constants.SOCK_CELL_INDICES, i)) sharedCellImage = sockCellImg;
			else if (contains(Constants.CARD_CELL_INDICES, i)) sharedCellImage = cardBgCellImg;
			else sharedCellImage = normalCellImg;

			ImageView cellView = new ImageView(sharedCellImage);

			cellView.setFitWidth(tileSize);
			cellView.setFitHeight(tileSize);
			cellView.setSmooth(false);

			cellStack.get(i).getChildren().add(cellView);

			int placeRow = i / cols;
			int placeCol = i % cols;

			if (placeRow % 2 == 1) {
				placeCol = cols - 1 - placeCol;
			}

			Cell curr = vBoard[placeRow][placeCol];

			if (curr instanceof DoorCell) {

				DoorCell door = (DoorCell) curr;

				String doorPath = "";

				if (i == Constants.WINNING_POSITION) {
					doorPath = "/images/cells/boo.png";
				} else if (door.getRole() == Role.SCARER) {
					doorPath = "/images/cells/scarer_door.png";
				} else if (door.getRole() == Role.LAUGHER) {
					doorPath = "/images/cells/laugher_door.png";
				}

				ImageView doorView = new ImageView(new Image(getClass()
						.getResource(doorPath).toExternalForm()));

				doorView.setFitWidth(tileSize);
				doorView.setFitHeight(tileSize);

				Label energyLabel = new Label();
				cellEnergyLabels.set(i, energyLabel);
				energyLabel.setText(door.getEnergy() + "");
				energyLabel
						.setStyle("-fx-text-fill: #FFD600; -fx-font-weight: bold; -fx-font-size: 14px;"
								+ stroke);
				updateCellStats(curr, cellStack.get(i), energyLabel, i);
				StackPane.setAlignment(energyLabel, Pos.BOTTOM_CENTER);

				cellStack.get(i).getChildren().addAll(doorView, energyLabel);
			}

			else if (curr instanceof MonsterCell) {

				Monster m = ((MonsterCell) curr).getCellMonster();

				if (m != game.getPlayer() && m != game.getOpponent()) {

					String safeName = m.getName().toLowerCase()
							.replaceAll("[ .]", "");
					String mPath = "/monsters/" + safeName + ".png";

					ImageView monsterView = new ImageView(new Image(getClass()
							.getResourceAsStream(mPath)));

					monsterView.setFitWidth(tileSize * 0.9);
					monsterView.setFitHeight(tileSize * 0.9);

					Label energyLabel = new Label();
					cellEnergyLabels.set(i, energyLabel);
					energyLabel
							.setStyle("-fx-text-fill: #FFD600; -fx-font-weight: bold; -fx-font-size: 14px;"
									+ stroke);
					updateCellStats(curr, cellStack.get(i), energyLabel, i);
					StackPane.setAlignment(energyLabel, Pos.BOTTOM_CENTER);

					cellStack.get(i).getChildren()
							.addAll(monsterView, energyLabel);
				}
			}
			else if (curr instanceof CardCell) {
				ImageView cardView = new ImageView(cardCellImg);
			    cardView.setFitWidth(tileSize);
			    cardView.setFitHeight(tileSize);
			    cardView.setPreserveRatio(true);
			    
			    cellStack.get(i).getChildren().add(cardView);
			}

			else if (curr instanceof ContaminationSock) {
				Label effectLabel = new Label();
				cellEnergyLabels.set(i, effectLabel);
				effectLabel.setText("GO TO CELL "
						+ (i + ((ContaminationSock) curr).getEffect()));
				effectLabel
						.setStyle("-fx-text-fill: #ff0000; -fx-font-weight: bold; -fx-font-size: 9px;"
								+ stroke);
				effectLabel.setWrapText(true);
				StackPane.setAlignment(effectLabel, Pos.BOTTOM_CENTER);
				cellStack.get(i).getChildren().add(effectLabel);
			}

			else if (curr instanceof ConveyorBelt) {
				Label effectLabel = new Label();
				cellEnergyLabels.set(i, effectLabel);
				effectLabel.setText("GO TO CELL "
						+ (i + ((ConveyorBelt) curr).getEffect()));
				effectLabel
						.setStyle("-fx-text-fill: #00FF00; -fx-font-weight: bold; -fx-font-size: 9px;"
								+ stroke);
				effectLabel.setWrapText(true);
				StackPane.setAlignment(effectLabel, Pos.BOTTOM_CENTER);
				cellStack.get(i).getChildren().add(effectLabel);
			}

			Label indexLabel = new Label(i + "");
			indexLabel.setTextFill(Color.WHITE);
			indexLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 10px;"
					+ "-fx-effect: dropshadow(gaussian, black, 1, 1.0, 0, 0);");

			StackPane.setAlignment(indexLabel, Pos.TOP_RIGHT);
			cellStack.get(i).getChildren().add(indexLabel);

			currentGrid.add(cellStack.get(i), col, row);
		}
		playRoot.setFocusTraversable(true);
		playRoot.requestFocus();
		playRoot.getChildren().add(currentGrid);

		updateShuffledCards();
		setPlayerStats();
		showDiceRollGUI();
		showEndTurn();
		
		playRoot.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.W) {
				game.getCurrent().setPosition(Constants.WINNING_POSITION);
				previousPlayerPos = game.getPlayer().getPosition();
				previousOpponentPos = game.getOpponent().getPosition();
				updateAllCellStats();
				updatePlayerStats();
				forcePlayers();
			} else if (e.getCode() == KeyCode.E) {
				game.getCurrent().alterEnergy(100);
				updatePlayerStats();
				if (game.getWinner() != null) {
					if (game.getWinner() == game.getOpponent()) {
						displayLoss();
					} else {
						displayWin();
					}
				}
			} else if (e.getCode() == KeyCode.F) {
				game.getCurrent().setPosition(Constants.WINNING_POSITION-3);
				previousPlayerPos = game.getPlayer().getPosition();
				previousOpponentPos = game.getOpponent().getPosition();
				updateAllCellStats();
				updatePlayerStats();
				forcePlayers();
			}
			else if (e.getCode() == KeyCode.S) {
				game.getCurrent().setShielded(!(game.getCurrent().isShielded()));
				updatePlayerStats();
			}
		});
		
		overlayPane = new Pane();
		overlayPane.setMouseTransparent(true);
		playRoot.getChildren().add(overlayPane);
	}

	private void forcePlayers() {
		int cols = Constants.BOARD_COLS;
		int rows = Constants.BOARD_ROWS;
		Monster player = game.getPlayer();
		Monster opponent = game.getOpponent();
		Monster[] monsters = { player, opponent };

		for (Monster m : monsters) {
			if (m == null)
				continue;
			boolean isPlayer = m == player;
			int boardIndex = m.getPosition();

			int r = boardIndex / cols;
			int c = boardIndex % cols;
			int gridRow = (rows - 1) - r;
			int gridCol = (r % 2 == 0) ? c : (cols - 1) - c;

			for (Node node : currentGrid.getChildren()) {
				StackPane cellStack = (StackPane) node;
				Integer nodeCol = GridPane.getColumnIndex(cellStack);
				Integer nodeRow = GridPane.getRowIndex(cellStack);
				if (nodeCol == null || nodeRow == null)
					continue;
				if (nodeCol == gridCol && nodeRow == gridRow) {
					final StackPane finalCell = cellStack;
					final boolean finalIsPlayer = isPlayer;
					Platform.runLater(() -> {
						Bounds cellBounds = finalCell.localToScene(finalCell
								.getBoundsInLocal());
						Bounds paneBounds = overlayPane
								.localToScene(overlayPane.getBoundsInLocal());
						double x = cellBounds.getMinX() - paneBounds.getMinX();
						double y = cellBounds.getMinY() - paneBounds.getMinY();
						ImageView overlay = finalIsPlayer ? playerOverlayView
								: opponentOverlayView;
						Label tag = finalIsPlayer ? playerTagLabel
								: opponentTagLabel;
						if (overlay != null) {
							overlay.setLayoutX(x);
							overlay.setLayoutY(y);
						}
						if (tag != null) {
							tag.setLayoutX(x);
							tag.setLayoutY(y + tileSize * 0.75);
						}
					});
					break;
				}
			}
		}

		if (game.getWinner() != null) {
			if (game.getWinner() == game.getOpponent()) {
				displayLoss();
			} else {
				displayWin();
			}
		}
	}

	public void showEndTurn() {
		Font font = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 24);
		endTurnBtn = new Button("END TURN");
		endTurnBtn.setFont(font);
		endTurnBtn.setTextFill(Color.WHITE);
		endTurnBtn.setBackground(null);
		endTurnBtn.setStyle("-fx-border-color: #FFBF00;"
				+ "-fx-border-width: 2;" + "-fx-border-radius: 6;"
				+ "-fx-background-radius: 6;"
				+ "-fx-background-color: rgba(0,0,0,0.5);"
				+ "-fx-padding: 8 16 8 16;");
		endTurnBtn.setVisible(false);

		StackPane.setAlignment(endTurnBtn, Pos.BOTTOM_CENTER);
		StackPane.setMargin(endTurnBtn, new Insets(0, 0, 30, 0));
		playRoot.getChildren().add(endTurnBtn);

		endTurnBtn.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			endTurnBtn.setVisible(false);
			updatePlayerStats();
			askPowerUp();
		});
	}

	public void displayCard(Card card) {
		AudioClip draw = new AudioClip(getClass().getResource(
				"/sounds/card.mp3").toExternalForm());
		draw.play();

		Font fontTitle = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 28);
		Font fontDesc = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 18);

		String luckyText = card.isLucky() ? "LUCKY CARD" : "UNLUCKY CARD";
		Color luckyColor = card.isLucky() ? Color.GOLD : Color.CRIMSON;

		Label titleLabel = new Label(card.getName().toUpperCase());
		Label descLabel = new Label(card.getDescription());
		Label luckyLabel = new Label(luckyText);

		titleLabel.setFont(fontTitle);
		titleLabel.setTextFill(Color.WHITE);
		titleLabel.setWrapText(true);
		titleLabel.setMaxWidth(420);
		titleLabel.setAlignment(Pos.CENTER);
		titleLabel.setTextAlignment(TextAlignment.CENTER);

		descLabel.setFont(fontDesc);
		descLabel.setTextFill(Color.LIGHTGRAY);
		descLabel.setWrapText(true);
		descLabel.setMaxWidth(420);
		descLabel.setAlignment(Pos.CENTER);
		descLabel.setTextAlignment(TextAlignment.CENTER);

		luckyLabel.setFont(fontDesc);
		luckyLabel.setTextFill(luckyColor);

		String stroke = "-fx-effect: dropshadow(gaussian, black, 2, 1.0, 0, 0);";
		titleLabel.setStyle(stroke);
		descLabel.setStyle(stroke);
		luckyLabel.setStyle(stroke);

		Label dismissLabel = new Label("Click anywhere to close");
		dismissLabel.setFont(fontDesc);
		dismissLabel.setTextFill(Color.DARKGRAY);
		dismissLabel.setStyle(stroke);

		VBox content = new VBox(14, luckyLabel, titleLabel, descLabel,
				dismissLabel);
		content.setAlignment(Pos.CENTER);
		content.setPadding(new Insets(24));

		StackPane cardPanel = new StackPane(content);
		cardPanel.setStyle("-fx-background-color: rgba(20,20,20,0.95);"
				+ "-fx-background-radius: 15;" + "-fx-border-color: "
				+ (card.isLucky() ? "gold" : "crimson") + ";"
				+ "-fx-border-width: 3;" + "-fx-border-radius: 15;");
		cardPanel.setMaxSize(460, Region.USE_PREF_SIZE);
		cardPanel.setEffect(new DropShadow(20, 0, 0,
				card.isLucky() ? Color.GOLD : Color.CRIMSON));

		Rectangle dim = new Rectangle();
		dim.setFill(Color.rgb(0, 0, 0, 0.7));
		dim.widthProperty().bind(playRoot.widthProperty());
		dim.heightProperty().bind(playRoot.heightProperty());

		StackPane fullOverlay = new StackPane(dim, cardPanel);
		fullOverlay.setPrefSize(1280, 720);
		StackPane.setAlignment(cardPanel, Pos.CENTER);

		fullOverlay.setOnMouseClicked(ev -> {
			clickSoundEffect.play();
			playRoot.getChildren().remove(fullOverlay);
		});
		updateShuffledCards();
		
		playRoot.getChildren().add(fullOverlay);
		fullOverlay.toFront();
	}

	private void updateShuffledCards(){
	    int rand = (int)(10 * Math.random() + 1);

	    if (cardsRemainingLabel == null) {
	        Font font = Font.loadFont(getClass().getResourceAsStream("/AutumnVoyage-Regular.ttf"), 16);
	        String stroke = "-fx-effect: dropshadow(gaussian, black, 2, 1.0, 0, 0);";

	        cardsRemainingLabel = new Label();
	        cardsRemainingLabel.setFont(font);
	        cardsRemainingLabel.setTextFill(Color.GOLD);
	        cardsRemainingLabel.setStyle(
	            stroke +
	            "-fx-background-color: rgba(0,0,0,0.5);" +
	            "-fx-background-radius: 6;" +
	            "-fx-border-color: gold;" +
	            "-fx-border-width: 1;" +
	            "-fx-border-radius: 6;" +
	            "-fx-padding: 4 10 4 10;"
	        );
	        StackPane.setAlignment(cardsRemainingLabel, Pos.CENTER_RIGHT);
	        StackPane.setMargin(cardsRemainingLabel, new Insets(240, 55, 0, 0));

	        cardsImageView = new ImageView(new Image(getClass().getResourceAsStream("/images/cards/card" + rand + ".png")));
	        cardsImageView.setFitWidth(100);
	        cardsImageView.setFitHeight(140);
	        cardsImageView.setPreserveRatio(true);
	        StackPane.setAlignment(cardsImageView, Pos.CENTER_RIGHT);
	        StackPane.setMargin(cardsImageView, new Insets(350, 88, 0, 0));

	        playRoot.getChildren().add(cardsImageView);
	        playRoot.getChildren().add(cardsRemainingLabel);
	    } else {
	        cardsImageView.setImage(new Image(getClass().getResourceAsStream("/images/cards/card" + rand + ".png")));
	    }

	    cardsRemainingLabel.setText("CARDS REMAINING: " + (Board.getCards().size()+1));
	}
	
	public void showDiceRollGUI() {
		ImageView roll = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/die/roll_laugher.png")));
		ImageView rollHovered = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/die/roll_laugherHovered.png")));
		ImageView background = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/die/roll_bg.png")));

		rollHovered.setVisible(true);

		playHoverEffect(rollBtn, rollHovered);

		diceOn = new StackPane(background, roll, rollHovered);
		diceOn.setPadding(new Insets(0, 10, 0, 0));
		diceOn.setAlignment(Pos.CENTER_RIGHT);

		playRoot.getChildren().add(diceOn);
	}

	public void updateAllCellStats() {
		Cell[][] vBoard = game.getBoard().getBoardCells();
		int cols = Constants.BOARD_COLS;

		for (int i = 0; i < Constants.BOARD_SIZE; i++) {
			StackPane cell = this.cellStack.get(i);

			int placeRow = i / cols;
			int placeCol = i % cols;

			if (placeRow % 2 == 1) {
				placeCol = cols - 1 - placeCol;
			}

			Cell curr = vBoard[placeRow][placeCol];
			Label energyLabel = cellEnergyLabels.get(i);

			if (energyLabel != null) {
				updateCellStats(curr, cell, energyLabel, i);
			}

		}

	}

	public void updateCellStats(Cell curr, StackPane cellStack,
			Label energyLabel, int i) {
		if (curr instanceof DoorCell) {
			DoorCell door = (DoorCell) curr;
			if (door.isActivated()) {
				cellStack.getChildren().removeIf(
						child -> {
							if (cellStack.getChildren().indexOf(child) == 0)
								return false;
							if ("player_overlay".equals(child.getUserData()))
								return false;
							if (child instanceof Label
									&& child.getUserData() == null
									&& ((Label) child).getTextFill().equals(
											Color.WHITE))
								return false;
							return true;
						});

				String roo;
				if (i == 99) {
					roo = "boo";
				} else {
					roo = door.getRole() + "";
					roo = roo.toLowerCase() + "_door";
				}

				ImageView openDoor = new ImageView(new Image(getClass()
						.getResourceAsStream(
								"/images/cells/" + roo + "Opened.png")));
				openDoor.setFitWidth(tileSize);
				openDoor.setFitHeight(tileSize);

				Rectangle red = new Rectangle(tileSize, tileSize);
				red.setFill(Color.RED);
				red.setOpacity(0.3);

				energyLabel.setText("");

				cellStack.getChildren().add(1, red);
				cellStack.getChildren().add(1, openDoor);
				cellStack.getChildren().add(energyLabel);
				StackPane.setAlignment(energyLabel, Pos.BOTTOM_CENTER);
			}
		}

		if (curr instanceof MonsterCell) {

			energyLabel.setText(((MonsterCell) curr).getCellMonster()
					.getEnergy() + "");

		}

	}

	private void displayBelt(Monster mover, int beltCellIndex,
			int destinationIndex) {
		AudioClip soundEffect = new AudioClip(getClass().getResource(
				"/sounds/conveyorBelt.mp3").toExternalForm());
		soundEffect.play();

		Font fontTitle = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 28);
		Font fontDesc = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 18);

		Label titleLabel = new Label("LANDED ON CONVEYOR BELT");
		Label descLabel = new Label("TRANSPORTED " + mover.getName()
				+ " FROM CELL " + beltCellIndex + " TO " + destinationIndex);

		titleLabel.setFont(fontTitle);
		titleLabel.setTextFill(Color.WHITE);
		titleLabel.setWrapText(true);
		titleLabel.setMaxWidth(420);
		titleLabel.setAlignment(Pos.CENTER);
		titleLabel.setTextAlignment(TextAlignment.CENTER);

		descLabel.setFont(fontDesc);
		descLabel.setTextFill(Color.LIGHTGRAY);
		descLabel.setWrapText(true);
		descLabel.setMaxWidth(420);
		descLabel.setAlignment(Pos.CENTER);
		descLabel.setTextAlignment(TextAlignment.CENTER);

		String stroke = "-fx-effect: dropshadow(gaussian, black, 2, 1.0, 0, 0);";
		titleLabel.setStyle(stroke);
		descLabel.setStyle(stroke);

		Label dismissLabel = new Label("Click anywhere to close");
		dismissLabel.setFont(fontDesc);
		dismissLabel.setTextFill(Color.DARKGRAY);
		dismissLabel.setStyle(stroke);

		VBox content = new VBox(14, titleLabel, descLabel, dismissLabel);
		content.setAlignment(Pos.CENTER);
		content.setPadding(new Insets(24));

		StackPane cardPanel = new StackPane(content);
		cardPanel.setStyle("-fx-background-color: rgba(20,20,20,0.95);"
				+ "-fx-background-radius: 15; -fx-border-color: lime;"
				+ "-fx-border-width: 3; -fx-border-radius: 15;");
		cardPanel.setMaxSize(460, Region.USE_PREF_SIZE);

		Rectangle dim = new Rectangle();
		dim.setFill(Color.rgb(0, 0, 0, 0.7));
		dim.widthProperty().bind(playRoot.widthProperty());
		dim.heightProperty().bind(playRoot.heightProperty());

		StackPane fullOverlay = new StackPane(dim, cardPanel);
		fullOverlay.setPrefSize(1280, 720);
		StackPane.setAlignment(cardPanel, Pos.CENTER);

		fullOverlay.setOnMouseClicked(ev -> {
			clickSoundEffect.play();
			playRoot.getChildren().remove(fullOverlay);
		});

		playRoot.getChildren().add(fullOverlay);
	}

	private void displaySock(Monster mover, int sockCellIndex,
			int destinationIndex) {
		AudioClip backSound = new AudioClip(getClass().getResource(
				"/sounds/sock.mp3").toExternalForm());
		backSound.play();
		
		Font fontTitle = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 28);
		Font fontDesc = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 18);

		Label titleLabel = new Label("LANDED ON CONTAMINATION");
		Label descLabel = new Label("TRANSPORTED " + mover.getName()
				+ " FROM CELL " + sockCellIndex + " TO " + destinationIndex + " AND LOST " + Constants.SLIP_PENALTY + " ENERGY");

		titleLabel.setFont(fontTitle);
		titleLabel.setTextFill(Color.CRIMSON);
		titleLabel.setWrapText(true);
		titleLabel.setMaxWidth(420);
		titleLabel.setAlignment(Pos.CENTER);
		titleLabel.setTextAlignment(TextAlignment.CENTER);

		descLabel.setFont(fontDesc);
		descLabel.setTextFill(Color.DARKRED);
		descLabel.setWrapText(true);
		descLabel.setMaxWidth(420);
		descLabel.setAlignment(Pos.CENTER);
		descLabel.setTextAlignment(TextAlignment.CENTER);

		String stroke = "-fx-effect: dropshadow(gaussian, black, 2, 1.0, 0, 0);";
		titleLabel.setStyle(stroke);
		descLabel.setStyle(stroke);

		Label dismissLabel = new Label("Click anywhere to close");
		dismissLabel.setFont(fontDesc);
		dismissLabel.setTextFill(Color.DARKGRAY);
		dismissLabel.setStyle(stroke);

		VBox content = new VBox(14, titleLabel, descLabel, dismissLabel);
		content.setAlignment(Pos.CENTER);
		content.setPadding(new Insets(24));

		StackPane cardPanel = new StackPane(content);
		cardPanel.setStyle("-fx-background-color: rgba(20,20,20,0.95);"
				+ "-fx-background-radius: 15; -fx-border-color: lime;"
				+ "-fx-border-width: 3; -fx-border-radius: 15;");
		cardPanel.setMaxSize(460, Region.USE_PREF_SIZE);

		Rectangle dim = new Rectangle();
		dim.setFill(Color.rgb(0, 0, 0, 0.7));
		dim.widthProperty().bind(playRoot.widthProperty());
		dim.heightProperty().bind(playRoot.heightProperty());

		StackPane fullOverlay = new StackPane(dim, cardPanel);
		fullOverlay.setPrefSize(1280, 720);
		StackPane.setAlignment(cardPanel, Pos.CENTER);

		fullOverlay.setOnMouseClicked(ev -> {
			clickSoundEffect.play();
			playRoot.getChildren().remove(fullOverlay);
		});

		playRoot.getChildren().add(fullOverlay);
	}

	private void displayDoorEffect(Monster monster, DoorCell door) {
		AudioClip soundEffect = new AudioClip(getClass().getResource(
				"/sounds/doorOpening.mp3").toExternalForm());
		soundEffect.play();
		Font fontTitle = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 24);
		Font fontSub = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 18);

		String stroke = "-fx-effect: dropshadow(gaussian, black, 2, 1.0, 0, 0);";
		String borderColor = door.getRole() == Role.LAUGHER ? "lime"
				: "deepskyblue";
		Color glowColor = door.getRole() == Role.LAUGHER ? Color.LIME
				: Color.DEEPSKYBLUE;

		Label nameLabel = new Label(monster.getName().toUpperCase()
				+ " LANDED ON A " + door.getRole() + " DOOR!");
		nameLabel.setFont(fontTitle);
		nameLabel.setTextFill(Color.WHITE);
		nameLabel.setWrapText(true);
		nameLabel.setMaxWidth(460);
		nameLabel.setAlignment(Pos.CENTER);
		nameLabel.setTextAlignment(TextAlignment.CENTER);
		nameLabel.setStyle(stroke);

		VBox content = new VBox(8, nameLabel);
		content.setAlignment(Pos.CENTER);

		Label monsterLabel = new Label(monster.getName() + " CHANGED THEIR ENERGY TO "
			+ monster.getEnergy());
		boolean monsterGained = door.getRole() == monster.getRole()? true: false;
		monsterLabel.setFont(fontSub);
		monsterLabel.setTextFill(monsterGained ? Color.LIME : Color.CRIMSON);
		monsterLabel.setStyle(stroke);
		content.getChildren().add(monsterLabel);

		for (Monster sm : Board.getStationedMonsters()) {
			if (sm.getRole() == monster.getRole()) {
				Label smLabel = new Label(sm.getName() + " CHANGED THEIR ENERGY TO "
						+ sm.getEnergy());
				smLabel.setFont(fontSub);
				boolean gained = door.getRole() == sm.getRole()? true: false;
				smLabel.setTextFill(gained ? Color.LIME : Color.CRIMSON);
				smLabel.setStyle(stroke);
				content.getChildren().add(smLabel);
			}
		}

		Label skipLabel = new Label("(click anywhere to continue)");
		skipLabel.setFont(fontSub);
		skipLabel.setTextFill(Color.GRAY);
		skipLabel.setStyle(stroke);
		content.getChildren().add(skipLabel);
		content.setPadding(new Insets(24));

		StackPane panel = new StackPane(content);
		panel.setStyle("-fx-background-color: rgba(20,20,20,0.95);"
				+ "-fx-background-radius: 15;" + "-fx-border-color: "
				+ borderColor + ";" + "-fx-border-width: 3;"
				+ "-fx-border-radius: 15;");
		panel.setMaxSize(500, Region.USE_PREF_SIZE);
		panel.setEffect(new DropShadow(20, 0, 0, glowColor));

		Rectangle dim = new Rectangle();
		dim.setFill(Color.rgb(0, 0, 0, 0.7));
		dim.widthProperty().bind(playRoot.widthProperty());
		dim.heightProperty().bind(playRoot.heightProperty());

		StackPane fullOverlay = new StackPane(dim, panel);
		fullOverlay.setPrefSize(1280, 720);
		StackPane.setAlignment(panel, Pos.CENTER);
		playRoot.getChildren().add(fullOverlay);

		fullOverlay.setOnMouseClicked(ev -> {
			clickSoundEffect.play();
			playRoot.getChildren().remove(fullOverlay);
		});
	}

	public void setPlayerStats() {
		Monster player = game.getPlayer();
		String safeName = player.getName().toLowerCase().replaceAll("[ .]", "");
		ImageView playerImage = new ImageView(new Image(getClass()
				.getResourceAsStream("/monsters/" + safeName + ".png")));

		Label playerTitle = new Label("Player:");
		Label playerName = new Label("Name: " + player.getName() + "");
		Label playerRole = new Label("Original Role: "
				+ player.getOriginalRole() + "");
		Label playerType = new Label("Type: "
				+ player.getClass().getSimpleName() + "");

		playerEnergy = new Label();
		playerCurrentRole = new Label();
		playerPosition = new Label();
		playerStatus = new Label();

		Monster opp = game.getOpponent();
		String safeNameOpp = opp.getName().toLowerCase().replaceAll("[ .]", "");
		ImageView oppImage = new ImageView(new Image(getClass()
				.getResourceAsStream("/monsters/" + safeNameOpp + ".png")));

		Label oppTitle = new Label("Opponent:");
		Label oppName = new Label("Name: " + opp.getName() + "");
		Label oppRole = new Label("Original Role: " + opp.getOriginalRole()
				+ "");
		Label oppType = new Label("Type: " + opp.getClass().getSimpleName()
				+ "");

		oppEnergy = new Label();
		oppCurrentRole = new Label();
		oppPosition = new Label();
		oppStatus = new Label();

		Color playerColor = player.getRole() == Role.LAUGHER ? Color.LIME
				: Color.SKYBLUE;
		Color oppColor = opp.getRole() == Role.LAUGHER ? Color.LIME
				: Color.SKYBLUE;

		Color playerTypeColor, oppTypeColor;

		if (player.getClass().getSimpleName().equalsIgnoreCase("Dasher")) {
			playerTypeColor = Color.YELLOW;
		} else if (player.getClass().getSimpleName()
				.equalsIgnoreCase("Schemer")) {
			playerTypeColor = Color.ORANGE;
		} else if (player.getClass().getSimpleName()
				.equalsIgnoreCase("Multitasker")) {
			playerTypeColor = Color.SILVER;
		} else {
			playerTypeColor = Color.CRIMSON;
		}

		if (opp.getClass().getSimpleName().equalsIgnoreCase("Dasher")) {
			oppTypeColor = Color.YELLOW;
		} else if (opp.getClass().getSimpleName().equalsIgnoreCase("Schemer")) {
			oppTypeColor = Color.ORANGE;
		} else if (opp.getClass().getSimpleName()
				.equalsIgnoreCase("Multitasker")) {
			oppTypeColor = Color.SILVER;
		} else {
			oppTypeColor = Color.CRIMSON;
		}

		playerImage.setFitWidth(80);
		playerImage.setFitHeight(80);
		playerImage.setPreserveRatio(true);

		oppImage.setFitWidth(80);
		oppImage.setFitHeight(80);
		oppImage.setPreserveRatio(true);

		playerTitle.setFont(font);
		playerTitle.setTextFill(playerColor);

		playerName.setFont(font);
		playerName.setTextFill(Color.WHITE);

		playerRole.setFont(font);
		playerRole.setTextFill(playerColor);

		playerType.setFont(font);
		playerType.setTextFill(playerTypeColor);

		playerEnergy.setFont(font);
		playerEnergy.setTextFill(Color.GOLD);

		playerCurrentRole.setFont(font);
		playerCurrentRole.setTextFill(Color.WHITE);

		playerPosition.setFont(font);
		playerPosition.setTextFill(Color.WHITE);

		playerStatus.setFont(font);
		playerStatus.setTextFill(Color.WHITE);

		oppTitle.setFont(font);
		oppTitle.setTextFill(oppColor);

		oppName.setFont(font);
		oppName.setTextFill(Color.WHITE);

		oppRole.setFont(font);
		oppRole.setTextFill(oppColor);

		oppType.setFont(font);
		oppType.setTextFill(oppTypeColor);

		oppEnergy.setFont(font);
		oppEnergy.setTextFill(Color.GOLD);

		oppCurrentRole.setFont(font);
		oppCurrentRole.setTextFill(Color.WHITE);

		oppPosition.setFont(font);
		oppPosition.setTextFill(Color.WHITE);

		oppStatus.setFont(font);
		oppStatus.setTextFill(Color.WHITE);

		String stroke = "-fx-effect: dropshadow(gaussian, black, 2, 1.0, 0, 0);";
		playerTitle.setStyle(stroke);
		playerName.setStyle(stroke);
		playerRole.setStyle(stroke);
		playerType.setStyle(stroke);
		playerEnergy.setStyle(stroke);
		playerCurrentRole.setStyle(stroke);
		playerPosition.setStyle(stroke);
		playerStatus.setStyle(stroke);

		oppTitle.setStyle(stroke);
		oppName.setStyle(stroke);
		oppRole.setStyle(stroke);
		oppType.setStyle(stroke);
		oppEnergy.setStyle(stroke);
		oppCurrentRole.setStyle(stroke);
		oppPosition.setStyle(stroke);
		oppStatus.setStyle(stroke);

		playerBox = new VBox(5);
		playerBox.getChildren().addAll(playerTitle, playerImage, playerName,
				playerRole, playerType, playerEnergy, playerCurrentRole,
				playerPosition, playerStatus);

		oppBox = new VBox(5);
		oppBox.getChildren().addAll(oppTitle, oppImage, oppName, oppRole,
				oppType, oppEnergy, oppCurrentRole, oppPosition, oppStatus);

		playerBox.setAlignment(Pos.TOP_LEFT);

		oppBox.setAlignment(Pos.TOP_LEFT);

		playerPanel = new StackPane(playerBox);
		playerPanel.setMaxSize(240, 320);
		playerPanel.setAlignment(Pos.TOP_LEFT);
		StackPane.setAlignment(playerPanel, Pos.TOP_LEFT);
		StackPane.setMargin(playerPanel, new Insets(20, 0, 0, 20));

		oppPanel = new StackPane(oppBox);
		oppPanel.setMaxSize(240, 320);
		oppPanel.setAlignment(Pos.TOP_LEFT);
		StackPane.setAlignment(oppPanel, Pos.TOP_LEFT);
		StackPane.setMargin(oppPanel, new Insets(360, 0, 0, 20));

		playerBox.setPadding(new Insets(0, 0, 0, 5));
		oppBox.setPadding(new Insets(0, 0, 0, 5));

		playRoot.getChildren().addAll(playerPanel, oppPanel);

		updatePlayerStats();
	}

	public void updatePlayerStats() {
		Monster player = game.getPlayer();
		Monster opp = game.getOpponent();

		playerEnergy.setText("Energy: " + player.getEnergy());
		playerCurrentRole.setText("Current Role: " + player.getRole());
		playerPosition.setText("Board position: " + player.getPosition());
		if (player.isConfused()) {
			playerStatus.setText("Status: Confused for "
					+ player.getConfusionTurns() + " rounds");
			playerStatus.setFont(minifont);
		} else if (player.isFrozen()) {
			playerStatus.setText("Status: Frozen");
		} else if (player.isShielded()) {
			playerStatus.setText("Status: Shielded");
		} else if (player instanceof Dasher
				&& (((Dasher) player).getMomentumTurns() > 0)) {
			playerStatus.setText("Status: Momuntem for "
					+ ((Dasher) player).getMomentumTurns() + " rounds");
			playerStatus.setFont(minifont);
		} else if (player instanceof MultiTasker
				&& (((MultiTasker) player).getNormalSpeedTurns() > 0)) {
			playerStatus.setText("Status: Focus Mode for "
					+ ((MultiTasker) player).getNormalSpeedTurns() + " rounds");
			playerStatus.setFont(minifont);
		} else {
			playerStatus.setText("Status: N/A");
		}

		oppEnergy.setText("Energy: " + opp.getEnergy());
		oppCurrentRole.setText("Current Role: " + opp.getRole());
		oppPosition.setText("Board position: " + opp.getPosition());
		if (opp.isConfused()) {
			oppStatus.setText("Status: Confused for " + opp.getConfusionTurns()
					+ " rounds");
			oppStatus.setFont(minifont);
		} else if (opp.isFrozen()) {
			oppStatus.setText("Status: Frozen");
		} else if (opp.isShielded()) {
			oppStatus.setText("Status: Shielded");
		} else if (opp instanceof Dasher
				&& (((Dasher) opp).getMomentumTurns() > 0)) {
			oppStatus.setText("Status: Momuntem for "
					+ ((Dasher) opp).getMomentumTurns() + " rounds");
			oppStatus.setFont(minifont);
		} else if (opp instanceof MultiTasker
				&& (((MultiTasker) opp).getNormalSpeedTurns() > 0)) {
			oppStatus.setText("Status: Focus Mode for "
					+ ((MultiTasker) opp).getNormalSpeedTurns() + " rounds");
			oppStatus.setFont(minifont);
		} else {
			oppStatus.setText("Status: N/A");
		}

	}
	
	public void updateGrid() {	
		updateAllCellStats();
		updatePlayers();
		updatePlayerStats();
	}

	private void displayLoss() {
		AudioClip loss = new AudioClip(getClass().getResource(
				"/sounds/lose.mp3").toExternalForm());
		mediaPlayer.setVolume(0);
		loss.play();

		Monster winner = game.getWinner();
		Monster loser = (winner == game.getPlayer()) ? game.getOpponent()
				: game.getPlayer();

		Font fontTitle = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 64);
		Font fontSub = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 24);
		Font fontInfo = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 20);
		Font fontBtn = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 24);

		Rectangle dim = new Rectangle();
		dim.setFill(Color.rgb(0, 0, 0, 0.85));
		dim.widthProperty().bind(playRoot.widthProperty());
		dim.heightProperty().bind(playRoot.heightProperty());

		String stroke = "-fx-effect: dropshadow(gaussian, black, 2, 1.0, 0, 0);";

		Label titleLabel = new Label("GAME OVER");
		titleLabel.setFont(fontTitle);
		titleLabel.setTextFill(Color.CRIMSON);
		titleLabel
				.setStyle("-fx-effect: dropshadow(three-pass-box, darkred, 20, 0.6, 0, 0);");

		Label winnerLabel = new Label(winner.getName().toUpperCase() + " WINS!");
		winnerLabel.setFont(fontSub);
		winnerLabel.setTextFill(Color.WHITE);
		winnerLabel.setStyle(stroke);

		Label roleLabel = new Label("Role: " + winner.getRole());
		roleLabel.setFont(fontInfo);
		roleLabel.setTextFill(winner.getRole() == Role.LAUGHER ? Color.LIME
				: Color.SKYBLUE);
		roleLabel.setStyle(stroke);

		Label winnerEnergyLabel = new Label(winner.getName()
				+ " Final Energy: " + winner.getEnergy());
		winnerEnergyLabel.setFont(fontInfo);
		winnerEnergyLabel.setTextFill(Color.GOLD);
		winnerEnergyLabel.setStyle(stroke);

		Label loserEnergyLabel = new Label(loser.getName() + " Final Energy: "
				+ loser.getEnergy());
		loserEnergyLabel.setFont(fontInfo);
		loserEnergyLabel.setTextFill(Color.LIGHTGRAY);
		loserEnergyLabel.setStyle(stroke);

		Button menuBtn = new Button("MAIN MENU");
		menuBtn.setFont(fontBtn);
		menuBtn.setTextFill(Color.WHITE);
		menuBtn.setBackground(null);
		menuBtn.setStyle("-fx-border-color: crimson;" + "-fx-border-width: 2;"
				+ "-fx-border-radius: 6;" + "-fx-background-radius: 6;"
				+ "-fx-background-color: rgba(0,0,0,0.5);"
				+ "-fx-padding: 8 24 8 24;");
		menuBtn.setEffect(new DropShadow(10, 0, 0, Color.DARKRED));

		menuBtn.setOnMouseEntered(e -> menuBtn
				.setStyle("-fx-border-color: crimson;" + "-fx-border-width: 2;"
						+ "-fx-border-radius: 6;" + "-fx-background-radius: 6;"
						+ "-fx-background-color: rgba(139,0,0,0.5);"
						+ "-fx-padding: 8 24 8 24;"));
		menuBtn.setOnMouseExited(e -> menuBtn
				.setStyle("-fx-border-color: crimson;" + "-fx-border-width: 2;"
						+ "-fx-border-radius: 6;" + "-fx-background-radius: 6;"
						+ "-fx-background-color: rgba(0,0,0,0.5);"
						+ "-fx-padding: 8 24 8 24;"));
		menuBtn.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			playerOverlayView = null;
			opponentOverlayView = null;
			playerTagLabel = null;
			opponentTagLabel = null;
			previousPlayerPos = 0;
			previousOpponentPos = 0;
			insPage = 1;
			mediaPlayer.setVolume(0.3);
			loss.stop();
			returnToMenu();
		});

		VBox content = new VBox(15, titleLabel, winnerLabel, roleLabel,
				winnerEnergyLabel, loserEnergyLabel, menuBtn);
		content.setAlignment(Pos.CENTER);

		StackPane fullOverlay = new StackPane(dim, content);
		fullOverlay.setPrefSize(1280, 720);
		StackPane.setAlignment(content, Pos.CENTER);

		playRoot.getChildren().add(fullOverlay);
	}

	private void displayWin() {
		AudioClip win = new AudioClip(getClass().getResource("/sounds/win.mp3")
				.toExternalForm());
		win.setVolume(1);
		mediaPlayer.setVolume(0.05);
		win.play();

		Monster winner = game.getWinner();
		Monster loser = (winner == game.getPlayer()) ? game.getOpponent()
				: game.getPlayer();

		Font fontTitle = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 64);
		Font fontSub = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 24);
		Font fontInfo = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 20);
		Font fontBtn = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 24);

		Rectangle dim = new Rectangle();
		dim.setFill(Color.rgb(0, 0, 0, 0.85));
		dim.widthProperty().bind(playRoot.widthProperty());
		dim.heightProperty().bind(playRoot.heightProperty());

		String stroke = "-fx-effect: dropshadow(gaussian, black, 2, 1.0, 0, 0);";

		Label titleLabel = new Label("YOU WIN!");
		titleLabel.setFont(fontTitle);
		titleLabel.setTextFill(Color.GOLD);
		titleLabel
				.setStyle("-fx-effect: dropshadow(three-pass-box, goldenrod, 20, 0.6, 0, 0);");

		Label winnerLabel = new Label(winner.getName().toUpperCase() + " WINS!");
		winnerLabel.setFont(fontSub);
		winnerLabel.setTextFill(Color.WHITE);
		winnerLabel.setStyle(stroke);

		Label roleLabel = new Label("Role: " + winner.getRole());
		roleLabel.setFont(fontInfo);
		roleLabel.setTextFill(winner.getRole() == Role.LAUGHER ? Color.LIME
				: Color.SKYBLUE);
		roleLabel.setStyle(stroke);

		Label winnerEnergyLabel = new Label(winner.getName()
				+ " Final Energy: " + winner.getEnergy());
		winnerEnergyLabel.setFont(fontInfo);
		winnerEnergyLabel.setTextFill(Color.GOLD);
		winnerEnergyLabel.setStyle(stroke);

		Label loserEnergyLabel = new Label(loser.getName() + " Final Energy: "
				+ loser.getEnergy());
		loserEnergyLabel.setFont(fontInfo);
		loserEnergyLabel.setTextFill(Color.LIGHTGRAY);
		loserEnergyLabel.setStyle(stroke);

		Button menuBtn = new Button("MAIN MENU");
		menuBtn.setFont(fontBtn);
		menuBtn.setTextFill(Color.WHITE);
		menuBtn.setBackground(null);
		menuBtn.setStyle("-fx-border-color: gold;" + "-fx-border-width: 2;"
				+ "-fx-border-radius: 6;" + "-fx-background-radius: 6;"
				+ "-fx-background-color: rgba(0,0,0,0.5);"
				+ "-fx-padding: 8 24 8 24;");
		menuBtn.setEffect(new DropShadow(10, 0, 0, Color.GOLDENROD));

		menuBtn.setOnMouseEntered(e -> menuBtn
				.setStyle("-fx-border-color: gold;" + "-fx-border-width: 2;"
						+ "-fx-border-radius: 6;" + "-fx-background-radius: 6;"
						+ "-fx-background-color: rgba(184,134,11,0.5);"
						+ "-fx-padding: 8 24 8 24;"));
		menuBtn.setOnMouseExited(e -> menuBtn
				.setStyle("-fx-border-color: gold;" + "-fx-border-width: 2;"
						+ "-fx-border-radius: 6;" + "-fx-background-radius: 6;"
						+ "-fx-background-color: rgba(0,0,0,0.5);"
						+ "-fx-padding: 8 24 8 24;"));
		menuBtn.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			playerOverlayView = null;
			opponentOverlayView = null;
			playerTagLabel = null;
			opponentTagLabel = null;
			previousPlayerPos = 0;
			previousOpponentPos = 0;
			insPage = 1;
			win.stop();
			mediaPlayer.setVolume(0.3);
			returnToMenu();
		});

		VBox content = new VBox(15, titleLabel, winnerLabel, roleLabel,
				winnerEnergyLabel, loserEnergyLabel, menuBtn);
		content.setAlignment(Pos.CENTER);

		StackPane fullOverlay = new StackPane(dim, content);
		fullOverlay.setPrefSize(1280, 720);
		StackPane.setAlignment(content, Pos.CENTER);

		playRoot.getChildren().add(fullOverlay);
	}

	public void updateTurn() {
		System.out.println("\nSTART NEW:");
		Monster player = game.getPlayer();
		boolean isPlayerTurn = (game.getCurrent() == player);
		System.out.println("Player's Position: "
				+ game.getPlayer().getPosition() + "\nPlayer's Energy: "
				+ game.getPlayer().getEnergy());
		System.out.println("\nOpponent's Position: "
				+ game.getOpponent().getPosition() + "\nOpponent's Energy: "
				+ game.getOpponent().getEnergy());
		if (isPlayerTurn) {
			playerPanel.setOpacity(1.0);
			playerPanel
					.setStyle("-fx-border-color: #FFBF00; -fx-border-width: 3; -fx-border-radius: 6; -fx-background-radius: 6;");
			playerPanel.setEffect(new DropShadow(10, -3, 3, Color.rgb(0, 0, 0,
					0.8)));

			oppPanel.setOpacity(0.4);
			oppPanel.setStyle("-fx-border-color: #555555; -fx-border-width: 3; -fx-border-radius: 6; -fx-background-radius: 6;");
			oppPanel.setEffect(null);
		} else {
			playerPanel.setOpacity(0.4);
			playerPanel
					.setStyle("-fx-border-color: #555555; -fx-border-width: 3; -fx-border-radius: 6; -fx-background-radius: 6;");
			playerPanel.setEffect(null);

			oppPanel.setOpacity(1.0);
			oppPanel.setStyle("-fx-border-color: #FFBF00; -fx-border-width: 3; -fx-border-radius: 6; -fx-background-radius: 6;");
			oppPanel.setEffect(new DropShadow(10, -3, 3, Color
					.rgb(0, 0, 0, 0.8)));
		}
	}

	public void updatePlayers() {

		int cols = Constants.BOARD_COLS;
		int rows = Constants.BOARD_ROWS;

		Monster player = game.getPlayer();
		Monster opponent = game.getOpponent();

		Monster[] players = { player, opponent };

		for (Monster m : players) {

			if (m == null)
				continue;

			boolean isPlayer = m == player;

			int startPos = isPlayer ? previousPlayerPos : previousOpponentPos;
			int endPos = m.getPosition();

			ImageView overlay = isPlayer ? playerOverlayView : opponentOverlayView;
			Label tag = isPlayer ? playerTagLabel : opponentTagLabel;

			double size = firstMove ? tileSize * 0.5 : tileSize * 0.8;
			
			if (overlay == null) {

				Image img = new Image(getClass().getResourceAsStream(
						"/monsters/" + m.getName().toLowerCase().replaceAll("[ .]", "") + ".png"));

				overlay = new ImageView(img);
				overlay.setFitWidth(size);
				overlay.setFitHeight(size);
				overlay.setPreserveRatio(true);
				overlay.setMouseTransparent(true);

				int r = endPos / cols;
				int c = endPos % cols;
				int gridRow = (rows - 1) - r;
				int gridCol = (r % 2 == 0) ? c : (cols - 1) - c;

				StackPane startCell = null;

				for (Node node : currentGrid.getChildren()) {
					StackPane cellStack = (StackPane) node;
					Integer nodeCol = GridPane.getColumnIndex(cellStack);
					Integer nodeRow = GridPane.getRowIndex(cellStack);
					if (nodeCol == null || nodeRow == null) continue;
					if (nodeCol == gridCol && nodeRow == gridRow) {
						startCell = cellStack;
						break;
					}
				}

				if (startCell != null) {
					Bounds cellBounds = startCell.localToScene(startCell.getBoundsInLocal());
					Bounds paneBounds = overlayPane.localToScene(overlayPane.getBoundsInLocal());

					double startX = cellBounds.getMinX() - paneBounds.getMinX();
					double startY = cellBounds.getMinY() - paneBounds.getMinY();
					
					if (endPos == 0 && firstMove) {
						if (isPlayer) {
							startX += tileSize * 0.05;
							startY += tileSize * 0.05;
						}
						else {
							startX += tileSize * 0.5;
							startY += tileSize * 0.5;
						}
					}

					overlay.setLayoutX(startX);
					overlay.setLayoutY(startY);
				}

				overlayPane.getChildren().add(overlay);

				tag = new Label(isPlayer ? "PLAYER" : "OPPONENT");
				int s = isPlayer ? 15 : 12;
				Font font = Font.loadFont(getClass().getResourceAsStream("/AutumnVoyage-Regular.ttf"), s);
				tag.setStyle("-fx-font-weight: bold; -fx-text-fill: white;"
						+ "-fx-effect: dropshadow(gaussian, black, 5, 0.7, 0, 0);");
				tag.setFont(font);
				tag.setMouseTransparent(true);

				overlayPane.getChildren().add(tag);

				if (startCell != null) {
					Bounds cellBounds = startCell.localToScene(startCell.getBoundsInLocal());
					Bounds paneBounds = overlayPane.localToScene(overlayPane.getBoundsInLocal());

					double startX = cellBounds.getMinX() - paneBounds.getMinX();
					double startY = cellBounds.getMinY() - paneBounds.getMinY();

					if (endPos == 0 && firstMove && !isPlayer) {
						startX += tileSize * 0.5;
						startY += tileSize * 0.5;
					}

					tag.setLayoutX(startX);
					tag.setLayoutY(startY + tileSize * 0.75);
				}

				if (isPlayer) {
					playerOverlayView = overlay;
					playerTagLabel = tag;
				} else {
					opponentOverlayView = overlay;
					opponentTagLabel = tag;
				}
			}

			overlay.setFitWidth(size);
			overlay.setFitHeight(size);

			ArrayList<Integer> path = new ArrayList<>();

			if (endPos > startPos) {
				for (int i = startPos + 1; i <= endPos; i++) {
					path.add(i);
				}
			} else {
				path.add(endPos);
			}

			SequentialTransition sequence = new SequentialTransition();
			SequentialTransition labelSequence = new SequentialTransition();

			for (int boardIndex : path) {

				int r = boardIndex / cols;
				int c = boardIndex % cols;
				int gridRow = (rows - 1) - r;
				int gridCol = (r % 2 == 0) ? c : (cols - 1) - c;

				StackPane targetCell = null;

				for (Node node : currentGrid.getChildren()) {
					StackPane cellStack = (StackPane) node;
					Integer nodeCol = GridPane.getColumnIndex(cellStack);
					Integer nodeRow = GridPane.getRowIndex(cellStack);
					if (nodeCol == null || nodeRow == null) continue;
					if (nodeCol == gridCol && nodeRow == gridRow) {
						targetCell = cellStack;
						break;
					}
				}

				if (targetCell == null) continue;

				Bounds cellBounds = targetCell.localToScene(targetCell.getBoundsInLocal());
				Bounds paneBounds = overlayPane.localToScene(overlayPane.getBoundsInLocal());

				double targetX;
				double targetY;

				if (firstMove && isPlayer) {
					targetX = (cellBounds.getMinX() - paneBounds.getMinX()) + (tileSize * 0.05);
					targetY = (cellBounds.getMinY() - paneBounds.getMinY()) + (tileSize * 0.05);
				} else if (firstMove && !isPlayer) {
					// Keeps the opponent clear in the bottom right quadrant
					targetX = (cellBounds.getMinX() - paneBounds.getMinX()) + (tileSize * 0.45);
					targetY = (cellBounds.getMinY() - paneBounds.getMinY()) + (tileSize * 0.45);
				} else {
					targetX = (cellBounds.getMinX() - paneBounds.getMinX()) + (tileSize - overlay.getFitWidth()) / 2;
					targetY = (cellBounds.getMinY() - paneBounds.getMinY()) + (tileSize - overlay.getFitHeight()) / 2;
				}

				Timeline move = new Timeline(new KeyFrame(Duration.millis(220),
						new KeyValue(overlay.layoutXProperty(), targetX, Interpolator.EASE_BOTH),
						new KeyValue(overlay.layoutYProperty(), targetY, Interpolator.EASE_BOTH)));

				sequence.getChildren().add(move);

				double labelYOffset = (firstMove && isPlayer) ? size * 0.85 : tileSize * 0.75;
				if (firstMove && !isPlayer) {
					labelYOffset = size * 0.85; 
				}

				Timeline labelMove = new Timeline(new KeyFrame(Duration.millis(220),
						new KeyValue(tag.layoutXProperty(), targetX, Interpolator.EASE_BOTH),
						new KeyValue(tag.layoutYProperty(), targetY + labelYOffset, Interpolator.EASE_BOTH)));

				labelSequence.getChildren().add(labelMove);
			}

			final ImageView finalOverlay = overlay;
			sequence.setOnFinished(ev -> {
				finalOverlay.setFitWidth(tileSize * 0.8);
				finalOverlay.setFitHeight(tileSize * 0.8);
			});

			sequence.play();
			labelSequence.play();

			if (isPlayer)
				previousPlayerPos = endPos;
			else {
				previousOpponentPos = endPos;
				firstMove = false;
			}
		}
	}
	
	public void displayOptions() {
		Image optionsScreen = new Image(getClass().getResourceAsStream(
				"/images/optionsScreen.png"));
		Image check = new Image(getClass().getResourceAsStream(
				"/images/checkmark.png"));
		ImageView optionsView = new ImageView(optionsScreen);
		ImageView musicView = new ImageView(check);
		ImageView fullView = new ImageView(check);

		Rectangle dim = new Rectangle(1280, 720);
		dim.setFill(Color.rgb(0, 0, 0, 0.7));
		dim.widthProperty().bind(menuRoot.widthProperty());
		dim.heightProperty().bind(menuRoot.heightProperty());

		Pane popupUI = new Pane(optionsView, musicView, fullView);
		popupUI.setMaxSize(800, 500);

		Button musicBtn = new Button("music");
		musicBtn.setPrefSize(32, 32);
		musicBtn.setLayoutX(31);
		musicBtn.setLayoutY(113.5);
		musicBtn.setOpacity(0);

		Button fullBtn = new Button("full");
		fullBtn.setPrefSize(32, 32);
		fullBtn.setLayoutX(31);
		fullBtn.setLayoutY(205.5);
		fullBtn.setOpacity(0);

		Button closeBtn = new Button("close");
		closeBtn.setPrefSize(105, 53);
		closeBtn.setLayoutX(687);
		closeBtn.setLayoutY(7);
		closeBtn.setOpacity(0);

		musicView.setLayoutX(28);
		musicView.setLayoutY(102);
		fullView.setLayoutX(28);
		fullView.setLayoutY(195);

		musicView
				.setVisible(mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING);
		fullView.setVisible(primaryStage.isFullScreen());

		popupUI.getChildren().addAll(musicBtn, fullBtn, closeBtn);

		StackPane fullOverlay = new StackPane(dim, popupUI);
		fullOverlay.setPrefSize(1280, 720);
		StackPane.setAlignment(popupUI, Pos.CENTER);

		closeBtn.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			menuRoot.getChildren().remove(fullOverlay);
		});

		musicBtn.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			if (musicView.isVisible()) {
				musicView.setVisible(false);
				mediaPlayer.stop();
			} else {
				musicView.setVisible(true);
				mediaPlayer.play();
			}
		});

		fullBtn.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			if (primaryStage.isFullScreen()) {
				fullView.setVisible(false);
				primaryStage.setFullScreen(false);
			} else {
				fullView.setVisible(true);
				primaryStage.setFullScreen(true);
			}

		});

		closeBtn.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			menuRoot.getChildren().remove(fullOverlay);
		});

		optionsView.setVisible(true);

		menuRoot.getChildren().add(fullOverlay);
	}

	public void displayError(StackPane root, String errorMsg) {
		AudioClip err = new AudioClip(getClass().getResource(
				"/sounds/error.mp3").toExternalForm());
		err.setVolume(0.4);
		err.play();
		Rectangle dim = new Rectangle(1280, 720);
		dim.setFill(Color.rgb(0, 0, 0, 0.7));
		dim.widthProperty().bind(root.widthProperty());
		dim.heightProperty().bind(root.heightProperty());

		ImageView background = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/errorbg.png")));
		ImageView okHovered = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/okHovered.png")));

		okHovered.setVisible(false);

		Font font = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 20);

		Label msg = new Label(errorMsg);

		msg.setFont(font);
		msg.setWrapText(true);
		msg.setMaxWidth(430);
		msg.setLayoutY(108);
		msg.setLayoutX(30);
		msg.setTextFill(Color.WHITE);

		Button okBtn = new Button("ok");
		okBtn.setAlignment(Pos.CENTER);
		okBtn.setPrefSize(112, 64);
		okBtn.setLayoutY(163);
		okBtn.setLayoutX(184);
		okBtn.setTextFill(Color.WHITE);
		okBtn.setOpacity(0);

		Pane popupUI = new Pane(background, okHovered, msg, okBtn);

		playHoverEffect(okBtn, okHovered);

		popupUI.setMaxSize(480, 240);

		StackPane fullOverlay = new StackPane(dim, popupUI);
		fullOverlay.setPrefSize(1280, 720);
		StackPane.setAlignment(popupUI, Pos.CENTER);

		root.getChildren().add(fullOverlay);

		okBtn.setOnAction(e -> {
			root.getChildren().remove(fullOverlay);
		});

	}

	public void showInstructions() {
		Rectangle dim = new Rectangle(1280, 720);
		dim.setFill(Color.rgb(0, 0, 0, 0.7));
		dim.widthProperty().bind(playRoot.widthProperty());
		dim.heightProperty().bind(playRoot.heightProperty());

		ImageView instructionsScreen = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/instructionsScreen.png")));
		ImageView nextHovered = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/nextHovered.png")));
		ImageView xHovered = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/xHovered.png")));

		ImageView text1 = new ImageView(new Image(getClass()
				.getResourceAsStream("/instructions/text1.png")));
		ImageView text2 = new ImageView(new Image(getClass()
				.getResourceAsStream("/instructions/text2.png")));
		ImageView text3 = new ImageView(new Image(getClass()
				.getResourceAsStream("/instructions/text3.png")));
		ImageView text4 = new ImageView(new Image(getClass()
				.getResourceAsStream("/instructions/text4.png")));
		ImageView text5 = new ImageView(new Image(getClass()
				.getResourceAsStream("/instructions/text5.png")));
		ImageView text6 = new ImageView(new Image(getClass()
				.getResourceAsStream("/instructions/text6.png")));
		ImageView text7 = new ImageView(new Image(getClass()
				.getResourceAsStream("/instructions/text7.png")));
		ImageView text8 = new ImageView(new Image(getClass()
				.getResourceAsStream("/instructions/text8.png")));
		ImageView text9 = new ImageView(new Image(getClass()
				.getResourceAsStream("/instructions/text9.png")));

		text2.setVisible(false);
		text3.setVisible(false);
		text4.setVisible(false);
		text5.setVisible(false);
		text6.setVisible(false);
		text7.setVisible(false);
		text8.setVisible(false);
		text9.setVisible(false);

		Pane popupUI = new Pane();
		popupUI.setMaxSize(500, 650);

		nextHovered.setVisible(false);
		xHovered.setVisible(false);

		Font font = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 38);
		Font fontS = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 20);

		Label next = new Label("NEXT");
		next.setFont(font);
		next.setLayoutX(207);
		next.setLayoutY(586);
		next.setTextFill(Color.WHITE);
		next.setMouseTransparent(true);

		Label page = new Label("1/9");
		page.setFont(fontS);
		page.setLayoutX(420);
		page.setLayoutY(600);
		page.setTextFill(Color.WHITE);
		page.setMouseTransparent(true);

		Button nextButton = new Button("next");
		nextButton.setPrefSize(139, 67);
		nextButton.setLayoutX(180);
		nextButton.setLayoutY(577);
		nextButton.setOpacity(0);

		Button xButton = new Button("x");
		xButton.setPrefSize(31, 31);
		xButton.setLayoutX(455);
		xButton.setLayoutY(16);
		xButton.setOpacity(0);

		popupUI.getChildren().addAll(instructionsScreen, nextHovered,
				nextButton, xHovered, xButton, text1, text2, text3, text4,
				text5, text6, text7, text8, text9, next, page);

		StackPane fullOverlay = new StackPane(dim, popupUI);

		playRoot.getChildren().addAll(fullOverlay);

		playHoverEffect(nextButton, nextHovered);
		playHoverEffect(xButton, xHovered);

		xButton.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			playRoot.getChildren().remove(fullOverlay);
			insPage = 1;
			selectRole();
		});

		nextButton.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			insPage++;

			if (insPage == 1) {
				text8.setVisible(false);
				text1.setVisible(true);
				page.setText("1/9");
			} else if (insPage == 2) {
				text1.setVisible(false);
				text2.setVisible(true);
				page.setText("2/9");
			} else if (insPage == 3) {
				text2.setVisible(false);
				text3.setVisible(true);
				page.setText("3/9");
			} else if (insPage == 4) {
				text3.setVisible(false);
				text4.setVisible(true);
				page.setText("4/9");
			} else if (insPage == 5) {
				text4.setVisible(false);
				text5.setVisible(true);
				page.setText("5/9");
			} else if (insPage == 6) {
				text5.setVisible(false);
				text6.setVisible(true);
				page.setText("6/9");
			} else if (insPage == 7) {
				text6.setVisible(false);
				text7.setVisible(true);
				page.setText("7/9");
			} else if (insPage == 8) {
				text7.setVisible(false);
				text8.setVisible(true);
				page.setText("8/9");
			} else if (insPage == 9) {
				text8.setVisible(false);
				text9.setVisible(true);
				page.setText("9/9");
				next.setText("DONE");
			} else if (insPage == 10) {
				insPage = 1;
				playRoot.getChildren().remove(fullOverlay);
				selectRole();
			}
		});

	}

	public void selectRole() {
		ImageView selectScreen = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/selectScreen.png")));
		ImageView laugherButton = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/laugherButton.png")));
		ImageView scarerButton = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/scarerButton.png")));
		ImageView confirmLaugher = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/confirmLaugher.png")));
		ImageView confirmScarer = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/confirmScarer.png")));
		ImageView confirmNeutral = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/confirmNeutral.png")));

		confirmLaugher.setVisible(false);
		confirmScarer.setVisible(false);
		confirmNeutral.setOpacity(0.5);

		Rectangle dim = new Rectangle(1280, 720);
		dim.setFill(Color.rgb(0, 0, 0, 0.7));
		dim.widthProperty().bind(playRoot.widthProperty());
		dim.heightProperty().bind(playRoot.heightProperty());

		Pane popupUI = new Pane(selectScreen, laugherButton, scarerButton,
				confirmNeutral, confirmLaugher, confirmScarer);
		popupUI.setMaxSize(470, 350);

		StackPane fullOverlay = new StackPane(dim, popupUI);
		fullOverlay.setPrefSize(1280, 720);
		StackPane.setAlignment(popupUI, Pos.CENTER);

		playRoot.getChildren().add(fullOverlay);

		laugherButton.setOnMouseEntered(e -> {
			if (role != Role.LAUGHER) {
				slide(laugherButton, -20);
			}
		});

		laugherButton.setOnMouseExited(e -> {
			if (role != Role.LAUGHER) {
				slide(laugherButton, 0);
			}
		});

		scarerButton.setOnMouseEntered(e -> {
			if (role != Role.SCARER) {
				slide(scarerButton, -20);
			}
		});

		scarerButton.setOnMouseExited(e -> {
			if (role != Role.SCARER) {
				slide(scarerButton, 0);
			}
		});

		laugherButton.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			role = Role.LAUGHER;
			confirmScarer.setVisible(false);
			confirmNeutral.setVisible(false);
			confirmLaugher.setVisible(true);
			slide(laugherButton, -20);
			slide(scarerButton, 0);
		});

		scarerButton.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			role = Role.SCARER;
			confirmScarer.setVisible(true);
			confirmNeutral.setVisible(false);
			confirmLaugher.setVisible(false);
			slide(scarerButton, -20);
			slide(laugherButton, 0);
		});

		EventHandler<MouseEvent> showConfirm = f -> {
			clickSoundEffect.play();
			playRoot.getChildren().remove(fullOverlay);
			System.out.println("selected: " + this.role);
			playGame();
		};
		confirmLaugher.setOnMouseClicked(showConfirm);
		confirmScarer.setOnMouseClicked(showConfirm);

	}

	public void showRandomMonsters(Runnable onComplete) {
		Rectangle dim = new Rectangle(1280, 720);
		dim.setFill(Color.rgb(0, 0, 0, 0.7));
		dim.widthProperty().bind(playRoot.widthProperty());
		dim.heightProperty().bind(playRoot.heightProperty());

		ImageView background = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/flickerbg.png")));
		ImageView nextHovered = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/nextHovered2.png")));
		ImageView nextNotHovered = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/nextNotHovered.png")));
		Font font = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 38);

		nextNotHovered.setOpacity(1);
		nextHovered.setVisible(false);

		Button nextDone = new Button("NEXT");
		nextDone.setFont(font);
		nextDone.setAlignment(Pos.CENTER);
		nextDone.setLayoutX(165);
		nextDone.setLayoutY(260);
		nextDone.setTextFill(Color.WHITE);
		nextDone.setBackground(null);

		nextNotHovered.setMouseTransparent(true);

		Pane popupUI = new Pane(background, nextHovered, nextNotHovered,
				nextDone);

		playHoverEffect(nextDone, nextHovered);

		popupUI.setMaxSize(470, 350);

		StackPane fullOverlay = new StackPane(dim, popupUI);
		fullOverlay.setPrefSize(1280, 720);
		StackPane.setAlignment(popupUI, Pos.CENTER);

		startMonsterRandomizer(fullOverlay, game.getPlayer().getRole());

		playRoot.getChildren().add(fullOverlay);

		nextDone.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			Role oppRole = game.getOpponent().getRole();
			startMonsterRandomizer(fullOverlay, oppRole);
			nextDone.setText("START");
			nextDone.setLayoutX(157);
			playRoot.getChildren().remove(fullOverlay);
			playRoot.getChildren().add(fullOverlay);
			nextDone.setOnMouseClicked(f -> {
				clickSoundEffect.play();
				playRoot.getChildren().remove(fullOverlay);
				if (onComplete != null) {
					onComplete.run();
				}
				updateTurn();
			});

		});

	}

	public void askPowerUp() {
		if (game.getCurrent().isFrozen()) {
			displayFrozen();
			rollBtn.setDisable(true);
			endTurnBtn.setVisible(true);
			endTurnBtn.setOnMouseClicked(f -> {
				clickSoundEffect.play();
				endTurnBtn.setVisible(false);
				rollBtn.setDisable(false);
				try {
					game.playTurn();
				} catch (InvalidMoveException e) {
				}
				updateTurn();
				askPowerUp();
			});
			return;
		}
		ImageView background = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/powerupbackground.png")));
		ImageView yes = new ImageView(new Image(getClass().getResourceAsStream(
				"/images/use.png")));
		ImageView yesHovered = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/useHovered.png")));
		ImageView no = new ImageView(new Image(getClass().getResourceAsStream(
				"/images/dontUse.png")));
		ImageView noHovered = new ImageView(new Image(getClass()
				.getResourceAsStream("/images/dontUseHovered.png")));
		AudioClip effect = new AudioClip(getClass().getResource(
				"/sounds/powerUpEffect.mp3").toExternalForm());
		yesHovered.setVisible(false);
		noHovered.setVisible(false);

		Button yesBtn = new Button("yes");
		yesBtn.setAlignment(Pos.CENTER);
		yesBtn.setPrefSize(207, 82);
		yesBtn.setLayoutY(198);
		yesBtn.setLayoutX(16);
		yesBtn.setTextFill(Color.WHITE);
		yesBtn.setOpacity(0);

		Button noBtn = new Button("no");
		noBtn.setAlignment(Pos.CENTER);
		noBtn.setPrefSize(207, 82);
		noBtn.setLayoutY(198);
		noBtn.setLayoutX(247);
		noBtn.setTextFill(Color.WHITE);
		noBtn.setOpacity(0);

		Label costLabel = new Label("(COST: " + Constants.POWERUP_COST
				+ " ENERGY)");
		Font font = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 24);
		costLabel.setFont(font);
		costLabel.setAlignment(Pos.CENTER);
		costLabel.setLayoutX(125);
		costLabel.setLayoutY(130);
		costLabel.setTextFill(Color.GOLD);

		playHoverEffect(yesBtn, yesHovered);
		playHoverEffect(noBtn, noHovered);

		Pane popupUI = new Pane(background, yesHovered, noHovered, no, yes,
				yesBtn, noBtn, costLabel);

		popupUI.setMaxSize(470, 350);

		StackPane fullOverlay = new StackPane(popupUI);
		fullOverlay.setPrefSize(1280, 720);
		StackPane.setAlignment(popupUI, Pos.CENTER);

		playRoot.getChildren().add(fullOverlay);

		yesBtn.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			try {
				int oldEnergy = game.getCurrent().getEnergy();
				game.usePowerup();
				String title = game.getCurrent().getName()
						+ " ACTIVATED THEIR POWER UP!";
				displayPowerUp(game.getCurrent(), title, oldEnergy, game
						.getCurrent().getEnergy());

				updateGrid();
				effect.play();
				effect.setVolume(0.4);

				rollBtn.setVisible(true);

				playRoot.getChildren().remove(fullOverlay);

			} catch (OutOfEnergyException out) {
				displayError(playRoot, out.getMessage());
			}
		});
		noBtn.setOnMouseClicked(e -> {
			clickSoundEffect.play();
			playRoot.getChildren().remove(fullOverlay);
			rollBtn.setVisible(true);
		});
	}

	public void startTurn(Monster turn, Monster opp) {

		Platform.runLater(() -> updatePlayers());

		System.out.println("Player's Position: "
				+ game.getPlayer().getPosition() + "\nOpponent's Position: "
				+ game.getOpponent().getPosition());

		askPowerUp();

		playRoot.getChildren().add(rollBtn);
	}

	private void roll() throws InvalidMoveException {
		try {
			AudioClip effect = new AudioClip(getClass().getResource(
					"/sounds/diceRoll.mp3").toExternalForm());
			effect.play();
			rollBtn.setDisable(true);

			Monster current = game.getCurrent();
			final Monster movingMonster = current;

			if (current.isFrozen()) {
				game.playTurn();
				displayFrozen();
				endTurnBtn.setVisible(true);
				endTurnBtn.setOnMouseClicked(f -> {
					clickSoundEffect.play();
					endTurnBtn.setVisible(false);
					rollBtn.setDisable(false);
					updateTurn();
					askPowerUp();
				});
				return;
			}

			if (current == game.getPlayer()) {
				previousPlayerPos = current.getPosition();
			} else {
				previousOpponentPos = current.getPosition();
			}
			
			int oldEng = current.getEnergy();
			int oldPos = current.getPosition();
			
			game.playTurn();
			int diceResult = game.getRoll();

			int newPos = current.getPosition();
			int cols = Constants.BOARD_COLS;
			int placeRow = newPos / cols;
			int placeCol = newPos % cols;
			if (placeRow % 2 == 1) placeCol = cols - 1 - placeCol;
			Cell landedCell = game.getBoard().getBoardCells()[placeRow][placeCol];
//			final Card drawnCard = (landedCell instanceof CardCell) ? topCard : null;
			final Monster cellMonster = (landedCell instanceof MonsterCell) ? ((MonsterCell) landedCell).getCellMonster() : null;
			final int finalOldEng = oldEng;
			final Cell finalLandedCell = landedCell;
			
			int computedDistance = diceResult;
			if (current instanceof MultiTasker && ((MultiTasker) current).getNormalSpeedTurns() <= 0) {
				computedDistance = diceResult / 2;
			} else if (current instanceof Dasher) {
				if (((Dasher) current).getMomentumTurns() > 0)
					computedDistance = diceResult * 3;
				else
					computedDistance = diceResult * 2;
			}
			
			final boolean zeroMovement = (computedDistance == 0);

			int onTransport = oldPos + computedDistance;
			onTransport %= Constants.BOARD_SIZE;

			if (game.getWinner() != null) {
				if (game.getWinner() == game.getPlayer())
					displayWin();
				else
					displayLoss();
			}

			final int transportIndex = onTransport;
			final Cell cellOnTransport = getCellAtBoardIndex(onTransport);
			
			startDiceRandomizer(diceOn, diceResult, () -> {
				updateGrid();
				
				
				if (zeroMovement) {
					
				} else if (cellOnTransport instanceof ContaminationSock) {
					displaySock(current, transportIndex, current.getPosition());
				} else if (cellOnTransport instanceof ConveyorBelt) {
					displayBelt(current, transportIndex, current.getPosition());
				} else if (finalLandedCell instanceof CardCell) {
					Platform.runLater(() -> {
				        displayCard(Board.usedCard);
				    });
				} else if (finalLandedCell instanceof MonsterCell) {
					if (cellMonster.getRole() == movingMonster.getRole()) {
						String title = movingMonster.getName() + " encountered an ally!";
						displayPowerUp(current, title, finalOldEng, movingMonster.getEnergy());
					} else {
						displaySwapEnergy(current, cellMonster);
					}
				} else if (finalLandedCell instanceof DoorCell && !((DoorCell) finalLandedCell).isOpened()) {
					((DoorCell) finalLandedCell).setOpened(true);
					displayDoorEffect(current, (DoorCell) finalLandedCell);
				} else if (cellOnTransport instanceof CardCell) {
				    if (Board.usedCard != null) {
				        displayCard(Board.usedCard);
				        Board.usedCard = null; // clear after display
				    }
				}

				if (game.getPlayer().isShieldBroke()) {
					displayShield(game.getPlayer());
				}
				if (game.getOpponent().isShieldBroke()) {
					displayShield(game.getOpponent());
				}

				endTurnBtn.setVisible(true);
				endTurnBtn.setOnMouseClicked(f -> {
					clickSoundEffect.play();
					endTurnBtn.setVisible(false);
					rollBtn.setDisable(false);
					updateTurn();
					askPowerUp();
				});
			});

		} catch (InvalidMoveException b) {
			if (game.getCurrent().isFrozen()) {
				displayFrozen();
			} else {
				displayError(playRoot, b.getMessage() + " Your roll makes you land on your opponent. Try rolling again.");
				rollBtn.setDisable(false);
			}
			System.out.println(b.getMessage());
		}
	}
	
	private void displayShield(Monster m) {
		AudioClip sound = new AudioClip(getClass().getResource(
				"/sounds/shield.mp3").toExternalForm());
		sound.setVolume(1);
		sound.play();

		Font font = Font.loadFont(getClass().getResourceAsStream("/AutumnVoyage-Regular.ttf"), 40);
		Font fontMini = Font.loadFont(getClass().getResourceAsStream("/AutumnVoyage-Regular.ttf"), 22);

		Rectangle dim = new Rectangle(1280, 720);
		dim.setFill(Color.rgb(0, 0, 0, 0.75));
		dim.widthProperty().bind(playRoot.widthProperty());
		dim.heightProperty().bind(playRoot.heightProperty());

		Label shieldBroke = new Label("SHIELD ACTIVATED");
		Label mini = new Label(m.getName().toUpperCase() + " BLOCKED THE EFFECT!");

		shieldBroke.setFont(font);
		mini.setFont(fontMini);
		shieldBroke.setTextFill(Color.GOLD);
		mini.setTextFill(Color.LIGHTGRAY);

		String stroke = "-fx-effect: dropshadow(gaussian, black, 4, 0.8, 0, 0);";
		shieldBroke.setStyle(stroke);
		mini.setStyle(stroke);

		VBox content = new VBox(12, shieldBroke, mini);
		content.setAlignment(Pos.CENTER);
		content.setPadding(new Insets(30, 50, 30, 50));

		StackPane alertPanel = new StackPane(content);
		alertPanel.setStyle("-fx-background-color: rgba(15, 25, 35, 0.95);"
				+ "-fx-background-radius: 12;"
				+ "-fx-border-color: cyan;"
				+ "-fx-border-width: 2;"
				+ "-fx-border-radius: 12;"
				+ "-fx-effect: dropshadow(three-pass-box, cyan, 15, 0.3, 0, 0);");
		alertPanel.setMaxSize(500, Region.USE_PREF_SIZE);

		StackPane layoutLayer = new StackPane(dim, alertPanel);
		layoutLayer.setAlignment(Pos.CENTER);

		playRoot.getChildren().add(layoutLayer);

		PauseTransition delay = new PauseTransition(Duration.seconds(2.5));
		delay.setOnFinished(event -> playRoot.getChildren().remove(layoutLayer));
		delay.play();
		
		m.setShieldBroke(false);
	}

	
	private void displaySwapEnergy(Monster current, Monster cellMonster) {
		Font fontTitle = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 28);
		Font fontDesc = Font
				.loadFont(
						getClass().getResourceAsStream(
								"/AutumnVoyage-Regular.ttf"), 18);

		Label titleLabel = new Label("LANDED ON ENEMY MONSTER");
		Label descLabel = new Label();
		if(current.getEnergy()<cellMonster.getEnergy())
			descLabel.setText("SWAPPED ENERGY SINCE STATIONED MONSTER HAD LESS ENERGY");
		else
			descLabel.setText("NO EFFECT HAPPENED STATIONED MONSTER HAD MORE ENERGY");
		titleLabel.setFont(fontTitle);
		titleLabel.setTextFill(Color.WHITE);
		titleLabel.setWrapText(true);
		titleLabel.setMaxWidth(420);
		titleLabel.setAlignment(Pos.CENTER);
		titleLabel.setTextAlignment(TextAlignment.CENTER);

		descLabel.setFont(fontDesc);
		descLabel.setTextFill(Color.WHITE);
		descLabel.setWrapText(true);
		descLabel.setMaxWidth(420);
		descLabel.setAlignment(Pos.CENTER);
		descLabel.setTextAlignment(TextAlignment.CENTER);

		String stroke = "-fx-effect: dropshadow(gaussian, black, 2, 1.0, 0, 0);";
		titleLabel.setStyle(stroke);
		descLabel.setStyle(stroke);

		Label dismissLabel = new Label("Click anywhere to close");
		dismissLabel.setFont(fontDesc);
		dismissLabel.setTextFill(Color.DARKGRAY);
		dismissLabel.setStyle(stroke);

		VBox content = new VBox(14, titleLabel, descLabel, dismissLabel);
		content.setAlignment(Pos.CENTER);
		content.setPadding(new Insets(24));

		StackPane cardPanel = new StackPane(content);
		cardPanel.setStyle("-fx-background-color: rgba(20,20,20,0.95);"
				+ "-fx-background-radius: 15; -fx-border-color: lime;"
				+ "-fx-border-width: 3; -fx-border-radius: 15;");
		cardPanel.setMaxSize(460, Region.USE_PREF_SIZE);

		Rectangle dim = new Rectangle();
		dim.setFill(Color.rgb(0, 0, 0, 0.7));
		dim.widthProperty().bind(playRoot.widthProperty());
		dim.heightProperty().bind(playRoot.heightProperty());

		StackPane fullOverlay = new StackPane(dim, cardPanel);
		fullOverlay.setPrefSize(1280, 720);
		StackPane.setAlignment(cardPanel, Pos.CENTER);

		fullOverlay.setOnMouseClicked(ev -> {
			clickSoundEffect.play();
			playRoot.getChildren().remove(fullOverlay);
		});

		playRoot.getChildren().add(fullOverlay);
	}

	
	public void playGame() {

		try {
			System.out.println("game running");
			game = new Game(role);

			System.out.println("Selected monster: "
					+ game.getPlayer().getName() + "\nOpponent Monster: "
					+ game.getOpponent().getName()
					+ "\nPlayer's starting energy: "
					+ game.getPlayer().getEnergy()
					+ "\nOpponent's starting energy: "
					+ game.getOpponent().getEnergy());
			game.getPlayer().setPosition(0);
			game.getOpponent().setPosition(0);
			showRandomMonsters(() -> {
				firstMove = true;
				createGrid();

				startTurn(game.getPlayer(), game.getOpponent());
			});
		} catch (IOException e) {
			System.err.println("Failed to load Data: " + e.getMessage());
			displayAlertReturn("Error",
					"Couldn't load the monster files or game board.");
		}
	}
}
