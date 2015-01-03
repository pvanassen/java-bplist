/* Werner Randelshofer 2006-01-08
 * Replaced Java 1.1 collections by Java 1.2 collections.
 */
/* XMLElement.java
 *
 * $Revision: 1.4 $
 * $Date: 2002/03/24 10:27:59 $
 * $Name: RELEASE_2_2_1 $
 *
 * This file is part of NanoXML 2 Lite.
 * Copyright (C) 2000-2002 Marc De Scheemaecker, All Rights Reserved.
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 *  1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software in
 *     a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 *
 *  2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 *
 *  3. This notice may not be removed or altered from any source distribution.
 *****************************************************************************/

package nl.pvanassen.bplist.ext.nanoxml;

import java.io.*;
import java.util.*;

/**
 * XMLElement is a representation of an XML object. The object is able to parse
 * XML code.
 * <P>
 * <DL>
 * <DT><B>Parsing XML Data</B></DT>
 * <DD>You can parse XML data using the following code:
 * <UL>
 * <CODE>
 * XMLElement xml = new XMLElement();<BR>
 * FileReader reader = new FileReader("filename.xml");<BR>
 * xml.parseFromReader(reader);
 * </CODE>
 * </UL>
 * </DD>
 * </DL>
 * <DL>
 * <DT><B>Retrieving Attributes</B></DT>
 * <DD>You can enumerate the attributes of an element using the method {@link #enumerateAttributeNames() enumerateAttributeNames}. The attribute values can be retrieved using the
 * method {@link #getStringAttribute(java.lang.String) getStringAttribute}. The following example shows how to list the attributes of an element:
 * <UL>
 * <CODE>
 * XMLElement element = ...;<BR>
 * Iterator iter = element.getAttributeNames();<BR>
 * while (iter.hasNext()) {<BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;String key = (String) iter.next();<BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;String value = element.getStringAttribute(key);<BR>
 * &nbsp;&nbsp;&nbsp;&nbsp;System.out.println(key + " = " + value);<BR>
 * }
 * </CODE>
 * </UL>
 * </DD>
 * </DL>
 * <DL>
 * <DT><B>Retrieving Child Elements</B></DT>
 * <DD>You can enumerate the children of an element using {@link #iterateChildren()
 * iterateChildren}. The number of child iterator can be retrieved using {@link #countChildren() countChildren}.</DD>
 * </DL>
 * <DL>
 * <DT><B>Elements Containing Character Data</B></DT>
 * <DD>If an iterator contains character data, like in the following example:
 * <UL>
 * <CODE>
 * &lt;title&gt;The Title&lt;/title&gt;
 * </CODE>
 * </UL>
 * you can retrieve that data using the method {@link #getContent() getContent}.</DD>
 * </DL>
 * <DL>
 * <DT><B>Subclassing XMLElement</B></DT>
 * <DD>When subclassing XMLElement, you need to override the method {@link #createAnotherElement() createAnotherElement} which has to return a new copy of the receiver.</DD>
 * </DL>
 * <P>
 *
 * @author Marc De Scheemaecker &lt;<A
 *         href="mailto:cyberelf@mac.com">cyberelf@mac.com</A>&gt;
 * @version 2005-06-18 Werner Randelshofer: Adapted for Java 2 Collections API. <br>
 *          $Name: RELEASE_2_2_1 $, $Revision: 1.4 $
 */
public class XMLElement {

    /**
     * Serialization serial version ID.
     */
    static final long serialVersionUID = 6685035139346394777L;

    /**
     * The attributes given to the element.
     * <dl>
     * <dt><b>Invariants:</b></dt>
     * <dd>
     * <ul>
     * <li>The field can be empty.
     * <li>The field is never <code>null</code>.
     * <li>The keySet().iterator and the values are strings.
     * </ul>
     * </dd>
     * </dl>
     */
    private Map<String, String> attributes;

    /**
     * Child iterator of the element.
     * <dl>
     * <dt><b>Invariants:</b></dt>
     * <dd>
     * <ul>
     * <li>The field can be empty.
     * <li>The field is never <code>null</code>.
     * <li>The iterator are instances of <code>XMLElement</code> or a subclass of <code>XMLElement</code>.
     * </ul>
     * </dd>
     * </dl>
     */
    private List<XMLElement> children;

