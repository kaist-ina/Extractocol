/* Soot - a J*va Optimization Framework
 * Copyright (C) 2004 Jennifer Lhotak
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

package soot.jimple.toolkits.annotation.defs;

import soot.*;
import soot.toolkits.scalar.*;
import soot.tagkit.*;

import java.util.*;

public class ReachingDefsTagger extends BodyTransformer {

	public ReachingDefsTagger(Singletons.Global g) {
	}

	public static ReachingDefsTagger v() {
		return G.v().soot_jimple_toolkits_annotation_defs_ReachingDefsTagger();
	}

	protected void internalTransform(Body b, String phaseName, Map<String, String> options) {

		LocalDefs ld = LocalDefs.Factory.newLocalDefs(b);

		for (Unit s : b.getUnits()) {
			// System.out.println("stmt: "+s);
			for (ValueBox vbox : s.getUseBoxes()) {
				Value v = vbox.getValue();
				if (v instanceof Local) {
					Local l = (Local) v;
					// System.out.println("local: "+l);
					for (Unit next : ld.getDefsOfAt(l, s)) {
						String info = l + " has reaching def: " + next;
						String className =  b.getMethod().getDeclaringClass().getName();
						s.addTag(new LinkTag(info, next, className, "Reaching Defs"));
					}
				}
			}
		}
	}
}
