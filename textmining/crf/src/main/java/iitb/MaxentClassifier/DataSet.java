/**
 * DataSet.java
 *
 * @author Sunita Sarawagi
 * @since 1.0
 * @version 1.3
 */
package iitb.MaxentClassifier;

import iitb.CRF.DataIter;
import iitb.CRF.DataSequence;

import java.util.List;

public class DataSet implements DataIter {

    List allRecords;
    int currPos = 0;

    public DataSet(List recs) {
        allRecords = recs;
    }

    @Override
    public void startScan() {
        currPos = 0;
    }

    @Override
    public boolean hasNext() {
        return (currPos < allRecords.size());
    }

    @Override
    public DataSequence next() {
        currPos++;
        return (DataRecord) allRecords.get(currPos - 1);
    }
};
