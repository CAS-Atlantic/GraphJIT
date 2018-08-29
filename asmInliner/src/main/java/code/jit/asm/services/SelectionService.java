/*******************************************************************************
 * Copyright (c) 2018 IBM Corp. and others
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution and is available at https://www.eclipse.org/legal/epl-2.0/
 * or the Apache License, Version 2.0 which accompanies this distribution and
 * is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package code.jit.asm.services;

import java.util.LinkedList;
import java.util.List;

import code.jit.asm.common.IGraphNode;
import code.jit.asm.common.utils.Constants;

public class SelectionService {

	private static SelectionService _instance = new SelectionService();
	
	private List<ISelector> _selectors = new LinkedList<ISelector>();
	
	private SelectionService(){
		String property = System.getProperty(Constants.IC_THRESH_VALUE);
		if(property!=null ){
			ICSelector.IC_THRESH = Integer.parseInt(property);
		}
		
		property = System.getProperty(Constants.SIZE_THRESH_VALUE);
		if(property!=null ){
			SizeSelector.SIZE_THRESH = Integer.parseInt(property);
		}
		_selectors.add(new ICSelector());
		_selectors.add(new SizeSelector());
	}

	public static synchronized SelectionService getSelectionService(){
		return _instance;
	} 
	
	public boolean select(IGraphNode receiver){
		for(ISelector selector : _selectors){
			if(!selector.select(receiver))  return false;
		}
		return true;
	}
}

interface ISelector{
	public boolean select(IGraphNode obj);
}

class SizeSelector implements ISelector{
	public static int SIZE_THRESH = 100;

	@Override
	public boolean select(IGraphNode obj) {
		int size = obj.getSize();
		if(size <= SIZE_THRESH){
			return true;
		}else{
			return false;
		}
	}
}

class ICSelector implements ISelector{
	public static int IC_THRESH = 0;
	
	@Override
	public boolean select(IGraphNode obj) {
		int ic = obj.getInvocationCount();
		if(ic>= IC_THRESH){
			return true;
		}else{
			System.out.println("The target object is cold ic: "+ic+" and current thresh is: "+ IC_THRESH);
			return false;
		}
	}
}

