/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 JMeta
 *	
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU Affero General Public License as
 *	published by the Free Software Foundation, either version 3 of the
 *	License, or (at your option) any later version.
 *	
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU Affero General Public License for more details.
 *	
 *	You should have received a copy of the GNU Affero General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meta.tests;

import junit.framework.TestCase;
import org.meta.configuration.AMPConfiguration;
import org.meta.configuration.DHTConfiguration;
import org.meta.configuration.MetaConfiguration;
import org.meta.configuration.WSConfiguration;

/**
 * Base class for tests to pre-configure the JMETA env
 */
public abstract class MetaBaseTests extends TestCase {

    @Override
    public void setUp() {
        //Init all configs with default values manually
        MetaConfiguration.setAmpConfiguration(new AMPConfiguration());
        MetaConfiguration.setDhtConfiguration(new DHTConfiguration());
        MetaConfiguration.setWSConfiguration(new WSConfiguration());
    }

}
