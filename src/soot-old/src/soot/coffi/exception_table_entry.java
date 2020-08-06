/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997 Clark Verbrugge
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







package soot.coffi;


/** An entry in some code's exception table; describes an exception
 * handler and the code it protects.
 * @see Code_attribute
 * @author Clark Verbrugge
 */
class exception_table_entry {
   /** Code offset of start of code protected by this handler (inclusive).
    * @see exception_table_entry#start_inst
    */
   public int start_pc;
   /** Code offset of end of code protected by this handler (exclusive).
    * @see exception_table_entry#end_inst
    */
   public int end_pc;
   /** Code offset of actual exception handler for the specified code block.
    * @see exception_table_entry#handler_inst
    * @see exception_table_entry#b
    */
   public int handler_pc;
   /** Constant pool index of a CONSTANT_Class entry describing the exception
    * this handler handles; if 0, this handler catches all exceptions.
    * @see CONSTANT_Class
    */
   public int catch_type;
   /** First Instruction object (after parsing) of code protected by this handler.
    * @see exception_table_entry#start_pc
    * @see Instruction
    */
   public Instruction start_inst;
   /** First Instruction object (after parsing) of code not protected by this handler
    * (or <i>null</i> for the end of code).
    * @see exception_table_entry#end_pc
    * @see Instruction
    */
   public Instruction end_inst;
   /** Instruction object (after parsing) of start of handler code.
    * @see exception_table_entry#handler_pc
    * @see Instruction
    */
   public Instruction handler_inst;
   /** Once basic blocks are constructed, the handler can be found by
    * referencing its basic block.
    * @see BasicBlock
    */
   public BasicBlock b;
}
