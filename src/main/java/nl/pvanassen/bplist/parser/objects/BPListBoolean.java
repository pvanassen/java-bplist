package nl.pvanassen.bplist.parser.objects;

public class BPListBoolean implements BPListElement<Boolean> {
    private final Boolean value;

    public BPListBoolean(Boolean value) {
	this.value = value;
    }

    public BPListType getType() {
	return BPListType.BOOLEAN;
    }

    public Boolean getValue() {
	return value;
    }
}
