package org.meta.plugins.PluginExemple.tcp.commands;

import java.util.ArrayList;

import org.meta.common.MetHash;
import org.meta.model.Searchable;
import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugin.tcp.amp.AMPAnswerFactory;

public class ExempleCommand extends AbstractCommand {

    @Override
    public AMPAnswerFactory execute(String answer, String hash) {
        ArrayList<Searchable> datas = new ArrayList<Searchable>();
        MetHash metHash = new MetHash(hash);
        
        Searchable s =  myTCPControler.getInTheModel(metHash);
        if(s != null){
            datas.add(s);
        }
        return new AMPAnswerFactory(answer, datas);
    }

}
