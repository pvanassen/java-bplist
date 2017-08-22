package nl.pvanassen.bplist;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.List;

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

    private void testInputStreamMode(String baseName) throws IOException {
        //List<BPListElement<?>> elements = elementParser.parseObjectTable(new FileInputStream(FileHelper.getFile(baseName + ".bplist")));
        byte[] copyBuffer = new Byte[1024];
        int copySize = 0;
        FileInputStream testFile = new FileInputStream(FileHelper.getFile(baseName + ".bplist"));
        ByteArrayOutputStream memory = new ByteArrayOutputStream();
        while((copySize = testFile.read(copyBuffer, 0, 1024)) != 0){
            memory.write(copyBuffer, 0, copySize);
        }
        testFile.close();
        ByteArrayInputStream memoryStream = new ByteArrayInputStream(memory.toByteArray());
        XMLElement xmlElement = convetToXml.convertToXml(memoryStream);
        assertNotNull(xmlElement);
        assertEquals(FileHelper.getContent(baseName + ".result"), xmlElement.getChildren().get(0).toString());
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
    public void testInputStreamModeAirPlay() throws IOException {
        testInputStream("airplay");
    }
    
    @Test
    public void testInputStreamModeITunesSmall() throws IOException {
        testInputStreamMode("iTunes-small");
    }

    @Test
    public void testInputStreamModeSample1() throws IOException {
        testInputStreamMode("sample1");
    }
    
    @Test
    public void testInputStreamModeSample2() throws IOException {
        testInputStreamMode("sample2");
    }
    @Test
    public void testInputStreamModeUID() throws IOException {
        testInputStreamMode("uid");
    }
    @Test
    public void testInputStreamModeUTF16() throws IOException {
        testInputStreamMode("utf16");
    }
}
