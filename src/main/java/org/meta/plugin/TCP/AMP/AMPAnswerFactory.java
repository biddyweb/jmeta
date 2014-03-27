package org.meta.plugin.TCP.AMP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.meta.modele.Searchable;

/**
 * Create a new AmpAnswerFactory to create an answer
 * @author faquin
 *
 */
public class AMPAnswerFactory extends AMPFactory {

	/**
	 * 
	 * @param answer the same number as the ask
	 * @param datas the Searchable objects to send back
	 */
	public AMPAnswerFactory(String answer, ArrayList<Searchable> datas){
		//write the answer number and the count of datas send
		HashMap<String, byte[]> map = new HashMap<String, byte[]>();
		map.put("_answer", answer.getBytes());
		map.put("_nbDatas", Integer.toString(datas.size()).getBytes());
		
		//Foreach Searchable data, get the AMPMessagePart
		for (Iterator<Searchable> i = datas.iterator(); i.hasNext();) {
			Searchable searchable = (Searchable) i.next();
			HashMap<String, byte[]> fragment = searchable.getAmpAnswerPart();
			map.putAll(fragment);
		}
		//finally build the map
		super.build(map);
	}
}
