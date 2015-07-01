package org.meta.plugin.webservice.forms.organizers;

import org.meta.plugin.webservice.forms.InterfaceOrganizer;

/**
 * A line organizer is an organize that put his children side by side
 * in the interface.
 * @author faquin
 *
 */
public class LineOrganizer extends InterfaceOrganizer {

    /**
     * 
     * @param id Unique ID
     */
    public LineOrganizer(String id) {
        super(id);
    }

    @Override
    protected String getType() {
        return "Line";
    }

}
