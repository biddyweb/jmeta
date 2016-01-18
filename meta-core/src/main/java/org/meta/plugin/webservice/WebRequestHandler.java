/*
 *
 * JMeta - Meta's java implementation
 *
 * Copyright (C) 2013-2015 Pablo Joubert
 * Copyright (C) 2013-2015 Thomas Lavocat
 * Copyright (C) 2013-2015 Nicolas Michon
 *
 * This file is part of JMeta.
 *
 * JMeta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * JMeta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.meta.plugin.webservice;

import java.io.IOException;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bson.BasicBSONObject;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.meta.api.ws.AbstractPluginWebServiceController;
import org.meta.api.ws.AbstractWebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle a request received from the web server.
 *
 * @author faquin
 *
 */
public class WebRequestHandler extends AbstractHandler {

    private Logger logger = LoggerFactory.getLogger(WebRequestHandler.class);

    private MetaWebServer webServiceReader = null;
    private HashMap<String, AbstractWebService> instanceMap = null;
    private int nbCommands = 0;

    /**
     *
     * @param wsServer the parent web service reader
     */
    public WebRequestHandler(final MetaWebServer wsServer) {
        webServiceReader = wsServer;
        instanceMap = new HashMap<>();
    }

    @Override
    public void handle(final String target,
            final Request base,
            final HttpServletRequest request,
            final HttpServletResponse response)
            throws IOException,
            ServletException {
        //Split the incomming url on every /
        String[] urlParse = target.split("/");
        String action = "";
        String plugin = "";
        String command = "";

        if (urlParse.length == 4) {
            //if theres 4 it means we've got two parameters, an action and
            //a command
            action = urlParse[urlParse.length - 3];
            plugin = urlParse[urlParse.length - 2];
            command = urlParse[urlParse.length - 1];
        } else if (urlParse.length == 3) {
            action = urlParse[urlParse.length - 2];
            plugin = urlParse[urlParse.length - 1];
        } else {
            // otherwise just one System action
            action = urlParse[urlParse.length - 1];
        }

        response.setContentType("application/json; charset=utf-8");

        AbstractPluginWebServiceController pluginInstance = null;

        if (!plugin.isEmpty()) {
            pluginInstance = webServiceReader.getPlugin(plugin);
        }

        //Three cases :
        // we have a command and a plugin
        // we only have a plugin
        // we have nothing
        if (!command.isEmpty() && pluginInstance != null) {
            //First case, get the associated command

            Class<? extends AbstractWebService> clazzWs
                    = pluginInstance.getCommand(command);
            //if clazzWs is not null it means it was found and executable
            if (clazzWs != null) {
                try {

                    String idCommand = request.getParameter("idCommand");
                    AbstractWebService commandWs = null;

                    if (idCommand != null && !idCommand.isEmpty()) {
                        commandWs = instanceMap.get(idCommand);
                    }
                    if (commandWs == null) {
                        idCommand = getNewId();
                        commandWs = clazzWs.getConstructor(AbstractPluginWebServiceController.class)
                                .newInstance(pluginInstance);
                        instanceMap.put(idCommand, commandWs);
                    }

                    BasicBSONObject result = null;

                    switch (action) {
                        //fflush memory
                        case "terminate":
                            commandWs = instanceMap.get(idCommand);
                            if (commandWs != null) {
                                commandWs.kill();
                            }
                            instanceMap.remove(idCommand);
                            break;

                        case "retrieveUpdate":
                            result = commandWs.retrieveUpdate().toJson();
                            break;

                        //execute command
                        case "execute":
                            result = commandWs.execute(request.getParameterMap())
                                    .toJson();
                            break;

                        //default case : get the interface descriptor
                        case "interface":
                        default:
                            result = commandWs.getInterface().toJson();
                            break;
                    }

                    if (result != null) {
                        result.append("idCommand", idCommand);
                        response.getWriter().print(result.toString());
                    }
                    response.setStatus(HttpServletResponse.SC_OK);
                    base.setHandled(true);
                } catch (final Exception e) {
                    logger.error("Exception in web service handler:", e);
                    e.printStackTrace(response.getWriter());
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    base.setHandled(true);
                }
            } else {
                logger.warn("Resource not found");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                base.setHandled(true);
            }
        } else if (!plugin.isEmpty() && pluginInstance != null) {
            //second case, we only have a plugin
            switch (action) {
                case "getCommandList":
                default:
                    response.getWriter().print(pluginInstance.getJsonCommandList());
                    break;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            base.setHandled(true);
        } else {
            //last case: list plugins
            switch (action) {
                case "getPluginsList":
                default:
                    response.getWriter().print(webServiceReader
                            .getPluginListAsJson());
                    break;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            base.setHandled(true);
        }
    }

    private String getNewId() {
        nbCommands++;
        return "command" + nbCommands;
    }

}
