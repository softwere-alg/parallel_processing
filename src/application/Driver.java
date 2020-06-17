package application;

import java.util.concurrent.CountDownLatch;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class Driver {
	// 車がある道路
	private static Road road;

	// シミュレーションスピード
	private static DoubleBinding speed;

	// マス目となるラベルのサイズ
	private static int labelWidth;
	private static int labelHeight;

	// 左上の余白
	private static int paddingX;
	private static int paddingY;

	// 3Dのブロックサイズ
	private static int blockSize3D;

	// 車の位置
	private int pos;

	// 1つ前の車の位置
	private int oldPos;

	// 車の速さ
	private int velocity;

	// 車
	private Circle car2D;
	private Node car3D;

	// スレッド
	private Thread t;

	// 2Dと3Dが両方動き終えたことを確認する
	private CountDownLatch countDownLatch;

    /**
     * setRoadメソッド
     * 道路を設定する
     * @param road 車がある道路
     */
	public static void setRoad(Road road) {
		Driver.road = road;
	}

    /**
     * setSpeed
     * シミュレーションスピードを設定する
     * @param speed シミュレーションスピード
     */
	public static void setSpeed(DoubleBinding speed) {
		Driver.speed = speed;
	}

    /**
     * コンストラクタ
     * @param pos 車の初期位置
     * @param velocity 速度
     * @param car2D 2D車
     * @param car3D 3D車
     */
	Driver(int pos, int velocity, Circle car2D, Node car3D) {
		this.pos = pos;
		this.velocity = velocity;
		this.car2D = car2D;
		road.add(pos);
		this.car3D = car3D;
		labelWidth = MainController.LABELWIDTH;
		labelHeight = MainController.LABELHEIGHT;
		paddingX = MainController.PADDINGX;
		paddingY = MainController.PADDINGY;
		blockSize3D = MainController.BLOCKSIZE3D;
	}

    /**
     * startメソッド
     * 車の処理をスタートする
     */
	public void start() {
		if (t != null) {
			t.interrupt();
		}
		t = new Thread(new carTask());
		t.setDaemon(true);
		t.start();
	}

	/**
     * stopメソッド
     * 車の処理をストップする
     */
	public void stop() {
		if (t != null) {
			t.interrupt();
		}
	}

    /**
     * posXメソッド
     * パラメータからX座標を返す
     * @param pos 数値
     * @return X座標
     */
	private int posX(int pos) {
		return road.lengthToX(pos);
	}

    /**
     * posYメソッド
     * パラメータからY座標を返す
     * @param pos 数値
     * @return Y座標
     */
	private int posY(int pos) {
		return road.lengthToY(pos);
	}

	/**
     * stepメソッド
     * 車の位置を更新する
     */
	private void step() {
		oldPos = pos;
		int dst = pos + 1;
		dst = dst % road.size();
		pos = road.update(pos, dst);
	}

	/**
     * paintメソッド
     * 車の描画処理をする
     */
	private void paint() {
		// 移動元、移動先の計算
		double fromX = posX(oldPos) - car2D.getCenterX();
		double fromY = posY(oldPos) - car2D.getCenterY();
		double toX = posX(pos) - car2D.getCenterX();
		double toY = posY(pos) - car2D.getCenterY();

		// 2D車
		TranslateTransition translateTransition = new TranslateTransition();
		// アニメーション対象ノードを登録
		translateTransition.setNode(car2D);
        // アニメーション1サイクル分の時間を設定
		translateTransition.setDuration(Duration.millis(1000.0/(velocity * speed.getValue())));
		translateTransition.setFromX(fromX);
		translateTransition.setFromY(fromY);
		translateTransition.setToX(toX);
		translateTransition.setToY(toY);
		// アニメーション補完方法を線形に設定
		translateTransition.setInterpolator(Interpolator.LINEAR);

		// アニメーションが終了したらカウントダウンラッチするイベントを設定
		translateTransition.setOnFinished(e -> {
			countDownLatch.countDown();
        });
		translateTransition.play();

		// 3D車
		translateTransition = new TranslateTransition();
		translateTransition.setNode(car3D);
		translateTransition.setDuration(Duration.millis(1000.0/(velocity * speed.getValue())));
		translateTransition.setFromX(blockSize3D * ((fromX + car2D.getCenterX() - paddingX - labelWidth/2) / labelWidth));
		translateTransition.setFromY(0);
		translateTransition.setFromZ(-blockSize3D * ((fromY + car2D.getCenterY() - paddingY - labelHeight/2) / labelHeight));
		translateTransition.setToX(blockSize3D * ((toX + car2D.getCenterX() - paddingX - labelWidth/2) / labelWidth));
		translateTransition.setToY(0);
		translateTransition.setToZ(-blockSize3D * ((toY + car2D.getCenterY() - paddingY - labelHeight/2) / labelHeight));
		translateTransition.setInterpolator(Interpolator.LINEAR);
		translateTransition.setOnFinished(e -> {
			countDownLatch.countDown();
        });
		// 車の向きを変える
		if (fromX != toX && fromY == toY) {
			if (fromX < toX) {
				car3D.setRotate(270);
			}
			else {
				car3D.setRotate(90);
			}
		}
		else if (fromX == toX && fromY != toY) {
			if (fromY < toY) {
				car3D.setRotate(0);
			}
			else {
				car3D.setRotate(180);
			}

		}
		translateTransition.play();
	}

	class carTask extends Task<Boolean> {
		@Override
		public Boolean call() throws Exception {
			while (true) {
				step();
				countDownLatch = new CountDownLatch(2);
				Platform.runLater(() -> paint());
		        countDownLatch.await();
			}
		}
	}
}