    /**
     * The name of the element.
     * <dl>
     * <dt><b>Invariants:</b></dt>
     * <dd>
     * <ul>
     * <li>The field is <code>null</code> iff the element is not initialized by either parse or setName.
     * <li>If the field is not <code>null</code>, it's not empty.
     * <li>If the field is not <code>null</code>, it contains a valid XML identifier.
     * </ul>
     * </dd>
     * </dl>
     */
    private String name;

    /**
     * The #PCDATA content of the object.
     * <dl>
     * <dt><b>Invariants:</b></dt>
     * <dd>
     * <ul>
     * <li>The field is <code>null</code> iff the element is not a #PCDATA element.
     * <li>The field can be any string, including the empty string.
     * </ul>
     * </dd>
     * </dl>
     */
    private String contents;

    /**
     * Conversion table for &amp;...; entities. The keySet().iterator are the
     * entity names without the &amp; and ; delimiters.
     * <dl>
     * <dt><b>Invariants:</b></dt>
     * <dd>
     * <ul>
     * <li>The field is never <code>null</code>.
     * <li>The field always contains the following associations: "lt"&nbsp;=&gt;&nbsp;"&lt;", "gt"&nbsp;=&gt;&nbsp;"&gt;", "quot"&nbsp;=&gt;&nbsp;"\"", "apos"&nbsp;=&gt;&nbsp;"'",
     * "amp"&nbsp;=&gt;&nbsp;"&amp;"
     * <li>The keySet().iterator are strings
     * <li>The values are char arrays
     * </ul>
     * </dd>
     * </dl>
     */
    private Map<String, char[]> entities;


    /**
     * <code>true</code> if the case of the element and attribute names are case
     * insensitive.
     */
    private boolean ignoreCase;

    /**
     * <code>true</code> if the leading and trailing whitespace of #PCDATA
     * sections have to be ignored.
     */
    private boolean ignoreWhitespace;

    /**
     * Creates and initializes a new XML element.
     *
     * @param entities
     *            The entity conversion table.
     * @param skipLeadingWhitespace
     *            <code>true</code> if leading and trailing whitespace in PCDATA
     *            content has to be removed.
     * @param ignoreCase
     *            <code>true</code> if the case of element and attribute names
     *            have to be ignored.
     *            </dl>
     *            <dl>
     *            <dt><b>Preconditions:</b></dt>
     *            <dd>
     *            <ul>
     *            <li><code>entities != null</code>
     *            </ul>
     *            </dd>
     *            </dl>
     *            <dl>
     *            <dt><b>Postconditions:</b></dt>
     *            <dd>
     *            <ul>
     *            <li>countChildren() => 0
     *            <li>iterateChildren() => empty enumeration
     *            <li>enumeratePropertyNames() => empty enumeration
     *            <li>getChildren() => empty vector
     *            <li>getContent() => ""
     *            <li>getLineNr() => 0
     *            <li>getName() => null
     *            </ul>
     *            </dd>
     *            </dl>
     *            <dl>
     */
    public XMLElement(Map<String, char[]> entities, boolean skipLeadingWhitespace, boolean ignoreCase) {
        this(entities, skipLeadingWhitespace, true, ignoreCase);
    }

    /**
     * Creates and initializes a new XML element.
     * <P>
     * This constructor should <I>only</I> be called from {@link #createAnotherElement() createAnotherElement} to create child iterator.
     *
     * @param entities
     *            The entity conversion table.
     * @param skipLeadingWhitespace
     *            <code>true</code> if leading and trailing whitespace in PCDATA
     *            content has to be removed.
     * @param fillBasicConversionTable
     *            <code>true</code> if the basic entities need to be added to
     *            the entity list.
     * @param ignoreCase
     *            <code>true</code> if the case of element and attribute names
     *            have to be ignored.
     *            </dl>
     *            <dl>
     *            <dt><b>Preconditions:</b></dt><dd>
     *            <ul>
     *            <li><code>entities != null</code> <li>if <code> fillBasicConversionTable == false</code> then <code>entities </code> contains at least the following entries:
     *            <code>amp </code>, <code>lt</code>, <code>gt</code>, <code>apos</code> and <code>quot</code>
     *            </ul>
     *            </dd>
     *            </dl>
     *            <dl>
     *            <dt><b>Postconditions:</b></dt><dd>
     *            <ul>
     *            <li>countChildren() => 0 <li>iterateChildren() => empty enumeration <li>enumeratePropertyNames() => empty enumeration <li>getChildren() => empty vector <li>
     *            getContent() => "" <li> getLineNr() => 0 <li>getName() => null
     *            </ul>
     *            </dd>
     *            </dl>
     *            <dl>
     * @see XMLElement#createAnotherElement()
     */
    protected XMLElement(Map<String, char[]> entities, boolean skipLeadingWhitespace, boolean fillBasicConversionTable, boolean ignoreCase) {
        ignoreWhitespace = skipLeadingWhitespace;
        this.ignoreCase = ignoreCase;
        name = null;
        contents = "";
        attributes = new HashMap<String, String>();
        children = new LinkedList<XMLElement>();
        this.entities = entities;
        if (fillBasicConversionTable) {
            this.entities.put("amp", new char[] { '&' });
            this.entities.put("quot", new char[] { '"' });
            this.entities.put("apos", new char[] { '\'' });
            this.entities.put("lt", new char[] { '<' });
            this.entities.put("gt", new char[] { '>' });
        }
    }

