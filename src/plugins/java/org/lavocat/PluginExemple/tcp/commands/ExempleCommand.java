package org.lavocat.PluginExemple.tcp.commands;

import org.meta.plugin.tcp.AbstractCommand;
import org.meta.plugin.tcp.amp.AMPAnswerFactory;

public class ExempleCommand extends AbstractCommand {

	@Override
	public AMPAnswerFactory execute(String answer, String hash) {
		// TODO Auto-generated method stub
		return new AMPAnswerFactory(answer, null);
	}

}
