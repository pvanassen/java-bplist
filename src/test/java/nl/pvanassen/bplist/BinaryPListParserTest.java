package nl.pvanassen.bplist;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Scanner;

import nl.pvanassen.bplist.converter.ConvertToXml;
import nl.pvanassen.bplist.ext.nanoxml.XMLElement;
import nl.pvanassen.bplist.parser.*;

import org.junit.*;
import static org.junit.Assert.*;

public class BinaryPListParserTest {
    private final ConvertToXml convetToXml = new ConvertToXml();
    private final ElementParser elementParser = new ElementParser();

    private void test(String baseName) throws IOException {
        List<BPListElement<?>> elements = elementParser.parseObjectTable(FileHelper.getFile(baseName + ".bplist"));
        XMLElement xmlElement = convetToXml.convertToXml(elements);
        assertNotNull(xmlElement);
        assertEquals(FileHelper.getContent(baseName + ".result"), xmlElement.getChildren().get(0).toString());
    }
    
    private void testPrettyPrint(String baseName) throws IOException {
        List<BPListElement<?>> elements = elementParser.parseObjectTable(FileHelper.getFile(baseName + ".bplist"));
        XMLElement xmlElement = convetToXml.convertToXml(elements);
        assertNotNull(xmlElement);
        ByteArrayOutputStream outputBufferBOS = new ByteArrayOutputStream();
        PrintWriter outputBufferPW = new PrintWriter(outputBufferBOS);
        convetToXml.dig(xmlElement);
        outputBufferPW.close();
        String output = "";
        Scanner outputBufferScanner = new Scanner(new ByteArrayInputStream(outputBufferBOS.toByteArray()));
        while(outputBufferScanner.hasNextLine()){
            output += outputBufferScanner.nextLine() + "\n";
        }
        assertEquals(FileHelper.getContent(baseName + ".resultPrettyPrint"), output);
    }
    
    @Test
    public void testAirplay() throws IOException {
        test("airplay");
    }
    
    @Test
    public void testITunesSmall() throws IOException {
        test("iTunes-small");
    }

    @Test
    public void testSample1() throws IOException {
        test("sample1");
    }
    
    @Test
    public void testSample2() throws IOException {
        test("sample2");
    }
    @Test
    public void testUID() throws IOException {
        test("uid");
    }
    @Test
    public void testUTF16() throws IOException {
        test("utf16");
    }
    @Test
    public void testAirplayPrettyPrint() throws IOException {
        testPrettyPrint("airplay");
    }
}
