package org.meta.api.ws.forms;

import java.util.ArrayList;
import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;

/**
 *  Is the super class of all organizer.
 *  Has a list of children
 *  
 *  Is able to ask his children to serialize as JSON
 *  
 *  @author faquin
 *
 */
public abstract class InterfaceOrganizer extends InterfaceElement{

    private ArrayList<InterfaceElement> childs = null;

    /**
     * 
     * @param id    Id of the column, better if unique.
     */
    public InterfaceOrganizer(String id) {
        super(id);
        childs = new ArrayList<InterfaceElement>();
    }

    /**
     * Return this as JSON, containing his children
     * @return 
     */
    public BasicBSONObject toJson() {
        //get parent json, and add it all children's json
        BasicBSONObject organiser     = super.toJson();
        BasicBSONList   oChilds     = new BasicBSONList();
        for(int i=0; i<childs.size(); i++){
            oChilds.put(i, childs.get(i).toJson());
        }
        organiser.append("content", oChilds);
        return organiser;
    }

    /**
     * @return Interface type
     */
    protected abstract String getType();

    /**
     * Add a child to the interface
     * @param child
     */
    public void addChild(InterfaceElement child){
        childs.add(child);
    }
    
    /**
     * Does what arrayList.remove does
     * @param child a child element to remove
     * @return true if the element was found in the list
     */
    public boolean removeChild(InterfaceElement child){
        return childs.remove(child);
    }
}
