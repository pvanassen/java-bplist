package nl.pvanassen.bplist.ext.base64;

import static nl.pvanassen.bplist.ext.base64.Constants.*;

/**
 * Encodes and decodes to and from Base64 notation.
 * <p>
 * Change Log:
 * </p>
 * <ul>
 * <li>v2.1 - Cleaned up javadoc comments and unused variables and methods. Added some convenience methods for reading and writing to and from files.</li>
 * <li>v2.0.2 - Now specifies UTF-8 encoding in places where the code fails on systems with other encodings (like EBCDIC).</li>
 * <li>v2.0.1 - Fixed an error when decoding a single byte, that is, when the encoded data was a single byte.</li>
 * <li>v2.0 - I got rid of methods that used booleans to set options. Now everything is more consolidated and cleaner. The code now detects when data that's being decoded is
 * gzip-compressed and will decompress it automatically. Generally things are cleaner. You'll probably have to change some method calls that you were making to support the new
 * options format (<tt>int</tt>s that you "OR" together).</li>
 * <li>v1.5.1 - Fixed bug when decompressing and decoding to a byte[] using <tt>decode( String s, boolean gzipCompressed )</tt>. Added the ability to "suspend" encoding in the
 * Output Stream so you can turn on and off the encoding if you need to embed base64 data in an otherwise "normal" stream (like an XML file).</li>
 * <li>v1.5 - Output stream pases on flush() command but doesn't do anything itself. This helps when using GZIP streams. Added the ability to GZip-compress objects before encoding
 * them.</li>
 * <li>v1.4 - Added helper methods to read/write files.</li>
 * <li>v1.3.6 - Fixed OutputStream.flush() so that 'position' is reset.</li>
 * <li>v1.3.5 - Added flag to turn on and off line breaks. Fixed bug in input stream where last buffer being read, if not completely full, was not returned.</li>
 * <li>v1.3.4 - Fixed when "improperly padded stream" error was thrown at the wrong time.</li>
 * <li>v1.3.3 - Fixed I/O streams which were totally messed up.</li>
 * </ul>
 * <p>
 * I am placing this code in the Public Domain. Do with it as you will. This software comes with no guarantees or warranties but with plenty of well-wishing instead! Please visit
 * <a href="http://iharder.net/base64">http://iharder.net/base64</a> periodically to check for updates or to contribute improvements.
 * </p>
 *
 * @author Robert Harder
 * @author rob@iharder.net
 * @version 2.1
 */
public class Base64 {

    /* ******** P U B L I C F I E L D S ******** */
    /** No options specified. Value is zero. */
    public final static int NO_OPTIONS = 0;
    /** Specify encoding. */
    public final static int ENCODE = 1;
    /** Specify decoding. */
    public final static int DECODE = 0;
    /** Specify that data should be gzip-compressed. */
    public final static int GZIP = 2;
    /** Don't break lines when encoding (violates strict Base64 specification) */
    public final static int DONT_BREAK_LINES = 8;

    /**
     * Translates a Base64 value to either its 6-bit reconstruction value or a
     * negative number indicating some other meaning.
     **/
    final static byte[] DECODABET = { -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 0 - 8
            -5, -5, // Whitespace: Tab and Linefeed
            -9, -9, // Decimal 11 - 12
            -5, // Whitespace: Carriage Return
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 14 -
            // 26
            -9, -9, -9, -9, -9, // Decimal 27 - 31
            -5, // Whitespace: Space
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, // Decimal 33 - 42
            62, // Plus sign at decimal 43
            -9, -9, -9, // Decimal 44 - 46
            63, // Slash at decimal 47
            52, 53, 54, 55, 56, 57, 58, 59, 60, 61, // Numbers zero through nine
            -9, -9, -9, // Decimal 58 - 60
            -1, // Equals sign at decimal 61
            -9, -9, -9, // Decimal 62 - 64
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, // Letters 'A' through
            // 'N'
            14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, // Letters 'O'
            // through 'Z'
            -9, -9, -9, -9, -9, -9, // Decimal 91 - 96
            26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, // Letters 'a'
            // through 'm'
            39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, // Letters 'n'
            // through 'z'
            -9, -9, -9, -9 // Decimal 123 - 126
    /*
     * ,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, // Decimal 127 - 139
     * -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, // Decimal 140 - 152
     * -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, // Decimal 153 - 165
     * -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, // Decimal 166 - 178
     * -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, // Decimal 179 - 191
     * -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, // Decimal 192 - 204
     * -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, // Decimal 205 - 217
     * -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, // Decimal 218 - 230
     * -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9, // Decimal 231 - 243
     * -9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9,-9 // Decimal 244 - 255
     */};
    private final static byte EQUALS_SIGN_ENC = -1; // Indicates equals sign in

