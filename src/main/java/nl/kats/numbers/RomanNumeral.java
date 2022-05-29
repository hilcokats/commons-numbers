package nl.kats.numbers;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The {@code RomanNumeral} class represents a roman numeral {@link String} as a {@link Number}, and provides several
 * methods for converting {@link String} representations to long values and vice versa, as well as validation methods.
 *
 * If the String representation adheres to the following rules, it is said to be a strict:
 * - The letters I, X, C can be repeated thrice in succession. Additionally, L, V, D cannot be repeated at all.
 * - If a lower value digit is written to the left of a higher value digit, it is subtracted.
 * - If a lower value digit is written to the right of a higher value digit, it is added.
 * - Only I, X, and C can be used as subtractive numerals.
 *
 * From these rules, it follows that roman numerals cannot strictly be used to represent negative values, zero, or any
 * value greater than 3999. This class however, also supports long values outside of this range, by representing
 * negative values with a signum (minus character, '-') and zero, values higher than 3999, and values lower than -3999,
 * using the vinculum notation.
 *
 * The vinculum was common extension to roman numerals used in the middle ages to indicate a value should be multiplied
 * by 1000 by overlining value. Similarly, two vinculumns (double overline) indicates multiplication by 1 million. The
 * vinculum in this implementation is represented by a apostrophe at the end of the part that needs to be multiplied.
 * One apostrophe indicates x1000, two apostrophes indicates times 1 million, etc. For example, 10200 is written X'CC.
 *
 * Additional to the extended notation, a {@link RomanNumeral} may be constructed using a value that doesn't follow the
 * first and last rule above, when it can still be considered interpretable. This way roman numerals that are not
 * strictly valid, can still be interpreted. For example, IIII for 4, as often used on clock faces, as an alternative
 * for IV.
 *
 * No matter how a {@link RomanNumeral} is constructed though, its internal String representation is always kept as
 * strict as possible, using a signum and / or a vinculum notation if needed. Thus 4 is always represented as IV, even
 * if it was constructed using the value IIII. Negative values, will always use the signum, so -5 is internally
 * represented as -V instead of something like VVVX, as VVVX (arguably) breaks more rules than -V. Values larger than
 * 3999 will always use the vinculum notation, so 4000 is represented as IV', even it was constructed using MMMM. Zero
 * is a special case, and represented by a single vinculum, without other symbols, which you could read as 0x1000 + 0.
 *
 * @author Hilco Kats
 */
public class RomanNumeral extends Number implements Comparable<RomanNumeral> {

    private static final long serialVersionUID = 2183928311928429L;

    private static final Pattern STRICT = Pattern.compile("^M{0,3}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})$");

    /**
     * A constant holding the maximum value a {@code RomanNumeral} can have, equivalent to {@link Long#MAX_VALUE}.
     */
    public static final RomanNumeral MAX_VALUE = RomanNumeral.of(Long.MAX_VALUE);

    /**
     * A constant holding the minimum value a {@code RomanNumeral} can have, equivalent to {@link Long#MIN_VALUE}.
     */
    public static final RomanNumeral MIN_VALUE = RomanNumeral.of(Long.MIN_VALUE);

    /**
     * A constant holding the value zero, which is represented by an apostrophe, which reads as 0x1000 + 0 = 0.
     */
    public static final RomanNumeral ZERO = RomanNumeral.of(0);

    /**
     * A constant holding the maximum value a RomanNumeral can have under the strictest definition; i.e. MMMCMXCIX
     * or 3999.
     */
    public static final RomanNumeral STRICT_MAX_VALUE = RomanNumeral.of(3999);

    /**
     * A constant holding the minimum value a RomanNumeral can have under the strictest definition; i.e. I or 1.
     */
    public static final RomanNumeral STRICT_MIN_VALUE = RomanNumeral.of(1);

    public enum Symbol {
        I(1), V(5), X(10), L(50), C(100), D(500), M(1000);

        final int value;

        Symbol(int value) {
            this.value = value;
        }
    }

