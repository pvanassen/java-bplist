package nl.pvanassen.bplist.parser;

/** Holder for a binary PList Uid element. */
public class BPLUid implements BPListElement<Integer> {

    private final int number;

    public BPLUid(int number) {
        super();
        this.number = number;
    }
    @Override
    public BPListType getType() {
        return BPListType.UID;
    }
    
    @Override
    public Integer getValue() {
        return number;
    }

    public int getNumber() {
        return number;
    }
}