    // encoding

    /** Defeats instantiation. */
    private Base64() {
    }

    /**
     * Serializes an object and returns the Base64-encoded version of that
     * serialized object. If the object cannot be serialized or there is another
     * error, the method will return <tt>null</tt>. The object is not
     * GZip-compressed before being encoded.
     *
     * @param serializableObject
     *            The object to encode
     * @return The Base64-encoded object
     * @since 1.4
     */
    public static String encodeObject(java.io.Serializable serializableObject) {
        return encodeObject(serializableObject, NO_OPTIONS);
    } // end encodeObject

    /**
     * Serializes an object and returns the Base64-encoded version of that
     * serialized object. If the object cannot be serialized or there is another
     * error, the method will return <tt>null</tt>.
     * <p>
     * Valid options:
     *
     * <pre>
     *   GZIP: gzip-compresses object before encoding it.
     *   DONT_BREAK_LINES: don't break lines at 76 characters
     *     <i>Note: Technically, this makes your encoding non-compliant.</i>
     * </pre>
     * <p>
     * Example: <code>encodeObject( myObj, Base64.GZIP )</code> or
     * <p>
     * Example: <code>encodeObject( myObj, Base64.GZIP | Base64.DONT_BREAK_LINES )</code>
     *
     * @param serializableObject
     *            The object to encode
     * @param options
     *            Specified options
     * @return The Base64-encoded object
     * @see Base64#GZIP
     * @see Base64#DONT_BREAK_LINES
     * @since 2.0
     */
    public static String encodeObject(java.io.Serializable serializableObject, int options) {
        // Streams
        java.io.ByteArrayOutputStream baos = null;
        java.io.OutputStream b64os = null;
        java.io.ObjectOutputStream oos = null;
        java.util.zip.GZIPOutputStream gzos = null;

        // Isolate options
        int gzip = (options & GZIP);
        int dontBreakLines = (options & DONT_BREAK_LINES);

        try {
            // ObjectOutputStream -> (GZIP) -> Base64 -> ByteArrayOutputStream
            baos = new java.io.ByteArrayOutputStream();
            b64os = new Base64OutputStream(baos, ENCODE | dontBreakLines);

            // GZip?
            if (gzip == GZIP) {
                gzos = new java.util.zip.GZIPOutputStream(b64os);
                oos = new java.io.ObjectOutputStream(gzos);
            } // end if: gzip
            else {
                oos = new java.io.ObjectOutputStream(b64os);
            }

            oos.writeObject(serializableObject);
        } // end try
        catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        } // end catch
        finally {
            try {
                oos.close();
            } catch (Exception e) {
            }
            try {
                gzos.close();
            } catch (Exception e) {
            }
            try {
                b64os.close();
            } catch (Exception e) {
            }
            try {
                baos.close();
            } catch (Exception e) {
            }
        } // end finally

