package org.meta.plugins.SimpleHashMap.tcp.commands;

import java.util.ArrayList;

import org.meta.common.MetHash;
import org.meta.model.Searchable;
import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugin.tcp.amp.AMPAnswerFactory;

public class Get extends AbstractCommand {

    @Override
    public AMPAnswerFactory execute(String answer, String hash) {
        ArrayList<Searchable> datas = new ArrayList<Searchable>();
        MetHash metHash = new MetHash(hash);
        datas.add(myTCPControler.getInTheModel(metHash));
        return new AMPAnswerFactory(answer, datas);
    }

}
