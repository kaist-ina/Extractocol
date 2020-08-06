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

package ca.mcgill.sable.soot.attributes;

import soot.*;
import org.eclipse.swt.graphics.RGB;

public class ColorAttribute{
            
    private int red;
    private int green;
    private int blue;
    private int fg;
    private String type;

    public ColorAttribute(int red, int green, int blue, boolean fg){
        this.red = red;
        this.green = green;
        this.blue = blue;
        if (fg){
            this.fg = 1;
        }
        else {
            this.fg = 0;
        }
    }

    public int red(){
        return red;
    }

    public int green(){
        return green;
    }

    public int blue(){
        return blue;
    }

    public int fg(){
        return fg;
    }

	/**
	 * @return
	 */
	public String type() {
		return type;
	}

	/**
	 * @param string
	 */
	public void type(String string) {
		type = string;
	}

	public RGB getRGBColor(){
		return new RGB(red(), green(), blue());
	}
}
