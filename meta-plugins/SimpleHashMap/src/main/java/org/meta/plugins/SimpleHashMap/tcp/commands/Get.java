package org.meta.plugins.SimpleHashMap.tcp.commands;

import java.util.ArrayList;
import org.meta.api.amp.AMPAnswerBuilder;
import org.meta.api.amp.PluginAMPCommand;
import org.meta.api.common.MetHash;
import org.meta.api.model.MetaData;
import org.meta.api.model.Search;
import org.meta.api.model.Searchable;

public class Get extends PluginAMPCommand {

    @Override
    public AMPAnswerBuilder execute(String answer, MetHash hash) {
        ArrayList<Searchable> datas = new ArrayList<Searchable>();
        Search s = (Search) myTCPControler.getInTheModel(hash);
        datas.add(myTCPControler.getInTheModel(hash));
        MetaData m = s.getMetaData();
        datas.addAll(s.getLinkedData());
        return new AMPAnswerBuilder(answer, datas);
    }

}
