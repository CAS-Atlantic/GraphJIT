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

/**
 * 
 */
package code.jit.asm.backplane;

import java.util.LinkedHashMap;
import java.util.Map;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;

import code.jit.asm.services.ConfigurationService;

/**
 * @author shijiex
 *
 */

public class Mapper {

	
	static Map<InlineCode, IMapper> _maps = new LinkedHashMap<InlineCode, IMapper>();
	
	static {
		_maps.put(InlineCode.METHOD_INLINE, new DefaultMapper());
		_maps.put(InlineCode.CLASS_INLINE, new ClassInlineMapper());
	}
	public static Remapper getMapper(MethodContext context, String fieldHoster){
		return _maps.get(ConfigurationService.get().INLINE_CODE).createMapper(context, fieldHoster);
	} 
}

interface IMapper{
	public Remapper createMapper(MethodContext context, String fieldHoster);
}


class DefaultMapper implements IMapper{

	
	/* (non-Javadoc)
	 * @see code.jit.asm.Field.IMapper#createMapper(code.jit.asm.backplane.MethodContext)
	 */
	@Override
	public Remapper createMapper(MethodContext context, String hoster) {
		if(context.getInliningBlob() == null ||context.getInliningBlob().getTemplateObject() == null ){
			return new SimpleRemapper("", "");
		}
		
		Map<String, String> maps = new LinkedHashMap<String, String>();
		String original = Type.getInternalName(context.getInliningBlob().getTemplateObject().getClass());
		String target  = Type.getInternalName(context.getInvocationReceiver().getReceiver().getClass());
		maps.put(original, target);
		return new SimpleRemapper(maps);	
	}
}

class ClassInlineMapper implements IMapper{

	
	/* (non-Javadoc)
	 * @see code.jit.asm.Field.IMapper#createMapper(code.jit.asm.backplane.MethodContext)
	 */
	@Override
	public Remapper createMapper(MethodContext context, String hoster) {
		//@TODO cache existing single Remapper.  
		
		Remapper _map;
		Map<String, String> map = context.getClassContext().createMapper(hoster);
		_map = new SimpleRemapper(map);
		return _map; 
	}
}

class InlineeFieldRemapper extends SimpleRemapper{

	/**
	 * @param mapping
	 */
	public InlineeFieldRemapper(Map map, ClassContext context){
		super(map);
	}

	@Override
	public String mapFieldName(String owner, String name, String desc) {
		String s = map(owner + '.' + name);
		return s == null ? name : s;
	}

}
