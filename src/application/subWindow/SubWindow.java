package application.subWindow;

import java.util.ArrayList;
import java.util.List;

import application.MainController;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Box;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;

public class SubWindow {
	// MainController.BLOCKSIZE3Dを変更したなら変えること
	final static private double[][][] dirtParams = {
			{{50, 0.5, 10, 0, 0, 20}, {50, 0.5, 10, 0, 0, -20}},
			{{10, 0.5, 50, -20, 0, 0}, {10, 0.5, 50, 20, 0, 0}},
			{{50, 0.5, 10, 0, 0, -20}, {10, 0.5, 40, 20, 0, 5}, {10, 0.5, 10, -20, 0, 20}},
			{{50, 0.5, 10, 0, 0, -20}, {10, 0.5, 40, -20, 0, 5}, {10, 0.5, 10, 20, 0, 20}},
			{{50, 0.5, 10, 0, 0, 20}, {10, 0.5, 40, 20, 0, -5}, {10, 0.5, 10, -20, 0, -20}},
			{{50, 0.5, 10, 0, 0, 20}, {10, 0.5, 40, -20, 0, -5}, {10, 0.5, 10, 20, 0, -20}}
	};

	final static private double[][][] roadParams = {
			{{50, 0.5, 30, 0, 0, 0}},
			{{30, 0.5, 50, 0, 0, 0}},
			{{40, 0.5, 30, -5, 0, 0}, {30, 0.5, 10, 0, 0, 20}},
			{{40, 0.5, 30, 5, 0, 0}, {30, 0.5, 10, 0, 0, 20}},
			{{40, 0.5, 30, -5, 0, 0}, {30, 0.5, 10, 0, 0, -20}},
			{{40, 0.5, 30, 5, 0, 0}, {30, 0.5, 10, 0, 0, -20}}
	};

	final static private double[][][] wallParams = {
			{{50, 2, 1, 0, 0, 25}, {50, 2, 1, 0, 0, -25}},
			{{1, 2, 50, -25, 0, 0}, {1, 2, 50, 25, 0, 0}},
			{{50, 2, 1, 0, 0, -25}, {1, 2, 50, 25, 0, 0}},
			{{50, 2, 1, 0, 0, -25}, {1, 2, 50, -25, 0, 0}},
			{{50, 2, 1, 0, 0, 25}, {1, 2, 50, 25, 0, 0}},
			{{50, 2, 1, 0, 0, 25}, {1, 2, 50, -25, 0, 0}}
	};

    @FXML
    private Group root;

    // カメラを動かすプレイヤー
	private Box player = new Box();

	// カメラ
	private Camera camera3D = new PerspectiveCamera(true);
	private Circle camera2D;
	private Polygon cameraView;
	private CameraOperation cameraOp = new CameraOperation();

	// キーボードの判定
	private KeyManager keyManager = new KeyManager();

	// 壁判定をする3Dオブジェクト
	private List<Box> walls = new ArrayList<>();

	// 場外となる場所
	private List<int[]> dirts = new ArrayList<>();

	// 3Dブロックサイズ
	private int blockSize3D = MainController.BLOCKSIZE3D;

    /**
     * setCamera2Dメソッド
     * 2Dカメラを設定する
     * @param camera2D
     */
	public void setCamera2D(Circle camera2D) {
		this.camera2D = camera2D;
	}

    /**
     * setCameraViewメソッド
     * 2Dカメラの視界を設定する
     * @param cameraView
     */
	public void setCameraView(Polygon cameraView) {
		this.cameraView = cameraView;
	}

    /**
     * setCarsメソッド
     * 3D車を描画対象に追加する
     * @param cars3D
     */
	public void setCars(List<Node> cars3D) {
		root.getChildren().addAll(cars3D);
	}

    /**
     * setLightメソッド
     * 3D信号を描画対象に追加する
     * @param light3D
     */
	public void setLight(List<Sphere> light3D) {
		root.getChildren().addAll(light3D);
	}

    /**
     * cameraUnLinkメソッド
     * カメラの操作をできなくする
     */
	public void cameraUnLink() {
		cameraOp.cameraUnLink();
	}

    /**
     * cameraLinckメソッド
     * カメラの操作をできるようにする
     * カメラの位置をパラメータで設定する
     * @param x 数値
     * @param z 数値
     */
	public void cameraLinck(double x, double z) {
		// 場外へカメラが移動しようとしていたら、移動させない
		for (int i = 0; i < dirts.size(); i++) {
			if (dirts.get(i)[0] == (int)x && dirts.get(i)[1] == (int)z) {
				cameraOp.cameraLink();
				return ;
			}
		}
		cameraOp.cameraUpdate(x, z);
		cameraOp.cameraLink();
	}

    /**
     * createDirtメソッド
     * 地面を作成
     * @param param 0:width, 1:height, 2:depth, 3:x 位置(ローカル), 4:y 位置(ローカル), 5:z 位置(ローカル)
     * @param x 位置(グローバル)
     * @param y 位置(グローバル)
     * @param z 位置(グローバル)
     */
	private Box createDirt(double[] param, int x, int y, int z) {
		Box dirt = new Box(param[0], param[1], param[2]);
		PhongMaterial material = new PhongMaterial();
		material.setDiffuseColor(Color.web("9FCC7F"));
		dirt.setMaterial(material);
		dirt.getTransforms().add(new Translate(param[3] + x, param[4] + y, param[5] + z));

		return dirt;
	}

