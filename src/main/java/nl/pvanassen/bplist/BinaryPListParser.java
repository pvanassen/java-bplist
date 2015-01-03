/*
 * @(#)BinaryPListParser.java
 *
 * Copyright (c) 2005-2013 Werner Randelshofer, Switzerland.
 * All rights reserved.
 *
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package nl.pvanassen.bplist;

import java.io.*;
import java.util.*;

import javax.xml.datatype.XMLGregorianCalendar;

import nl.pvanassen.bplist.ext.base64.Base64;
import nl.pvanassen.bplist.ext.nanoxml.XMLElement;
import nl.pvanassen.bplist.parser.objects.*;

/**
 * Reads a binary PList file and returns it as a NanoXML XMLElement.
 * <p>
 * The NanoXML XMLElement returned by this reader is equivalent to the XMLElement returned, if a PList file in XML format is parsed with NanoXML.
 * <p>
 * Description about property list taken from <a href= "http://developer.apple.com/documentation/Cocoa/Conceptual/PropertyLists/index.html#//apple_ref/doc/uid/10000048i" > Apple's
 * online documentation</a>:
 * <p>
 * "A property list is a data representation used by Mac OS X Cocoa and Core Foundation as a convenient way to store, organize, and access standard object types. Frequently called
 * a plist, a property list is an object of one of several certain Cocoa or Core Foundation types, including arrays, dictionaries, strings, binary data, numbers, dates, and Boolean
 * values. If the object is a container (an array or dictionary), all objects contained within it must also be supported property list objects. (Arrays and dictionaries can contain
 * objects not supported by the architecture, but are then not property lists, and cannot be saved and restored with the various property list methods.)" Description of the binary
 * plist format derived from http://opensource.apple.com/source/CF/CF-635/CFBinaryPList.c EBNF description of the file format:
 * 
 * <pre>
 * bplist ::= header objectTable
 * offsetTable trailer
 * 
 * header ::= magicNumber fileFormatVersion magicNumber ::= "bplist"
 * fileFormatVersion ::= "00"
 * 
 * objectTable ::= { null | bool | fill | number | date | data | string |
 * uid | array | dict }
 * 
 * null ::= 0b0000 0b0000
 * 
 * bool ::= false | true false ::= 0b0000 0b1000 true ::= 0b0000 0b1001
 * 
 * fill ::= 0b0000 0b1111 // fill byte
 * 
 * number ::= int | real int ::= 0b0001 0bnnnn byte*(2^nnnn) // 2^nnnn
 * big-endian bytes real ::= 0b0010 0bnnnn byte*(2^nnnn) // 2^nnnn
 * big-endian bytes
 * 
 * unknown::= 0b0011 0b0000 byte*8 // 8 byte float big-endian bytes ?
 * 
 * date ::= 0b0011 0b0011 byte*8 // 8 byte float big-endian bytes
 * 
 * data ::= 0b0100 0bnnnn [int] byte* // nnnn is number of bytes // unless
 * 0b1111 then a int // variable-sized object follows // to indicate the
 * number of bytes
 * 
 * string ::= asciiString | unicodeString asciiString ::= 0b0101 0bnnnn
 * [int] byte* unicodeString ::= 0b0110 0bnnnn [int] short* // nnnn is
 * number of bytes // unless 0b1111 then a int // variable-sized object
 * follows // to indicate the number of bytes
 * 
 * uid ::= 0b1000 0bnnnn byte* // nnnn+1 is # of bytes
 * 
 * array ::= 0b1010 0bnnnn [int] objref* // // nnnn is number of objref //
 * unless 0b1111 then a int // variable-sized object follows // to indicate
 * the number of objref
 * 
 * dict ::= 0b1010 0bnnnn [int] keyref* objref* // nnnn is number of keyref
 * and // objref pairs // unless 0b1111 then a int // variable-sized object
 * follows // to indicate the number of pairs
 * 
 * objref = byte | short // if refCount // is less than 256 then objref is
 * // an unsigned byte, otherwise it // is an unsigned big-endian short
 * 
 * keyref = byte | short // if refCount // is less than 256 then objref is
 * // an unsigned byte, otherwise it // is an unsigned big-endian short
 * 
 * unused ::= 0b0111 0bxxxx | 0b1001 0bxxxx | 0b1011 0bxxxx | 0b1100 0bxxxx
 * | 0b1110 0bxxxx | 0b1111 0bxxxx
 * 
 * 
 * offsetTable ::= { int } // List of ints, byte size of which // is given
 * in trailer // These are the byte offsets into // the file. // The number
 * of the ffsets is given // in the trailer.
 * 
 * trailer ::= refCount offsetCount objectCount topLevelOffset
 * 
 * refCount ::= byte*8 // unsigned big-endian long offsetCount ::= byte*8 //
 * unsigned big-endian long objectCount ::= byte*8 // unsigned big-endian
 * long topLevelOffset ::= byte*8 // unsigned big-endian long
 * </pre>
 * 
 * *
 * 
 * @see nl.pvanassen.bplist.ext.nanoxml.XMLElement
 * @author Werner Randelshofer
 * @version $Id$
 */