        // Return value according to relevant encoding.
        try {
            return new String(baos.toByteArray(), PREFERRED_ENCODING);
        } // end try
        catch (java.io.UnsupportedEncodingException uue) {
            return new String(baos.toByteArray());
        } // end catch

    } // end encode

    /**
     * Encodes a byte array into Base64 notation. Does not GZip-compress data.
     *
     * @param source
     *            The data to convert
     * @since 1.4
     */
    public static String encodeBytes(byte[] source) {
        return encodeBytes(source, 0, source.length, NO_OPTIONS);
    } // end encodeBytes

    /**
     * Encodes a byte array into Base64 notation.
     * <p>
     * Valid options:
     *
     * <pre>
     *   GZIP: gzip-compresses object before encoding it.
     *   DONT_BREAK_LINES: don't break lines at 76 characters
     *     <i>Note: Technically, this makes your encoding non-compliant.</i>
     * </pre>
     * <p>
     * Example: <code>encodeBytes( myData, Base64.GZIP )</code> or
     * <p>
     * Example: <code>encodeBytes( myData, Base64.GZIP | Base64.DONT_BREAK_LINES )</code>
     *
     * @param source
     *            The data to convert
     * @param options
     *            Specified options
     * @see Base64#GZIP
     * @see Base64#DONT_BREAK_LINES
     * @since 2.0
     */
    public static String encodeBytes(byte[] source, int options) {
        return encodeBytes(source, 0, source.length, options);
    } // end encodeBytes

    /**
     * Encodes a byte array into Base64 notation. Does not GZip-compress data.
     *
     * @param source
     *            The data to convert
     * @param off
     *            Offset in array where conversion should begin
     * @param len
     *            Length of data to convert
     * @since 1.4
     */
    public static String encodeBytes(byte[] source, int off, int len) {
        return encodeBytes(source, off, len, NO_OPTIONS);
    } // end encodeBytes

    /**
     * Encodes a byte array into Base64 notation.
     * <p>
     * Valid options:
     *
     * <pre>
     *   GZIP: gzip-compresses object before encoding it.
     *   DONT_BREAK_LINES: don't break lines at 76 characters
     *     <i>Note: Technically, this makes your encoding non-compliant.</i>
     * </pre>
     * <p>
     * Example: <code>encodeBytes( myData, Base64.GZIP )</code> or
     * <p>
     * Example: <code>encodeBytes( myData, Base64.GZIP | Base64.DONT_BREAK_LINES )</code>
     *
     * @param source
     *            The data to convert
     * @param off
     *            Offset in array where conversion should begin
     * @param len
     *            Length of data to convert
     * @param options
     *            Specified options
     * @see Base64#GZIP
     * @see Base64#DONT_BREAK_LINES
     * @since 2.0
     */
    public static String encodeBytes(byte[] source, int off, int len, int options) {
        // Isolate options
        int dontBreakLines = (options & DONT_BREAK_LINES);
        int gzip = (options & GZIP);

        // Compress?
        if (gzip == GZIP) {
            java.io.ByteArrayOutputStream baos = null;
            java.util.zip.GZIPOutputStream gzos = null;
            Base64OutputStream b64os = null;

            try {
                // GZip -> Base64 -> ByteArray
                baos = new java.io.ByteArrayOutputStream();
                b64os = new Base64OutputStream(baos, ENCODE | dontBreakLines);
                gzos = new java.util.zip.GZIPOutputStream(b64os);

                gzos.write(source, off, len);
                gzos.close();
            } // end try
            catch (java.io.IOException e) {
                e.printStackTrace();
                return null;
            } // end catch
            finally {
                try {
                    gzos.close();
                } catch (Exception e) {
                }
                try {
                    b64os.close();
                } catch (Exception e) {
                }
                try {
                    baos.close();
                } catch (Exception e) {
                }
            } // end finally

            // Return value according to relevant encoding.
            try {
                return new String(baos.toByteArray(), PREFERRED_ENCODING);
            } // end try
            catch (java.io.UnsupportedEncodingException uue) {
                return new String(baos.toByteArray());
            } // end catch
        } // end if: compress
          // Else, don't compress. Better not to use streams at all then.
        else {
            // Convert option to boolean in way that code likes it.
            boolean breakLines = dontBreakLines == 0;

            int len43 = (len * 4) / 3;
            byte[] outBuff = new byte[(len43) // Main 4:3
                    + ((len % 3) > 0 ? 4 : 0) // Account for padding
                    + (breakLines ? (len43 / MAX_LINE_LENGTH) : 0)]; // New
            // lines
            int d = 0;
            int e = 0;
            int len2 = len - 2;
            int lineLength = 0;
            for (; d < len2; d += 3, e += 4) {
                Encode3to4.encode3to4(source, d + off, 3, outBuff, e);

                lineLength += 4;
                if (breakLines && (lineLength == MAX_LINE_LENGTH)) {
                    outBuff[e + 4] = NEW_LINE;
                    e++;
                    lineLength = 0;
                } // end if: end of line
            } // en dfor: each piece of array

            if (d < len) {
                Encode3to4.encode3to4(source, d + off, len - d, outBuff, e);
                e += 4;
            } // end if: some padding needed

            // Return value according to relevant encoding.
            try {
                return new String(outBuff, 0, e, PREFERRED_ENCODING);
            } // end try
            catch (java.io.UnsupportedEncodingException uue) {
                return new String(outBuff, 0, e);
            } // end catch

        } // end else: don't compress

    } // end encodeBytes

    /* ******** D E C O D I N G M E T H O D S ******** */
    /**
     * Decodes four bytes from array <var>source</var> and writes the resulting
     * bytes (up to three of them) to <var>destination</var>. The source and
     * destination arrays can be manipulated anywhere along their length by
     * specifying <var>srcOffset</var> and <var>destOffset</var>. This method
     * does not check to make sure your arrays are large enough to accomodate
     * <var>srcOffset</var> + 4 for the <var>source</var> array or
     * <var>destOffset</var> + 3 for the <var>destination</var> array. This
     * method returns the actual number of bytes that were converted from the
     * Base64 encoding.
     *
     * @param source
     *            the array to convert
     * @param srcOffset
     *            the index where conversion begins
     * @param destination
     *            the array to hold the conversion
     * @param destOffset
     *            the index where output will be put
     * @return the number of decoded bytes converted
     * @since 1.3
     */
    static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset) {
        // Example: Dk==
        if (source[srcOffset + 2] == EQUALS_SIGN) {
            // Two ways to do the same thing. Don't know which way I like best.
            // int outBuff = ( ( DECODABET[ source[ srcOffset ] ] << 24 ) >>> 6
            // )
            // | ( ( DECODABET[ source[ srcOffset + 1] ] << 24 ) >>> 12 );
            int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18) | ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12);

            destination[destOffset] = (byte) (outBuff >>> 16);
            return 1;
        } // Example: DkL=
        else if (source[srcOffset + 3] == EQUALS_SIGN) {
            // Two ways to do the same thing. Don't know which way I like best.
            // int outBuff = ( ( DECODABET[ source[ srcOffset ] ] << 24 ) >>> 6
            // )
            // | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
            // | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 );
            int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18) | ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12) | ((DECODABET[source[srcOffset + 2]] & 0xFF) << 6);

            destination[destOffset] = (byte) (outBuff >>> 16);
            destination[destOffset + 1] = (byte) (outBuff >>> 8);
            return 2;
        } // Example: DkLE
        else {
            try {
                // Two ways to do the same thing. Don't know which way I like
                // best.
                // int outBuff = ( ( DECODABET[ source[ srcOffset ] ] << 24 )
                // >>> 6 )
                // | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
                // | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 )
                // | ( ( DECODABET[ source[ srcOffset + 3 ] ] << 24 ) >>> 24 );
                int outBuff = ((DECODABET[source[srcOffset]] & 0xFF) << 18) | ((DECODABET[source[srcOffset + 1]] & 0xFF) << 12) | ((DECODABET[source[srcOffset + 2]] & 0xFF) << 6)
                        | ((DECODABET[source[srcOffset + 3]] & 0xFF));

                destination[destOffset] = (byte) (outBuff >> 16);
                destination[destOffset + 1] = (byte) (outBuff >> 8);
                destination[destOffset + 2] = (byte) (outBuff);

                return 3;
            } catch (Exception e) {
                System.out.println("" + source[srcOffset] + ": " + (DECODABET[source[srcOffset]]));
                System.out.println("" + source[srcOffset + 1] + ": " + (DECODABET[source[srcOffset + 1]]));
                System.out.println("" + source[srcOffset + 2] + ": " + (DECODABET[source[srcOffset + 2]]));
                System.out.println("" + source[srcOffset + 3] + ": " + (DECODABET[source[srcOffset + 3]]));
                return -1;
            } // e nd catch
        }
    } // end decodeToBytes

    /**
     * Very low-level access to decoding ASCII characters in the form of a byte
     * array. Does not support automatically gunzipping or any other "fancy"
     * features.
     *
     * @param source
     *            The Base64 encoded data
     * @param off
     *            The offset of where to begin decoding
     * @param len
     *            The length of characters to decode
     * @return decoded data
     * @since 1.3
     */
    public static byte[] decode(byte[] source, int off, int len) {
        int len34 = (len * 3) / 4;
        byte[] outBuff = new byte[len34]; // Upper limit on size of output
        int outBuffPosn = 0;

        byte[] b4 = new byte[4];
        int b4Posn = 0;
        int i = 0;
        byte sbiCrop = 0;
        byte sbiDecode = 0;
        for (i = off; i < (off + len); i++) {
            sbiCrop = (byte) (source[i] & 0x7f); // Only the low seven bits
            sbiDecode = DECODABET[sbiCrop];

            if (sbiDecode >= WHITE_SPACE_ENC) // White space, Equals sign or
            // better
            {
                if (sbiDecode >= EQUALS_SIGN_ENC) {
                    b4[b4Posn++] = sbiCrop;
                    if (b4Posn > 3) {
                        outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn);
                        b4Posn = 0;

                        // If that was the equals sign, break out of 'for' loop
                        if (sbiCrop == EQUALS_SIGN) {
                            break;
                        }
                    } // end if: quartet built

                } // end if: equals sign or better

            } // end if: white space, equals sign or better
            else {
                System.err.println("Bad Base64 input character at " + i + ": " + source[i] + "(decimal)");
                return null;
            } // end else:
        } // each input character

        byte[] out = new byte[outBuffPosn];
        System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
        return out;
    } // end decode

    /**
     * Decodes data from Base64 notation, automatically detecting
     * gzip-compressed data and decompressing it.
     *
     * @param s
     *            the string to decode
     * @return the decoded data
     * @since 1.4
     */
    public static byte[] decode(String s) {
        byte[] bytes;
        try {
            bytes = s.getBytes(PREFERRED_ENCODING);
        } // end try
        catch (java.io.UnsupportedEncodingException uee) {
            bytes = s.getBytes();
        } // end catch
          // </change>

        // Decode
        bytes = decode(bytes, 0, bytes.length);

        // Check to see if it's gzip-compressed
        // GZIP Magic Two-Byte Number: 0x8b1f (35615)
        if ((bytes != null) && (bytes.length >= 4)) {

            int head = (bytes[0] & 0xff) | ((bytes[1] << 8) & 0xff00);
            if (java.util.zip.GZIPInputStream.GZIP_MAGIC == head) {
                java.io.ByteArrayInputStream bais = null;
                java.util.zip.GZIPInputStream gzis = null;
                java.io.ByteArrayOutputStream baos = null;
                byte[] buffer = new byte[2048];
                int length = 0;

                try {
                    baos = new java.io.ByteArrayOutputStream();
                    bais = new java.io.ByteArrayInputStream(bytes);
                    gzis = new java.util.zip.GZIPInputStream(bais);

                    while ((length = gzis.read(buffer)) >= 0) {
                        baos.write(buffer, 0, length);
                    } // end while: reading input

                    // No error? Get new bytes.
                    bytes = baos.toByteArray();

                } // end try
                catch (java.io.IOException e) {
                    // Just return originally-decoded bytes
                } // end catch
                finally {
                    try {
                        baos.close();
                    } catch (Exception e) {
                    }
                    try {
                        gzis.close();
                    } catch (Exception e) {
                    }
                    try {
                        bais.close();
                    } catch (Exception e) {
                    }
                } // end finally

            } // end if: gzipped
        } // end if: bytes.length >= 2

        return bytes;
    } // end decode

    /**
     * Attempts to decode Base64 data and deserialize a Java Object within.
     * Returns <tt>null</tt> if there was an error.
     *
     * @param encodedObject
     *            The Base64 data to decode
     * @return The decoded and deserialized object
     * @since 1.5
     */
    public static Object decodeToObject(String encodedObject) {
        // Decode and gunzip if necessary
        byte[] objBytes = decode(encodedObject);

        java.io.ByteArrayInputStream bais = null;
        java.io.ObjectInputStream ois = null;
        Object obj = null;

        try {
            bais = new java.io.ByteArrayInputStream(objBytes);
            ois = new java.io.ObjectInputStream(bais);

            obj = ois.readObject();
        } // end try
        catch (java.io.IOException e) {
            e.printStackTrace();
            obj = null;
        } // end catch
        catch (java.lang.ClassNotFoundException e) {
            e.printStackTrace();
            obj = null;
        } // end catch
        finally {
            try {
                bais.close();
            } catch (Exception e) {
            }
            try {
                ois.close();
            } catch (Exception e) {
            }
        } // end finally

        return obj;
    } // end decodeObject

    /**
     * Convenience method for encoding data to a file.
     *
     * @param dataToEncode
     *            byte array of data to encode in base64 form
     * @param filename
     *            Filename for saving encoded data
     * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
     * @since 2.1
     */
    public static boolean encodeToFile(byte[] dataToEncode, String filename) {
        boolean success = false;
        Base64OutputStream bos = null;
        try {
            bos = new Base64OutputStream(new java.io.FileOutputStream(filename), Base64.ENCODE);
            bos.write(dataToEncode);
            success = true;
        } // end try
        catch (java.io.IOException e) {

            success = false;
        } // end catch: IOException
        finally {
            try {
                bos.close();
            } catch (Exception e) {
            }
        } // end finally

        return success;
    } // end encodeToFile

    /**
     * Convenience method for decoding data to a file.
     *
     * @param dataToDecode
     *            Base64-encoded data as a string
     * @param filename
     *            Filename for saving decoded data
     * @return <tt>true</tt> if successful, <tt>false</tt> otherwise
     * @since 2.1
     */
    public static boolean decodeToFile(String dataToDecode, String filename) {
        boolean success = false;
        Base64OutputStream bos = null;
        try {
            bos = new Base64OutputStream(new java.io.FileOutputStream(filename), Base64.DECODE);
            bos.write(dataToDecode.getBytes(PREFERRED_ENCODING));
            success = true;
        } // end try
        catch (java.io.IOException e) {
            success = false;
        } // end catch: IOException
        finally {
            try {
                bos.close();
            } catch (Exception e) {
            }
        } // end finally

        return success;
    } // end decodeToFile

    /**
     * Convenience method for reading a base64-encoded file and decoding it.
     *
     * @param filename
     *            Filename for reading encoded data
     * @return decoded byte array or null if unsuccessful
     * @since 2.1
     */
    public static byte[] decodeFromFile(String filename) {
        byte[] decodedData = null;
        Base64InputStream bis = null;
        try {
            // Set up some useful variables
            java.io.File file = new java.io.File(filename);
            byte[] buffer = null;
            int length = 0;
            int numBytes = 0;

            // Check for size of file
            if (file.length() > Integer.MAX_VALUE) {
                System.err.println("File is too big for this convenience method (" + file.length() + " bytes).");
                return null;
            } // end if: file too big for int index
            buffer = new byte[(int) file.length()];

            // Open a stream
            bis = new Base64InputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(file)), Base64.DECODE);

            // Read until done
            while ((numBytes = bis.read(buffer, length, 4096)) >= 0) {
                length += numBytes;
            }

            // Save in a variable to return
            decodedData = new byte[length];
            System.arraycopy(buffer, 0, decodedData, 0, length);

        } // end try
        catch (java.io.IOException e) {
            System.err.println("Error decoding from file " + filename);
        } // end catch: IOException
        finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } // end finally

        return decodedData;
    } // end decodeFromFile

    /**
     * Convenience method for reading a binary file and base64-encoding it.
     *
     * @param filename
     *            Filename for reading binary data
     * @return base64-encoded string or null if unsuccessful
     * @since 2.1
     */
    public static String encodeFromFile(String filename) {
        String encodedData = null;
        Base64InputStream bis = null;
        try {
            // Set up some useful variables
            java.io.File file = new java.io.File(filename);
            byte[] buffer = new byte[(int) (file.length() * 1.4)];
            int length = 0;
            int numBytes = 0;

            // Open a stream
            bis = new Base64InputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(file)), Base64.ENCODE);

            // Read until done
            while ((numBytes = bis.read(buffer, length, 4096)) >= 0) {
                length += numBytes;
            }

            // Save in a variable to return
            encodedData = new String(buffer, 0, length, PREFERRED_ENCODING);

        } // end try
        catch (java.io.IOException e) {
            System.err.println("Error encoding from file " + filename);
        } // end catch: IOException
        finally {
            try {
                bis.close();
            } catch (Exception e) {
            }
        } // end finally

        return encodedData;
    } // end encodeFromFile
} // end class Base64

