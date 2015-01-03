package nl.pvanassen.bplist.parser;

public class BPListData implements BPListElement<byte[]> {
    private final byte[] value;
    
    public BPListData(byte[] value) {
        this.value = value;
    }
    @Override
    public BPListType getType() {
        return BPListType.DATA;
    }
    
    @Override
    public byte[] getValue() {
        return value;
    }
}