public class BinaryPListParser {
    /**
     * Object table. We gradually fill in objects from the binary PList object
     * table into this list.
     */
    private List<Object> objectTable;
    private ElementParser parser = new ElementParser();

    /**
     * Parses a binary PList file and turns it into a XMLElement. The XMLElement
     * is equivalent with a XML PList file parsed using NanoXML.
     * 
     * @param file
     *            A file containing a binary PList.
     * @return Returns the parsed XMLElement.
     * @throws IOException If the file is not found
     */
    public XMLElement parse(File file) throws IOException {

        // Parse the OBJECT TABLE
        // ----------------------
        objectTable = parser.parseObjectTable(file);

        // Convert the object table to XML and return it
        XMLElement root = new XMLElement(new HashMap<String, char[]>(), false, false);
        root.setName("plist");
        root.setAttribute("version", "1.0");
        convertObjectTableToXML(root, objectTable.get(0));
        return root;
    }

    /**
     * Converts the object table in the binary PList into an XMLElement.
     */
    private void convertObjectTableToXML(XMLElement parent, Object object) {
        XMLElement elem = parent.createAnotherElement();
        if (object instanceof BPLDict) {
            BPLDict dict = (BPLDict) object;
            elem.setName("dict");
            for (int i = 0; i < dict.getKeyref().length; i++) {
                XMLElement key = parent.createAnotherElement();
                key.setName("key");
                key.setContent(dict.getKey(i));
                elem.addChild(key);
                convertObjectTableToXML(elem, dict.getValue(i));
            }
        } else if (object instanceof BPLArray) {
            BPLArray arr = (BPLArray) object;
            elem.setName("array");
            for (int i = 0; i < arr.getObjref().length; i++) {
                convertObjectTableToXML(elem, arr.getValue(i));
            }

        } else if (object instanceof String) {
            elem.setName("string");
            elem.setContent((String) object);
        } else if (object instanceof Integer) {
            elem.setName("integer");
            elem.setContent(object.toString());
        } else if (object instanceof Long) {
            elem.setName("integer");
            elem.setContent(object.toString());
        } else if (object instanceof Float) {
            elem.setName("real");
            elem.setContent(object.toString());
        } else if (object instanceof Double) {
            elem.setName("real");
            elem.setContent(object.toString());
        } else if (object instanceof Boolean) {
            elem.setName("boolean");
            elem.setContent(object.toString());
        } else if (object instanceof byte[]) {
            elem.setName("data");
            elem.setContent(Base64.encodeBytes((byte[]) object, Base64.DONT_BREAK_LINES));
        } else if (object instanceof XMLGregorianCalendar) {
            elem.setName("date");
            elem.setContent(((XMLGregorianCalendar) object).toXMLFormat() + "Z");
        } else if (object instanceof BPLUid) {
            elem.setName("UID");
            elem.setContent(Integer.toString(((BPLUid) object).getNumber()));
        } else {
            elem.setName("unsupported");
            elem.setContent(object.toString());
        }
        parent.addChild(elem);
    }

}
