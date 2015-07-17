package org.meta.api.amp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.meta.api.model.Searchable;

/**
 * Create a new AmpAnswerFactory to create an answer
 * @author faquin
 *
 */
public class AMPAnswerBuilder extends AMPBuilder {

    /**
     *
     * @param answer the same number as the ask
     * @param datas the Searchable objects to send back
     */
    public AMPAnswerBuilder(String answer, ArrayList<Searchable> datas){
        //write the answer number and the count of datas send
        LinkedHashMap<String, byte[]> map = new LinkedHashMap<String, byte[]>();
        map.put("_answer", answer.getBytes());
        map.put("_nbDatas", Integer.toString(datas.size()).getBytes());

        int nb = 0;
        //Foreach Searchable data, get the AMPMessagePart
        for (Iterator<Searchable> i = datas.iterator(); i.hasNext();nb++) {
            Searchable searchable = (Searchable) i.next();
            LinkedHashMap<String, byte[]> fragment = searchable.getAmpAnswerPart();
            for (Iterator<String> j = fragment.keySet().iterator(); j.hasNext();) {
                String key = (String) j.next();
                map.put("_"+nb+key, fragment.get(key));
            }
        }
        //finally build the map
        super.build(map);
    }
}
