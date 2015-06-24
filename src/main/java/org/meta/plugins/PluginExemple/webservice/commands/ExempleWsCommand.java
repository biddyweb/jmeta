package org.meta.plugins.PluginExemple.webservice.commands;

import java.util.ArrayList;
import java.util.Iterator;
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

public class ExempleWsCommand extends AbstractWebService {

    private InterfaceDescriptor descriptor     = null;
    private TextOutput          output         = null;
    private int                 nbRefresh      = 0;

    public ExempleWsCommand(){
        // Describe a full interface for test
        ColumnOrganizer column         = new ColumnOrganizer("column1");
        column.addChild(new DateInput("birthDate", "Birth Date"));
        LineOrganizer     line         = new LineOrganizer("ligne1");
        column.addChild(line);
        line.addChild(new TextInput("firstName", "First Name"));
        line.addChild(new TextInput("lastName", "Last Name"));
        
        ColumnOrganizer right = new ColumnOrganizer("right");
        line.addChild(right);
        right.addChild(new DateInput("dateInscription", "Inscription Date"));
        right.addChild(new DateInput("dateInscription1", "Inscription Date"));
        right.addChild(new DateInput("dateInscription2", "Inscription Date"));

        ArrayList<Select> buttons = new ArrayList<Select>();
        buttons.add(new Select("option1", "option 1"));
        buttons.add(new Select("option2", "option 2"));
        buttons.add(new Select("option3", "option 3"));
        column.addChild(new SelectList("select", "select label", buttons));

        ArrayList<RadioButton> radio = new ArrayList<RadioButton>();
        radio.add(new RadioButton("radio1", "radio 1"));
        radio.add(new RadioButton("radio2", "radio 2"));
        radio.add(new RadioButton("radio3", "radio 3"));
        column.addChild(new RadioList("RadioList", "Radio list label", radio));

        ArrayList<CheckBox> check = new ArrayList<CheckBox>();
        check.add(new CheckBox("check1", "check 1"));
        check.add(new CheckBox("check2", "check 2"));
        check.add(new CheckBox("check3", "check 3"));
        column.addChild(new CheckBoxLists("checklist", "CheckList label", check));

        output = new TextOutput("output", "Sortie");

        column.addChild(output);
        descriptor = new InterfaceDescriptor(column);
    }


    @Override
    public InterfaceDescriptor execute(Map<String, String[]> map) {
        output.flush();
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
