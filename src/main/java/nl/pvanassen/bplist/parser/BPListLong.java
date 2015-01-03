package nl.pvanassen.bplist.parser;

public class BPListLong implements BPListElement<Long> {
    private final long value;
    
    public BPListLong(long value) {
        this.value = value;
    }
    @Override
    public BPListType getType() {
        return BPListType.LONG;
    }
    
    @Override
    public Long getValue() {
        return value;
    }
}
