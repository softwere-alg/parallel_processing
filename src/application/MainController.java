package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

import application.subWindow.SubWindow;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class MainController {
	final static private int SQUAREWIDTH = 30;
	final static private int SQUAREHEIGHT = 22;
	final static public int LABELWIDTH = 20;
	final static public int LABELHEIGHT = 20;
	final static private int CARRADIUS = 8;
	final static private int LIGHTWIDTH = 3;
	final static public int PADDINGX = 20;
	final static public int PADDINGY = 20;
	final static public int BLOCKSIZE3D = 50;
	final static private boolean COLORCHANGE = true;

	@FXML
	private Button startButton;

	@FXML
	private Button stopButton;

	@FXML
	private Button resetButton;

	@FXML
	private CheckBox mutexCheckBox;

	@FXML
	private Pane display;

	@FXML
	private ToggleButton road1;

	@FXML
	private ToggleButton road2;

	@FXML
	private ToggleButton road3;

	@FXML
	private ToggleButton road4;

	@FXML
	private ToggleButton road5;

	@FXML
	private ToggleButton road6;

	@FXML
	private ToggleButton car;

	@FXML
	private ToggleGroup parts;

	@FXML
	private ColorPicker colorPicker;

	@FXML
	private TextField inputVelocity;

	@FXML
	private VBox property;

	@FXML
	private ColorPicker carPropertyColor;

	@FXML
	private TextField carPropertyVelocity;

	@FXML
	private Button updateButton;

	@FXML
	private Button deleteButton;

	@FXML
	private Button saveButton;

	@FXML
	private Button openButton;

	@FXML
	private VBox partsBox;

	@FXML
	private ToggleButton light;

	@FXML
	private VBox lightProperty;

	@FXML
	private VBox carProperty;

	@FXML
	private TextField inputBlueTime;

	@FXML
	private TextField inputRedTime;

	@FXML
	private TextField lightPropertyBlue;

	@FXML
	private TextField lightPropertyRed;

	@FXML
	private BorderPane root;

	@FXML
	private Slider speedBar;

	// 動かす車をまとめて管理するためのリスト
	private List<Driver> drivers = new ArrayList<>();
	private List<Circle> cars = new ArrayList<>();
	private List<Integer> carsVelocity = new ArrayList<>();

	// 選択された道路の画像イメージ
	private Image img;

	// 設置する車の色を確認するためのサンプル
	private Circle carSample;

	// マス目を管理するリスト
	private List<Label> label = new ArrayList<>();

	// クリックした車を保存する
	private Circle activeCar;

	// クリックした信号を保存する
	private Line activeLight;

	// 2Dの車を表示するグループ
	private Group group;

	// シミュレーション中かどうか
	private boolean playing = false;

	// ドラッグ中に必要となるダミー
	private Circle dummy;

	// 信号を管理するためリスト
	private List<Line> lights = new ArrayList<>();
	private List<int[]> lightsTime = new ArrayList<>();
	private List<Light> lightGroup = new ArrayList<>();

	// サブウィンドウのステージ
	private Stage stage;

	// 2D上で3Dの位置と方向を確認するための図形
	private Circle camera2D;
	private Polygon cameraView;

	// サブウィンドウにアクセルするためのコントローラー
	private SubWindow controller;

	/**
	 * getLabelImageNameメソッド
	 * ラベルに設定してある画像名を取得する
	 * @param label 対象となるラベル
	 */
	private String getLabelImageName(Label label) {
		// これが成り立つときにこのメソッドを呼び出してはいけない
		if (((ImageView)label.getGraphic()).getImage() == null) {
			throw new IllegalArgumentException();
		}
		String path = ((ImageView)label.getGraphic()).getImage().getUrl();
		String[] splitPath = path.split("/");
		String fileName = splitPath[splitPath.length - 1];
		return fileName;
	}

	/**
	 * setLightDirectionメソッド
	 * 信号の方向を調整する
	 * @param label 信号が置いてあるマスのラベル
	 * @param light 方向を調整する信号
	 */
	private void setLightDirection(Label label, Line light) {
		// ラベルに設定してあるファイル名取得
		String fileName = getLabelImageName(label);

		double stX;
		double stY;
		double endX;
		double endY;
		switch (fileName) {
		case "road1.png":
			stX = label.getTranslateX() + LABELWIDTH/2;
			stY = label.getTranslateY();
			endX = label.getTranslateX() + LABELWIDTH/2;
			endY = label.getTranslateY() + LABELHEIGHT;
			break;
		case "road2.png":
			stX = label.getTranslateX();
			stY = label.getTranslateY() + LABELHEIGHT/2;
			endX = label.getTranslateX() + LABELWIDTH;
			endY = label.getTranslateY() + LABELHEIGHT/2;
			break;
		case "road3.png":
			stX = label.getTranslateX();
			stY = label.getTranslateY();
			endX = label.getTranslateX() + LABELWIDTH;
			endY = label.getTranslateY() + LABELHEIGHT;
			break;
		case "road4.png":
			stX = label.getTranslateX() + LABELWIDTH;
			stY = label.getTranslateY();
			endX = label.getTranslateX();
			endY = label.getTranslateY() + LABELHEIGHT;
			break;
		case "road5.png":
			stX = label.getTranslateX() + LABELWIDTH;
			stY = label.getTranslateY();
			endX = label.getTranslateX();
			endY = label.getTranslateY() + LABELHEIGHT;
			break;
		case "road6.png":
			stX = label.getTranslateX();
			stY = label.getTranslateY();
			endX = label.getTranslateX() + LABELWIDTH;
			endY = label.getTranslateY() + LABELHEIGHT;
			break;
		default:
			throw new IllegalStateException();
		}

		// 信号を方向と位置を設定
		light.setStartX(stX);
		light.setStartY(stY);
		light.setEndX(endX);
		light.setEndY(endY);
	}

	/**
	 * roadUpdateメソッド
	 * マス目に道路に関係する処理をする
	 * @param l 処理対象となるラベル
	 */
	private void roadUpdate(Label l) {
		// マス目の左上はルート解析の都合上変更できないようにする
		if (label.get(0) == l) {
			return ;
		}

		// imgに設定されている道路をマス目に貼り付ける
		((ImageView)(l).getGraphic()).setImage(img);

		// もし信号がすでに道路にあるなら方向を調整する
		Line light = lights.get(label.indexOf(l));
		if (light != null) {
			setLightDirection(l, light);
		}
	}

	/**
	 * errorMessageメソッド
	 * エラーメッセージを表示する
	 * @param msg エラーメッセージ
	 */
	private void errorMessage(String msg) {
		Alert alert = new Alert(AlertType.ERROR, "", ButtonType.OK);
		alert.setTitle("エラー");
		alert.getDialogPane().setContentText(msg);
		alert.showAndWait().orElse(ButtonType.OK);
	}

	/**
	 * carMouseClickedメソッド
	 * 車がクリックされたときの処理を行う
	 * @param event MouseEvent
	 */
	private void carMouseClicked(MouseEvent event) {
		// シミュレーション中なら何もしない
		if (playing) {
			return ;
		}

		// アクティブカーが設定されているなら、表示を元に戻す
		if (activeCar != null) {
			activeCar.setStyle("");
		}

		// アクティブ信号が設定されているなら、表示を元に戻す
		if (activeLight != null) {
			activeLight.setStyle("");
			activeLight = null;
		}

		// クリックされた車をアクティブカーとして設定する
		activeCar = (Circle)event.getTarget();
		activeCar.setStyle("-fx-stroke: green;-fx-stroke-width: 1;-fx-stroke-dash-array: 12 2 4 2;-fx-stroke-dash-offset: 6;-fx-stroke-line-cap: butt;");

		// 左下のボックスをアクティブにして、クリックされた車の情報を設定する
		property.setDisable(false);
		carProperty.setDisable(false);
		lightProperty.setDisable(true);
		carPropertyColor.setValue((Color)activeCar.getFill());
		carPropertyVelocity.setText(carsVelocity.get(cars.indexOf(activeCar)).toString());
	}

	/**
	 * lightMouseClickedメソッド
	 * 信号がクリックされたときの処理を行う
	 * @param event MouseEvent
	 */
	private void lightMouseClicked(MouseEvent event) {
		// シミュレーション中なら何もしない
		if (playing) {
			return ;
		}

		// 左クリックのとき
		if (event.getButton() == MouseButton.PRIMARY) {
			// アクティブカーが設定されているなら、表示を元に戻す
			if (activeLight != null) {
				activeLight.setStyle("");
			}

			// アクティブ信号が設定されているなら、表示を元に戻す
			if (activeCar != null) {
				activeCar.setStyle("");
				activeCar = null;
			}

			// クリックされた信号をアクティブ信号として設定する
			activeLight = (Line)event.getTarget();
			activeLight.setStyle("-fx-stroke: green;-fx-stroke-width: 1;-fx-stroke-dash-array: 12 2 4 2;-fx-stroke-dash-offset: 6;-fx-stroke-line-cap: butt;");

			// 左下のボックスをアクティブにして、クリックされた信号の情報を設定する
			property.setDisable(false);
			carProperty.setDisable(true);
			lightProperty.setDisable(false);
			lightPropertyBlue.setText(String.valueOf(lightsTime.get(lights.indexOf(activeLight))[1]));
			lightPropertyRed.setText(String.valueOf(lightsTime.get(lights.indexOf(activeLight))[0]));
		}
		// 右クリックのとき
		else if (event.getButton() == MouseButton.SECONDARY) {
			// 信号の色が赤ならば緑に、緑ならば赤に切り替える
			if (((Line)event.getTarget()).getStroke() == Color.RED) {
				((Line)event.getTarget()).setStroke(Color.GREEN);
			}
			else {
				((Line)event.getTarget()).setStroke(Color.RED);
			}
		}
	}

	/**
	 * mouseClickedメソッド
	 * マス目がクリックされたときの処理を行う
	 * @param event MouseEvent
	 */
	private void mouseClicked(MouseEvent event) {
		// シミュレーション中なら何もしない
		if (playing) {
			return ;
		}

		// imgに何か設定されているときは道路6個の内いずれか選択されているはず
		// マス目に道路に関係する処理をする
		if (img != null) {
			roadUpdate((Label)event.getTarget());
			return ;
		}

		ToggleButton t = (ToggleButton)parts.getSelectedToggle();
		// 車のボタンが選択されていて、マス目に道路が設置されている場合
		if ("car".equals(t.getId()) && ((ImageView)((Label)event.getTarget()).getGraphic()).getImage() != null) {
			try {
				// 速度を取得
				int velocity = Integer.valueOf(inputVelocity.getText());
				if (velocity < 0 || velocity > 1000) {
					throw new NumberFormatException();
				}

				// 車を置く位置を計算
				double x = ((Label)event.getTarget()).getTranslateX() + LABELWIDTH/2;
				double y = ((Label)event.getTarget()).getTranslateY() + LABELHEIGHT/2;
				// 同じマスに車を置くことにならないか確認
				for (Circle c : cars) {
					if (c.getCenterX() + c.getTranslateX() == x && c.getCenterY() + c.getTranslateY() == y) {
						return ;
					}
				}

				// 車を生成
				Circle car = new Circle();
				car.setFill(colorPicker.getValue());
				car.setRadius(CARRADIUS);
				car.setCenterX(x);
				car.setCenterY(y);

				// 車にクリックイベントを設定
				EventHandler<MouseEvent> carMouseClicked = (e) -> this.carMouseClicked(e);
				car.addEventHandler(MouseEvent.MOUSE_CLICKED, carMouseClicked);

				cars.add(car);
				carsVelocity.add(velocity);
				Platform.runLater(() -> group.getChildren().add(car));
			}
			catch (NumberFormatException e) {
				errorMessage("1000以下の正の整数を入力してください");
			}
		}
		// 信号のボタンが選択されていて、マス目に道路が設置されている場合
		else if ("light".equals(t.getId()) && ((ImageView)((Label)event.getTarget()).getGraphic()).getImage() != null) {
			try {
				// 時間を取得
				int blueTime = Integer.valueOf(inputBlueTime.getText());
				int redTime = Integer.valueOf(inputRedTime.getText());
				if (blueTime < 0 || redTime < 0) {
					throw new NumberFormatException();
				}

				// 信号を生成
				Line light = new Line();
				light.setStrokeWidth(LIGHTWIDTH);
				light.setStroke(Color.RED);
				setLightDirection((Label)event.getTarget(), light);

				// 同じ場所に信号がないか確認
				int index = (int)((((Label)(event.getTarget())).getTranslateX() - LABELWIDTH) / LABELWIDTH) + SQUAREWIDTH * (int)((((Label)(event.getTarget())).getTranslateY() - LABELHEIGHT) / LABELHEIGHT);
				if (lights.get(index) != null) {
					return ;
				}

				// 信号にクリックイベントを設定
				EventHandler<MouseEvent> lightMouseClicked = (e) -> this.lightMouseClicked(e);
				light.addEventHandler(MouseEvent.MOUSE_CLICKED, lightMouseClicked);

				lights.set(index, light);
				lightsTime.set(index, new int[] {redTime, blueTime});
				Platform.runLater(() -> group.getChildren().add(light));
			}
			catch (NumberFormatException e) {
				errorMessage("正の整数を入力してください");
			}
		}
	}

	/**
	 * mouseDetectedメソッド
	 * ドラッグの開始を検知する
	 * @param event MouseEvent
	 */
	private void mouseDetected(MouseEvent event) {
		// シミュレーション中か道路が選択されていないなら、何もしない
		if (playing || img == null) {
			return ;
		}

		// 過去にドラッグしてdummyが設定されているなら、dummyを消す
		if (dummy != null) {
			Circle c = dummy;
			Platform.runLater(() -> display.getChildren().remove(c));
		}

		// ダミーの設定
		dummy = new Circle(0, 0, 0);
		display.getChildren().add(dummy);

		//ドラッグ中のマウス形状を変更
		display.setCursor(Cursor.CROSSHAIR);

		//ドラッグ開始
		dummy.startFullDrag();

		//		event.consume();
	}

	/**
	 * mouseDragメソッド
	 * ドラッグ中の処理を行う
	 * @param event MouseEvent
	 */
	private void mouseDrag(MouseEvent event){
		// シミュレーション中なら2Dカメラを行う
		if (playing) {
			camera2D.setCenterX(((Label)event.getTarget()).getTranslateX() + LABELWIDTH/2);
			camera2D.setCenterY(((Label)event.getTarget()).getTranslateY() + LABELHEIGHT/2);
			cameraView.setTranslateX(((Label)event.getTarget()).getTranslateX() + LABELWIDTH/2);
			cameraView.setTranslateY(((Label)event.getTarget()).getTranslateY() + LABELHEIGHT/2);
		}
		// マス目に道路を設定する
		else {
			roadUpdate((Label)event.getTarget());
		}
	}

	@FXML
	void initialize() {
		// マス目のラベルに設定するイベント
		EventHandler<MouseDragEvent> mouseDrag = (event) -> this.mouseDrag(event);
		EventHandler<MouseEvent> mouseDragDetected = (event) -> this.mouseDetected(event);
		EventHandler<MouseEvent> mouseClicked = (event) -> this.mouseClicked(event);

		// マス目を作成
		for (int i = 0; i < SQUAREHEIGHT; i++) {
			for (int j = 0; j < SQUAREWIDTH; j++) {
				Label l = new Label();
				l.setStyle("-fx-border-color: Black;");
				l.setPrefSize(LABELWIDTH, LABELHEIGHT);
				l.setTranslateX(PADDINGX + LABELWIDTH * j);
				l.setTranslateY(PADDINGY + LABELHEIGHT * i);

				// ラベルの中に道路を表示するためのイメージビューを作成
				ImageView img = new ImageView();
				img.setFitHeight(LABELHEIGHT);
				img.setFitWidth(LABELWIDTH);
				img.setPreserveRatio(true);
				l.setGraphic(img);

				// イベントを設定
				l.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseClicked);
				l.addEventHandler(MouseDragEvent.MOUSE_DRAG_OVER, mouseDrag);
				l.addEventHandler(MouseEvent.DRAG_DETECTED, mouseDragDetected);

				// 信号のリストはマス目の数と同じにして、どこに信号が配置されているか確認しやすくする
				lights.add(null);
				lightsTime.add(null);

				label.add(l);
				Platform.runLater(() -> display.getChildren().add(l));
			}
		}

		// 道路は一番上の横直線が選択されているようにする
		img = new Image(new File("img/road1.png").toURI().toString());

		// carSampleの初期状態設定
		carSample = new Circle();
		carSample.setFill(Color.WHITE);
		carSample.setRadius(10);
		carSample.setCenterX(10);
		carSample.setCenterY(10);
		((Pane)car.getGraphic()).getChildren().add(carSample);

		group = new Group();
		Platform.runLater( () -> display.getChildren().add(group));

		// マス目の左上の初期状態設定
		((ImageView)label.get(0).getGraphic()).setImage(new Image(new File("img/road6.png").toURI().toString()));

		// スライダーのラベル設定
		speedBar.setLabelFormatter(new StringConverter<Double>() {
			@Override
			public String toString(Double n) {
				if (n == 100) return "fast";
				if (n == 0) return "slow";
				return "";
			}

			@Override
			public Double fromString(String s) {
				switch (s) {
				case "fast":
					return 100d;
				case "slow":
					return 0d;
				default:
					return 50d;
				}
			}
		});
	}

	@FXML
	void mouseEntered(MouseEvent event) {
		// マウスのボタンが押されなくなったらカーソルを元に戻す
		if (! event.isPrimaryButtonDown()) {
			display.setCursor(Cursor.DEFAULT);
		}
	}

	@FXML
	void mouseReleased(MouseDragEvent event) {
		// マウスの形状を元に戻す
		display.setCursor(Cursor.DEFAULT);

		// シミュレーション中なら2Dカメラの場所に3Dカメラを移動
		if (playing) {
			controller.cameraLinck(BLOCKSIZE3D * (camera2D.getCenterX() - PADDINGX - LABELWIDTH/2) / LABELWIDTH, -BLOCKSIZE3D * (camera2D.getCenterY() - PADDINGY - LABELHEIGHT/2) / LABELHEIGHT);
		}

		// ダミーを消す
		if (dummy != null) {
			Circle c = dummy;
			Platform.runLater(() -> display.getChildren().remove(c));
		}
	}

	@FXML
	void onClickParts(ActionEvent event) {
		ToggleButton t = (ToggleButton)parts.getSelectedToggle();
		if (t != null) {
			// 車が信号のパーツが押されたらimgを未設定にする
			if ("car".equals(t.getId()) || "light".equals(t.getId())) {
				img = null;
			}
			// 道路の場合はimgに対象の道路の画像を保存する
			else {
				File f = new File(String.format("img/%s.png", t.getId()));
				img = new Image(f.toURI().toString());
			}
		}
		// 常に何かのパーツボタンが選択されているようにする
		else {
			((ToggleButton)event.getTarget()).setSelected(true);
		}
	}

	/**
	 * resetメソッド
	 * マス目の情報をリセットする
	 */
	private void reset() {
		// 左下のボックスを選択できなくする
		property.setDisable(true);
		carProperty.setDisable(true);
		lightProperty.setDisable(true);

		// 各種の情報を初期化
		drivers.clear();
		lightGroup.clear();
		Platform.runLater(() -> group.getChildren().clear());
		cars.clear();
		carsVelocity.clear();
		for (Label l : label) {
			((ImageView)(l.getGraphic())).setImage(null);
		}
		activeCar = null;
		activeLight = null;
		for (int i = 0; i < lights.size(); i++) {
			lights.set(i, null);
			lightsTime.set(i, null);
		}
	}

	@FXML
	void onClickReset(ActionEvent event) {
		// 確認ダイアログを出して確認
		Alert alert = new Alert(AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);
		alert.setTitle("確認");
		alert.getDialogPane().setContentText("リセットしますか？");
		ButtonType button = alert.showAndWait().orElse(ButtonType.YES);
		if (button == ButtonType.YES) {
			reset();
			// マス目の左上を設定
			((ImageView)label.get(0).getGraphic()).setImage(new Image(new File("img/road6.png").toURI().toString()));
		}
	}

	/**
	 * cameraDragDetectedメソッド
	 * マス目の情報をリセットする
	 * @param event MouseEvent
	 */
	private void cameraDragDetected(MouseEvent event) {
		//マウス形状を変更
		display.setCursor(Cursor.CLOSED_HAND);

		// 3Dカメラの操作ができないようにする
		controller.cameraUnLink();

		//ドラッグ開始
		camera2D.startFullDrag();

		//		event.consume();
	}

	/**
	 * createModelFromObjメソッド
	 * URL指定でOBJファイルからメッシュを作成する
	 * @param url
	 * @return 作成したモデル
	 */
	public Node createModelFromObj( String url ) {
		// 戻り値の3Dモデルグループを作成
		Group   root   = new Group();

		// 3Dモデルのインポーターを作成
		ObjModelImporter    importer    = new ObjModelImporter();

		// インポータにモデル・ファイルを設定
		try{
			importer.read( url );
		}catch( ImportException e ){
			e.printStackTrace();
		}

		// 3Dモデルを取込
		Node[]  meshes  = importer.getImport();
		root.getChildren().addAll( meshes );

		// インポータを閉じる
		importer.close();

		return root;
	}

	/**
	 * createOBJ
	 * パラメータで指定した色の.objを作成する
	 * @param color 色
	 */
	private static void createOBJ(Color color) throws IOException {
		// フォルダを作成
		File folder = new File("tmp");
		folder.mkdir();

		// もとになるファイルを読み込み
		File objfile = new File("3DModel/Car.obj");
		File mtlfile = new File("3DModel/Car.mtl");
		List<String> objlines;
		List<String> mtllines;
		objlines = Files.readAllLines(objfile.toPath());
		mtllines = Files.readAllLines(mtlfile.toPath());

		// ボディの色のみ変更
		// Car.objの3行目[colorの文字コード].mtlに書き換え
		objlines.set(2, "mtllib " + color.toString() + ".mtl");
		// Car.mtlの17行目をrbg書き換え
		mtllines.set(16, String.format("Kd %.6f %.6f %.6f", color.getRed(), color.getGreen(), color.getBlue()));

		// ファイルの書き出し
		objfile = new File("tmp/" + color.toString() + ".obj");
		PrintWriter writer = new PrintWriter(objfile);
		for (String s : objlines) {
			writer.println(s);
		}
		writer.close();
		objfile = new File("tmp/" + color.toString() + ".mtl");
		writer = new PrintWriter(objfile);
		for (String s : mtllines) {
			writer.println(s);
		}
		writer.close();
	}

	@FXML
	void onClickStart(ActionEvent event) {
		// 作成した道路の解析
		List<int[]> square = new ArrayList<>();
		square.add(new int[] {0, 0});
		int nextIndex = 1;
		// 行き先の方向
		// 右が1, 下が2, 左が3, 上が4
		int direction = 1;
		try {
			while (true) {
				square.add(new int[] {nextIndex % SQUAREWIDTH, nextIndex / SQUAREWIDTH});
				String fileName = "";
				if (((ImageView)(label.get(nextIndex).getGraphic())).getImage() != null) {
					fileName = getLabelImageName(label.get(nextIndex));
				}
				if (direction == 1) {
					if (fileName.equals("road1.png")) {
						if (nextIndex % SQUAREWIDTH == SQUAREWIDTH - 1) {
							throw new IllegalStateException();
						}
						nextIndex++;
						direction = 1;
					}
					else if (fileName.equals("road3.png")) {
						nextIndex -= SQUAREWIDTH;
						direction = 4;
						if (nextIndex < 0) {
							throw new IllegalStateException();
						}
					}
					else if (fileName.equals("road5.png")) {
						nextIndex += SQUAREWIDTH;
						direction = 2;
						if (nextIndex >= SQUAREWIDTH * SQUAREHEIGHT) {
							throw new IllegalStateException();
						}
					}
					else {
						throw new IllegalStateException();
					}
				}
				else if (direction == 2) {
					if (fileName.equals("road2.png")) {
						nextIndex += SQUAREWIDTH;
						direction = 2;
						if (nextIndex >= SQUAREWIDTH * SQUAREHEIGHT) {
							throw new IllegalStateException();
						}
					}
					else if (fileName.equals("road3.png")) {
						if (nextIndex % SQUAREWIDTH == 0) {
							throw new IllegalStateException();
						}
						nextIndex--;
						direction = 3;
					}
					else if (fileName.equals("road4.png")) {
						if (nextIndex % SQUAREWIDTH == SQUAREWIDTH - 1) {
							throw new IllegalStateException();
						}
						nextIndex++;
						direction = 1;
					}
					else {
						throw new IllegalStateException();
					}
				}
				else if (direction == 3) {
					if (fileName.equals("road1.png")) {
						if (nextIndex % SQUAREWIDTH == 0) {
							throw new IllegalStateException();
						}
						nextIndex--;
						direction = 3;
					}
					else if (fileName.equals("road4.png")) {
						nextIndex -= SQUAREWIDTH;
						direction = 4;
						if (nextIndex < 0) {
							throw new IllegalStateException();
						}
					}
					else if (fileName.equals("road6.png")) {
						nextIndex += SQUAREWIDTH;
						direction = 2;
						if (nextIndex >= SQUAREWIDTH * SQUAREHEIGHT) {
							throw new IllegalStateException();
						}
					}
					else {
						throw new IllegalStateException();
					}
				}
				else if (direction == 4) {
					if (fileName.equals("road2.png")) {
						nextIndex -= SQUAREWIDTH;
						direction = 4;
						if (nextIndex < 0) {
							throw new IllegalStateException();
						}
					}
					else if (fileName.equals("road5.png")) {
						if (nextIndex % SQUAREWIDTH == 0) {
							throw new IllegalStateException();
						}
						nextIndex--;
						direction = 3;
					}
					else if (fileName.equals("road6.png")) {
						if (nextIndex % SQUAREWIDTH == SQUAREWIDTH - 1) {
							throw new IllegalStateException();
						}
						nextIndex++;
						direction = 1;
					}
					else {
						throw new IllegalStateException();
					}
				}
				else {
					throw new IllegalStateException();
				}

				if (nextIndex == 0) {
					break;
				}
			}
		}
		catch (IllegalStateException e) {
			errorMessage("道路が一本道になっていません");
			return ;
		}

		Road road = new Road(square, mutexCheckBox.selectedProperty());
		DoubleProperty speedValue = speedBar.valueProperty();
		DoubleBinding speed = speedValue.add(1);

		// 道路上にある車を調べる
		Driver.setRoad(road);
		Driver.setSpeed(speed);
		List<Node> cars3D = new ArrayList<>();
		for (Circle c : cars) {
			c.setCenterX(c.getCenterX() + c.getTranslateX());
			c.setCenterY(c.getCenterY() + c.getTranslateY());
			c.setTranslateX(0);
			c.setTranslateY(0);

			// 車が道路上にあるか確認
			int index;
			try {
				index = road.XYToLength(c.getCenterX(), c.getCenterY());
			}
			catch (IllegalArgumentException e) {
				continue;
			}

			// 3Dの車を生成
			Node car;
			if (COLORCHANGE) {
				try {
					createOBJ((Color) c.getFill());
				} catch (IOException e) {
					errorMessage("3Dモデルの作成に失敗しました");
					return ;
				}
				car = createModelFromObj("tmp/" + c.getFill().toString() + ".obj");
			}
			else {
				car = createModelFromObj("3DModel/car.obj");
			}
			car.setScaleX(4);
			car.setScaleY(4);
			car.setScaleZ(4);
			car.setLayoutY(-2);
			car.setRotationAxis(new Point3D(0, 1, 0));
			cars3D.add(car);

			drivers.add(new Driver(index, carsVelocity.get(cars.indexOf(c)), c, car));
		}

		if (drivers.size() == 0) {
			errorMessage("道路上に車がありません");
			return ;
		}
		
		if (mutexCheckBox.isSelected() && road.size() / 2 <= drivers.size()) {
			errorMessage("車の数に対して、道路が少なすぎます" + System.lineSeparator() + "デッドロックが発生する恐れがあります");
		}

		// 信号を調べる
		Light.setRoad(road);
		Light.setSpeed(speed);
		List<Sphere> light3D = new ArrayList<>();
		for (int i = 0; i < lights.size(); i++) {
			// 信号が設定されていない
			if (lights.get(i) == null) {
				continue;
			}

			// 信号が道路上か確認
			int index;
			try {
				index = road.XYToLength(PADDINGX + LABELWIDTH/2 + LABELWIDTH * (double)(i % SQUAREWIDTH), PADDINGY + LABELHEIGHT/2 + LABELHEIGHT * (double)(i / SQUAREWIDTH));
			}
			catch (IllegalArgumentException e) {
				continue;
			}

			// 3Dの信号を生成
			Sphere light = new Sphere();
			light.setRadius(2);
			PhongMaterial material = new PhongMaterial();
			material.setDiffuseColor((Color) lights.get(i).getStroke());
			light.setMaterial(material);
			light.getTransforms().add(new Translate(BLOCKSIZE3D * (i % SQUAREWIDTH), -10, -BLOCKSIZE3D * (i / SQUAREWIDTH)));
			light3D.add(light);

			lightGroup.add(new Light(index, lightsTime.get(i), lights.get(i).getStroke() == Color.RED ? true : false, lights.get(i), light));
		}

		// アクティブカーとアクティブ信号を解除する
		if (activeCar != null) {
			activeCar.setStyle("");
		}
		activeCar = null;
		if (activeLight != null) {
			activeLight.setStyle("");
		}
		activeLight = null;

		// 操作可能・不可能を切り替える
		property.setDisable(true);
		carProperty.setDisable(true);
		lightProperty.setDisable(true);
		stopButton.setDisable(false);
		startButton.setDisable(true);
		resetButton.setDisable(true);
		openButton.setDisable(true);
		saveButton.setDisable(true);
		partsBox.setDisable(true);

		playing = true;

		try {
			stage = new Stage();

			// 新たなステージ用に画面構造（シーングラフ）を得る
			FXMLLoader loader = new FXMLLoader(SubWindow.class.getResource("SubWindow.fxml"));
			Parent root = loader.load();
			// loaderからfxmlに対応付けたコントローラクラスインスタンスを得る
			controller = (SubWindow) loader.getController();

			Scene scene = new Scene(root, 600, 400, true, SceneAntialiasing.BALANCED);

			// 3Dマップ生成に使用するデータをまとめる
			String[] mapData = new String[SQUAREWIDTH * SQUAREHEIGHT];
			for (int i = 0; i < label.size(); i++) {
				if (((ImageView)label.get(i).getGraphic()).getImage() == null) {
					mapData[i] = "";
				}
				else {
					mapData[i] = getLabelImageName(label.get(i));
				}
			}

			// 2Dカメラの設定をする
			camera2D = new Circle(PADDINGX + LABELWIDTH/2, PADDINGY + LABELHEIGHT/2, 5, Color.BLACK);
			Platform.runLater(() -> display.getChildren().add(camera2D));
			EventHandler<MouseEvent> mouseDragDetected = (e) -> cameraDragDetected(e);
			camera2D.addEventHandler(MouseEvent.DRAG_DETECTED, mouseDragDetected);
			cameraView = new Polygon(0, 0, -LABELWIDTH/2, -30, LABELWIDTH/2, -30);
			cameraView.setTranslateX(PADDINGX + LABELWIDTH/2);
			cameraView.setTranslateY(PADDINGY + LABELHEIGHT/2);
			cameraView.setFill(Color.YELLOW);
			cameraView.setOpacity(0.5);
			Platform.runLater(() -> display.getChildren().add(cameraView));

			// コントローラーを通してサブウィンドウに各種データを設定
			controller.setCamera2D(camera2D);
			controller.setCameraView(cameraView);
			controller.setCars(cars3D);
			controller.setLight(light3D);
			controller.setMap(mapData, SQUAREWIDTH);
			controller.start();

			// メインウィンドウを閉じたときの処理
			this.root.getScene().getWindow().showingProperty().addListener((observable, oldValue, newValue) -> {
				if (oldValue == true && newValue == false) {
					// 一時ファイルの削除
					File dir = new File("tmp");
					File[] list = dir.listFiles();
					if(list != null) {
						for (File f : list) {
							f.delete();
						}
					}
					
					// サブウィンドウも閉じるようにする
					stage.close();
				}
			});

			stage.setTitle("3Dモニター");
			stage.setScene(scene);
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}

		// 信号スタート
		for (Light l : lightGroup) {
			l.start();
		}

		// 車スタート
		for (Driver d : drivers) {
			d.start();
		}
	}

	@FXML
	void onClickStop(ActionEvent event) {
		// 信号を止める
		for (Light l : lightGroup) {
			l.stop();
		}

		// 車を止める
		for (Driver d : drivers) {
			d.stop();
		}

		drivers.clear();
		lightGroup.clear();

		playing = false;

		// 操作可能・不可能を切り替える
		property.setDisable(true);
		carProperty.setDisable(true);
		lightProperty.setDisable(true);
		stopButton.setDisable(true);
		startButton.setDisable(false);
		resetButton.setDisable(false);
		openButton.setDisable(false);
		saveButton.setDisable(false);
		partsBox.setDisable(false);

		// 2Dカメラを消す
		if (camera2D != null) {
			Circle c = camera2D;
			Polygon p = cameraView;
			Platform.runLater(() -> display.getChildren().remove(c));
			Platform.runLater(() -> display.getChildren().remove(p));
			camera2D = null;
			cameraView = null;
		}

		// サブウィンドウを消す
		stage.close();
	}

	@FXML
	void selectColor(ActionEvent event) {
		// 選択した色にcarSampleを変える
		carSample.setFill(colorPicker.getValue());
	}

	@FXML
	void onClickDelete(ActionEvent event) {
		// 車が選択されている場合
		if (activeCar != null) {
			// 左下のボックスを選択不能に
			property.setDisable(true);
			carProperty.setDisable(true);
			lightProperty.setDisable(true);

			// 車をリストから削除
			carsVelocity.remove(cars.indexOf(activeCar));
			cars.remove(activeCar);

			Circle c = activeCar;
			Platform.runLater(() -> group.getChildren().remove(c));
			activeCar = null;
		}
		// 信号が選択されている場合
		else if (activeLight != null) {
			// 左下のボックスを選択不能に
			property.setDisable(true);
			carProperty.setDisable(true);
			lightProperty.setDisable(true);

			// リストの信号をnullに置き換える
			lightsTime.set(lights.indexOf(activeLight), null);
			lights.set(lights.indexOf(activeLight), null);

			Line l = activeLight;
			Platform.runLater(() -> group.getChildren().remove(l));
			activeLight = null;
		}
	}

	@FXML
	void onClickUpdate(ActionEvent event) {
		// 車が選択されている場合
		if (activeCar != null) {
			try {
				// 速度を取得
				int velocity = Integer.valueOf(carPropertyVelocity.getText());
				if (velocity < 0 || velocity > 1000) {
					throw new NumberFormatException();
				}

				// 車の色と速度を設定する
				activeCar.setFill(carPropertyColor.getValue());
				carsVelocity.set(cars.indexOf(activeCar), velocity);
			}
			catch (NumberFormatException e) {
				errorMessage("1000以下の正の整数を入力してください");
			}
		}
		// 信号が選択されている場合
		else if (activeLight != null) {
			try {
				// 時間を取得
				int blueTime = Integer.valueOf(lightPropertyBlue.getText());
				int redTime = Integer.valueOf(lightPropertyRed.getText());
				if (blueTime < 0 || redTime < 0) {
					throw new NumberFormatException();
				}

				// 時間を設定する
				lightsTime.get(lights.indexOf(activeLight))[1] = blueTime;
				lightsTime.get(lights.indexOf(activeLight))[0] = redTime;
			}
			catch (NumberFormatException e) {
				errorMessage("整数を入力してください");
			}
		}
	}

	/**
	 * saveメソッド
	 * マス目の情報を保存する
	 * @param objOutStream オブジェクトを書き込むストリーム
	 */
	private void save(ObjectOutputStream objOutStream) throws IOException {
		// 道路
		// ファイル名を保存する
		List<String> road = new ArrayList<>();
		for (Label l : label) {
			Image img = ((ImageView)(l.getGraphic())).getImage();
			String fileName = "";
			if (img != null) {
				fileName = getLabelImageName(l);
			}
			road.add(fileName);
		}
		objOutStream.writeObject(road);

		// 車 色
		List<String> color = new ArrayList<>();
		for (Circle c : cars) {
			color.add(c.getFill().toString());
		}
		objOutStream.writeObject(color);

		// 車 位置
		// circleの中心を保存する
		List<int[]> pos = new ArrayList<>();
		for (Circle c : cars) {
			pos.add(new int[] {(int)c.getCenterX(), (int)c.getCenterY()});
		}
		objOutStream.writeObject(pos);

		// 車 速度
		List<Integer> velocity = new ArrayList<>();
		for (Circle c : cars) {
			velocity.add(carsVelocity.get(cars.indexOf(c)));
		}
		objOutStream.writeObject(velocity);

		// 信号 場所
		List<Boolean> lightPos = new ArrayList<>();
		// 信号 時間
		List<int[]> lightTime = new ArrayList<>();
		// 信号 初期状態
		List<String> lightState = new ArrayList<>();
		for (int i = 0; i < lights.size(); i++) {
			// 信号が設定されていない
			if (lights.get(i) == null) {
				lightPos.add(false);
			}
			// 信号が設定されている
			else {
				lightPos.add(true);
			}

			if (lightsTime.get(i) != null) {
				lightTime.add(lightsTime.get(i));
			}

			if (lights.get(i) != null) {
				lightState.add(lights.get(i).getStroke().toString());
			}
		}
		objOutStream.writeObject(lightPos);
		objOutStream.writeObject(lightTime);
		objOutStream.writeObject(lightState);
	}

	@FXML
	void onClickSave(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("."));
		fileChooser.getExtensionFilters().add(new ExtensionFilter("バイナリ", "*.bin"));

		File selected = fileChooser.showSaveDialog(null);
		if(selected != null) {
			// マス目の情報をファイルに保存する
			try (ObjectOutputStream objOutStream = new ObjectOutputStream(new FileOutputStream(selected));) {
				save(objOutStream);
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * readメソッド
	 * マス目の情報を読み込む
	 * @param file 情報を読み取るファイル
	 */
	@SuppressWarnings("unchecked")
	private void read(File file) {
		List<String> road;
		List<String> color;
		List<int[]> pos;
		List<Integer> velocity;
		List<Boolean> lightPos;
		List<int[]> lightTime;
		List<String> lightState;
		try (ObjectInputStream objInStream = new ObjectInputStream(new FileInputStream(file));){
			// 道
			road = (List<String>)objInStream.readObject();
			// 車 色
			color = (List<String>)objInStream.readObject();
			// 車 位置
			pos = (List<int []>)objInStream.readObject();
			// 車 速度
			velocity = (List<Integer>)objInStream.readObject();
			// 信号 場所
			lightPos = (List<Boolean>)objInStream.readObject();
			// 信号 時間
			lightTime = (List<int[]>)objInStream.readObject();
			// 信号 初期状態
			lightState = (List<String>)objInStream.readObject();

		}
		catch (IOException | ClassNotFoundException e) {
			errorMessage("対応していないファイルです");
			return ;
		}

		reset();

		try {
			// 道路の情報を読み取る
			for (int i = 0; i < road.size(); i++) {
				// 道路が設置されていない
				if ("".equals(road.get(i))) {
					continue;
				}
				File f = new File(String.format("img/%s", road.get(i)));
				Image img = new Image(f.toURI().toString());
				((ImageView)label.get(i).getGraphic()).setImage(img);
			}

			// 車の情報を読み取る
			for (int i = 0; i< color.size(); i++) {
				// 車を生成
				Circle car = new Circle();
				car.setFill(Color.valueOf(color.get(i)));
				car.setCenterX(pos.get(i)[0]);
				car.setCenterY(pos.get(i)[1]);
				cars.add(car);
				car.setRadius(CARRADIUS);
				carsVelocity.add(velocity.get(i));

				// イベントを設定
				EventHandler<MouseEvent> carMouseClicked = (e) -> this.carMouseClicked(e);
				car.addEventHandler(MouseEvent.MOUSE_CLICKED, carMouseClicked);

				Platform.runLater(() -> group.getChildren().add(car));
			}

			int c = 0;
			for (int i = 0; i < lights.size(); i++) {
				// 信号が設定されていない
				if (! lightPos.get(i)) {
					lights.set(i, null);
				}
				// 信号が設定されている
				else {
					// 信号を生成
					Line l = new Line();
					l.setStrokeWidth(LIGHTWIDTH);
					l.setStroke(Color.valueOf(lightState.get(c)));
					lights.set(i, l);
					lightsTime.set(i, lightTime.get(c));
					setLightDirection(label.get(i), l);

					// イベントを設定
					EventHandler<MouseEvent> lightMouseClicked = (e) -> this.lightMouseClicked(e);
					l.addEventHandler(MouseEvent.MOUSE_CLICKED, lightMouseClicked);

					Platform.runLater(() -> group.getChildren().add(l));
					c++;
				}
			}
		}
		catch (Exception e) {
			reset();
			errorMessage("対応していないファイルです");
			return ;
		}

	}

	@FXML
	void onClickOpen(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("."));
		fileChooser.getExtensionFilters().add(new ExtensionFilter("バイナリ", "*.bin"));

		File selected = fileChooser.showOpenDialog(null);
		if(selected != null) {
			read(selected);
		}
	}

	@FXML
	void onDragDropped(DragEvent event) {
		// シミュレーション中なら何もしない
		if (playing) {
			return ;
		}

		// ドラッグアンドドロップしたものがファイルなら読み込む
		if (event.getDragboard().hasFiles()) {
			for (File f : event.getDragboard().getFiles()) {
				if (f.isFile()) {
					read(f);
				}
				else {
					errorMessage("ファイルではありません");
				}
				event.setDropCompleted(true);
				break;
			}
		}
		else {
			event.setDropCompleted(false);
		}
	}

	@FXML
	void onDragOver(DragEvent event) {
		// シミュレーション中なら何もしない
		if (playing) {
			return ;
		}
		event.acceptTransferModes(TransferMode.COPY);
	}

}
