package com.meta.controler.P2P;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Properties;

import com.google.inject.Guice;
import com.google.inject.Injector;

import il.technion.ewolf.kbr.KeybasedRouting;
import il.technion.ewolf.kbr.MessageHandler;
import il.technion.ewolf.kbr.Node;
import il.technion.ewolf.kbr.openkad.KadNetModule;

/*
 *	JMeta - Meta's java implementation
 *	Copyright (C) 2013 Thomas LAVOCAT
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
/**
 * 
 * @author Thomas LAVOCAT
 *
 */
public class P2PControler implements MessageHandler{
	
	private KeybasedRouting kbr;

	public P2PControler() throws IOException, URISyntaxException{
		// set kademlia udp port and protocol
		Properties props = new Properties();
		Injector injector = Guice.createInjector(new KadNetModule()
		    .setProperty("openkad.net.udp.port", "5555"));

		// create
		kbr = injector.getInstance(KeybasedRouting.class);

		// start listening on local port
		kbr.create();

		// join the network
		// format of the uri: [protocol]://[address:port]/
		ArrayList<URI> lstURI = new ArrayList<URI>();
		lstURI.add(new URI("openkad.udp://1.2.3.4:5555/"));
		kbr.join(lstURI);
		
	}
	
	@Override
	public void onIncomingMessage(Node from, String tag, Serializable content) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Serializable onIncomingRequest(Node from, String tag,
			Serializable content) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
