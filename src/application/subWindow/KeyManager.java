package application.subWindow;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class KeyManager {
	// 押下中のボタンを保存する
	private Map<KeyCode, Boolean> map = new HashMap<>();

    /**
     * handleEventメソッド
     * キーボードの押す・離すを判定する
     * @param event KeyEvent
     */
	public void handleEvent(KeyEvent event) {
		if (event.getEventType() == KeyEvent.KEY_PRESSED) {
			map.put(event.getCode(), true);
		}
		else if (event.getEventType() == KeyEvent.KEY_RELEASED) {
			map.put(event.getCode(), false);
		}
	}

    /**
     * isPressedtメソッド
     * パラメータのキーワードが押下中か調べる
     * @param code 調べるキーボードのコード
     * @return 押下中ならtrue
     */
	public boolean isPressed(KeyCode code) {
		return map.containsKey(code) ? map.get(code) : false;
	}
}