    /**
     * Adds a child element.
     *
     * @param child
     *            The child element to add.
     *            </dl>
     *            <dl>
     *            <dt><b>Preconditions:</b></dt>
     *            <dd>
     *            <ul>
     *            <li><code>child != null</code>
     *            <li><code>child.getName() != null</code>
     *            <li><code>child</code> does not have a parent element
     *            </ul>
     *            </dd>
     *            </dl>
     *            <dl>
     *            <dt><b>Postconditions:</b></dt>
     *            <dd>
     *            <ul>
     *            <li>countChildren() => old.countChildren() + 1
     *            <li>iterateChildren() => old.iterateChildren() + child
     *            <li>getChildren() => old.iterateChildren() + child
     *            </ul>
     *            </dd>
     *            </dl>
     *            <dl>
     * @see XMLElement#countChildren()
     * @see XMLElement#iterateChildren()
     * @see XMLElement#getChildren()
     * @see XMLElement#removeChild(XMLElement) removeChild(XMLElement)
     */
    public void addChild(XMLElement child) {
        children.add(child);
    }

    /**
     * Adds or modifies an attribute.
     *
     * @param name
     *            The name of the attribute.
     * @param value
     *            The value of the attribute.
     *            </dl>
     *            <dl>
     *            <dt><b>Preconditions:</b></dt>
     *            <dd>
     *            <ul>
     *            <li><code>name != null</code>
     *            <li><code>name</code> is a valid XML identifier
     *            <li><code>value != null</code>
     *            </ul>
     *            </dd>
     *            </dl>
     *            <dl>
     *            <dt><b>Postconditions:</b></dt>
     *            <dd>
     *            <ul>
     *            <li>enumerateAttributeNames() => old.enumerateAttributeNames() + name
     *            <li>getAttribute(name) => value
     *            </ul>
     *            </dd>
     *            </dl>
     *            <dl>
     * @see XMLElement#setDoubleAttribute(java.lang.String, double)
     *      setDoubleAttribute(String, double)
     * @see XMLElement#setIntAttribute(java.lang.String, int)
     *      setIntAttribute(String, int)
     * @see XMLElement#enumerateAttributeNames()
     * @see XMLElement#getAttribute(java.lang.String) getAttribute(String)
     * @see XMLElement#getAttribute(java.lang.String, java.lang.Object)
     *      getAttribute(String, Object)
     * @see XMLElement#getAttribute(java.lang.String, java.util.HashMap, java.lang.String, boolean) getAttribute(String, HashMap, String,
     *      boolean)
     * @see XMLElement#getStringAttribute(java.lang.String)
     *      getStringAttribute(String)
     * @see XMLElement#getStringAttribute(java.lang.String, java.lang.String)
     *      getStringAttribute(String, String)
     * @see XMLElement#getStringAttribute(java.lang.String, java.util.HashMap, java.lang.String, boolean) getStringAttribute(String, HashMap,
     *      String, boolean)
     */
    public void setAttribute(String name, Object value) {
        if (ignoreCase) {
            name = name.toUpperCase();
        }
        attributes.put(name, value.toString());
    }

    /**
     * Enumerates the child iterator.
     * <dl>
     * <dt><b>Postconditions:</b></dt>
     * <dd>
     * <ul>
     * <li><code>result != null</code>
     * </ul>
     * </dd>
     * </dl>
     *
     * @see XMLElement#addChild(XMLElement) addChild(XMLElement)
     * @see XMLElement#countChildren()
     * @see XMLElement#getChildren()
     * @see XMLElement#removeChild(XMLElement) removeChild(XMLElement)
     */
    private Iterator<XMLElement> iterateChildren() {
        return children.iterator();
    }

