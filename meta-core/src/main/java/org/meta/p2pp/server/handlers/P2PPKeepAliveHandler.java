/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meta.p2pp.server.handlers;

import org.meta.api.model.ModelStorage;
import org.meta.p2pp.P2PPConstants;
import org.meta.p2pp.server.P2PPCommandHandler;
import org.meta.p2pp.server.P2PPServerClientContext;
import org.meta.p2pp.server.P2PPServerRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dyslesiq
 */
public class P2PPKeepAliveHandler extends P2PPCommandHandler {

    private final Logger logger = LoggerFactory.getLogger(P2PPKeepAliveHandler.class);

    /**
     *
     * @param modelStorage the model storage
     */
    public P2PPKeepAliveHandler(final ModelStorage modelStorage) {
        super(modelStorage);
    }

    @Override
    public void handle(final P2PPServerClientContext clientContext, final P2PPServerRequestContext request) {
        logger.debug("handle keep alive");
        //Nothing to do for keep-alive request ?
        request.setStatus(P2PPConstants.ServerRequestStatus.FINISHED);
        clientContext.handlerComplete(request);
    }

}
