package nl.pvanassen.bplist.parser;

public class BPListBoolean implements BPListElement<Boolean> {
    private final Boolean value;

    public static BPListBoolean TRUE = new BPListBoolean(Boolean.TRUE);
    public static BPListBoolean FALSE = new BPListBoolean(Boolean.FALSE);
    
    BPListBoolean(Boolean value) {
        this.value = value;
    }

    public BPListType getType() {
        return BPListType.BOOLEAN;
    }

    public Boolean getValue() {
        return value;
    }
}
