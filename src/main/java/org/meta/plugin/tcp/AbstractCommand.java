package org.meta.plugin.tcp;

import org.meta.plugin.AbstractPluginTCPControler;
import org.meta.plugin.tcp.amp.AMPAnswerFactory;

/**
 * Define how need to work a tcp command in a plugin
 * @author faquin
 *
 */
public abstract class AbstractCommand {

    protected AbstractPluginTCPControler myTCPControler = null;

    public AbstractCommand(){}
    
    /**
     * Execute the command with the given parameters.
     * This command is called via {@link AskHandlerThread} by the request of
     * an othe machine.
     * 
     * @param answer    the unique code defining the question
     * @param hash      the string representation of the hash
     * @return
     */
    public abstract AMPAnswerFactory execute(String answer, String hash);

    /**
     * Who's my tcpControler ?
     * @param abstractPluginTCPControler it's it
     */
    public void setPluginTCPControler(
            AbstractPluginTCPControler abstractPluginTCPControler) {
        this.myTCPControler = abstractPluginTCPControler;
    }
}
