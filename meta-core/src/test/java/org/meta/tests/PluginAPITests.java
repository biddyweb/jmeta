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
package org.meta.tests;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.meta.api.common.AsyncOperation;
import org.meta.api.common.OperationListener;
import org.meta.api.common.exceptions.MetaException;
import org.meta.api.model.DataFile;
import org.meta.api.plugin.DownloadOperation;
import org.meta.api.plugin.MetAPI;
import org.meta.controller.MetaController;
import org.meta.model.MetaFile;

/**
 *
 * @author dyslesiq
 */
public class PluginAPITests extends MetaBaseTests {

    private static MetaController controller;

    private static MetAPI api;

    private static DataFile toDownload;

    @BeforeClass
    public static void setup() {
        controller = new MetaController();
        try {
            controller.initAndStartAll();
            api = controller.getPluginAPI();
        } catch (MetaException ex) {
            Assert.fail(ex.getMessage());
        }
        createDownloadable();
    }

    /**
     *
     */
    public static void createDownloadable() {
        toDownload = controller.getModel().getFactory().getDataFile(new File("/home/nico/Videos/Hannibal.S03E03.FASTSUB.VOSTFR.HDTV.XviD-RUDY.avi"));

        if (!api.storePush(toDownload)) {
            Assert.fail("failed to store/push file");
        }
    }

    @Test
    public void testDownload() throws URISyntaxException {
        URI uri = new URI("file:/home/nico/META_DOWNLOAD");
        DataFile destination = new MetaFile(toDownload.getHash(), uri, toDownload.getSize());

        DownloadOperation dop = api.download(destination);
        dop.addListener(new OperationListener<AsyncOperation>() {

            @Override
            public void failed(AsyncOperation operation) {
                Assert.fail("Download failed. Error: " + operation.getFailureMessage());
            }

            @Override
            public void complete(AsyncOperation operation) {
                System.out.println("DOWNLOAD COMPLETE!!!!!!!");

            }
        });
        dop.awaitUninterruptibly();
    }

}
