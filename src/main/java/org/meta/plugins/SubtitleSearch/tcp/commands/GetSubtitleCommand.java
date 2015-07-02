package org.meta.plugins.SubtitleSearch.tcp.commands;

import java.util.ArrayList;

import org.meta.common.MetHash;
import org.meta.model.DataFile;
import org.meta.model.Searchable;
import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugin.tcp.amp.AMPAnswerBuilder;

public class GetSubtitleCommand extends AbstractCommand{

    @Override
    public AMPAnswerBuilder execute(String answer, MetHash hash) {
        ArrayList<Searchable> datas = new ArrayList<Searchable>();
        DataFile subtitle = (DataFile) myTCPControler.getInTheModel(hash);
        if(subtitle != null){
            datas.add(subtitle);
        }
        return new AMPAnswerBuilder(answer, datas);
    }

}
