package nl.pvanassen.bplist.ext.base64;

class Constants {
    /* ******** P R I V A T E F I E L D S ******** */
    /** Maximum line length (76) of Base64 output. */
    final static int MAX_LINE_LENGTH = 76;
    /** The new line character (\n) as a byte. */
    final static byte NEW_LINE = (byte) '\n';
    /** The equals sign (=) as a byte. */
    final static byte EQUALS_SIGN = (byte) '=';
    /** Preferred encoding. */
    final static String PREFERRED_ENCODING = "UTF-8";
    /** Indicates white space in encoding */
    final static byte WHITE_SPACE_ENC = -5; 

    private Constants() {
        
    }
    
    
}
