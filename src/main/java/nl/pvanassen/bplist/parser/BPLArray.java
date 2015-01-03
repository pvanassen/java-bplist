package nl.pvanassen.bplist.parser;

import java.util.*;

/**
 * Holder for a binary PList array element.
 */
public class BPLArray implements BPListElement<List<BPListElement<?>>> {

    private final List<BPListElement<?>> objectTable;
    private final int[] objref;

    BPLArray(List<BPListElement<?>> objectTable, int[] objref) {
        super();
        this.objectTable = objectTable;
        this.objref = objref;
    }

    public BPListElement<?> getValue(int i) {
        return objectTable.get(objref[i]);
    }

    public int[] getObjref() {
        return objref;
    }
    
    @Override
    public BPListType getType() {
        return BPListType.ARRAY;
    }
    
    @Override
    public List<BPListElement<?>> getValue() {
        List<BPListElement<?>> array = new ArrayList<BPListElement<?>>(objref.length);
        for (int objr : objref) {
            array.add(objectTable.get(objr));
        }
        return array;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("Array{");
        for (int i = 0; i < objref.length; i++) {
            if (i > 0) {
                buf.append(',');
            }
            if ((objectTable.size() > objref[i]) && (objectTable.get(objref[i]) != this)) {
                buf.append(objectTable.get(objref[i]));
            } else {
                buf.append("*" + objref[i]);
            }
        }
        buf.append('}');
        return buf.toString();
    }
}