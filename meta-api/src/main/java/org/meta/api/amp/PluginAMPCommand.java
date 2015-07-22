package org.meta.api.amp;

import org.meta.api.common.MetHash;

/**
 * Define how need to work a tcp command in a plugin.
 *
 * @author faquin
 *
 */
public abstract class PluginAMPCommand {

    /**
     *
     */
    protected PluginAMPController myTCPControler = null;

    /**
     *
     */
    public PluginAMPCommand() {
    }

    /**
     * Execute the command with the given parameters. This command is called via {@link AskHandlerThread} by
     * the request of an othe machine.
     *
     * @param answer the unique code defining the question
     * @param hash the MetHash object of the question
     * @return The builder for the query
     */
    public abstract AMPBuilder execute(String answer, MetHash hash);

    /**
     * Who's my tcpControler ?
     *
     * @param abstractPluginTCPControler it's it
     */
    public final void setPluginTCPControler(final PluginAMPController abstractPluginTCPControler) {
        this.myTCPControler = abstractPluginTCPControler;
    }
}
