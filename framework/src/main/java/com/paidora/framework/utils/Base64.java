package com.paidora.framework.utils;

/**
 * Этот класс тупо спизжен из java.util.prefs и в нём исправлены лишь права доступа к методам.
 * <p/>
 * Static systems for translating Base64 encoded strings to byte arrays
 * and vice-versa.
 *
 * @author Josh Bloch
 * @version 1.4, 01/23/03
 * @since 1.4
 */
public final class Base64 {

    private Base64() {
    }

    /**
     * Translates the specified byte array into a Base64 string as per
     * Preferences.put(byte[]) and inserts "\r\n" sequences at specified lineLength.
     */
    public static String byteArrayToBase64(byte[] a, int lineLength) {
        return insertLineSeparators(byteArrayToBase64(a), lineLength);
    }

    private static String insertLineSeparators(String uncutted, int lineLength) {
        if (uncutted.length() <= lineLength) {
            return uncutted;
        }
        StringBuilder sb = new StringBuilder(uncutted.length() + uncutted.length() / lineLength * 2 + 4);
        int from = 0;
        while (true) {
            int to = from + lineLength;
            if (to >= uncutted.length()) {
                sb.append(uncutted.substring(from));
                break;
            }
            sb.append(uncutted, from, to);
            sb.append("\r\n");
            from = to;
        }
        return sb.toString();
    }

    /**
     * Translates the specified byte array into a Base64 string as per
     * Preferences.put(byte[]).
     */
    public static String byteArrayToBase64(byte[] a) {
        return byteArrayToBase64(a, false);
    }

    /**
     * Translates the specified byte array into an "aternate representation"
     * Base64 string.  This non-standard variant uses an alphabet that does
     * not contain the uppercase alphabetic characters, which makes it
     * suitable for use in situations where case-folding occurs.
     */
    public static String byteArrayToAltBase64(byte[] a) {
        return byteArrayToBase64(a, true);
    }

