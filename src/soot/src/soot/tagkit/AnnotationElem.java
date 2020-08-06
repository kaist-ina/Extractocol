/* Soot - a J*va Optimization Framework
 * Copyright (C) 2005 Jennifer Lhotak
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

package soot.tagkit;

import soot.util.Switchable;


/** 
 * Represents the base class of annotation elements
 * each annotation can have several elements 
 * for Java 1.5.
 */

public abstract class AnnotationElem implements Switchable
{

    char kind;
    String name;

    public AnnotationElem(char k, String name){
        this.kind = k;
        this.name = name;
    }
    
    public String toString(){
        return "Annotation Element: kind: "+kind+" name: "+name;
    }

    public char getKind(){
        return kind;
    }

    public String getName(){
        return name;
    }

	public void setName(String name) {
		this.name = name;
	}
}

