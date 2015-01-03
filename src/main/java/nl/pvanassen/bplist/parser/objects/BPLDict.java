package nl.pvanassen.bplist.parser.objects;

import java.util.List;

/**
 * Holder for a binary PList dict element.
 */
public class BPLDict {

    private final List<Object> objectTable;
    private final int[] keyref;
    private final int[] objref;

    public BPLDict(List<Object> objectTable, int[] keyref, int[] objref) {
        super();
        this.objectTable = objectTable;
        this.keyref = keyref;
        this.objref = objref;
    }

    public String getKey(int i) {
        return objectTable.get(keyref[i]).toString();
    }

    public Object getValue(int i) {
        return objectTable.get(objref[i]);
    }

    public int[] getKeyref() {
        return keyref;
    }

    public int[] getObjref() {
        return objref;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("BPLDict{");
        for (int i = 0; i < keyref.length; i++) {
            if (i > 0) {
                buf.append(',');
            }
            if ((keyref[i] < 0) || (keyref[i] >= objectTable.size())) {
                buf.append("#" + keyref[i]);
            } else if (objectTable.get(keyref[i]) == this) {
                buf.append("*" + keyref[i]);
            } else {
                buf.append(objectTable.get(keyref[i]));
                // buf.append(keyref[i]);
            }
            buf.append(":");
            if ((objref[i] < 0) || (objref[i] >= objectTable.size())) {
                buf.append("#" + objref[i]);
            } else if (objectTable.get(objref[i]) == this) {
                buf.append("*" + objref[i]);
            } else {
                buf.append(objectTable.get(objref[i]));
                // buf.append(objref[i]);
            }
        }
        buf.append('}');
        return buf.toString();
    }
}