package nl.pvanassen.bplist.parser;

public interface BPListElement<T> {
    BPListType getType();

    T getValue();
}
