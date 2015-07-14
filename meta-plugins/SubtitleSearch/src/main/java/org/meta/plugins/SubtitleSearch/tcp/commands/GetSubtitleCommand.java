package org.meta.plugins.SubtitleSearch.tcp.commands;

import java.util.ArrayList;
import org.meta.api.amp.AMPAnswerBuilder;
import org.meta.api.amp.PluginAMPCommand;
import org.meta.api.common.MetHash;
import org.meta.api.model.DataFile;
import org.meta.api.model.Searchable;

public class GetSubtitleCommand extends PluginAMPCommand{

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
