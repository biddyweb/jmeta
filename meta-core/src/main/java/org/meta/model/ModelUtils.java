package org.meta.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.meta.api.model.Data;
import org.meta.api.model.MetaData;

/**
 * <p>ModelUtils class.</p>
 *
 * @author nico
 * @version $Id: $
 */
public class ModelUtils {

    /**
     * Check if a data match with some filters.
     *
     * @param data the Data to check
     * @param metaDataFilters the key is a MetaData name, the value is the one to match with the corresponding
     * data's meta-data value. By default, an inexistent filter is ignored. TODO make it optional TODO place
     * operands
     * @return true if the given Data' meta-data match the filters, false otherwise
     */
    public static boolean matchDataMetaData(final Data data, final Map<String, String> metaDataFilters) {
        boolean isValid = true;
        if (metaDataFilters == null) {
            return isValid;
        }
        Iterator<Entry<String, String>> ifESet = metaDataFilters.entrySet().iterator();
        while (isValid && ifESet.hasNext()) {
            Entry<String, String> fESet = ifESet.next();
            MetaData a = data.getMetaData(fESet.getKey());
            if (a != null) {
                isValid = a.getValue().equals(fESet.getValue());
            }
        }
        return isValid;
    }

}
