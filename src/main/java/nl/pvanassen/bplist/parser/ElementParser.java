package nl.pvanassen.bplist.parser;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import nl.pvanassen.bplist.parser.objects.BPLArray;
import nl.pvanassen.bplist.parser.objects.BPLDict;
import nl.pvanassen.bplist.parser.objects.BPLUid;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElementParser {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Factory for generating XML data types. */
    private static DatatypeFactory datatypeFactory;

    /** Time interval based dates are measured in seconds from 2001-01-01. */
    private final static long TIMER_INTERVAL_TIMEBASE = new GregorianCalendar(
	    2001, 0, 1, 1, 0, 0).getTimeInMillis();

    public List<Object> parseObjectTable(File file) throws IOException {
	RandomAccessFile raf = null;
	try {
	    raf = new RandomAccessFile(file, "r");
	    return parseObjectTable(raf);
	} finally {
	    IOUtils.closeQuietly(raf);
	}
    }

    /**
     * Parse object table with a random access file. This method will not close
     * the file for you.
     * 
     * @param raf
     *            Random access file
     * @return List of objects parsed
     * @throws IOException
     *             In case of an error
     */
    public List<Object> parseObjectTable(RandomAccessFile raf)
	    throws IOException {

	// Parse the HEADER
	// ----------------
	// magic number ("bplist")
	// file format version ("00")
	int bpli = raf.readInt();
	int st00 = raf.readInt();
	if (bpli != 0x62706c69 || st00 != 0x73743030) {
	    throw new IOException(
		    "parseHeader: File does not start with 'bplist00' magic.");
	}

	// Parse the TRAILER
	// ----------------
	// byte size of offset ints in offset table
	// byte size of object refs in arrays and dicts
	// number of offsets in offset table (also is number of objects)
	// element # in offset table which is top level object
	raf.seek(raf.length() - 32);
	// count of offset ints in offset table
	int offsetCount = (int) raf.readLong();
	// count of object refs in arrays and dicts
	int refCount = (int) raf.readLong();
	// count of offsets in offset table (also is number of objects)
	int objectCount = (int) raf.readLong();
	// element # in offset table which is top level object
	int topLevelOffset = (int) raf.readLong();
	raf.seek(8);

	// Read everything in memory hmmmm
	byte[] buf = new byte[topLevelOffset - 8];
	raf.readFully(buf);
	ByteArrayInputStream stream = new ByteArrayInputStream(buf);
	
	return parseObjectTable(new DataInputStream(stream), refCount);
    }

    /**
     * Object Formats (marker byte followed by additional info in some cases)
     * <ul>
     * <li>null 0000 0000</li>
     * <li>bool 0000 1000 // false</li>
     * <li>bool 0000 1001 // true</li>
     * <li>fill 0000 1111 // fill byte</li>
     * <li>int 0001 nnnn ... // # of bytes is 2^nnnn, big-endian bytes</li>
     * <li>real 0010 nnnn ... // # of bytes is 2^nnnn, big-endian bytes</li>
     * <li>date 0011 0011 ... // 8 byte float follows, big-endian bytes</li>
     * <li>data 0100 nnnn [int] ... // nnnn is number of bytes unless 1111 then
     * int count follows, followed by bytes</li>
     * <li>string 0101 nnnn [int] ... // ASCII string, nnnn is # of chars, if
     * 1111 then int count, else bytes</li>
     * <li>string 0110 nnnn [int] ... // Unicode string, nnnn is # of chars,
     * else 1111 then int count, then big-endian 2-byte shorts</li>
     * <li>0111 xxxx // unused</li>
     * <li>uid 1000 nnnn ... // nnnn+1 is # of bytes</li>
     * <li>1001 xxxx // unused</li>
     * <li>array 1010 nnnn [int] objref* // nnnn is count, unless '1111', then
     * int count follows</li>
     * <li>1011 xxxx // unused</li>
     * <li>1100 xxxx // unused</li>
     * <li>dict 1101 nnnn [int] keyref* objref* // nnnn is count, unless '1111',
     * then int count follows</li>
     * <li>1110 xxxx // unused</li>
     * <li>1111 xxxx // unused</li>
     * </ul>
     * 
     */
    private List<Object> parseObjectTable(DataInputStream in, int refCount)
	    throws IOException {
	List<Object> objectTable = new LinkedList<Object>();
	int marker;
	while ((marker = in.read()) != -1) {
	    // System.err.println("parseObjectTable marker=" +
	    // Integer.toBinaryString(marker)+" 0x"+Integer.toHexString(marker)+" @0x"+Long.toHexString(getPosition()));
	    switch ((marker & 0xf0) >> 4) {
	    case 0: {
		parseBoolean(marker & 0xf, objectTable);
		break;
	    }
	    case 1: {
		int count = 1 << (marker & 0xf);
		parseInt(in, count, objectTable);
		break;
	    }
	    case 2: {
		int count = 1 << (marker & 0xf);
		parseReal(in, count, objectTable);
		break;
	    }
	    case 3: {
		switch (marker & 0xf) {
		case 3:
		    parseDate(in, objectTable);
		    break;
		default:
		    throw new IOException("parseObjectTable: illegal marker "
			    + Integer.toBinaryString(marker));
		}
		break;
	    }
	    case 4: {
		int count = marker & 0xf;
		if (count == 15) {
		    count = readCount(in);
		}
		parseData(in, count, objectTable);
		break;
	    }
	    case 5: {
		int count = marker & 0xf;
		if (count == 15) {
		    count = readCount(in);
		}
		parseAsciiString(in, count, objectTable);
		break;
	    }
	    case 6: {
		int count = marker & 0xf;
		if (count == 15) {
		    count = readCount(in);
		}
		parseUnicodeString(in, count, objectTable);
		break;
	    }
	    case 7: {
		if (logger.isDebugEnabled()) {
		    logger.debug("parseObjectTable: illegal marker "
			    + Integer.toBinaryString(marker));
		}
		return objectTable;
		// throw new
		// IOException("parseObjectTable: illegal marker "+Integer.toBinaryString(marker));
		// break;
	    }
	    case 8: {
		int count = (marker & 0xf) + 1;
		if (logger.isDebugEnabled()) {
		    logger.debug("uid " + count);
		}
		parseUID(in, count, objectTable);
		break;
	    }
	    case 9: {
		throw new IOException("parseObjectTable: illegal marker "
			+ Integer.toBinaryString(marker));
		// break;
	    }
	    case 10: {
		int count = marker & 0xf;
		if (count == 15) {
		    count = readCount(in);
		}
		if (refCount > 255) {
		    parseShortArray(in, count, objectTable);
		} else {
		    parseByteArray(in, count, objectTable);
		}
		break;
	    }
	    case 11: {
		throw new IOException("parseObjectTable: illegal marker "
			+ Integer.toBinaryString(marker));
		// break;
	    }
	    case 12: {
		throw new IOException("parseObjectTable: illegal marker "
			+ Integer.toBinaryString(marker));
		// break;
	    }
	    case 13: {
		int count = marker & 0xf;
		if (count == 15) {
		    count = readCount(in);
		}
		if (refCount > 256) {
		    parseShortDict(in, count, objectTable);
		} else {
		    parseByteDict(in, count, objectTable);
		}
		break;
	    }
	    case 14: {
		throw new IOException("parseObjectTable: illegal marker "
			+ Integer.toBinaryString(marker));
		// break;
	    }
	    case 15: {
		throw new IOException("parseObjectTable: illegal marker "
			+ Integer.toBinaryString(marker));
		// break;
	    }
	    }
	}
	return objectTable;
    }

    /**
     * Reads a count value from the object table. Count values are encoded using
     * the following scheme:
     * 
     * int 0001 nnnn ... // # of bytes is 2^nnnn, big-endian bytes
     */
    private int readCount(DataInputStream in) throws IOException {
	int marker = in.read();
	if (marker == -1) {
	    throw new IOException("variableLengthInt: Illegal EOF in marker");
	}
	if (((marker & 0xf0) >> 4) != 1) {
	    throw new IOException("variableLengthInt: Illegal marker "
		    + Integer.toBinaryString(marker));
	}
	int count = 1 << (marker & 0xf);
	int value = 0;
	for (int i = 0; i < count; i++) {
	    int b = in.read();
	    if (b == -1) {
		throw new IOException("variableLengthInt: Illegal EOF in value");
	    }
	    value = (value << 8) | b;
	}
	return value;
    }

    /**
     * null 0000 0000 bool 0000 1000 // false bool 0000 1001 // true fill 0000
     * 1111 // fill byte
     */
    private void parseBoolean(int primitive, List<Object> objectTable)
	    throws IOException {
	switch (primitive) {
	case 0:
	    objectTable.add(null);
	    break;
	case 8:
	    objectTable.add(Boolean.FALSE);
	    break;
	case 9:
	    objectTable.add(Boolean.TRUE);
	    break;
	case 15:
	    // fill byte: don't add to object table
	    break;
	default:
	    throw new IOException("parsePrimitive: illegal primitive "
		    + Integer.toBinaryString(primitive));
	}
    }

    /**
     * array 1010 nnnn [int] objref* // nnnn is count, unless '1111', then int
     * count follows
     */
    private void parseByteArray(DataInputStream in, int count,
	    List<Object> objectTable) throws IOException {
	BPLArray arr = new BPLArray(objectTable, new int[count]);

	for (int i = 0; i < count; i++) {
	    arr.getObjref()[i] = in.readByte() & 0xff;
	    if (arr.getObjref()[i] == -1) {
		throw new IOException("parseByteArray: illegal EOF in objref*");
	    }
	}

	objectTable.add(arr);
    }

    /**
     * array 1010 nnnn [int] objref* // nnnn is count, unless '1111', then int
     * count follows
     */
    private void parseShortArray(DataInputStream in, int count,
	    List<Object> objectTable) throws IOException {
	BPLArray arr = new BPLArray(objectTable, new int[count]);

	for (int i = 0; i < count; i++) {
	    arr.getObjref()[i] = in.readShort() & 0xffff;
	    if (arr.getObjref()[i] == -1) {
		throw new IOException("parseShortArray: illegal EOF in objref*");
	    }
	}

	objectTable.add(arr);
    }

    /*
     * data 0100 nnnn [int] ... // nnnn is number of bytes unless 1111 then int
     * count follows, followed by bytes
     */

    private void parseData(DataInputStream in, int count,
	    List<Object> objectTable) throws IOException {
	byte[] data = new byte[count];
	in.readFully(data);
	objectTable.add(data);
    }

    /**
     * byte dict 1101 nnnn keyref* objref* // nnnn is less than '1111'
     */
    private void parseByteDict(DataInputStream in, int count,
	    List<Object> objectTable) throws IOException {
	BPLDict dict = new BPLDict(objectTable, new int[count], new int[count]);

	for (int i = 0; i < count; i++) {
	    dict.getKeyref()[i] = in.readByte() & 0xff;
	}
	for (int i = 0; i < count; i++) {
	    dict.getObjref()[i] = in.readByte() & 0xff;
	}
	objectTable.add(dict);
    }

    /**
     * short dict 1101 ffff int keyref* objref* // int is count
     */
    private void parseShortDict(DataInputStream in, int count,
	    List<Object> objectTable) throws IOException {
	BPLDict dict = new BPLDict(objectTable, new int[count], new int[count]);

	for (int i = 0; i < count; i++) {
	    dict.getKeyref()[i] = in.readShort() & 0xffff;
	}
	for (int i = 0; i < count; i++) {
	    dict.getObjref()[i] = in.readShort() & 0xffff;
	}
	objectTable.add(dict);
    }

    /**
     * string 0101 nnnn [int] ... // ASCII string, nnnn is # of chars, else 1111
     * then int count, then bytes
     */
    private void parseAsciiString(DataInputStream in, int count,
	    List<Object> objectTable) throws IOException {
	byte[] buf = new byte[count];
	in.readFully(buf);
	String str = new String(buf, "ASCII");
	objectTable.add(str);
    }

    private void parseUID(DataInputStream in, int count,
	    List<Object> objectTable) throws IOException {
	if (count > 4) {
	    throw new IOException("parseUID: unsupported byte count: " + count);
	}
	byte[] uid = new byte[count];
	in.readFully(uid);
	objectTable.add(new BPLUid(new BigInteger(uid).intValue()));
    }

    /**
     * int 0001 nnnn ... // # of bytes is 2^nnnn, big-endian bytes
     */
    private void parseInt(DataInputStream in, int count,
	    List<Object> objectTable) throws IOException {
	if (count > 8) {
	    throw new IOException("parseInt: unsupported byte count: " + count);
	}
	long value = 0;
	for (int i = 0; i < count; i++) {
	    int b = in.read();
	    if (b == -1) {
		throw new IOException("parseInt: Illegal EOF in value");
	    }
	    value = (value << 8) | b;
	}
	objectTable.add(value);
    }

    /**
     * real 0010 nnnn ... // # of bytes is 2^nnnn, big-endian bytes
     */
    private void parseReal(DataInputStream in, int count,
	    List<Object> objectTable) throws IOException {
	switch (count) {
	case 4:
	    objectTable.add(new Float(in.readFloat()));
	    break;
	case 8:
	    objectTable.add(new Double(in.readDouble()));
	    break;
	default:
	    throw new IOException("parseReal: unsupported byte count:" + count);
	}
    }

    /**
     * unknown 0011 0000 ... // 8 byte float follows, big-endian bytes
     */
    /*
     * private void parseUnknown(DataInputStream in) throws IOException {
     * in.skipBytes(1); objectTable.add("unknown"); }
     */

    /**
     * date 0011 0011 ... // 8 byte float follows, big-endian bytes
     */
    private void parseDate(DataInputStream in, List<Object> objectTable)
	    throws IOException {
	objectTable.add(fromTimerInterval(in.readDouble()));
    }

    /**
     * string 0110 nnnn [int] ... // Unicode string, nnnn is # of chars, else
     * 1111 then int count, then big-endian 2-byte shorts
     */
    private void parseUnicodeString(DataInputStream in, int count,
	    List<Object> objectTable) throws IOException {
	char[] buf = new char[count];
	for (int i = 0; i < count; i++) {
	    buf[i] = in.readChar();
	}
	String str = new String(buf);
	objectTable.add(str);
    }

    /**
     * Timer interval based dates are measured in seconds from 1/1/2001. Timer
     * intervals have no time zone.
     */
    private static XMLGregorianCalendar fromTimerInterval(double timerInterval) {
	GregorianCalendar gc = new GregorianCalendar();
	gc.setTime(new Date(TIMER_INTERVAL_TIMEBASE + (long) timerInterval
		* 1000L));
	XMLGregorianCalendar xmlgc = getDatatypeFactory()
		.newXMLGregorianCalendar(gc);
	xmlgc.setFractionalSecond(null);
	xmlgc.setTimezone(DatatypeConstants.FIELD_UNDEFINED);
	return xmlgc;
    }

    /** Gets the factory for XML data types. */
    private static DatatypeFactory getDatatypeFactory() {
	if (datatypeFactory == null) {
	    try {
		datatypeFactory = DatatypeFactory.newInstance();
	    } catch (DatatypeConfigurationException ex) {
		InternalError ie = new InternalError(
			"Can't create XML datatype factory.");
		ie.initCause(ex);
		throw ie;
	    }
	}
	return datatypeFactory;
    }
}
