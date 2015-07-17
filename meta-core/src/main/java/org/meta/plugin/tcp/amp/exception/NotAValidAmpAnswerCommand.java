package org.meta.plugin.tcp.amp.exception;

/**
 *
 * @author nico
 */
public class NotAValidAmpAnswerCommand extends InvalidAMPCommand {

    /**
     *
     * @param type
     */
    public NotAValidAmpAnswerCommand(String type){
        super("Error, the type "+type+"is not an valid JMeta's Model type");
    }
}
