/*******************************************************************************
 * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
 * Bodden, and others.
 ******************************************************************************/
package soot.jimple.infoflow.test.junit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soot.jimple.infoflow.IInfoflow;
import soot.jimple.infoflow.InfoflowConfiguration;
/**
 * test taint propagation in sets
 */
public class SetTests extends JUnitTests {
    
    @Test(timeout=300000)
    public void concreteHashSetTest(){
    	System.out.println("Running test case concreteHashSetTest...");
    	IInfoflow infoflow = initInfoflow();

    	int oldAPLength = InfoflowConfiguration.getAccessPathLength();
    	try {
	    	InfoflowConfiguration.setAccessPathLength(1);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void concreteWriteReadHashTest()>");
			infoflow.getConfig().setFlowSensitiveAliasing(false);
			infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
			checkInfoflow(infoflow, 1);
			System.out.println("Test case concreteHashSetTest done.");
    	}
		finally {
			InfoflowConfiguration.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
		}
    }
    
    @Test(timeout=600000)	// implicit flow, takes ~74s
    public void containsTest(){
    	System.out.println("Running test case containsTest...");
    	IInfoflow infoflow = initInfoflow();
    	infoflow.getConfig().setEnableImplicitFlows(true);
    	infoflow.getConfig().setEnableStaticFieldTracking(false);
		infoflow.getConfig().setFlowSensitiveAliasing(false);

    	int oldAPLength = InfoflowConfiguration.getAccessPathLength();
    	try {
	    	InfoflowConfiguration.setAccessPathLength(1);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void containsTest()>");
			infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
			checkInfoflow(infoflow, 1);
			System.out.println("Test case containsTest done.");
    	}
		finally {
			InfoflowConfiguration.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
		}
    }
    
    @Test(timeout=600000)
    public void concreteTreeSetPos0Test(){
    	System.out.println("Running test case concreteTreeSetPos0Test...");
    	IInfoflow infoflow = initInfoflow();
    	
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void concreteWriteReadTreePos0Test()>");
		infoflow.getConfig().setFlowSensitiveAliasing(false);
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		checkInfoflow(infoflow, 1);
		
		System.out.println("Test case concreteTreeSetPos0Test done.");
    }
    
    @Test(timeout=600000)
    public void concreteTreeSetPos1Test(){
    	System.out.println("Running test case concreteTreeSetPos1Test...");
    	IInfoflow infoflow = initInfoflow();
    	
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void concreteWriteReadTreePos1Test()>");
		infoflow.getConfig().setFlowSensitiveAliasing(false);
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		checkInfoflow(infoflow, 1);
		
		System.out.println("Test case concreteTreeSetPos1Test done.");
    }
    
    @Test(timeout=300000)
    public void concreteLinkedSetPos0Test(){
    	System.out.println("Running test case concreteLinkedSetPos0Test...");
    	IInfoflow infoflow = initInfoflow();
    	
    	int oldAPLength = InfoflowConfiguration.getAccessPathLength();
    	try {
	    	InfoflowConfiguration.setAccessPathLength(1);
	    	infoflow.getConfig().setFlowSensitiveAliasing(false);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void concreteWriteReadLinkedPos0Test()>");
			infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
			checkInfoflow(infoflow, 1);
			System.out.println("Test case concreteLinkedSetPos0Test done.");
    	}
    	finally {
    		InfoflowConfiguration.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
    	}
    }
    
    @Test(timeout=300000)
    public void concreteLinkedSetPos1Test(){
    	System.out.println("Running test case concreteLinkedSetPos1Test...");
    	IInfoflow infoflow = initInfoflow();
    	
    	int oldAPLength = InfoflowConfiguration.getAccessPathLength();
    	try {
	    	InfoflowConfiguration.setAccessPathLength(1);
	    	infoflow.getConfig().setFlowSensitiveAliasing(false);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void concreteWriteReadLinkedPos1Test()>");
			infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
			checkInfoflow(infoflow, 1);
			System.out.println("Test case concreteLinkedSetPos1Test done.");
    	}
    	finally {
    		InfoflowConfiguration.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
    	}
    }
    
    @Test(timeout=300000)
    public void setTest(){
    	System.out.println("Running test case setTest...");
    	IInfoflow infoflow = initInfoflow();
    	
    	int oldAPLength = InfoflowConfiguration.getAccessPathLength();
    	try {
	    	InfoflowConfiguration.setAccessPathLength(1);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void writeReadTest()>");
			infoflow.getConfig().setFlowSensitiveAliasing(false);
			infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
			checkInfoflow(infoflow, 1);
			System.out.println("Test case setTest done.");
    	}
		finally {
			InfoflowConfiguration.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
		}
    }
    
    @Test(timeout=300000)
    public void setIteratorTest(){
    	System.out.println("Running test case setIteratorTest...");
    	IInfoflow infoflow = initInfoflow();
    	
    	int oldAPLength = InfoflowConfiguration.getAccessPathLength();
    	try {
	    	InfoflowConfiguration.setAccessPathLength(1);
	    	List<String> epoints = new ArrayList<String>();
	    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void iteratorTest()>");
			infoflow.getConfig().setFlowSensitiveAliasing(false);
			infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
			checkInfoflow(infoflow, 1);		
			System.out.println("Test case setIteratorTest done.");
    	}
    	finally {
    		InfoflowConfiguration.setAccessPathLength(oldAPLength);	// this is a global setting! Restore it when we're done
    	}
    }
    
    @Test(timeout=300000)
    public void concreteNegativeTest(){
    	System.out.println("Running test case concreteNegativeTest...");
    	IInfoflow infoflow = initInfoflow();
    	
    	List<String> epoints = new ArrayList<String>();
    	epoints.add("<soot.jimple.infoflow.test.SetTestCode: void concreteWriteReadNegativeTest()>");
		infoflow.getConfig().setFlowSensitiveAliasing(false);
		infoflow.computeInfoflow(appPath, libPath, epoints, sources, sinks);
		negativeCheckInfoflow(infoflow);
		
		System.out.println("Test case concreteNegativeTest done.");
    }
}