    /**
     * Creates a new similar XML element.
     * <p>
     * You should override this method when subclassing XMLElement.
     * </p>
     * @return Element
     */
    public XMLElement createAnotherElement() {
        return new XMLElement(entities, ignoreWhitespace, false, ignoreCase);
    }

    /**
     * Changes the content string.
     *
     * @param content
     *            The new content string.
     */
    public void setContent(String content) {
        contents = content;
    }

    /**
     * Changes the name of the element.
     *
     * @param name
     *            The new name.
     *            </dl>
     *            <dl>
     *            <dt><b>Preconditions:</b></dt>
     *            <dd>
     *            <ul>
     *            <li><code>name != null</code>
     *            <li><code>name</code> is a valid XML identifier
     *            </ul>
     *            </dd>
     *            </dl>
     * @see XMLElement#getName()
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Writes the XML element to a string.
     *
     * @see XMLElement#write(java.io.Writer) write(Writer)
     */
    @Override
    public String toString() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out);
            write(writer);
            writer.flush();
            return new String(out.toByteArray());
        } catch (IOException e) {
            // Java exception handling suxx
            return super.toString();
        }
    }

    /**
     * Writes the XML element to a writer.
     *
     * @param writer
     *            The writer to write the XML data to.
     *            </dl>
     *            <dl>
     *            <dt><b>Preconditions:</b></dt>
     *            <dd>
     *            <ul>
     *            <li><code>writer != null</code>
     *            <li><code>writer</code> is not closed
     *            </ul>
     *            </dd>
     *            </dl>
     * @throws java.io.IOException
     *             If the data could not be written to the writer.
     * @see XMLElement#toString()
     */
    private void write(Writer writer) throws IOException {
        if (name == null) {
            writeEncoded(writer, contents);
            return;
        }
        writer.write('<');
        writer.write(name);
        if (!attributes.isEmpty()) {
            Iterator<String> iter = attributes.keySet().iterator();
            while (iter.hasNext()) {
                writer.write(' ');
                String key = iter.next();
                String value = attributes.get(key);
                writer.write(key);
                writer.write('=');
                writer.write('"');
                writeEncoded(writer, value);
                writer.write('"');
            }
        }
        if ((contents != null) && (contents.length() > 0)) {
            writer.write('>');
            writeEncoded(writer, contents);
            writer.write('<');
            writer.write('/');
            writer.write(name);
            writer.write('>');
        } else if (children.isEmpty()) {
            writer.write('/');
            writer.write('>');
        } else {
            writer.write('>');
            Iterator<XMLElement> iter = iterateChildren();
            while (iter.hasNext()) {
                XMLElement child = iter.next();
                child.write(writer);
            }
            writer.write('<');
            writer.write('/');
            writer.write(name);
            writer.write('>');
        }
    }

    /**
     * Writes a string encoded to a writer.
     *
     * @param writer
     *            The writer to write the XML data to.
     * @param str
     *            The string to write encoded.
     *            </dl>
     *            <dl>
     *            <dt><b>Preconditions:</b></dt>
     *            <dd>
     *            <ul>
     *            <li><code>writer != null</code>
     *            <li><code>writer</code> is not closed
     *            <li><code>str != null</code>
     *            </ul>
     *            </dd>
     *            </dl>
     */
    private void writeEncoded(Writer writer, String str) throws IOException {
        for (int i = 0; i < str.length(); i += 1) {
            char ch = str.charAt(i);
            switch (ch) {
                case '<':
                    writer.write('&');
                    writer.write('l');
                    writer.write('t');
                    writer.write(';');
                    break;
                case '>':
                    writer.write('&');
                    writer.write('g');
                    writer.write('t');
                    writer.write(';');
                    break;
                case '&':
                    writer.write('&');
                    writer.write('a');
                    writer.write('m');
                    writer.write('p');
                    writer.write(';');
                    break;
                case '"':
                    writer.write('&');
                    writer.write('q');
                    writer.write('u');
                    writer.write('o');
                    writer.write('t');
                    writer.write(';');
                    break;
                case '\'':
                    writer.write('&');
                    writer.write('a');
                    writer.write('p');
                    writer.write('o');
                    writer.write('s');
                    writer.write(';');
                    break;
                default:
                    int unicode = ch;
                    if ((unicode < 32) || (unicode > 126)) {
                        writer.write('&');
                        writer.write('#');
                        writer.write('x');
                        writer.write(Integer.toString(unicode, 16));
                        writer.write(';');
                    } else {
                        writer.write(ch);
                    }
            }
        }
    }

}
