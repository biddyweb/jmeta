package org.meta.plugin.webservice.forms.organizers;

import org.meta.plugin.webservice.forms.InterfaceOrganizer;

/**
 *
 * @author faquin
 *
 */
public class LineOrganizer extends InterfaceOrganizer {

    public LineOrganizer(String id) {
        super(id);
    }

    @Override
    protected String getType() {
        return "Line";
    }

}
