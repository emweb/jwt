/*
 * Copyright (C) 2009 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/**
 * 
 */
package eu.webtoolkit.jwt.utils;

import java.util.EnumSet;

import eu.webtoolkit.jwt.AlignmentFlag;
import eu.webtoolkit.jwt.AnimationEffect;
import eu.webtoolkit.jwt.Key;
import eu.webtoolkit.jwt.KeyboardModifier;
import eu.webtoolkit.jwt.RenderHint;
import eu.webtoolkit.jwt.ValidationStyleFlag;

/**
 * @author wim
 * 
 */
public class EnumUtils {

	public static <E extends Enum<E>> EnumSet<E> mask(EnumSet<E> theSet, E mask) {
		EnumSet<E> retval = EnumSet.copyOf(theSet);
		EnumSet<E> theMask = EnumSet.of(mask);
		retval.retainAll(theMask);
		return retval;
	}

	public static <E extends Enum<E>> EnumSet<E> mask(EnumSet<E> theSet, EnumSet<E> mask) {
		EnumSet<E> retval = EnumSet.copyOf(theSet);
		retval.retainAll(mask);
		return retval;
	}

	public static <E extends Enum<E>> E enumFromSet(EnumSet<E> theSet) {
		if (theSet.isEmpty()) {
			return null;
		} else {
			return theSet.iterator().next();
		}
	}

	public static <E extends Enum<E>> EnumSet<E> setOnly(EnumSet<E> theSet, E flag) {
		theSet.clear();
		theSet.add(flag);
		return theSet;
	}

	public static Key keyFromValue(int key) {
		for (Key k : Key.values()) {
			if (k.getValue() == key)
				return k;
		}

		return Key.Unknown;
	}

	public static <E extends Enum<E>> EnumSet<E> or(EnumSet<E> theSet, E flag) {
		EnumSet<E> retval = EnumSet.copyOf(theSet);
		retval.add(flag);
		return retval;
	}

	public static <E extends Enum<E>> E max(E e1, E e2) {
		return e1.ordinal() > e2.ordinal() ? e1 : e2;
	}
	
	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> int valueOf(EnumSet<E> enumSet) {
		if (!enumSet.isEmpty()) {
			Object o = enumSet.iterator().next();
			
			if (o instanceof AnimationEffect)
				return valueOfAnimationEffects(enumSet);
			else if (o instanceof ValidationStyleFlag)
				return valueOfValidationStyleFlags(enumSet);
			else if (o instanceof KeyboardModifier)
				return valueOfKeyboardModifiers(enumSet);
			else if (o instanceof AlignmentFlag)
				return valueOfAlignmentFlags(enumSet);
			else if (o instanceof RenderHint)
				return valueOfRenderHints(enumSet);
			else
				throw new RuntimeException("Not supported valueOf()");
		} else
			return 0;
	}

	public static int valueOf(int v) {
		return v;
	}
	
	public static <E extends Enum<E>> int valueOfAnimationEffects(EnumSet<E> enumSet) {
		int result = 0;

		if (enumSet.contains(AnimationEffect.SlideInFromLeft))
			result = 0x1;
		else if (enumSet.contains(AnimationEffect.SlideInFromRight))
			result = 0x2;
		else if (enumSet.contains(AnimationEffect.SlideInFromBottom))
			result = 0x3;
		else if (enumSet.contains(AnimationEffect.SlideInFromTop))
			result = 0x4;
		else if (enumSet.contains(AnimationEffect.Pop))
			result = 0x5;
		
		if (enumSet.contains(AnimationEffect.Fade))
			result |= 0x100;
		
		return result;
	}
	
	private static <E extends Enum<E>> int valueOfValidationStyleFlags(EnumSet<E> enumSet) {
		int result = 0;

		// TODO: could replace this with a loop that uses result |= 1 << v.ordinal()
		
		if (enumSet.contains(ValidationStyleFlag.InvalidStyle))
			result |= 0x1;
		if (enumSet.contains(ValidationStyleFlag.ValidStyle))
			result |= 0x2;
		
		return result;
	}

	private static <E extends Enum<E>> int valueOfKeyboardModifiers(EnumSet<E> enumSet) {
		int result = 0;

		for (Enum<E> e : enumSet) {
			if (e.ordinal() != 0)
				result |= 1 << (e.ordinal() - 1);
		}

		return result;
	}

	private static <E extends Enum<E>> int valueOfAlignmentFlags(EnumSet<E> enumSet) {
		int result = 0;

		for (Enum<E> e : enumSet) {
			result |= 1 << e.ordinal();
		}

		return result;
	}

	private static <E extends Enum<E>> int valueOfRenderHints(EnumSet<E> enumSet) {
		int result = 0;

		for (Enum<E> e : enumSet) {
			result |= 1 << e.ordinal();
		}

		return result;
	}
}
