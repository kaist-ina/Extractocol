/* Soot - a J*va Optimization Framework
 * Copyright (C) 2004 Ondrej Lhotak
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
 	        	
/* 04.04.2006 mbatch	if there is a $ in the name,
 *						we need to check if it's a real file, 
 * 						not just inner class								
 */

package soot;

import soot.options.Options;

/** A class provider looks for a file of a specific format for a specified
 * class, and returns a ClassSource for it if it finds it.
 */
public class JavaClassProvider implements ClassProvider
{
	public static class JarException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public JarException(String className) {
			super("Class "+className+" was found in an archive, but Soot doesn't support reading source files out of an archive");
		}
		
	}
	
    /** Look for the specified class. Return a ClassSource for it if found,
     * or null if it was not found. */
    public ClassSource find( String className ) {

    	if(Options.v().polyglot() &&
	       soot.javaToJimple.InitialResolver.v().hasASTForSootName(className)){
            soot.javaToJimple.InitialResolver.v().setASTForSootName(className);
            return new JavaClassSource(className);
    	} else { //jastAdd; or polyglot AST not yet produced
	    	/* 04.04.2006 mbatch	if there is a $ in the name,
			 *						we need to check if it's a real file, 
			 * 						not just inner class								
			 */
	      	boolean checkAgain = className.indexOf('$') >= 0;
	      	
	      	SourceLocator.FoundFile file = null;
	      	try {
		        String javaClassName = SourceLocator.v().getSourceForClass(className);
		        String fileName = javaClassName.replace('.', '/') + ".java";
		        file = SourceLocator.v().lookupInClassPath(fileName);
		
		        /* 04.04.2006 mbatch	if inner class not found,
			     *						check if it's a real file							
				 */
		        if( file == null) {
		        
		          if (checkAgain) {
		            fileName = className.replace('.', '/') + ".java";
		            file = SourceLocator.v().lookupInClassPath(fileName);
		          }
		        }
		        /* 04.04.2006 mbatch	end */
		
		        if (file == null)
		        	return null;         
		        
		        if( file.isZipFile()) {
		            throw new JarException(className);
		        }
		        return new JavaClassSource(className, file.getFile());
	      	}
	      	finally {
	      		if (file != null)
	      			file.close();
	      	}
    	}

    }
}