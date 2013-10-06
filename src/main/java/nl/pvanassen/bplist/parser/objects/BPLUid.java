package nl.pvanassen.bplist.parser.objects;

/** Holder for a binary PList Uid element. */
public class BPLUid {

    private final int number;

    public BPLUid(int number) {
        super();
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}