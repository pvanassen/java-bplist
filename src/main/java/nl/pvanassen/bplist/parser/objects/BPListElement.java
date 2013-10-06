package nl.pvanassen.bplist.parser.objects;

public interface BPListElement<T> {
    BPListType getType();
    T getValue();
}
