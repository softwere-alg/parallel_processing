package application;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Line;
import javafx.scene.shape.Sphere;

public class Light {
	final static private Color[] COLOR = {Color.RED, Color.GREEN};

	// 信号が設置してある道路
	private static Road road;

	// シミュレーションスピード
	private static DoubleBinding speed;

	// このインスタンスの信号がどこにあるかを示すインデックス
	private int index;

	// 信号の切り替え時間
	private int[] time;

	// 信号の状態
	private boolean state;

	// 信号
	private Line light2D;
	private Sphere light3D;

	// スレッド
	private Thread t;

    /**
     * setRoadメソッド
     * 道路を設定する
     * @param road 信号がある道路
     */
	public static void setRoad(Road road) {
		Light.road = road;
	}

    /**
     * setSpeed
     * シミュレーションスピードを設定する
     * @param speed シミュレーションスピード
     */
	public static void setSpeed(DoubleBinding speed) {
		Light.speed = speed;
	}


    /**
     * コンストラクタ
     * @param index 信号の位置
     * @param time 切り替え時間
     * @param state 信号の状態
     * @param light2D 2D信号
     * @param light3D 3D信号
     */
	Light(int index, int[] time, boolean state, Line light2D, Sphere light3D) {
		this.index = index;
		this.time = time;
		this.state = state;
		this.light2D = light2D;
		road.setLight(index, state);
		this.light3D = light3D;
	}

    /**
     * startメソッド
     * 信号の処理をスタートする
     */
	public void start() {
		if (t != null) {
			t.interrupt();
		}
		t = new Thread(new lightTask());
		t.setDaemon(true);
		t.start();
	}

	/**
     * stopメソッド
     * 信号の処理をストップする
     */
	public void stop() {
		if (t != null) {
			t.interrupt();
		}
	}

	/**
     * changeStateメソッド
     * 信号の状態を変化させる
     */
	private void changeState() {
		state = !state;
		road.update(index, state);
	}

	/**
     * paintメソッド
     * 信号の描画処理をする
     */
	private void paint() {
		light2D.setStroke(COLOR[state ? 0 : 1]);
		light3D.setMaterial(new PhongMaterial(COLOR[state ? 0 : 1]));
	}

	class lightTask extends Task<Boolean> {
		@Override
		public Boolean call() throws Exception {
			while (true) {
				Thread.sleep((int)(time[state ? 0 : 1] * 1000 / speed.getValue()));
				changeState();
				Platform.runLater(() -> paint());
			}
		}
	}
}
