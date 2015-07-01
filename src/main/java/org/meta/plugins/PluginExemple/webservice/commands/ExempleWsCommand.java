package org.meta.plugins.PluginExemple.webservice.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.meta.model.Searchable;
import org.meta.plugin.webservice.AbstractWebService;
import org.meta.plugin.webservice.forms.fields.DateInput;
import org.meta.plugin.webservice.forms.fields.TextOutput;
import org.meta.plugin.webservice.forms.submit.SelfSubmitButton;
import org.meta.plugin.webservice.forms.submit.SubmitToButton;

public class ExempleWsCommand extends AbstractWebService {

    private TextOutput          output         = null;
    private int                 nbRefresh      = 0;
    private DateInput           birthDate      = null;

    public ExempleWsCommand(){
        // Describe a full interface for test
        birthDate = new DateInput("birthDate", "Birth Date");
        rootColumn.addChild(birthDate);
        rootColumn.addChild(new SelfSubmitButton("submit", "submit form to me"));
        rootColumn.addChild(
                new SubmitToButton(
                        "submitTo", 
                        "submit to secondExample", 
                        "secondExample"));
        output = new TextOutput("output", "Sortie");
        output.append("message");
        rootColumn.addChild(output);
    }


    @Override
    public void executeCommand(Map<String, String[]> map) {
        output.flush();
        birthDate.setValue(getParameter(birthDate.getId(), map));
                output.append("Your sended parameters are :");
        for (Iterator<String> i = map.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            output.append(" - "+key+" : "+map.get(key));
        }
    }

    @Override
    public void applySmallUpdate() {
        nbRefresh++;
        output.append("refresh number"+nbRefresh);
        birthDate.setValue("tututu");
    }

    @Override
    public void callbackSuccess(ArrayList<Searchable> results) {
        // TODO Auto-generated method stub
    }
}
