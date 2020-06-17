package application.subWindow;

import java.util.List;

import application.MainController;
import javafx.animation.AnimationTimer;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Box;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class CameraOperation extends AnimationTimer {
	final static private int MOVEDELTA = 2;
	final static private int ANGLEDELTA = 3;

	// カメラ
	private Camera camera3D;
	private Circle camera2D;
	private Polygon cameraView;

	// カメラを動かすプレイヤー
	private Box player;

	// カメラの位置と向き
	private double z = 0;
	private double x = 0;
	private double y = -3.5;
	private double angle = 0;

	// キーボードの判定
	private KeyManager keyManager;

	// 壁判定をする3Dオブジェクト
	private List<Box> walls;

	// カメラの操作が有効
	private boolean link = true;

	// 3Dブロックサイズ
	private int blockSize3D = MainController.BLOCKSIZE3D;

	// マス目となるラベルのサイズ
	private static int labelWidth = MainController.LABELWIDTH;
	private static int labelHeight = MainController.LABELHEIGHT;

	// 左上の余白
	private static int paddingX = MainController.PADDINGX;
	private static int paddingY = MainController.PADDINGY;

    /**
     * setCamera2Dメソッド
     * 2Dカメラをセットする
     * @param camera2D 2Dカメラ
     */
	public void setCamera2D(Circle camera2D) {
		this.camera2D = camera2D;
	}

    /**
     * setCameraViewメソッド
     * 2Dカメラの視界をセットする
     * @param cameraView 2Dカメラの視界
     */
	public void setCameraView(Polygon cameraView) {
		this.cameraView = cameraView;
	}

    /**
     * setCamera3Dメソッド
     * 3Dカメラをセットする
     * @param camera3D カメラ
     */
	public void setCamera(Camera camera3D) {
		this.camera3D = camera3D;
	}

    /**
     * setKeyManagerメソッド
     * keyManagerをセットする
     * @param keyManager
     */
	public void setKeyManager(KeyManager keyManager) {
		this.keyManager = keyManager;
	}

    /**
     * setPlayerメソッド
     * plaerをセットする
     * @param player
     */
	public void setPlayer(Box player) {
		this.player = player;
	}

    /**
     * setWallsメソッド
     * 壁を設定する
     * @param walls
     */
	public void setWalls(List<Box> walls) {
		this.walls = walls;
	}

    /**
     * cameraUnLinkメソッド
     * カメラを操作不能にする
     */
	public void cameraUnLink() {
		link = false;
	}

    /**
     * cameraLinkメソッド
     * カメラを操作可能にする
     */
	public void cameraLink() {
		link = true;
	}

    /**
     * cameraUpdateメソッド
     * 3Dカメラの位置を更新する
     * @param x 位置
     * @param z 位置
     */
	public void cameraUpdate(double x, double z) {
		this.x = x;
		this.z = z;
	}

    /**
     * isFrontIntersectsメソッド
     * 前方に壁があるか調べる
     * @return 壁があるならtrue
     */
	private boolean isFrontIntersects() {
		Bounds p = player.localToParent(new BoundingBox(-1.5, -2.5, 1.0, 3.0, 5.0, 0.5));
		// 全ての壁とプレイヤーを表すボックスが交わっていないか確認
		for (Box b : walls) {
			if (p.intersects(b.getBoundsInParent())) {
				return true;
			}
		}
		return false;
	}

    /**
     * isBackIntersectsメソッド
     * 後方に壁があるか調べる
     * @return 壁があるならtrue
     */
	private boolean isBackIntersects() {
		Bounds p = player.localToParent(new BoundingBox(-1.5, -2.5, -1.5, 3.0, 5.0, 0.5));
		for (Box b : walls) {
			if (p.intersects(b.getBoundsInParent())) {
				return true;
			}
		}
		return false;
	}

    /**
     * actionZメソッド
     * 壁がないなら前に進む
     * @param highSpeed trueなら高速移動
     */
	private void actionZ(boolean highSpeed) {
		if (! isFrontIntersects()) {
			if (highSpeed) {
				x += Math.sin(angle * Math.PI / 180) * MOVEDELTA * 10;
				z += Math.cos(angle * Math.PI / 180) * MOVEDELTA * 10;
			}
			else {
				x += Math.sin(angle * Math.PI / 180) * MOVEDELTA;
				z += Math.cos(angle * Math.PI / 180) * MOVEDELTA;
			}
		}
	}

    /**
     * actionXメソッド
     * 壁がないなら後ろに進む
     */
	private void actionX() {
		if (! isBackIntersects()) {
			x -= Math.sin(angle * Math.PI / 180) * MOVEDELTA;
			z -= Math.cos(angle * Math.PI / 180) * MOVEDELTA;
		}
	}

    /**
     * actionLEFTメソッド
     * カメラの向きを左に向ける
     */
	private void actionLEFT() {
		angle -= ANGLEDELTA;
	}

    /**
     * actionRIGHTメソッド
     * カメラの向きを右に向ける
     */
	private void actionRIGHT() {
		angle += ANGLEDELTA;
	}

	@Override
	public void handle(long now) {
		// カメラの操作が有効な時
		if (link) {
			if (keyManager.isPressed(KeyCode.Z) && keyManager.isPressed(KeyCode.LEFT)) {
				actionZ(keyManager.isPressed(KeyCode.SPACE));
				actionLEFT();
			}
			else if (keyManager.isPressed(KeyCode.Z) && keyManager.isPressed(KeyCode.RIGHT)) {
				actionZ(keyManager.isPressed(KeyCode.SPACE));
				actionRIGHT();
			}
			else if (keyManager.isPressed(KeyCode.X) && keyManager.isPressed(KeyCode.LEFT)) {
				actionX();
				actionLEFT();
			}
			else if (keyManager.isPressed(KeyCode.X) && keyManager.isPressed(KeyCode.RIGHT)) {
				actionX();
				actionRIGHT();
			}
			else if (keyManager.isPressed(KeyCode.Z)) {
				actionZ(keyManager.isPressed(KeyCode.SPACE));
			}
			else if (keyManager.isPressed(KeyCode.X)) {
				actionX();
			}
			else if (keyManager.isPressed(KeyCode.LEFT)) {
				actionLEFT();
			}
			else if (keyManager.isPressed(KeyCode.RIGHT)) {
				actionRIGHT();
			}

			// 3Dカメラを動かす
			camera3D.getTransforms().clear();
			camera3D.getTransforms().add(new Translate(x, y, z));
			camera3D.getTransforms().add(new Rotate(angle, new Point3D(0, 1, 0)));

			// プレイヤーを動かす
			player.getTransforms().clear();
			player.getTransforms().add(new Translate(x, y, z));
			player.getTransforms().add(new Rotate(angle, new Point3D(0, 1, 0)));

			// 2Dカメラを動かす
			camera2D.setCenterX((double)labelWidth * x / blockSize3D + paddingX + labelWidth/2);
			camera2D.setCenterY((double)labelHeight * (-z) / blockSize3D + paddingY + labelHeight/2);

			// 2Dカメラの視界を動かす
			cameraView.setTranslateX((double)labelWidth * x / blockSize3D + paddingX + labelWidth/2);
			cameraView.setTranslateY((double)labelHeight * (-z) / blockSize3D + paddingY + labelHeight/2);
			cameraView.getTransforms().clear();
			cameraView.getTransforms().add(new Rotate(angle, 0, 0));
		}
	}
}
