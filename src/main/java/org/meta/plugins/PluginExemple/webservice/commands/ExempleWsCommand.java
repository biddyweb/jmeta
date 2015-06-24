package org.meta.plugins.PluginExemple.webservice.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bson.BasicBSONObject;
import org.meta.model.Searchable;
import org.meta.plugin.webservice.AbstractWebService;
import org.meta.plugin.webservice.forms.InterfaceDescriptor;
import org.meta.plugin.webservice.forms.fields.DateInput;
import org.meta.plugin.webservice.forms.fields.TextInput;
import org.meta.plugin.webservice.forms.fields.TextOutput;
import org.meta.plugin.webservice.forms.fields.checkbox.CheckBox;
import org.meta.plugin.webservice.forms.fields.checkbox.CheckBoxLists;
import org.meta.plugin.webservice.forms.fields.radio.RadioButton;
import org.meta.plugin.webservice.forms.fields.radio.RadioList;
import org.meta.plugin.webservice.forms.fields.select.Select;
import org.meta.plugin.webservice.forms.fields.select.SelectList;
import org.meta.plugin.webservice.forms.organizers.ColumnOrganizer;
import org.meta.plugin.webservice.forms.organizers.LineOrganizer;
import org.meta.plugin.webservice.forms.submit.SelfSubmitButton;
import org.meta.plugin.webservice.forms.submit.SubmitToButton;

public class ExempleWsCommand extends AbstractWebService {

    private InterfaceDescriptor descriptor     = null;
    private TextOutput          output         = null;
    private int                 nbRefresh      = 0;
	private DateInput birthDate;

    public ExempleWsCommand(){
        // Describe a full interface for test
        ColumnOrganizer column         = new ColumnOrganizer("column1");
        birthDate = new DateInput("birthDate", "Birth Date");
        column.addChild(birthDate);
        column.addChild(new SelfSubmitButton("submit", "submit form to me"));
        column.addChild(new SubmitToButton("submitTo", "submit to an oth", "secondExample"));
        output = new TextOutput("output", "Sortie");
        output.append("message");

        column.addChild(output);
        descriptor = new InterfaceDescriptor(column);
    }


    @Override
    public InterfaceDescriptor execute(Map<String, String[]> map) {
        output.flush();
        birthDate.setValue(getParameter(birthDate.getId(), map));
                output.append("Your sended parameters are :");
        for (Iterator<String> i = map.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            output.append(" - "+key+" : "+map.get(key));
        }
        return descriptor;
    }

    @Override
    public InterfaceDescriptor retrieveUpdate() {
        nbRefresh++;
        output.append("refresh number"+nbRefresh);
        return descriptor;
    }

    @Override
    public void callback(ArrayList<Searchable> results) {
        // TODO Auto-generated method stub

    }

    @Override
    public InterfaceDescriptor getInterface(Map<String, String[]> map) {
        return descriptor;
    }

    @Override
    public BasicBSONObject getNextStep() {
        // TODO Auto-generated method stub
        return null;
    }
}
