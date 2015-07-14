package org.meta.plugins.SubtitleSearch.tcp.commands;

import java.util.ArrayList;
import org.meta.api.amp.AMPAnswerBuilder;
import org.meta.api.amp.PluginAMPCommand;
import org.meta.api.common.MetHash;
import org.meta.api.model.Data;
import org.meta.api.model.Search;
import org.meta.api.model.Searchable;

public class SearchSubtitleCommand extends PluginAMPCommand {

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
