package org.meta.plugins.SubtitleSearch.tcp.commands;

import java.util.ArrayList;

import org.meta.common.MetHash;
import org.meta.model.Searchable;
import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugin.tcp.amp.AMPAnswerBuilder;

public class SearchSubtitleCommand extends AbstractCommand {

    @Override
    public AMPAnswerBuilder execute(String answer, MetHash hash) {
        ArrayList<Searchable> datas = new ArrayList<Searchable>();
        datas.add(myTCPControler.getInTheModel(hash).toOnlyTextData());
        return new AMPAnswerBuilder(answer, datas);
    }

}
