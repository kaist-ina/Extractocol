/* Soot - a J*va Optimization Framework
 * Copyright (C) 2002 Ondrej Lhotak
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

package soot.util;
import java.util.HashMap;
import java.util.Map;

import heros.ThreadSafe;

/** A class that numbers strings, so they can be placed in bitsets.
 *
 * @author Ondrej Lhotak
 */

@ThreadSafe
public class StringNumberer extends ArrayNumberer<NumberedString> {
    private Map<String, NumberedString> stringToNumbered =
    		new HashMap<String, NumberedString>(1024);
    
    public synchronized NumberedString findOrAdd( String s ) {
    	NumberedString numStr = new NumberedString(s);
        NumberedString ret = stringToNumbered.putIfAbsent(s, numStr);
        if( ret == null ) {
            add(numStr);
            return numStr;
        }
        return ret;
    }
}