    private static String byteArrayToBase64(byte[] a, boolean alternate) {
        int aLen = a.length;
        int numFullGroups = aLen / 3;
        int numBytesInPartialGroup = aLen - 3 * numFullGroups;
        int resultLen = 4 * ((aLen + 2) / 3);
        StringBuilder result = new StringBuilder(resultLen);
        char[] intToAlpha = (alternate ? INT_TO_ALT_BASE_64 : INT_TO_BASE_64);

        // Translate all full groups from byte array elements to Base64
        int inCursor = 0;
        for (int i = 0; i < numFullGroups; i++) {
            int byte0 = a[inCursor++] & 0xff;
            int byte1 = a[inCursor++] & 0xff;
            int byte2 = a[inCursor++] & 0xff;
            result.append(intToAlpha[byte0 >> 2]);
            result.append(intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
            result.append(intToAlpha[(byte1 << 2) & 0x3f | (byte2 >> 6)]);
            result.append(intToAlpha[byte2 & 0x3f]);
        }

        // Translate partial group if present
        if (numBytesInPartialGroup != 0) {
            int byte0 = a[inCursor++] & 0xff;
            result.append(intToAlpha[byte0 >> 2]);
            if (numBytesInPartialGroup == 1) {
                result.append(intToAlpha[(byte0 << 4) & 0x3f]);
                result.append("==");
            } else {
                // assert numBytesInPartialGroup == 2;
                //noinspection UnusedAssignment
                int byte1 = a[inCursor++] & 0xff;
                result.append(intToAlpha[(byte0 << 4) & 0x3f | (byte1 >> 4)]);
                result.append(intToAlpha[(byte1 << 2) & 0x3f]);
                result.append('=');
            }
        }
        // assert inCursor == a.length;
        // assert result.length() == resultLen;
        return result.toString();
    }

    /**
     * This array is a lookup table that translates 6-bit positive integer
     * index values into their "Base64 Alphabet" equivalents as specified
     * in Table 1 of RFC 2045.
     */
    private static final char[] INT_TO_BASE_64 = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    /**
     * This array is a lookup table that translates 6-bit positive integer
     * index values into their "Alternate Base64 Alphabet" equivalents.
     * This is NOT the real Base64 Alphabet as per in Table 1 of RFC 2045.
     * This alternate alphabet does not use the capital letters.  It is
     * designed for use in environments where "case folding" occurs.
     */
    private static final char[] INT_TO_ALT_BASE_64 = {
            '!', '"', '#', '$', '%', '&', '\'', '(', ')', ',', '-', '.', ':',
            ';', '<', '>', '@', '[', ']', '^', '`', '_', '{', '|', '}', '~',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '?'
    };

    /**
     * Translates the specified Base64 string (as per Preferences.get(byte[]))
     * into a byte array.
     *
     * @throws IllegalArgumentException if <tt>s</tt> is not a valid Base64
     *                                  string.
     */
    public static byte[] base64ToByteArray(String s) {
        return base64ToByteArray(s, false);
    }

    /**
     * Translates the specified "aternate representation" Base64 string
     * into a byte array.
     *
     * @throws IllegalArgumentException or ArrayOutOfBoundsException
     *                                  if <tt>s</tt> is not a valid alternate representation
     *                                  Base64 string.
     */
    public static byte[] altBase64ToByteArray(String s) {
        return base64ToByteArray(s, true);
    }

    private static byte[] base64ToByteArray(String s, boolean alternate) {
        s = removeLineFeeds(s);
        byte[] alphaToInt = (alternate ? ALT_BASE_64_TO_INT : BASE_64_TO_INT);
        int sLen = s.length();
        int numGroups = sLen / 4;
        if (4 * numGroups != sLen) {
            throw new IllegalArgumentException(
                    "String length must be a multiple of four.");
        }
        int missingBytesInLastGroup = 0;
        int numFullGroups = numGroups;
        if (sLen != 0) {
            if (s.charAt(sLen - 1) == '=') {
                missingBytesInLastGroup++;
                numFullGroups--;
            }
            if (s.charAt(sLen - 2) == '=') {
                missingBytesInLastGroup++;
            }
        }
        byte[] result = new byte[3 * numGroups - missingBytesInLastGroup];

        // Translate all full groups from base64 to byte array elements
        int inCursor = 0, outCursor = 0;
        for (int i = 0; i < numFullGroups; i++) {
            int ch0 = base64toInt(s.charAt(inCursor++), alphaToInt);
            int ch1 = base64toInt(s.charAt(inCursor++), alphaToInt);
            int ch2 = base64toInt(s.charAt(inCursor++), alphaToInt);
            int ch3 = base64toInt(s.charAt(inCursor++), alphaToInt);
            result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));
            result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
            result[outCursor++] = (byte) ((ch2 << 6) | ch3);
        }

