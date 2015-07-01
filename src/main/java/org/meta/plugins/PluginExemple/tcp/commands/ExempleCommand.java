package org.meta.plugins.PluginExemple.tcp.commands;

import java.util.ArrayList;

import org.meta.common.MetHash;
import org.meta.model.Searchable;
import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugin.tcp.amp.AMPAnswerBuilder;

public class ExempleCommand extends AbstractCommand {

    @Override
    public AMPAnswerBuilder execute(String answer, MetHash hash) {
        ArrayList<Searchable> datas = new ArrayList<Searchable>();
        
        Searchable s =  myTCPControler.getInTheModel(hash);
        if(s != null){
            datas.add(s);
        }
        return new AMPAnswerBuilder(answer, datas);
    }

}
