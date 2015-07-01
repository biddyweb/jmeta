package org.meta.plugins.SimpleHashMap.tcp.commands;

import java.util.ArrayList;

import org.meta.common.MetHash;
import org.meta.model.MetaData;
import org.meta.model.Search;
import org.meta.model.Searchable;
import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugin.tcp.amp.AMPAnswerBuilder;

public class Get extends AbstractCommand {

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
