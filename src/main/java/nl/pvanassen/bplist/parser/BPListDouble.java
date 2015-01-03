package nl.pvanassen.bplist.parser;

public class BPListDouble implements BPListElement<Double> {
    private final double value;
    
    public BPListDouble(double value) {
        this.value = value;
    }
    @Override
    public BPListType getType() {
        return BPListType.FLOAT;
    }
    
    @Override
    public Double getValue() {
        return value;
    }
}