    public static final String VINCULUM = "'";

    private String roman;
    private long arabic;

    private RomanNumeral(long arabic) {
        this.roman = parse(arabic);
        this.arabic = arabic;
    }

    /**
     * Creates a {@link RomanNumeral} instance for the given long value.
     *
     * @return RomanNumeral instance that is equivalent with the given int value.
     */
    public static RomanNumeral of(long arabic) {
        return new RomanNumeral(arabic);
    }

    /**
     * Creates a {@link RomanNumeral} instance based on the given String value, as long as this value can be interpreted
     * as such. Any value that cannot be parsed will cause a NumberFormatException.
     *
     * Note that the internal String representation of the resulting RomanNumeral will be as strict as possible, using
     * a signum and / or vinculum notation if needed. As a consequence this is not necessarily equal to the given value.
     *
     * @param roman String representation of the roman numeral
     * @throws NumberFormatException if roman is not an interpretable representation of a roman numeral
     */
    public static RomanNumeral of(String roman) {
        return new RomanNumeral(parse(roman));
    }

    /**
     * Creates a {@link RomanNumeral} instance based on the given String value, as long as this is in strict roman
     * numeral notation. Any other value will cause a NumberFormatException.
     *
     * The internal String representation of the resulting RomanNumeral is guaranteed to be equal to the given value.
     *
     * @param roman String representation of the roman numeral
     * @throws NumberFormatException if roman is not an interpretable representation of a roman numeral
     */
    public static RomanNumeral ofStrict(String roman) {
        var romanNumeral =  of(roman);
        if (!romanNumeral.isStrict() || !romanNumeral.uppercase().equals(roman.toUpperCase())) {
            throw new NumberFormatException("Given value is not in strict roman numeral notation");
        }
        return romanNumeral;
    }

    /**
     * Converts the given roman numeral String representation to its long value, as long as it is strictly
     * possible to do so using the vinculum notation. Throws a {@link NumberFormatException} otherwise.
     *
     * @throws NumberFormatException if roman is not an interpretable representation of a roman numeral
     */
    public static long parse(String roman) {
        long value = 0;
        var sign = 1;
        var previousVinculumLevel = 7; // Max multiplier is 6, so set this to 7 as a start value. This value should only decrease.
        // Determine the sign of the value
        while (roman.startsWith("-")) {
            roman = roman.substring(1);
            sign *= -1;
        }
        // Parse the values for the different vinculum levels
        while (roman.contains(VINCULUM)) {
            var partValue = parsePart(roman.substring(0, roman.indexOf(VINCULUM)));

            if (partValue > 999) {
                throw new NumberFormatException("A roman numeral in vinculum notation cannot parts representing a value higher than 999");
            }

            roman = roman.substring(roman.indexOf(VINCULUM));
            var vinculumLevel = 0;
            while (roman.startsWith(VINCULUM)) {
                vinculumLevel += 1;
                partValue *= 1000;
                roman = roman.substring(1);
            }

            if (vinculumLevel >= previousVinculumLevel) {
                throw new NumberFormatException("Incorrect use of vinculum");
            } else {
                previousVinculumLevel = vinculumLevel;
            }

            value += Math.abs(partValue);
        }
        // Parse the non-vinculum part
        if (roman.length() > 0) {
            value += parsePart(roman);
        }
        value *= sign;
        return value;
    }

    private static long parsePart(String roman) {
        roman = roman == null ? "" : roman;
        roman = roman.toUpperCase();

        var value = 0;
        var current = Symbol.I;
        var sign = 1;

        try {
            for (int i = roman.length() - 1; i >= 0; i--) {
                var symbol = roman.charAt(i);
                if (symbol == '-') {
                    value *= -1;
                } else {
                    var numeral = Symbol.valueOf(symbol + "");
                    sign = numeral == current ? sign : numeral.value < current.value ? -1 : 1;
                    value += numeral.value * sign;
                    current = numeral;
                }
            }
        } catch (Throwable t) {
            throw new NumberFormatException("Could not parse roman numeral. Cause: " + t.getMessage());
        }

        return value;
    }

