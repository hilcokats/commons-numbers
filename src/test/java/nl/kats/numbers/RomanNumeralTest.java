package nl.kats.numbers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RomanNumeralTest {

    @Test
    void testExceptions() {
        // Use same vinculum level twice
        assertThrows(NumberFormatException.class, () -> RomanNumeral.parse("X'V'"));
        assertThrows(NumberFormatException.class, () -> RomanNumeral.of("X'V'"));
        // increasing vinculum level
        assertThrows(NumberFormatException.class, () -> RomanNumeral.parse("X'V''"));
        assertThrows(NumberFormatException.class, () -> RomanNumeral.of("X'V''"));
        // Vinculum parts higher than 999
        assertThrows(NumberFormatException.class, () -> RomanNumeral.of("MMMCMXCIX'").intValue());
        // Not a strict value
        assertThrows(NumberFormatException.class, () -> RomanNumeral.ofStrict("VV"));
        assertThrows(NumberFormatException.class, () -> RomanNumeral.ofStrict("-V"));
        assertThrows(NumberFormatException.class, () -> RomanNumeral.ofStrict("X'"));
        // Not a valid symbol
        assertThrows(NumberFormatException.class, () -> RomanNumeral.ofStrict("Y'"));
    }

    @Test
    void testExtremes() {
        // From long to roman
        assertEquals("-IX''''''CCXXIII'''''CCCLXXII''''XXXVI'''DCCCLIV''DCCLXXV'DCCCVIII", RomanNumeral.parse(Long.MIN_VALUE));
        assertEquals( "IX''''''CCXXIII'''''CCCLXXII''''XXXVI'''DCCCLIV''DCCLXXV'DCCCVII", RomanNumeral.parse(Long.MAX_VALUE));

        // From roman to long
        assertEquals(Long.MIN_VALUE, RomanNumeral.parse("-IX''''''CCXXIII'''''CCCLXXII''''XXXVI'''DCCCLIV''DCCLXXV'DCCCVIII"));
        assertEquals(Long.MAX_VALUE, RomanNumeral.parse( "IX''''''CCXXIII'''''CCCLXXII''''XXXVI'''DCCCLIV''DCCLXXV'DCCCVII"));
    }

    @Test
    void testVinculum() {
        // Incorrect, but interpretable values, are internally represented as the strictest possible representation
        assertEquals(1001, RomanNumeral.of("I'I").intValue());
        assertEquals("MI", RomanNumeral.of("I'I").uppercase());

        // From long to roman
        assertEquals("IV'", RomanNumeral.parse(4000));
        assertEquals("IV'I", RomanNumeral.parse(4001));
        assertEquals("IV'CMXCIX", RomanNumeral.parse(4999));
        assertEquals("XCIX'CMXCIX", RomanNumeral.parse(99999));
        assertEquals("CMXCIX'CMXCIX", RomanNumeral.parse(999999));
        assertEquals("III''CMXCIX'CMXCIX", RomanNumeral.parse(3999999));

        assertEquals("-IV'", RomanNumeral.parse(-4000));
        assertEquals("-IV'I", RomanNumeral.parse(-4001));
        assertEquals("-IV'CMXCIX", RomanNumeral.parse(-4999));
        assertEquals("-XCIX'CMXCIX", RomanNumeral.parse(-99999));
        assertEquals("-CMXCIX'CMXCIX", RomanNumeral.parse(-999999));
        assertEquals("-III''CMXCIX'CMXCIX", RomanNumeral.parse(-3999999));

        // From roman to long
        assertEquals(4000, RomanNumeral.parse("IV'"));
        assertEquals(4001, RomanNumeral.parse("IV'I"));
        assertEquals(4999, RomanNumeral.parse("IV'CMXCIX"));
        assertEquals(99999, RomanNumeral.parse("XCIX'CMXCIX"));
        assertEquals(999999, RomanNumeral.parse("CMXCIX'CMXCIX"));
        assertEquals(3999999, RomanNumeral.parse("III''CMXCIX'CMXCIX"));

        assertEquals(-4000, RomanNumeral.parse("-IV'"));
        assertEquals(-4001, RomanNumeral.parse("-IV'I"));
        assertEquals(-4999, RomanNumeral.parse("-IV'CMXCIX"));
        assertEquals(-99999, RomanNumeral.parse("-XCIX'CMXCIX"));
        assertEquals(-999999, RomanNumeral.parse("-CMXCIX'CMXCIX"));
        assertEquals(-3999999, RomanNumeral.parse("-III''CMXCIX'CMXCIX"));
    }

    @Test
    void testStrict() {
        assertTrue(RomanNumeral.of("V").isStrict());
        assertFalse(RomanNumeral.of("-V").isStrict());
        assertTrue(RomanNumeral.of("VX").isStrict());
    }

    @Test
    void testLength() {
        assertEquals(1, RomanNumeral.of(1).length());
        assertEquals(1, RomanNumeral.of("I").length());
        assertEquals(1, RomanNumeral.of("IIIIV").length());
    }

    @Test
    void testParse() {
        assertEquals("I", RomanNumeral.parse(1));
        assertEquals("II", RomanNumeral.parse(2));
        assertEquals("III", RomanNumeral.parse(3));
        assertEquals("IV", RomanNumeral.parse(4));
        assertEquals("V", RomanNumeral.parse(5));

        assertEquals("-I", RomanNumeral.parse(-1));
        assertEquals("-II", RomanNumeral.parse(-2));
        assertEquals("-III", RomanNumeral.parse(-3));
        assertEquals("-IV", RomanNumeral.parse(-4));
        assertEquals("-V", RomanNumeral.parse(-5));
    }

    @Test
    void testParseString() {
        assertEquals(1, RomanNumeral.parse("I"));
        assertEquals(2, RomanNumeral.parse("II"));
        assertEquals(3, RomanNumeral.parse("III"));
        assertEquals(4, RomanNumeral.parse("IV"));
        assertEquals(5, RomanNumeral.parse("V"));

        assertEquals(-1, RomanNumeral.parse("-I"));
        assertEquals(-2, RomanNumeral.parse("-II"));
        assertEquals(-3, RomanNumeral.parse("-III"));
        assertEquals(-4, RomanNumeral.parse("-IV"));
        assertEquals(-5, RomanNumeral.parse("-V"));
    }

    @Test
    void testZero() {
        // Strictest, vinculum notation is used internally
        assertEquals("'", RomanNumeral.ZERO.toString());
        assertEquals("'", RomanNumeral.ZERO.uppercase());
        assertEquals("'", RomanNumeral.ZERO.lowercase());
        assertEquals(RomanNumeral.of(0), RomanNumeral.ZERO);
        assertEquals(RomanNumeral.of("'"), RomanNumeral.ZERO);
        assertEquals("'", RomanNumeral.of(0).toString());
        // Alternative notations for creating zero
        assertEquals(RomanNumeral.of(0), RomanNumeral.of(""));
        assertEquals(RomanNumeral.of(0), RomanNumeral.of("VVX"));
    }

    @Test
    void testNegativeNumbers() {
        assertEquals(RomanNumeral.of(-5), RomanNumeral.of("VVVX"));
        assertEquals(RomanNumeral.of("-I"), RomanNumeral.of(-1));
        assertEquals(RomanNumeral.of(-1), RomanNumeral.of("IVVX"));
    }

    @Test
    void testArabicVersusRoman() {
        assertEquals(RomanNumeral.of(""), RomanNumeral.of(0));
        assertEquals(RomanNumeral.of("I"), RomanNumeral.of(1));
        assertEquals(RomanNumeral.of("II"), RomanNumeral.of(2));
        assertEquals(RomanNumeral.of("III"), RomanNumeral.of(3));
        assertEquals(RomanNumeral.of("IV"), RomanNumeral.of(4));
        assertEquals(RomanNumeral.of("V"), RomanNumeral.of(5));
        assertEquals(RomanNumeral.of("VI"), RomanNumeral.of(6));
        assertEquals(RomanNumeral.of("VII"), RomanNumeral.of(7));
        assertEquals(RomanNumeral.of("VIII"), RomanNumeral.of(8));
        assertEquals(RomanNumeral.of("IX"), RomanNumeral.of(9));
        assertEquals(RomanNumeral.of("X"), RomanNumeral.of(10));
        assertEquals(RomanNumeral.of("XI"), RomanNumeral.of(11));
        assertEquals(RomanNumeral.of("XII"), RomanNumeral.of(12));
        assertEquals(RomanNumeral.of("XIII"), RomanNumeral.of(13));
        assertEquals(RomanNumeral.of("XIV"), RomanNumeral.of(14));
        assertEquals(RomanNumeral.of("XV"), RomanNumeral.of(15));
        assertEquals(RomanNumeral.of("XVI"), RomanNumeral.of(16));
        assertEquals(RomanNumeral.of("XVII"), RomanNumeral.of(17));
        assertEquals(RomanNumeral.of("XVIII"), RomanNumeral.of(18));
        assertEquals(RomanNumeral.of("XIX"), RomanNumeral.of(19));
        assertEquals(RomanNumeral.of("XX"), RomanNumeral.of(20));
        assertEquals(RomanNumeral.of("XXI"), RomanNumeral.of(21));
        assertEquals(RomanNumeral.of("XXII"), RomanNumeral.of(22));
        assertEquals(RomanNumeral.of("XXIII"), RomanNumeral.of(23));
        assertEquals(RomanNumeral.of("XXIV"), RomanNumeral.of(24));
        assertEquals(RomanNumeral.of("XXX"), RomanNumeral.of(30));
        assertEquals(RomanNumeral.of("XL"), RomanNumeral.of(40));
        assertEquals(RomanNumeral.of("L"), RomanNumeral.of(50));
        assertEquals(RomanNumeral.of("LX"), RomanNumeral.of(60));
        assertEquals(RomanNumeral.of("LXX"), RomanNumeral.of(70));
        assertEquals(RomanNumeral.of("LXXX"), RomanNumeral.of(80));
        assertEquals(RomanNumeral.of("XC"), RomanNumeral.of(90));
        assertEquals(RomanNumeral.of("C"), RomanNumeral.of(100));
        assertEquals(RomanNumeral.of("CI"), RomanNumeral.of(101));
        assertEquals(RomanNumeral.of("CII"), RomanNumeral.of(102));
        assertEquals(RomanNumeral.of("CC"), RomanNumeral.of(200));
        assertEquals(RomanNumeral.of("CCC"), RomanNumeral.of(300));
        assertEquals(RomanNumeral.of("CD"), RomanNumeral.of(400));
        assertEquals(RomanNumeral.of("CDLXVIII"), RomanNumeral.of(468));
        assertEquals(RomanNumeral.of("D"), RomanNumeral.of(500));
        assertEquals(RomanNumeral.of("DC"), RomanNumeral.of(600));
        assertEquals(RomanNumeral.of("DCC"), RomanNumeral.of(700));
        assertEquals(RomanNumeral.of("DCCC"), RomanNumeral.of(800));
        assertEquals(RomanNumeral.of("CM"), RomanNumeral.of(900));
        assertEquals(RomanNumeral.of("M"), RomanNumeral.of(1000));
        assertEquals(RomanNumeral.of("MI"), RomanNumeral.of(1001));
        assertEquals(RomanNumeral.of("MII"), RomanNumeral.of(1002));
        assertEquals(RomanNumeral.of("MIII"), RomanNumeral.of(1003));
        assertEquals(RomanNumeral.of("MDCCCXLIX"), RomanNumeral.of(1849));
        assertEquals(RomanNumeral.of("MCM"), RomanNumeral.of(1900));
        assertEquals(RomanNumeral.of("MCMXCIV"), RomanNumeral.of(1994));
        assertEquals(RomanNumeral.of("MM"), RomanNumeral.of(2000));
        assertEquals(RomanNumeral.of("MMI"), RomanNumeral.of(2001));
        assertEquals(RomanNumeral.of("MMII"), RomanNumeral.of(2002));
        assertEquals(RomanNumeral.of("MMC"), RomanNumeral.of(2100));
        assertEquals(RomanNumeral.of("MMM"), RomanNumeral.of(3000));
    }

    @Test
    void testStrictBackAndForth() {
        for (int i = 1; i <= 3999; i++) {
            var roman = RomanNumeral.of(i);

            var arabic = RomanNumeral.of(roman.intValue());
            var romanAgain = RomanNumeral.of(arabic.intValue());

            assertEquals(i, arabic.intValue());
            assertEquals(roman, romanAgain);
        }
    }

    @Test
    void testLooseBackAndForth() {
        for (int i = 0; i <= 10_000; i++) {
            var roman = RomanNumeral.of(i);

            var arabic = RomanNumeral.of(roman.intValue());
            var romanAgain = RomanNumeral.of(arabic.intValue());

            assertEquals(i, arabic.intValue());
            assertEquals(roman, romanAgain);
        }
    }

    @Test
    void testPlus() {
        var i = RomanNumeral.of("I");
        var ii = RomanNumeral.of("II");
        var iv = RomanNumeral.of("IV");
        assertEquals(ii, i.plus(i));
        assertEquals(iv, i.plus(i));
    }

    @Test
    void testMinus() {
        var i = RomanNumeral.of("I");
        var ii = RomanNumeral.of("II");
        var iii = RomanNumeral.of("III");
        assertEquals(ii, iii.minus(i));
        assertEquals(i, iii.minus(i));
    }

    @Test
    void testIsValid() {
        // 4
        assertFalse(RomanNumeral.isStrict("IIII"));
        assertTrue(RomanNumeral.of("IIII").isStrict());
        assertTrue(RomanNumeral.isStrict("IV"));
        assertTrue(RomanNumeral.of("IV").isStrict());

        // 49
        assertFalse(RomanNumeral.isStrict("IL"));
        assertTrue(RomanNumeral.of("IL").isStrict());
        assertTrue(RomanNumeral.isStrict("XLIX"));
        assertTrue(RomanNumeral.of("XLIX").isStrict());

        // 4,000
        assertFalse(RomanNumeral.isStrict("MMMM"));
        assertFalse(RomanNumeral.of("MMMM").isStrict());
        assertFalse(RomanNumeral.isStrict("IV'"));
        assertFalse(RomanNumeral.of("IV'").isStrict());

        // 4,004,004
        assertFalse(RomanNumeral.isStrict("IV''IV'IV"));
        assertFalse(RomanNumeral.of("IV''IV'IV").isStrict());
    }

    @Test
    void testFallBackToStrict() {
        // 0
        var empty = RomanNumeral.of("");
        var vvx = RomanNumeral.of("VVX");
        assertEquals(empty, vvx);
        assertEquals(empty.toString(), vvx.toString());
        assertEquals(vvx.toString(), "'");

        // 4
        var iiii = RomanNumeral.of("IIII");
        var iv = RomanNumeral.of("IV");
        assertEquals(iiii, iv);
        assertEquals(iiii.toString(), iv.toString());
        assertEquals(iiii.toString(), "IV");

        // 49
        var il = RomanNumeral.of("IL");
        var xlix = RomanNumeral.of("XLIX");
        assertEquals(il, xlix);
        assertEquals(il.toString(), xlix.toString());
        assertEquals(il.toString(), "XLIX");

        // 334
        var ivxlcdm = RomanNumeral.of("IVXLCDM");
        var cccxxxiv = RomanNumeral.of("CCCXXXIV");
        assertEquals(ivxlcdm, cccxxxiv);
        assertEquals(ivxlcdm.toString(), cccxxxiv.toString());
        assertEquals(ivxlcdm.toString(), "CCCXXXIV");

        // 1999
        var mim = RomanNumeral.of("MIM");
        var mcmxcix = RomanNumeral.of("MCMXCIX");
        assertEquals(mim, mcmxcix);
        assertEquals(mim.toString(), mcmxcix.toString());
        assertEquals(mim.toString(), "MCMXCIX");
    }

    @Test
    void testLowerCase() {
        assertEquals(RomanNumeral.of("iv").toString(), "IV");
    }

    @Test
    void testSum() {
        assertEquals(RomanNumeral.of("III"), RomanNumeral.sum(RomanNumeral.of("I"), RomanNumeral.of("II")));
        assertEquals("III", RomanNumeral.sum("I", "II"));
    }

    @Test
    void testMax() {
        assertEquals(RomanNumeral.of("I"), RomanNumeral.max(RomanNumeral.of("I"), RomanNumeral.of("I")));
        assertEquals(RomanNumeral.of("V"), RomanNumeral.max(RomanNumeral.of("I"), RomanNumeral.of("V")));
        assertEquals(RomanNumeral.of("X"), RomanNumeral.max(RomanNumeral.of("V"), RomanNumeral.of("X")));
        assertEquals(RomanNumeral.of("L"), RomanNumeral.max(RomanNumeral.of("X"), RomanNumeral.of("L")));
        assertEquals(RomanNumeral.of("C"), RomanNumeral.max(RomanNumeral.of("L"), RomanNumeral.of("C")));
        assertEquals(RomanNumeral.of("D"), RomanNumeral.max(RomanNumeral.of("C"), RomanNumeral.of("D")));
        assertEquals(RomanNumeral.of("M"), RomanNumeral.max(RomanNumeral.of("D"), RomanNumeral.of("M")));
    }

    @Test
    void testMin() {
        assertEquals(RomanNumeral.of("I"), RomanNumeral.min(RomanNumeral.of("I"), RomanNumeral.of("I")));
        assertEquals(RomanNumeral.of("I"), RomanNumeral.min(RomanNumeral.of("I"), RomanNumeral.of("V")));
        assertEquals(RomanNumeral.of("V"), RomanNumeral.min(RomanNumeral.of("V"), RomanNumeral.of("X")));
        assertEquals(RomanNumeral.of("X"), RomanNumeral.min(RomanNumeral.of("X"), RomanNumeral.of("L")));
        assertEquals(RomanNumeral.of("L"), RomanNumeral.min(RomanNumeral.of("L"), RomanNumeral.of("C")));
        assertEquals(RomanNumeral.of("C"), RomanNumeral.min(RomanNumeral.of("C"), RomanNumeral.of("D")));
        assertEquals(RomanNumeral.of("D"), RomanNumeral.min(RomanNumeral.of("D"), RomanNumeral.of("M")));
    }

    @Test
    void testUppercase() {
        assertEquals("MDCCCXLIX", RomanNumeral.of(1849).uppercase());
        assertEquals("MDCCCXLIX", RomanNumeral.of("MDCCCXLIX").uppercase());
        assertEquals("MDCCCXLIX", RomanNumeral.of("mdcccxlix").uppercase());
    }

    @Test
    void testLowercase() {
        assertEquals("mdcccxlix", RomanNumeral.of(1849).lowercase());
        assertEquals("mdcccxlix", RomanNumeral.of("MDCCCXLIX").lowercase());
        assertEquals("mdcccxlix", RomanNumeral.of("mdcccxlix").lowercase());
    }
}
