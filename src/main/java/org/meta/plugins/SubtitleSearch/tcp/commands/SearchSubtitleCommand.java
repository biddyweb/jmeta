package org.meta.plugins.SubtitleSearch.tcp.commands;

import java.util.ArrayList;

import org.meta.model.Searchable;
import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugin.tcp.amp.AMPAnswerFactory;

public class SearchSubtitleCommand extends AbstractCommand {

	@Override
	public AMPAnswerFactory execute(String answer, String hash) {
		ArrayList<Searchable> datas = new ArrayList<Searchable>();
		datas.add(myTCPControler.getInTheModel(hash).toOnlyTextData());
		return new AMPAnswerFactory(answer, datas);
	}

}