    /**
     * Converts the given long value to its strictest possible roman numeral equivalent.
     */
    public static String parse(long arabic) {
        var signum = Long.signum(arabic) == -1 ? "-" : "";

        // Math.abs(Long.MIN_VALUE) return Long.MIN_VALUE, which is negative, causing the rest of the algorithm to
        // fail.  Therefore 1 is added here, and subtracted again later in the algorithm to support Long.MIN_VALUE.
        int addedValue = 0;
        if (arabic == Long.MIN_VALUE) {
            addedValue = 1;
            arabic += addedValue;
        }

        arabic = Math.abs(arabic);

        if (arabic < 3999) {
            // Standard roman numeral
            return signum + parsePositive(arabic, addedValue);
        } else {
            // Roman numeral using vinculum
            var value = "";
            var parts = prependZeros(String.valueOf(arabic)).split("(?<=\\G.{3})");
            for (int i = parts.length - 1 ; i >= 0; i--) {
                if (!"000".equals(parts[i])) {
                    value = parsePositive(Long.valueOf(parts[i]), 0) + String.join("", Collections.nCopies(parts.length - i - 1, VINCULUM)) + value;
                    if (addedValue == 1) {
                        value += "I";
                        addedValue = 0;
                    }
                }
            }
            return signum + value;
        }
    }

    private static String prependZeros(String s) {
        var rest = s.length() % 3;
        if (rest == 0) {
            return s;
        } else {
            var prefix = String.join("", Collections.nCopies(3 - rest, "0"));
            return prefix + s;
        }
    }

    private static String parsePositive(long arabic, int addedValue) {

        StringBuilder value = new StringBuilder();
        var symbols = List.of("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I");

        if (arabic > 0) {
            while (arabic > 0) {
                for (var s : symbols) {
                    var sval = parse(s);
                    if (arabic >= sval) {
                        value.append(s);
                        arabic -= sval;
                        // Subtract the value we added before in case of Long.MIN_VALUE.
                        arabic -= addedValue;
                        // And make sure we don't add this in any of the next iterations
                        addedValue = 0;
                        break;
                    }
                }
            }
        } else if (arabic == 0) {
            value.append(VINCULUM);
        } else {
            throw new IllegalStateException("Negative value in parsePositive(long, int)");
        }

        return value.toString();
    }

    /**
     * Determine whether the given value is valid as a roman numeral value by the strict definition.
     *
     * This method will return false if the given value is null, or when it represents a negative number, zero or any
     * number greater than 3999. Additionally it will return false if it does not follow the rules for how symbols may
     * be combined.
     *
     * @param roman value to validate
     * @return true if the given value is strictly valid as roman numeral, false otherwise.
     */
    public static boolean isStrict(String roman) {
        return roman != null && !roman.isEmpty() && STRICT.matcher(roman).matches();
    }

    /**
     * Determines whether this {@link RomanNumeral} represents a value that is strictly a roman numeral. In essense,
     * this means this method will return true only if this instance represents a value between (and including) 1 and
     * 3999.
     * @return true if this instance lies strictly between 1 (inclusive) and 3999 (inclusive)
     */
    public boolean isStrict() {
        return this.arabic > 0 && this.arabic < 4000;
    }

    /**
     * Returns the length of the String representation of this {@link RomanNumeral}. This length will be based on the
     * strictest possible representation as returned by {@link #toString()}, and therefore not necessarily equal to
     * the length of a possible String representation that was used to create this instance.
     * @return length of this {@link RomanNumeral}'s String representation.
     */
    public int length() {
        return roman.length();
    }

    /**
     * Adds the given {@link RomanNumeral} to this instance. As a result this instance will then represent the value
     * equal to this new value. Note that after this method returns, a strict roman numeral may have changed to
     * non-strict roman numeral or vice versa, which you can check using {@link #isStrict()}.
     * @param roman the value to add
     * @return this instance for chaining
     */
    public RomanNumeral plus(RomanNumeral roman) {
        this.arabic += roman.arabic;
        this.roman = parse(this.arabic);
        return this;
    }

