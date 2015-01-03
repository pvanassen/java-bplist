package nl.pvanassen.bplist.parser;

public class BPListFloat implements BPListElement<Float> {
    private final float value;
    
    public BPListFloat(float value) {
        this.value = value;
    }
    @Override
    public BPListType getType() {
        return BPListType.FLOAT;
    }
    
    @Override
    public Float getValue() {
        return value;
    }
}
