package nl.pvanassen.bplist.parser;

import java.io.UnsupportedEncodingException;

public class BPListString implements BPListElement<String> {
    private final String value;
    
    public BPListString(char[] buf) {
        this.value = new String(buf);
    }
    
    public BPListString(byte[] buf) throws UnsupportedEncodingException {
        this.value = new String(buf, "ASCII");
    }
    @Override
    public BPListType getType() {
        return BPListType.STRING;
    }
    
    @Override
    public String getValue() {
        return value;
    }
}