    /**
     * Subtracts the given {@link RomanNumeral} from this instance. As a result this instance will then represent the
     * value equal to this new value. Note that after this method returns, a strict roman numeral may have changed to
     * a non-strict roman numeral or vice versa, which you can check using {@link #isStrict()}.
     * @param roman the value to subtract
     * @return this instance for chaining
     */
    public RomanNumeral minus(RomanNumeral roman) {
        this.arabic -= roman.arabic;
        this.roman = parse(this.arabic);
        return this;
    }

    /**
     * Adds two roman numerals together and returns a String representation of the resulting sum.
     *
     * @param a the first operand
     * @param b the second operand
     * @return the sum of {@code a} and {@code b}]
     */
    public static String sum(String a, String b) {
        return RomanNumeral.of(a).plus(RomanNumeral.of(b)).uppercase();
    }

    /**
     * Adds two {@link RomanNumeral}s together and returns this sum as a new {@link RomanNumeral} instance.
     *
     * @param a the first operand
     * @param b the second operand
     * @return the sum of {@code a} and {@code b}]
     */
    public static RomanNumeral sum(RomanNumeral a, RomanNumeral b) {
        return RomanNumeral.of(a.arabic + b.arabic);
    }

    /**
     * Returns the greater of two {@code RomanNumeral} values.
     *
     * @param a the first operand
     * @param b the second operand
     * @return the greater of {@code a} and {@code b}, or {@code a} if equal
     */
    public static RomanNumeral max(RomanNumeral a, RomanNumeral b) {
        return a.arabic >= b.arabic ? a : b;
    }

    /**
     * Returns the smaller of two {@code RomanNumeral} values.
     *
     * @param a the first operand
     * @param b the second operand
     * @return the smaller of {@code a} and {@code b}, or {@code a} if equal
     */
    public static RomanNumeral min(RomanNumeral a, RomanNumeral b) {
        return a.arabic <= b.arabic ? a : b;
    }

    /**
     * Returns the value of this {@code RomanNumeral} as an {@code int} after a narrowing primitive conversion.
     */
    @Override
    public int intValue() {
        return (int)arabic;
    }

    /**
     * Returns the value of this {@code RomanNumeral} as a {@code long} value.
     */
    @Override
    public long longValue() {
        return arabic;
    }

    /**
     * Returns the value of this {@code RomanNumeral} as a {@code float} after a widening primitive conversion.
     */
    @Override
    public float floatValue() {
        return arabic;
    }

    /**
     * Returns the value of this {@code RomanNumeral} as a {@code double} after a widening primitive conversion.
     */
    @Override
    public double doubleValue() {
        return arabic;
    }

    /**
     * Returns the value of this {@code RomanNumeral} as an uppercase String.
     */
    public String uppercase() {
        return roman;
    }

    /**
     * Returns the value of this {@code RomanNumeral} as a lowercase String.
     */
    public String lowercase() {
        return roman.toLowerCase();
    }

    /**
     * Compares this {@code RomanNumeral} to the given value, using their internal long representations.
     *
     * @return the value {@code 0} if {@code this equals other}; a value less than {@code 0} if {@code this < other};
     * and a value greater than {@code 0} if {@code this > other}
     */
    @Override
    public int compareTo(RomanNumeral other) {
        return Long.compare(arabic, other.arabic) ;
    }

    /**
     * Returns the String representation of this {@code RomanNumeral}. Equivalent to {{@link #uppercase()}}.
     */
    @Override
    public String toString() {
        return uppercase();
    }

    /**
     * Compares this {@code RomanNumeral} to the given one, returning true if they represent the same value, and
     * false otherwise.
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof RomanNumeral && roman.equals(((RomanNumeral)other).roman);
    }

    /**
     * Returns the hashCode of this {@code RomanNumeral}.
     */
    @Override
    public int hashCode() {
        return roman.hashCode();
    }
}
