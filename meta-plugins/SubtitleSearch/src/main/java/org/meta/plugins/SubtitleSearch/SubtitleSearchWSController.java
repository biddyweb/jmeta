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
package org.meta.plugins.SubtitleSearch;

import java.util.LinkedHashMap;
import org.meta.api.plugin.MetAPI;
import org.meta.api.ws.AbstractPluginWebServiceControler;
import org.meta.api.ws.AbstractWebService;
import org.meta.plugins.SubtitleSearch.webservice.commands.AddSubtitle;
import org.meta.plugins.SubtitleSearch.webservice.commands.GetSubtitles;
import org.meta.plugins.SubtitleSearch.webservice.commands.SearchSubtitles;

/**
 *
 * @author nico
 */
public class SubtitleSearchWSController extends AbstractPluginWebServiceControler {

    /**
     *
     * @param pluginAPI plugin api object
     */
    public SubtitleSearchWSController(final MetAPI pluginAPI) {
        super(pluginAPI);
    }

    @Override
    protected void registercommands(
            LinkedHashMap<String, Class<? extends AbstractWebService>> commands) {
        commands.put("searchSubtitles", SearchSubtitles.class);
        commands.put("getSubtitles", GetSubtitles.class);
        commands.put("addSubtitle", AddSubtitle.class);
    }

}
