package org.meta.plugin.TCP.AMP;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.meta.modele.DataFile;
import org.meta.modele.DataString;
import org.meta.modele.MetaData;
import org.meta.modele.Search;
import org.meta.modele.Searchable;
import org.meta.plugin.TCP.AMP.exception.NotAValidAMPCommand;
import org.meta.plugin.TCP.AMP.exception.NotAValidAmpAnswerCommand;

public class AMPAnswerParser extends AMPParser{

	//Do not initialize those variables, because it's made by the mumy
	//in her constructor ;) via the implement method "useContent"
	private String 					answer		;
	private ArrayList<Searchable>	datas 		;
	
	public AMPAnswerParser(byte[] bs) throws NotAValidAMPCommand {
		super(bs);
	}

	@Override
	protected void useContent(LinkedHashMap<String, byte[]> content) throws NotAValidAmpAnswerCommand {
		answer = new String(content.get("_answer"));
		content.remove("_answer");
		datas = new ArrayList<Searchable>();
		extractDatas(content);
	}
	
	/**
	 * Extract {@link Searchable} from {@link LinkedHashMap}s
	 * @param content
	 * @throws NotAValidAmpAnswerCommand 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private void extractDatas(LinkedHashMap<String, byte[]> content) throws NotAValidAmpAnswerCommand{
		//get the count of datas
		int nbDatas = Integer.parseInt(new String(content.get("_nbDatas")));
		//and remove it
		content.remove("_nbDatas");
		
		//Now, for each datas
		for(int i=0; i<nbDatas; i++){
			//re-create the fragment
			LinkedHashMap<String, byte[]> fragment = new LinkedHashMap<>();
			//extract the next type
			String type = new String(content.get("_"+i+"_type"));
			//add it in the fragment
			fragment.put("_type", content.get("_"+i+"_type"));
			//remove it from the content.
			content.remove("_"+i+"_type");
			
			boolean 			nextTypeFound 	= false;
			Iterator<String> 	j 				= content.keySet().iterator();
			
			//Until we find the next type, it means we are in the same object
			//So for each element in content
			while (j.hasNext() && !nextTypeFound) {
				//get key and value
				String key = j.next();
				byte[] value = content.get(key);
				//if it's a type stop the loop
				if(key.contains("_type")){
					nextTypeFound = true;
				}else{
					//otherwise, remove the key from content
					content.put(key, null);
					//and add the fragment
					fragment.put(key.replaceFirst("_"+i, ""), value);
				}
			}
			
			//Now that we have a rebuild fragment
			//We can invoke the object by his type
			//and feed him with this fragment.
			Class clazz;
			try {
				clazz = Class.forName(new String(fragment.get("_type")));
				Searchable searchable 		= (Searchable) clazz.newInstance();
				
				//security check. In case of someone wo'll try to execute somthing
				//on an other object than on of the model.
				if(	searchable instanceof Search   || 
					searchable instanceof MetaData ||
					searchable instanceof DataFile ||
					searchable instanceof DataString
				){
					searchable.unParseFromAmpFragment(fragment);
					datas.add(searchable);
				}
			} catch (ClassNotFoundException | 
					InstantiationException | 
					IllegalAccessException e) 
			{
				throw new NotAValidAmpAnswerCommand(type);
			}

		}
	}

	/**
	 * 
	 * @return The answer code
	 */
	public String getAnswer() {
		return answer;
	}

	/**
	 * 
	 * @return return the list of extracted datas
	 */
	public ArrayList<Searchable> getDatas() {
		return datas;
	}
}
