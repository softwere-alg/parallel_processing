package application;

import java.util.Arrays;
import java.util.List;

import javafx.beans.property.BooleanProperty;

public class Road {
	final static private int SPACE = 2;
	
	// データにアクセスできる
	private boolean available = true;

	// 道路に車があるか
	private boolean[] road;

	// 道順を示すリスト
	private List<int[]> route;

	// 信号により進めないか
	private boolean[] light;

	// 排他制御を行うか
	private BooleanProperty bp;

	// マス目となるラベルのサイズ
	private int labelWidth;
	private int labelHeight;

	// 左上の余白
	private int paddingX;
	private int paddingY;

    /**
     * コンストラクタ
     * @param route 道順情報
     * @param bp 排他制御を行うか
     * @param width マス目のサイズ
     * @param height マス目のサイズ
     */
	Road(List<int[]> route, BooleanProperty bp) {
		this.route = route;
		road = new boolean[route.size()];
		Arrays.fill(road, false);
		light = new boolean[route.size()];
		Arrays.fill(light, false);
		this.bp = bp;
		labelWidth = MainController.LABELWIDTH;
		labelHeight = MainController.LABELHEIGHT;
		paddingX = MainController.PADDINGX;
		paddingY = MainController.PADDINGY;
	}

    /**
     * addメソッド
     * posに車があることを設定する
     * 排他制御を行わないため、初期設定のみで使用すること
     * @param pos 車のある位置
     */
	public void add(int pos) {
		road[pos] = true;
	}

    /**
     * setLightメソッド
     * 信号の設定を行う
     * 排他制御を行わないため、初期設定のみで使用すること
     * @param index 設定する信号のインデックス
     * @param b 初期値
     */
	public void setLight(int index, boolean b) {
		light[index] = b;
	}

    /**
     * sizeメソッド
     * 道路のサイズを返す
     * @return 道路のサイズを返す
     */
	public int size() {
		return road.length;
	}

    /**
     * lengthToXメソッド
     * パラメータから2D上のX座標を返す
     * @param index 数値
     * @return X座標
     */
	public int lengthToX(int index) {
		return route.get(index)[0] * labelWidth + paddingX + labelWidth/2;
	}

    /**
     * lengthToYメソッド
     * パラメータから2D上のY座標を返す
     * @param index 数値
     * @return Y座標
     */
	public int lengthToY(int index) {
		return route.get(index)[1] * labelHeight + paddingY + labelHeight/2;
	}

    /**
     * XYToLengthメソッド
     * x, yから当てはまる道路のインデックスを返す
     * @param x 数値
     * @param y 数値
     * @return 道路のインデックス
     */
	public int XYToLength(double x, double y) throws IllegalArgumentException {
		int intx = (int)(x - (paddingX + labelWidth/2)) / labelWidth;
		int inty = (int)(y - (paddingY + labelHeight/2)) / labelHeight;
		for (int i = 0; i < route.size(); i++) {
			if (route.get(i)[0] == intx && route.get(i)[1] == inty) {
				return i;
			}
		}
		throw new IllegalArgumentException("引数の値が不正です");
	}

    /**
     * updateメソッド
     * 車をsrcからdstに動かす
     * @param src 移動元
     * @param dst 移動先
     * @return 実際に動かせた移動先
     */
	public int update(int src, int dst) {
		if (bp.getValue()) {
			return updateSynchronized(src, dst);
		}
		else {
			return updateUnSynchronized(src, dst);
		}
	}

    /**
     * updateUnSynchronizedメソッド
     * 排他制御を行わずに車をsrcからdstに動かす
     * @param src 移動元
     * @param dst 移動先
     * @return 実際に動かせた移動先
     */
	public int updateUnSynchronized(int src, int dst) {
		int srcIndex = src;
		int dstIndex = dst;
		int realDstIndex;
		if (dstIndex == srcIndex) {
			realDstIndex = dstIndex;
		}
		else if (dstIndex > srcIndex) {
			for (realDstIndex = srcIndex + 1; realDstIndex <= dstIndex; realDstIndex++) {
				if (light[realDstIndex]) {
					break;
				}
			}
			realDstIndex--;
		}
		else {
			for (realDstIndex = srcIndex + 1; realDstIndex < road.length; realDstIndex++) {
				if (light[realDstIndex]) {
					break;
				}
			}
			if (realDstIndex == road.length) {
				for (realDstIndex = 0; realDstIndex <= dstIndex; realDstIndex++) {
					if (light[realDstIndex]) {
						break;
					}
				}
				if (realDstIndex == 0) {
					realDstIndex = road.length - 1;
				}
				else {
					realDstIndex--;
				}
			}
			else {
				realDstIndex--;
			}
		}
		road[srcIndex] = false;
		road[realDstIndex] = true;

		return realDstIndex;
	}
	
