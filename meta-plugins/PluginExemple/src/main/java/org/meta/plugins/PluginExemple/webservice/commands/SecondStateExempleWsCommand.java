/*
 *
 * JMeta - Meta's java implementation
 *
 * Copyright (C) 2013-2015 Pablo Joubert
 * Copyright (C) 2013-2015 Thomas Lavocat
 * Copyright (C) 2013-2015 Nicolas Michon
 *
 * This file is part of JMeta.
 *
 * JMeta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JMeta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.meta.plugins.PluginExemple.webservice.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.meta.api.ws.AbstractPluginWebServiceController;
import org.meta.api.ws.AbstractWebService;
import org.meta.api.ws.forms.fields.DateInput;
import org.meta.api.ws.forms.fields.TextInput;
import org.meta.api.ws.forms.fields.TextOutput;
import org.meta.api.ws.forms.fields.checkbox.CheckBox;
import org.meta.api.ws.forms.fields.checkbox.CheckBoxLists;
import org.meta.api.ws.forms.fields.radio.RadioButton;
import org.meta.api.ws.forms.fields.radio.RadioList;
import org.meta.api.ws.forms.fields.select.Select;
import org.meta.api.ws.forms.fields.select.SelectList;
import org.meta.api.ws.forms.organizers.ColumnOrganizer;
import org.meta.api.ws.forms.organizers.LineOrganizer;
import org.meta.api.ws.forms.submit.SelfSubmitButton;
import org.meta.api.ws.forms.submit.SubmitToButton;

/**
 *
 * @author nico
 */
public class SecondStateExempleWsCommand extends AbstractWebService {

    private TextOutput output = null;
    private int nbRefresh = 0;
    private TextInput firstName;
    private TextInput lastName;
    private DateInput dateInscription;
    private DateInput dateInscription1;
    private DateInput dateInscription2;
    private Select option1;
    private Select option2;
    private Select option3;
    private RadioButton radio1;
    private RadioButton radio2;
    private RadioButton radio3;
    private CheckBox check1;
    private CheckBox check2;
    private CheckBox check3;
    private SelectList select;
    private RadioList radioList;
    private CheckBoxLists checklist;
    private DateInput birthDate;

    /**
     *
     * @param controler
     */
    public SecondStateExempleWsCommand(AbstractPluginWebServiceController controler) {
        super(controler);
        // Describe a full interface for test
        birthDate = new DateInput("birthDate", "Birth Date");
        rootColumn.addChild(birthDate);
        LineOrganizer line = new LineOrganizer("ligne1");
        rootColumn.addChild(line);
        firstName = new TextInput("firstName", "First Name");
        line.addChild(firstName);
        lastName = new TextInput("lastName", "Last Name");
        line.addChild(lastName);

        ColumnOrganizer right = new ColumnOrganizer("right");
        line.addChild(right);
        dateInscription = new DateInput("dateInscription", "Inscription Date");
        right.addChild(dateInscription);
        dateInscription1 = new DateInput("dateInscription1", "Inscription Date");
        right.addChild(dateInscription1);
        dateInscription2 = new DateInput("dateInscription2", "Inscription Date");
        right.addChild(dateInscription2);

        ArrayList<Select> buttons = new ArrayList<Select>();
        option1 = new Select("option1", "option 1");
        buttons.add(option1);
        option2 = new Select("option2", "option 2");
        buttons.add(option2);
        option3 = new Select("option3", "option 3");
        buttons.add(option3);
        select = new SelectList("select", "select label", buttons);
        rootColumn.addChild(select);

        ArrayList<RadioButton> radio = new ArrayList<RadioButton>();
        radio1 = new RadioButton("radio1", "radio 1");
        radio.add(radio1);
        radio2 = new RadioButton("radio2", "radio 2");
        radio.add(radio2);
        radio3 = new RadioButton("radio3", "radio 3");
        radio.add(radio3);
        radioList = new RadioList("RadioList", "Radio list label", radio);
        rootColumn.addChild(radioList);

        ArrayList<CheckBox> check = new ArrayList<CheckBox>();
        check1 = new CheckBox("check1", "check 1");
        check.add(check1);
        check2 = new CheckBox("check2", "check 2");
        check.add(check2);
        check3 = new CheckBox("check3", "check 3");
        check.add(check3);
        checklist = new CheckBoxLists("checklist", "CheckList label", check);
        rootColumn.addChild(checklist);

        rootColumn.addChild(new SelfSubmitButton("submit", "submit form to me"));
        rootColumn.addChild(new SubmitToButton("submitTo", "submit to an oth", "example"));
        output = new TextOutput("output", "Sortie");
        output.append("message");

        rootColumn.addChild(output);
    }

    @Override
    public void executeCommand(Map<String, String[]> map) {
        output.flush();
        birthDate.setValue(getParameter(birthDate.getId(), map));
        firstName.setValue(getParameter(firstName.getId(), map));
        lastName.setValue(getParameter(lastName.getId(), map));
        dateInscription.setValue(getParameter(dateInscription.getId(), map));
        dateInscription1.setValue(getParameter(dateInscription1.getId(), map));
        dateInscription2.setValue(getParameter(dateInscription2.getId(), map));
        if (getParameters(checklist.getId(), map) != null) {
            List<String> checked = Arrays.asList(getParameters(checklist.getId(), map));

            check1.setChecked(checked.contains(check1.getId()));
            check2.setChecked(checked.contains(check2.getId()));
            check3.setChecked(checked.contains(check3.getId()));
        }
        if (getParameters(radioList.getId(), map) != null) {
            List<String> selected = Arrays.asList(getParameters(radioList.getId(), map));
            radio1.setSelected(selected.contains(radio1.getId()));
            radio2.setSelected(selected.contains(radio2.getId()));
            radio3.setSelected(selected.contains(radio3.getId()));
        }
        if (getParameters(select.getId(), map) != null) {
            List<String> selected = Arrays.asList(getParameters(select.getId(), map));
            option1.setSelected(selected.contains(option1.getId()));
            option2.setSelected(selected.contains(option2.getId()));
            option3.setSelected(selected.contains(option3.getId()));
        }
        output.append("Your sended parameters are :");
        for (Iterator<String> i = map.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            output.append(" - " + key + " : " + map.get(key));
        }
    }

    @Override
    public void applySmallUpdate() {
        nbRefresh++;
        output.append("refresh number" + nbRefresh);
    }
}
