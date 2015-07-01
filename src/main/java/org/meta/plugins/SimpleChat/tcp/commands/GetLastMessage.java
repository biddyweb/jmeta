package org.meta.plugins.SimpleChat.tcp.commands;

import java.util.ArrayList;

import org.meta.common.MetHash;
import org.meta.model.Search;
import org.meta.model.Searchable;
import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugin.tcp.amp.AMPAnswerBuilder;

public class GetLastMessage extends AbstractCommand {

    @Override
    public AMPAnswerBuilder execute(String answer, MetHash hash) {
        ArrayList<Searchable> datas = new ArrayList<Searchable>();
        
        Search s = (Search) myTCPControler.getInTheModel(hash);
        if(s != null){
            datas.add(s);
            datas.addAll(s.getLinkedData());
        }
        return new AMPAnswerBuilder(answer, datas);
    }

}
