package com.meta.controler.P2P;

import il.technion.ewolf.kbr.Key;
import il.technion.ewolf.kbr.KeybasedRouting;
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
 * make a search in a new thread, give the response to P2Plistener given 
 * in the constrcutor
 * @author Thomas LAVOCAT
 *
 */
public class ThreadLookForPeers extends Thread {
	
	private Key 				hash	=	null;
	private P2PListener 		listener=	null;
	private KeybasedRouting 	kbr		=	null;

	public ThreadLookForPeers(
			KeybasedRouting kbr, 
			Key hash, 
			P2PListener listener) 
	{
		this.kbr = kbr;
		this.hash = hash;
		this.listener = listener;
	}

	@Override
	public void run() {
		listener.nodesFounded(kbr.findNode(hash));
	}
}