    /**
     * carCheckメソッド
     * dstからnumマス先まで車がないことを調べる
     * @param dst 移動元
     * @param num 確認するマス数
     * @return 車があるならtrue
     */
	private synchronized boolean carCheck(int dst, int num) {
		if (dst + num < road.length) {
			for (int i = dst; i < dst + num; i++) {
				if (road[i]) {
					return true;
				}
			}
			return false;
		}
		else {
			for (int i = dst; i < road.length; i++) {
				if (road[i]) {
					return true;
				}
			}
			for (int i = 0; i < (dst + num) % road.length; i++) {
				if (road[i]) {
					return true;
				}
			}
			return false;
		}
	}

    /**
     * updateSynchronizedメソッド
     * 排他制御を行い車をsrcからdstに動かす
     * @param src 移動元
     * @param dst 移動先
     * @return 実際に動かせた移動先
     */
	public synchronized int updateSynchronized(int src, int dst) {
		// 更新権を得るまで待機
		while(! available)
			try {
				wait();
			} catch (Exception e) {}
		available = false;

		int srcIndex = src;
		int dstIndex = dst;
		int realDstIndex;
		// 移動元と移動先が同じ場合
		if (dstIndex == srcIndex) {
			realDstIndex = dstIndex;
		}
		// 移動先の方が大きいインデックス
		// 通常の場合
		else if (dstIndex > srcIndex) {
			for (realDstIndex = srcIndex + 1; realDstIndex <= dstIndex; realDstIndex++) {
				// 移動先に車があるか、信号により規制されている場合移動しない
				if (carCheck(realDstIndex, SPACE) || light[realDstIndex]) {
					break;
				}
			}
			realDstIndex--;
		}
		// 移動先の方が小さいインデックス
		// 左上の道路を0として割り振っているため、
		// 左上の真下などから移動する場合にインデックスの大小が反転することがある
		else {
			for (realDstIndex = srcIndex + 1; realDstIndex < road.length; realDstIndex++) {
				if (carCheck(realDstIndex, SPACE) || light[realDstIndex]) {
					break;
				}
			}
			if (realDstIndex == road.length) {
				for (realDstIndex = 0; realDstIndex <= dstIndex; realDstIndex++) {
					if (carCheck(realDstIndex, SPACE) || light[realDstIndex]) {
						break;
					}
				}
				if (realDstIndex == 0) {
					realDstIndex = road.length - 1;
				}
				else {
					realDstIndex--;
				}
			}
			else {
				realDstIndex--;
			}
		}
		road[srcIndex] = false;
		road[realDstIndex] = true;
		int ret;
		if (realDstIndex == dstIndex) {
			ret = realDstIndex;
		}
		else {
			ret = realDstIndex;
		}

		available = true;
		notifyAll();
		return ret;
	}

    /**
     * updateメソッド
     * 信号の色を変化させる
     * @param index 変化させる信号のインデックス
     * @param b 変化させる状態
     */
	public void update(int index, boolean b) {
		if (bp.getValue()) {
			updateSynchronized(index, b);
		}
		else {
			updateUnSynchronized(index, b);
		}
	}

    /**
     * updateSynchronizedメソッド
     * 排他制御を行い信号の色を変化させる
     * @param index 変化させる信号のインデックス
     * @param b 変化させる状態
     */
	public synchronized void updateSynchronized(int index, boolean b) {
		// 更新権を得るまで待機
		while(! available)
			try {
				wait();
			} catch (Exception e) {}
		available = false;

		// 信号を変化
		light[index] = b;

		available = true;
		notifyAll();
	}

    /**
     * updateUnSynchronizedメソッド
     * 排他制御を行わずに信号の色を変化させる
     * @param index 変化させる信号のインデックス
     * @param b 変化させる状態
     */
	public synchronized void updateUnSynchronized(int index, boolean b) {
		// 信号を変化
		light[index] = b;
	}
}