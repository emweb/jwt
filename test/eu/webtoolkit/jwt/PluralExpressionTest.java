package eu.webtoolkit.jwt;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PluralExpressionTest {
	int eval(final String expression, long n) {
		return PluralExpression.evalPluralCase(expression, n);
	}
	
    @Test
    public void basicExpressionTest() {
    	{
	    	String e;
	    	
	    	e = "1 + 2";
	    	assertEquals(3, eval(e, -1));
	    	
	    	e = "1 + n";
	    	assertEquals(3, eval(e, 2));
	    	
	    	e = "3 - n";
	    	assertEquals(1, eval(e, 2));
	    	
	    	e = "3 * n";
	    	assertEquals(6, eval(e, 2));
	    	
	    	e = "4 / n";
	    	assertEquals(2, eval(e, 2));
	    	
	    	e = "5 % n";
	    	assertEquals(1, eval(e, 2));
	    	
	    	e = "(5 + n) * (n + 2) + (n * n)";
	    	assertEquals(49, eval(e, 3));
	    	
	    	e = "n == 4";
	    	assertEquals(1, eval(e, 4));
    	}
    	
    	{
    		final String e = "n == 3";
    		final String e2 = "n != 3";
    		assertEquals(0, eval(e, 4));
    		assertEquals(1, eval(e2, 4));
    		assertEquals(0, eval(e + " && " + e2, 4));
    		assertEquals(1, eval(e + " || " + e2, 4));
    		
    		final String te = e + " ? n + 3 : n * n";
    		assertEquals(6, eval(te, 3));
    		assertEquals(16, eval(te, 4));
    	}
    	
    	{
    		final String lt_e = "n < 3";
    		assertEquals(1, eval(lt_e, 2));
    		assertEquals(0, eval(lt_e, 3));
    		assertEquals(0, eval(lt_e, 4));
    		
    		final String lte_e = "n <= 3";
    		assertEquals(1, eval(lte_e, 2));
    		assertEquals(1, eval(lte_e, 3));
    		assertEquals(0, eval(lte_e, 4));
    		
    		final String gt_e = "n > 3";
    		assertEquals(0, eval(gt_e, 2));
    		assertEquals(0, eval(gt_e, 3));
    		assertEquals(1, eval(gt_e, 4));
    		
    		final String gte_e = "n >= 3";
    		assertEquals(0, eval(gte_e, 2));
    		assertEquals(1, eval(gte_e, 3));
    		assertEquals(1, eval(gte_e, 4));
    		
    		final String combined =
    				lt_e + " || " + lte_e + " && " + gt_e + "&&" + gte_e;
    		assertEquals(1, eval(combined, 2));
    		assertEquals(0, eval(combined, 3));
    		assertEquals(0, eval(combined, 4));
    	}
    	
    	{
    		final String e = "2 + 3 * n";
    		assertEquals(8, eval(e, 2));
    	}
    	
    	{
    		String e = "2 < 3 == n";
    		assertEquals(0, eval(e, 2));
    		
    		e += " || 1";
    		assertEquals(1, eval(e, 2));
    		
    		e += " ? 2 : 4";
    		assertEquals(2, eval(e, 2));
    	}
    }
    
    @Test
    public void basicLanguageTest() {
    	{
    		// Polish
    		final String e = "n==1 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2";
    		assertEquals(0, eval(e, 1));
    		assertEquals(1, eval(e, 3));
    		assertEquals(1, eval(e, 22));
    		assertEquals(2, eval(e, 6));
    		assertEquals(2, eval(e, 30));
    	}
    	
    	{
    		// Japanese/Vietnamese/Korean
    		final String e = "0";
    		assertEquals(0, eval(e, 0));
    		assertEquals(0, eval(e, 1));
    		assertEquals(0, eval(e, 3));
    		assertEquals(0, eval(e, 22));
    	}
    	
    	{
    		// English, German, Dutch, Swedish, Danish, Norwegian, Faroese,
    		// Spanish, Portuguese, Italian, Bulgarian
    		// Greek
    		// Finnish, Estonian, Hungarian
    		// Hebrew
    		// Esperanto
    		// Turkish
    		final String e = "n != 1";
    		assertEquals(1, eval(e, 0));
    		assertEquals(0, eval(e, 1));
    		assertEquals(1, eval(e, 3));
    		assertEquals(1, eval(e, 22));
    	}
    	
    	{
    		// Brazilian Portuguese, French
    		final String e = "n > 1";
    		assertEquals(0, eval(e, 0));
    		assertEquals(0, eval(e, 1));
    		assertEquals(1, eval(e, 3));
    		assertEquals(1, eval(e, 22));
    	}
    	
    	{
    		// Russian, Ukrainian, Serbian, Croatian
    		final String e = "n%10==1 && n%100!=11 ? 0 :" +
    				"n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2";
    		
    		assertEquals(2, eval(e, 0));
    		assertEquals(0, eval(e, 1));
    		assertEquals(1, eval(e, 2));
    		assertEquals(1, eval(e, 3));
    		assertEquals(1, eval(e, 4));
    		
    		assertEquals(2, eval(e, 11));
    		assertEquals(2, eval(e, 12));
    		assertEquals(2, eval(e, 13));
    		assertEquals(2, eval(e, 14));
    		
    		assertEquals(2, eval(e, 211));
    		assertEquals(2, eval(e, 212));
    		assertEquals(2, eval(e, 213));
    		assertEquals(2, eval(e, 214));
    		
    		assertEquals(0, eval(e, 201));
    		assertEquals(1, eval(e, 202));
    		assertEquals(1, eval(e, 203));
    		assertEquals(1, eval(e, 204));
    	}
    }
}
