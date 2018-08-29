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

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import code.jit.asm.backplane.BytecodeResource;
import code.jit.asm.backplane.ClassContext;
import code.jit.asm.common.ICache;
import code.jit.asm.common.IGraphNode;
import code.jit.asm.common.logging.GraphLogger;
import code.jit.asm.common.utils.Configs;


/**
 * @author shijiex
 *
 */
public class BytecodeCacheService {
	private static final Logger _logger = GraphLogger.get(BytecodeCacheService.class);
	
	private static class CachePlacer{
		private static final BytecodeCacheService _instance = new BytecodeCacheService();
	}

	private ICache _cacheInstance = null;
	private BytecodeCacheService(){
		_logger.debug("Start Bytecode Cache Service");
		_cacheInstance = Configs._globalcache ? new GlobalCache() : new ICache() {

			@Override
			public boolean put(IGraphNode key, Object obj) {
				_logger.info("ObjectCache: put >> IGraphNode for key {}", key);
				return key.cacheData(obj);
			}

			@Override
			public Object get(IGraphNode key) {
				if (key instanceof IGraphNode) {
					Object res = key.getCacheData();
					_logger.info("ObjectCache: get << IGraphNode for key {} and object {}", key, res);
					return res;
				}
				return null;
			}

			@Override
			public void clear() {
				// Nothing to do here.
			}

			@Override
			public void start() {
				//Nothing to do here.
			}
		};
	}
	
	public void start(){
		_cacheInstance.start();
	}
	
	public BytecodeResource getBcClass(IGraphNode key){
		BytecodeResource bc = null;
		try{
			Object obj =  _cacheInstance.get(key);
			if(obj == null){
				_logger.info("CacheService: getBCClass miss for key: {}", key);
				return null;
			}
			bc = (BytecodeResource) obj;
			bc.hit();
		}catch(Exception e){
			_logger.error(e.toString());
		}
		
		_logger.info("CacheService: getBCClass:  hit for {} {} ", key, bc);
		//return bc.clone();
		return bc;
	}
	
	public BytecodeResource getBcClass(Object obj){
		
		if(obj instanceof IGraphNode){
			IGraphNode key = (IGraphNode) obj;
			return getBcClass(key);
		}
		return null;
	}
	
	public boolean put(String className, ClassContext context){
		try{
			return _cacheInstance.put(context.getOwner(), new BytecodeResource(context));	
		}catch(Exception e){		
			_logger.error(e.toString());
		}
		return false;
	}
	
	
	public static BytecodeCacheService get(){
		return CachePlacer._instance;
	}
	
	public void reset(){
		_cacheInstance.clear();
	}
}

/**
 * For the GlobalCache: 
 * 1) A purger that removes BytecodeClass using least used in recent time. 
 * 2) Your IGraphNode should override hashCode() and equals() 
 * 
 * @author Shijie xu
 *
 */
class GlobalCache implements ICache{

	private ConcurrentMap<IGraphNode, BytecodeResource> _map = new ConcurrentHashMap<IGraphNode, BytecodeResource>();

	private final int MAX_ENTITES = Configs.CACHE_GLOBAL_CACHE_SIZE;  // The maximal entities allowed in the GlobalCache (This is in theory).
	private final float _ratio = Configs.CACHE_GLOBAL_CACHE_RATIO;   // Each purge removes (1-_ratios)*entities. 
	
	ScheduledExecutorService exec = Executors.newScheduledThreadPool(1,  new ThreadFactory(){
		final AtomicInteger count = new AtomicInteger(1);
		
		@Override
		public Thread newThread(Runnable r) {
			Thread thread  = new Thread();
			thread.setName("GLobalCache: purge "+count.getAndIncrement());
			thread.setDaemon(true);
			return thread;
		}
		
	});
	
	public GlobalCache(){

	}
	
	
	@Override
	public boolean put(IGraphNode key, Object obj) {
		if(obj instanceof BytecodeResource){
			BytecodeResource bObj = (BytecodeResource)obj;
			
			return _map.putIfAbsent(key, bObj)==null;
		}
		return false;
	}

	@Override
	public Object get(IGraphNode  key) {
		return _map.get(key);
	}

	@Override
	public void clear() {
		_map.clear();
		
	}


	@Override
	public void start() {
		exec.scheduleAtFixedRate(()->{
			//@TODO the future developer can think up better purge policy. Here I only use a simple NON-STRICT Longest un-used first. 
			if(_map.size() > MAX_ENTITES*_ratio){
				int len = (int) (MAX_ENTITES*(1-_ratio));
				while(len>0){
					IGraphNode longest = null;
					long max=-1;
					Iterator<IGraphNode> iter = _map.keySet().iterator();
					//It is NON-Strict because during iteration the BytecodeClass in global Cache might be updated. 
					while(iter.hasNext()){
						IGraphNode node = iter.next();
						BytecodeResource resource = _map.get(node);
						if(longest == null || max < resource.getLife()){
							longest = node;
							max = _map.get(longest).getLife();
						}
					}
					if(longest!=null)
						_map.remove(longest);
				}
				len--;
			}
			
		}, 1, 2, TimeUnit.SECONDS);
		
	}
}

