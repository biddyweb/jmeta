package org.meta.plugins.PluginExemple.tcp.commands;

import java.util.ArrayList;
import org.meta.api.amp.AMPAnswerBuilder;
import org.meta.api.amp.AMPBuilder;
import org.meta.api.amp.PluginAMPCommand;
import org.meta.api.common.MetHash;
import org.meta.api.model.Searchable;

public class ExempleCommand extends PluginAMPCommand {

    @Override
    public AMPBuilder execute(String answer, MetHash hash) {
        ArrayList<Searchable> datas = new ArrayList<Searchable>();
        
        Searchable s =  myTCPControler.getInTheModel(hash);
        if(s != null){
            datas.add(s);
        }
        return new AMPAnswerBuilder(answer, datas);
    }

}
