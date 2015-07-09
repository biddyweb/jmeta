package org.meta.plugin.tcp.amp.exception;

public class NotAValidAmpAnswerCommand extends InvalidAMPCommand {

    public NotAValidAmpAnswerCommand(String type){
        super("Error, the type "+type+"is not an valid JMeta's Model type");
    }
}