        // Translate partial group, if present
        if (missingBytesInLastGroup != 0) {
            int ch0 = base64toInt(s.charAt(inCursor++), alphaToInt);
            int ch1 = base64toInt(s.charAt(inCursor++), alphaToInt);
            result[outCursor++] = (byte) ((ch0 << 2) | (ch1 >> 4));

            if (missingBytesInLastGroup == 1) {
                //noinspection UnusedAssignment
                int ch2 = base64toInt(s.charAt(inCursor++), alphaToInt);
                //noinspection UnusedAssignment
                result[outCursor++] = (byte) ((ch1 << 4) | (ch2 >> 2));
            }
        }
        // assert inCursor == s.length()-missingBytesInLastGroup;
        // assert outCursor == result.length;
        return result;
    }

    /**
     * Translates the specified character, which is assumed to be in the
     * "Base 64 Alphabet" into its equivalent 6-bit positive integer.
     *
     * @throws IllegalArgumentException or ArrayOutOfBoundsException if
     *                                  c is not in the Base64 Alphabet.
     */
    private static int base64toInt(char c, byte[] alphaToInt) {
        int result = alphaToInt[((int) c)];
        if (result < 0) {
            throw new IllegalArgumentException("Illegal character " + c);
        }
        return result;
    }

    /**
     * This array is a lookup table that translates unicode characters
     * drawn from the "Base64 Alphabet" (as specified in Table 1 of RFC 2045)
     * into their 6-bit positive integer equivalents.  Characters that
     * are not in the Base64 alphabet but fall within the bounds of the
     * array are translated to -1.
     */
    private static final byte[] BASE_64_TO_INT = {
            (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1,
            (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1,
            (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 62, (byte) -1, (byte) -1, (byte) -1, (byte) 63, (byte) 52, (byte) 53, (byte) 54,
            (byte) 55, (byte) 56, (byte) 57, (byte) 58, (byte) 59, (byte) 60, (byte) 61, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 0, (byte) 1, (byte) 2, (byte) 3, (byte) 4,
            (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) 9, (byte) 10, (byte) 11, (byte) 12, (byte) 13, (byte) 14, (byte) 15, (byte) 16, (byte) 17, (byte) 18, (byte) 19, (byte) 20, (byte) 21, (byte) 22, (byte) 23,
            (byte) 24, (byte) 25, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 26, (byte) 27, (byte) 28, (byte) 29, (byte) 30, (byte) 31, (byte) 32, (byte) 33, (byte) 34,
            (byte) 35, (byte) 36, (byte) 37, (byte) 38, (byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 43, (byte) 44, (byte) 45, (byte) 46, (byte) 47, (byte) 48, (byte) 49, (byte) 50, (byte) 51
    };

    /**
     * This array is the analogue of BASE_64_TO_INT, but for the nonstandard
     * variant that avoids the use of uppercase alphabetic characters.
     */
    private static final byte[] ALT_BASE_64_TO_INT = {
            (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1,
            (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) 0, (byte) 1,
            (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8, (byte) -1, (byte) 62, (byte) 9, (byte) 10, (byte) 11, (byte) -1, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57,
            (byte) 58, (byte) 59, (byte) 60, (byte) 61, (byte) 12, (byte) 13, (byte) 14, (byte) -1, (byte) 15, (byte) 63, (byte) 16, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1,
            (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1, (byte) -1,
            (byte) -1, (byte) -1, (byte) -1, (byte) 17, (byte) -1, (byte) 18, (byte) 19, (byte) 21, (byte) 20, (byte) 26, (byte) 27, (byte) 28, (byte) 29, (byte) 30, (byte) 31, (byte) 32, (byte) 33,
            (byte) 34, (byte) 35, (byte) 36, (byte) 37, (byte) 38, (byte) 39, (byte) 40, (byte) 41, (byte) 42, (byte) 43, (byte) 44, (byte) 45, (byte) 46, (byte) 47, (byte) 48, (byte) 49, (byte) 50,
            (byte) 51, (byte) 22, (byte) 23, (byte) 24, (byte) 25
    };

    private static String removeLineFeeds(String source) {
        StringBuilder sb = new StringBuilder(source.length());
        char[] chars = source.toCharArray();
        for (char ch : chars) {
            if (ch != '\n' && ch != '\r') {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

//    public static void main(String[] args) {
//        int numRuns = Integer.parseInt(args[0]);
//        int numBytes = Integer.parseInt(args[1]);
//        Random rnd = new Random();
//        for (int i = 0; i < numRuns; i++) {
//            for (int j = 0; j < numBytes; j++) {
//                byte[] arr = new byte[j];
//                for (int k = 0; k < j; k++) {
//                    arr[k] = (byte) rnd.nextInt();
//                }
//
//                String s = byteArrayToBase64(arr);
//                byte [] b = base64ToByteArray(s);
//                if (!Arrays.equals(arr, b)) {
//                    System.out.println("Dismal failure!");
//                }
//
//                s = byteArrayToAltBase64(arr);
//                b = altBase64ToByteArray(s);
//                if (!Arrays.equals(arr, b)) {
//                    System.out.println("Alternate dismal failure!");
//                }
//            }
//        }
//    }
}

