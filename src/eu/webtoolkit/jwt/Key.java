package eu.webtoolkit.jwt;


/**
 * Enumeration for key codes.
 * 
 * These are key codes that identify a key on a keyboard. All keys listed here
 * can be identified across all browsers and (Western) keyboards. A Key is
 * returned by {@link WKeyEvent#getKey()}. If you want to identify a character,
 * you should use the {@link WKeyEvent#getCharCode()} method instead.
 * <p>
 * 
 * @see WInteractWidget#keyWentDown()
 * @see WInteractWidget#keyWentUp()
 */
public enum Key {
	/**
	 * Unknown key.
	 */
	Key_unknown(0),
	/**
	 * Enter key.
	 */
	Key_Enter(13),
	/**
	 * Tab key.
	 */
	Key_Tab(9),
	/**
	 * Backspace key.
	 */
	Key_Backspace(8),
	/**
	 * Shift key.
	 */
	Key_Shift(16),
	/**
	 * Control key.
	 */
	Key_Control(17),
	/**
	 * Alt key.
	 */
	Key_Alt(18),
	/**
	 * Page up key.
	 */
	Key_PageUp(33),
	/**
	 * Page down key.
	 */
	Key_PageDown(34),
	/**
	 * End key.
	 */
	Key_End(35),
	/**
	 * Home key.
	 */
	Key_Home(36),
	/**
	 * Left arrow key.
	 */
	Key_Left(37),
	/**
	 * Up arrow key.
	 */
	Key_Up(38),
	/**
	 * Right arrow key.
	 */
	Key_Right(39),
	/**
	 * Down arrow key.
	 */
	Key_Down(40),
	/**
	 * Insert key.
	 */
	Key_Insert(45),
	/**
	 * Delete key.
	 */
	Key_Delete(46),
	/**
	 * Escape key.
	 */
	Key_Escape(27),
	/**
	 * F1 function key.
	 */
	Key_F1(112),
	/**
	 * F2 function key.
	 */
	Key_F2(113),
	/**
	 * F3 function key.
	 */
	Key_F3(114),
	/**
	 * F4 function key.
	 */
	Key_F4(115),
	/**
	 * F5 function key.
	 */
	Key_F5(116),
	/**
	 * F6 function key.
	 */
	Key_F6(117),
	/**
	 * F7 function key.
	 */
	Key_F7(118),
	/**
	 * F8 function key.
	 */
	Key_F8(119),
	/**
	 * F9 function key.
	 */
	Key_F9(120),
	/**
	 * F10 function key.
	 */
	Key_F10(121),
	/**
	 * F11 function key.
	 */
	Key_F11(122),
	/**
	 * F12 function key.
	 */
	Key_F12(123),
	/**
	 * Space.
	 */
	Key_Space(' '),
	/**
	 * &apos;A&apos; key
	 */
	Key_A('A'),
	/**
	 * &apos;B&apos; key
	 */
	Key_B('B'),
	/**
	 * &apos;C&apos; key
	 */
	Key_C('C'),
	/**
	 * &apos;D&apos; key
	 */
	Key_D('D'),
	/**
	 * &apos;E&apos; key
	 */
	Key_E('E'),
	/**
	 * &apos;F&apos; key
	 */
	Key_F('F'),
	/**
	 * &apos;G&apos; key
	 */
	Key_G('G'),
	/**
	 * &apos;H&apos; key
	 */
	Key_H('H'),
	/**
	 * &apos;I&apos; key
	 */
	Key_I('I'),
	/**
	 * &apos;J&apos; key
	 */
	Key_J('J'),
	/**
	 * &apos;K&apos; key
	 */
	Key_K('K'),
	/**
	 * &apos;L&apos; key
	 */
	Key_L('L'),
	/**
	 * &apos;M&apos; key
	 */
	Key_M('M'),
	/**
	 * &apos;N&apos; key
	 */
	Key_N('N'),
	/**
	 * &apos;O&apos; key
	 */
	Key_O('O'),
	/**
	 * &apos;P&apos; key
	 */
	Key_P('P'),
	/**
	 * &apos;Q&apos; key
	 */
	Key_Q('Q'),
	/**
	 * &apos;R&apos; key
	 */
	Key_R('R'),
	/**
	 * &apos;S&apos; key
	 */
	Key_S('S'),
	/**
	 * &apos;T&apos; key
	 */
	Key_T('T'),
	/**
	 * &apos;U&apos; key
	 */
	Key_U('U'),
	/**
	 * &apos;V&apos; key
	 */
	Key_V('V'),
	/**
	 * &apos;W&apos; key
	 */
	Key_W('W'),
	/**
	 * &apos;X&apos; key
	 */
	Key_X('X'),
	/**
	 * &apos;Y&apos; key
	 */
	Key_Y('Y'),
	/**
	 * &apos;Z&apos; key
	 */
	Key_Z('Z');

	private int value;

	Key(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
