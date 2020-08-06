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
package soot.jimple.infoflow.taintWrappers;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.infoflow.InfoflowManager;
import soot.jimple.infoflow.data.Abstraction;

/**
 * Set of taint wrappers. It supports taint wrapping for a class if at least one
 * of the contained wrappers supports it. The resulting taints are the union of
 * all taints produced by the contained wrappers.
 * 
 * @author Steven Arzt
 */
public class TaintWrapperSet implements ITaintPropagationWrapper {

	private Set<ITaintPropagationWrapper> wrappers = new HashSet<ITaintPropagationWrapper>();
	private AtomicInteger hits = new AtomicInteger();
	private AtomicInteger misses = new AtomicInteger();
	
	@Override
	public void initialize(InfoflowManager manager) {
		for (ITaintPropagationWrapper w : this.wrappers)
			w.initialize(manager);
	}
	
	/**
	 * Adds the given wrapper to the chain of wrappers.
	 * @param wrapper The wrapper to add to the chain.
	 */
	public void addWrapper(ITaintPropagationWrapper wrapper) {
		this.wrappers.add(wrapper);
	}

	@Override
	public Set<Abstraction> getTaintsForMethod(Stmt stmt, Abstraction d1,
			Abstraction taintedPath) {
		Set<Abstraction> resList = new HashSet<Abstraction>();
		for (ITaintPropagationWrapper w : this.wrappers) {
			Set<Abstraction> curAbsSet = w.getTaintsForMethod(stmt, d1, taintedPath);
			if (curAbsSet != null)
				resList.addAll(curAbsSet);
		}
		
		// Bookkeeping for statistics
		if (resList.isEmpty())
			misses.incrementAndGet();
		else
			hits.incrementAndGet();
		
		return resList;
	}

	@Override
	public boolean isExclusive(Stmt stmt, Abstraction taintedPath) {
		for (ITaintPropagationWrapper w : this.wrappers)
			if (w.isExclusive(stmt, taintedPath))
				return true;
		return false;
	}

	@Override
	public boolean supportsCallee(SootMethod method) {
		for (ITaintPropagationWrapper w : this.wrappers)
			if (w.supportsCallee(method))
				return true;
		return false;
	}
	
	@Override
	public boolean supportsCallee(Stmt callSite) {
		for (ITaintPropagationWrapper w : this.wrappers)
			if (w.supportsCallee(callSite))
				return true;
		return false;
	}

	@Override
	public int getWrapperHits() {
		return hits.get();
	}

	@Override
	public int getWrapperMisses() {
		return misses.get();
	}

	@Override
	public Set<Abstraction> getAliasesForMethod(Stmt stmt, Abstraction d1,
			Abstraction taintedPath) {
		Set<Abstraction> resList = new HashSet<Abstraction>();
		for (ITaintPropagationWrapper w : this.wrappers) {
			Set<Abstraction> curAbsSet = w.getAliasesForMethod(stmt, d1, taintedPath);
			if (curAbsSet != null)
				resList.addAll(curAbsSet);
		}
		return resList;
	}
	
}
