/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */






package soot.toolkits.scalar;

import soot.*;
import soot.toolkits.graph.UnitGraph;

import java.util.*;

import static soot.toolkits.scalar.LocalDefs.Factory.newLocalDefs;

/**
 *   Provides an interface to find the Units that use
 *   a Local defined at a given Unit.
 */
public interface LocalUses
{
	static final public class Factory {
		private Factory() {}

		public static LocalUses newLocalUses(Body body) {
			return newLocalUses(body, newLocalDefs(body));
		}
		
		public static LocalUses newLocalUses(Body body, LocalDefs localDefs) {
			return new SimpleLocalUses(body, localDefs);
		}
		
		public static LocalUses newLocalUses(UnitGraph graph) {
			return newLocalUses(graph.getBody(), newLocalDefs(graph));
		}
		
		public static LocalUses newLocalUses(UnitGraph graph, LocalDefs localDefs) {
			return newLocalUses(graph.getBody(), localDefs);
		}
	}
	
    /**
     *   Returns a list of the Units that use the Local that is 
     *   defined by a given Unit. 
     *   
     *   @param s  the unit we wish to query for the use of the Local
     *             it defines.
     *   @return  a list of the Local's uses.
     */    
    public List<UnitValueBoxPair> getUsesOf(Unit s);
}













