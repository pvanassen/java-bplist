package nl.pvanassen.bplist.parser;

public class BPListNull implements BPListElement<Object> {
    public BPListType getType() {
        return BPListType.NULL;
    }

    public Object getValue() {
        return null;
    }
}
