package org.meta.plugins.SubtitleSearch.tcp.commands;

import java.util.ArrayList;

import org.meta.common.MetHash;
import org.meta.model.Data;
import org.meta.model.Search;
import org.meta.model.Searchable;
import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugin.tcp.amp.AMPAnswerBuilder;

public class SearchSubtitleCommand extends AbstractCommand {

    @Override
    public AMPAnswerBuilder execute(String answer, MetHash hash) {
        ArrayList<Searchable> datas = new ArrayList<Searchable>();
        //retrieving the search
        Search s = (Search) myTCPControler.getInTheModel(hash);
        //if result if not null, add it and his children to the result list
        if(s != null){
            datas.add(s);
            for(Data data : s.getLinkedData())
                datas.add(data.toOnlyTextData());
        }
        return new AMPAnswerBuilder(answer, datas);
    }

}
