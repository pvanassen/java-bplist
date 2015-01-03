package nl.pvanassen.bplist.parser.objects;

import java.util.List;

/**
 * Holder for a binary PList array element.
 */
public class BPLArray {

    private final List<Object> objectTable;
    private final int[] objref;

    public BPLArray(List<Object> objectTable, int[] objref) {
        super();
        this.objectTable = objectTable;
        this.objref = objref;
    }

    public Object getValue(int i) {
        return objectTable.get(objref[i]);
    }

    public int[] getObjref() {
        int[] returnValue = new int[objref.length];
        System.arraycopy(objref, 0, returnValue, 0, objref.length);
        return returnValue;
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