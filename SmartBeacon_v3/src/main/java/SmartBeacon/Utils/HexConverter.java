package SmartBeacon.Utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Class to convert hex values to strings and vice versa
 * @author stucn3, wermp2
 */
public final class HexConverter {

   /**
     * Converts a string of hex. values to a UTF-8 string
     * @param hex The hex data to convert
     * @return a UTF-8 string
     */
    public static String hexStringToUTF8(String hex) {
        //Remove spaces
        hex = hex.replace(" ", "");

        //convert
        ByteBuffer buff = ByteBuffer.allocate(hex.length() / 2);
        for (int i = 0; i < hex.length(); i += 2) {
            buff.put((byte) Integer.parseInt(hex.substring(i, i + 2), 16));
        }
        buff.rewind();
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = cs.decode(buff);

        return cb.toString();
    }

    /**
     * Converts an array of shorts to a string of hex. values
     * @param hex The array of shorts
     * @return a hex. string
     */
    public static String shortToHexString(short[] hex) {
        StringBuilder sb = new StringBuilder();
        String temp;

        for (int i = 0; i < hex.length; i++) {
            temp = Integer.toHexString(hex[i]);
            if (temp.length() < 2) {
                temp = "0" + temp;
            }

            sb.append(temp + " ");
        }

        String hexString = sb.toString();

        return hexString;
    }

    /**
     * Converts a UTF-8 string to a hex. string
     * @param string the UTF-8 string to convert
     * @return The hex. string
     */
    public static String stringToHex(String string) {
        StringBuilder sb = new StringBuilder();
        String[] array = string.split("");
        String temp;
        Charset cs = Charset.forName("UTF-8");
        for (int i = 0; i < string.length(); i++) {
            temp = String.format("%x", new BigInteger(1, array[i].getBytes(cs)));
            if (temp.length() < 2) {
                temp = "0" + temp;
            }

            sb.append(temp + " ");
        }
        String finalstring = sb.toString();
        return finalstring.toUpperCase();
    }

    /**
     * Converts an int to a hex. value
     * @param number the int to convert
     * @return a hex. value
     */
    public static String intToHex(int number) {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toHexString(number));
        if (sb.length() < 2) {
            sb.insert(0, '0'); // pad with leading zero if needed
        }
        String finalstring = sb.toString();
        return finalstring.toUpperCase();
    }

}
