package org.meta.plugins.SimpleChat.tcp.commands;

import java.util.ArrayList;
import org.meta.api.amp.AMPAnswerBuilder;
import org.meta.api.amp.PluginAMPCommand;
import org.meta.api.common.MetHash;
import org.meta.api.model.Search;
import org.meta.api.model.Searchable;

/**
 *
 * @author nico
 */
public class GetLastMessage extends PluginAMPCommand {

    @Override
    public AMPAnswerBuilder execute(String answer, MetHash hash) {
        ArrayList<Searchable> datas = new ArrayList<Searchable>();

        Search s = (Search) myTCPControler.getInTheModel(hash);
        if (s != null) {
            datas.add(s);
            datas.addAll(s.getLinkedData());
        }
        return new AMPAnswerBuilder(answer, datas);
    }

}
