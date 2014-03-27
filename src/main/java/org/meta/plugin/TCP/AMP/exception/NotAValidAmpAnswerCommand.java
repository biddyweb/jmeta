package org.meta.plugin.TCP.AMP.exception;

public class NotAValidAmpAnswerCommand extends NotAValidAMPCommand {

	public NotAValidAmpAnswerCommand(String type){
		super("Error, the type "+type+"is not an valid JMeta's Model type");
	}
}
