package org.meta.plugin.webservice.forms;

import java.util.ArrayList;

import org.bson.BasicBSONObject;
import org.bson.types.BasicBSONList;

/**
 *
 * @author faquin
 *
 */
public abstract class InterfaceOrganizer extends InterfaceElement{

    private ArrayList<InterfaceElement> childs = null;

    public InterfaceOrganizer(String id) {
        super(id);
        childs = new ArrayList<InterfaceElement>();
    }

    public BasicBSONObject toJson() {
        BasicBSONObject organiser     = super.toJson();
        BasicBSONList   oChilds     = new BasicBSONList();
        for(int i=0; i<childs.size(); i++){
            oChilds.put(i, childs.get(i).toJson());
        }
        organiser.append("content", oChilds);
        return organiser;
    }

    protected abstract String getType();

    public void addChild(InterfaceElement child){
        childs.add(child);
    }
}
