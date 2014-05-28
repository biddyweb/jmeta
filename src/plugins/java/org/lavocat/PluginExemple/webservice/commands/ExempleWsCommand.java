package org.lavocat.PluginExemple.webservice.commands;

import java.util.ArrayList;
import java.util.Map;

import org.meta.plugin.webservice.AbstractWebService;
import org.meta.plugin.webservice.forms.InterfaceDescriptor;
import org.meta.plugin.webservice.forms.fields.DateInput;
import org.meta.plugin.webservice.forms.fields.TextInput;
import org.meta.plugin.webservice.forms.fields.checkbox.CheckBox;
import org.meta.plugin.webservice.forms.fields.checkbox.CheckBoxLists;
import org.meta.plugin.webservice.forms.fields.radio.RadioButton;
import org.meta.plugin.webservice.forms.fields.radio.RadioList;
import org.meta.plugin.webservice.forms.fields.select.Select;
import org.meta.plugin.webservice.forms.fields.select.SelectList;
import org.meta.plugin.webservice.forms.organizers.ColumnOrganizer;
import org.meta.plugin.webservice.forms.organizers.LineOrganizer;

public class ExempleWsCommand extends AbstractWebService {

	@Override
	public InterfaceDescriptor getInterface() {
		// Describe a full interface for test		
		ColumnOrganizer column 		= new ColumnOrganizer("column1");
		column.addChild(new DateInput("birthDate", "Birth Date"));
		LineOrganizer 	line	 	= new LineOrganizer("ligne1");
		column.addChild(line);
		line.addChild(new TextInput("firstName", "First Name"));
		line.addChild(new TextInput("lastName", "Last Name"));
		
		ArrayList<Select> buttons = new ArrayList<Select>();
		buttons.add(new Select("option1", "option1"));
		buttons.add(new Select("option2", "option2"));
		buttons.add(new Select("option3", "option3"));
		column.addChild(new SelectList("list", buttons));
		
		ArrayList<RadioButton> radio = new ArrayList<RadioButton>();
		buttons.add(new Select("radio1", "radio1"));
		buttons.add(new Select("radio2", "radio2"));
		buttons.add(new Select("radio3", "radio3"));
		column.addChild(new RadioList("list", radio));
		
		ArrayList<CheckBox> check = new ArrayList<CheckBox>();
		buttons.add(new Select("check1", "check1"));
		buttons.add(new Select("check2", "check2"));
		buttons.add(new Select("check3", "check3"));
		column.addChild(new CheckBoxLists("list", check));
		
		InterfaceDescriptor descriptor = new InterfaceDescriptor(column);
		return descriptor;
	}

	@Override
	public String execute(Map<String, String[]> map) {
		return "you rules !";
	}
}
