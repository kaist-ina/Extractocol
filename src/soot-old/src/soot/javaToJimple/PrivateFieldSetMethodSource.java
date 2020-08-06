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

package soot.javaToJimple;

import java.util.*;
public class PrivateFieldSetMethodSource implements soot.MethodSource {

    private final soot.Type fieldType;
    private final String fieldName;
    private final boolean isStatic;
    public PrivateFieldSetMethodSource( soot.Type fieldType, String fieldName, boolean isStatic ) {
    	this.fieldType = fieldType;
    	this.fieldName = fieldName;
    	this.isStatic = isStatic;
    }
    
    public soot.Body getBody(soot.SootMethod sootMethod, String phaseName){
        
        soot.Body body = soot.jimple.Jimple.v().newBody(sootMethod);
        LocalGenerator lg = new LocalGenerator(body);
        
        soot.Local fieldBase = null;
        soot.Local assignLocal = null;
        // create parameters
        int paramCounter = 0;
        Iterator paramIt = sootMethod.getParameterTypes().iterator();
        while (paramIt.hasNext()) {
            soot.Type sootType = (soot.Type)paramIt.next();
            soot.Local paramLocal = lg.generateLocal(sootType);
            
            soot.jimple.ParameterRef paramRef = soot.jimple.Jimple.v().newParameterRef(sootType, paramCounter);
            soot.jimple.Stmt stmt = soot.jimple.Jimple.v().newIdentityStmt(paramLocal, paramRef);
            body.getUnits().add(stmt);
            
            if (paramCounter == 0){
                fieldBase = paramLocal;
            }
            assignLocal = paramLocal;
            paramCounter++;
        }
        
        // create field type local
        //soot.Local fieldLocal = lg.generateLocal(fieldType);
        // assign local to fieldRef
        soot.SootFieldRef field = soot.Scene.v().makeFieldRef( sootMethod.getDeclaringClass(), fieldName, fieldType, isStatic);

        soot.jimple.FieldRef fieldRef = null;
        if (isStatic) {
            fieldRef = soot.jimple.Jimple.v().newStaticFieldRef(field);
        }
        else {
            fieldRef = soot.jimple.Jimple.v().newInstanceFieldRef(fieldBase, field);
        }
        soot.jimple.AssignStmt assign = soot.jimple.Jimple.v().newAssignStmt(fieldRef, assignLocal);
        body.getUnits().add(assign);

        //return local
        soot.jimple.Stmt retStmt = soot.jimple.Jimple.v().newReturnStmt(assignLocal);
        body.getUnits().add(retStmt);
        
        return body;
     
    }
}