    /**
     * createRoadメソッド
     * 道路を作成
     * @param param 0:width, 1:height, 2:depth, 3:x 位置(ローカル), 4:y 位置(ローカル), 5:z 位置(ローカル)
     * @param x 位置(グローバル)
     * @param y 位置(グローバル)
     * @param z 位置(グローバル)
     */
	private Box createRoad(double[] param, int x, int y, int z) {
		Box road = new Box(param[0], param[1], param[2]);
		PhongMaterial material = new PhongMaterial();
		material.setDiffuseColor(Color.GRAY);
		road.setMaterial(material);
		road.getTransforms().add(new Translate(param[3] + x, param[4] + y, param[5] + z));

		return road;
	}

    /**
     * createWallメソッド
     * 壁を作成
     * @param param 0:width, 1:height, 2:depth, 3:x 位置(ローカル), 4:y 位置(ローカル), 5:z 位置(ローカル)
     * @param x 位置(グローバル)
     * @param y 位置(グローバル)
     * @param z 位置(グローバル)
     */
	private Box createWall(double[] param, int x, int y, int z) {
		Box wall = new Box(param[0], param[1], param[2]);
		PhongMaterial material = new PhongMaterial();
		material.setDiffuseColor(Color.BROWN);
		wall.setMaterial(material);
		wall.getTransforms().add(new Translate(param[3] + x, param[4] + y, param[5] + z));

		return wall;
	}

    /**
     * createBlockメソッド
     * ブロックを作成
     * @param x 位置(グローバル)
     * @param y 位置(グローバル)
     * @param z 位置(グローバル)
     * @param num 道路の種類
     */
	private Group createBlock(int x, int y, int z, int num) {
		Group g = new Group();

		// 地面を作成
		for (int i = 0; i < dirtParams[num].length; i++) {
			g.getChildren().add(createDirt(dirtParams[num][i], x, y, z));
		}

		// 道を作成
		for (int i = 0; i < roadParams[num].length; i++) {
			g.getChildren().add(createRoad(roadParams[num][i], x, y, z));
		}

		// 壁を作成
		for (int i = 0; i < wallParams[num].length; i++) {
			Box wall = createWall(wallParams[num][i], x, y, z);
			g.getChildren().add(wall);
			walls.add(wall);
		}

		return g;
	}

    /**
     * setMapメソッド
     * 3Dのマップを作成する
     * @param map マップを作成する道路を指定する
     * @param width マス目の横の数
     */
	public void setMap(String[] map, int width) {
		for (int i = 0; i < map.length; i++) {
			Group g;
			switch (map[i]) {
			case "road1.png":
				g = createBlock(blockSize3D * (i%width), 0, -blockSize3D * (i/width), 0);
				break;
			case "road2.png":
				g = createBlock(blockSize3D * (i%width), 0, -blockSize3D * (i/width), 1);
				break;
			case "road3.png":
				g = createBlock(blockSize3D * (i%width), 0, -blockSize3D * (i/width), 2);
				break;
			case "road4.png":
				g = createBlock(blockSize3D * (i%width), 0, -blockSize3D * (i/width), 3);
				break;
			case "road5.png":
				g = createBlock(blockSize3D * (i%width), 0, -blockSize3D * (i/width), 4);
				break;
			case "road6.png":
				g = createBlock(blockSize3D * (i%width), 0, -blockSize3D * (i/width), 5);
				break;
			default:
				g = new Group(createDirt(new double[] {blockSize3D, 0.5, blockSize3D, 0, 0, 0}, blockSize3D * (i%width), 0, -blockSize3D * (i/width)));
				dirts.add(new int[] {blockSize3D * (i%width), -blockSize3D * (i/width)});
			}
			root.getChildren().add(g);
		}
	}

    /**
     * keyEventメソッド
     * キーボード入力の処理を行う
     * @param event KeyEvent
     */
	private void keyEvent(KeyEvent event) {
		keyManager.handleEvent(event);
	}

    /**
     * startメソッド
     * サブウィンドウの動作を開始する
     */
	public void start() {
		Scene scene = root.getScene();

		// 空のグラデーションを作成
		Stop[] stops = new Stop[]{new Stop(0, Color.WHITE),
				new Stop(0.5, Color.web("71afec")),
				new Stop(1, Color.web("2454a0"))};
		LinearGradient sky = new LinearGradient(0, 400, 0, 0, false, CycleMethod.NO_CYCLE, stops);
		scene.setFill(sky);

		// シーンにカメラをセット
		scene.setCamera(camera3D);

		// シーンにキーボード入力のイベントを設定
		EventHandler<KeyEvent> keyEvent = (event) -> this.keyEvent(event);
		scene.addEventHandler(KeyEvent.ANY, keyEvent);

		// プレイヤー設定
		player.setWidth(3);
		player.setHeight(5);
		player.setDepth(3);
		player.getTransforms().add(new Translate(0, 0, 0));
		root.getChildren().add(player);

		// カメラ設定
		cameraOp.setCamera2D(camera2D);
		cameraOp.setCameraView(cameraView);
		cameraOp.setCamera(camera3D);
		cameraOp.setKeyManager(keyManager);
		cameraOp.setPlayer(player);
		cameraOp.setWalls(walls);
		cameraOp.start();
	}

	@FXML
	public void initialize() {
		// カメラ設定
		camera3D.setFarClip(1000);
		camera3D.getTransforms().add(new Translate(0, 0, 0));
		root.getChildren().add(camera3D);

		// アンビエント光
		AmbientLight ambientLight = new AmbientLight(Color.rgb(255,255,255));
		root.getChildren().add(ambientLight);
	}
}