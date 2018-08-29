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

package code.jit.asm.backplane;

public class InconsistentException extends Exception {
	
	private String _givenName;
	private String _acName;
	
	public InconsistentException(String exception, String givenName, String bcName){
		super(exception);
		_givenName = givenName;
		_acName = bcName;
	}
	

	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer(super.toString());
		buffer.append("(").append( _givenName).append(",").append(_acName).append(")");
		return buffer.toString();
	}
}
