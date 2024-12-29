/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Enumeration for key codes.
 *
 * <p>These are key codes that identify a key on a keyboard. All keys listed here can be identified
 * across all browsers and (Western) keyboards. A Key is returned by {@link WKeyEvent#getKey()}. If
 * you want to identify a character, you should use the {@link WKeyEvent#getCharCode()} method
 * instead.
 *
 * <p>
 *
 * @see WInteractWidget#keyWentDown()
 * @see WInteractWidget#keyWentUp()
 */
public enum Key {
  /** Unknown key. */
  Unknown(0),
  /** Enter key. */
  Enter(13),
  /** Tab key. */
  Tab(9),
  /** Backspace key. */
  Backspace(8),
  /** Shift key. */
  Shift(16),
  /** Control key. */
  Control(17),
  /** Alt key. */
  Alt(18),
  /** Page up key. */
  PageUp(33),
  /** Page down key. */
  PageDown(34),
  /** End key. */
  End(35),
  /** Home key. */
  Home(36),
  /** Left arrow key. */
  Left(37),
  /** Up arrow key. */
  Up(38),
  /** Right arrow key. */
  Right(39),
  /** Down arrow key. */
  Down(40),
  /** Insert key. */
  Insert(45),
  /** Delete key. */
  Delete(46),
  /** Escape key. */
  Escape(27),
  /** F1 function key. */
  F1(112),
  /** F2 function key. */
  F2(113),
  /** F3 function key. */
  F3(114),
  /** F4 function key. */
  F4(115),
  /** F5 function key. */
  F5(116),
  /** F6 function key. */
  F6(117),
  /** F7 function key. */
  F7(118),
  /** F8 function key. */
  F8(119),
  /** F9 function key. */
  F9(120),
  /** F10 function key. */
  F10(121),
  /** F11 function key. */
  F11(122),
  /** F12 function key. */
  F12(123),
  /** Space. */
  Space(' '),
  /** &apos;A&apos; key */
  A('A'),
  /** &apos;B&apos; key */
  B('B'),
  /** &apos;C&apos; key */
  C('C'),
  /** &apos;D&apos; key */
  D('D'),
  /** &apos;E&apos; key */
  E('E'),
  /** &apos;F&apos; key */
  F('F'),
  /** &apos;G&apos; key */
  G('G'),
  /** &apos;H&apos; key */
  H('H'),
  /** &apos;I&apos; key */
  I('I'),
  /** &apos;J&apos; key */
  J('J'),
  /** &apos;K&apos; key */
  K('K'),
  /** &apos;L&apos; key */
  L('L'),
  /** &apos;M&apos; key */
  M('M'),
  /** &apos;N&apos; key */
  N('N'),
  /** &apos;O&apos; key */
  O('O'),
  /** &apos;P&apos; key */
  P('P'),
  /** &apos;Q&apos; key */
  Q('Q'),
  /** &apos;R&apos; key */
  R('R'),
  /** &apos;S&apos; key */
  S('S'),
  /** &apos;T&apos; key */
  T('T'),
  /** &apos;U&apos; key */
  U('U'),
  /** &apos;V&apos; key */
  V('V'),
  /** &apos;W&apos; key */
  W('W'),
  /** &apos;X&apos; key */
  X('X'),
  /** &apos;Y&apos; key */
  Y('Y'),
  /** &apos;Z&apos; key */
  Z('Z'),
  /** &apos;1&apos; key */
  Key_1('1'),
  /** &apos;2&apos; key */
  Key_2('2'),
  /** &apos;3&apos; key */
  Key_3('3'),
  /** &apos;4&apos; key */
  Key_4('4'),
  /** &apos;5&apos; key */
  Key_5('5'),
  /** &apos;6&apos; key */
  Key_6('6'),
  /** &apos;7&apos; key */
  Key_7('7'),
  /** &apos;8&apos; key */
  Key_8('8'),
  /** &apos;9&apos; key */
  Key_9('9'),
  /** &apos;0&apos; key */
  Key_0('0');

  private int value;

  Key(int value) {
    this.value = value;
  }

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return value;
  }
}
