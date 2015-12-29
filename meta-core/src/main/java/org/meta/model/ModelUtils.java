package org.meta.model;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.meta.api.model.Data;
import org.meta.api.model.MetaData;

public class ModelUtils {
    
    /**
     * Check if a data match with some filters.
     * 
     * @param data
     * @param metaDataFilters the key is a MetaData name, the value is the one to match
     *        with the corresponding data's metadata value.
     *        By default, an inexistent filter is ignored. TODO make it optional
     *        TODO place operands
     * @return
     */
    public static boolean matchDataMetaData(Data data, Map<String, String> metaDataFilters){
        boolean isValid = true;
        Iterator<Entry<String, String>> ifESet = metaDataFilters.entrySet().iterator();
        while(isValid && ifESet.hasNext()){
            Entry<String, String> fESet = ifESet.next();
            MetaData a = data.getMetaData(fESet.getKey());
            if(a != null){
                isValid = a.getValue().equals(fESet.getValue());
            }
        }
        return isValid;
    }

}
