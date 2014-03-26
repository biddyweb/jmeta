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
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("_answer", answer);
		map.put("_nbDatas", datas.size()+"");
		
		for (Iterator<Searchable> i = datas.iterator(); i.hasNext();) {
			Searchable searchable = (Searchable) i.next();
			HashMap<String, String> fragment = searchable.getAmpAnswerPart();
			map.putAll(fragment);
		}
		
		super.build(map);
	}
}
