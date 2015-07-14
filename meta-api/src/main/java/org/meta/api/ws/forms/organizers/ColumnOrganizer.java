package org.meta.api.ws.forms.organizers;

/**
 * A column organizer is an organizer that put his children one below the other
 * in the interface
 */
import org.meta.api.ws.forms.InterfaceOrganizer;

/**
 *
 * @author faquin
 *
 */
public class ColumnOrganizer extends InterfaceOrganizer {

    /**
     * 
     * @param id Unique ID
     */
    public ColumnOrganizer(String id) {
        super(id);
    }

    @Override
    protected String getType() {
        return "Column";
    }

}
