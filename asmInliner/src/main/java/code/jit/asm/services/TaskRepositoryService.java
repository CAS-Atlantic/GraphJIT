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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import code.jit.asm.backplane.BytecodeResource;

public class TaskRepositoryService implements Runnable{
	
	private static TaskRepositoryService _instance = new TaskRepositoryService();
	
	private BlockingQueue<BytecodeResource> _tasks = new LinkedBlockingQueue<BytecodeResource>();
	
	private Executor _threadFactory=Executors.newFixedThreadPool(1,new ThreadFactory() {
		final AtomicInteger count = new AtomicInteger(1);
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setName("Bytecode-Inliner-" + count.getAndIncrement());
			return thread;
		}
	});

	@Override
	public void run() {
		while(true){
			BytecodeResource task = _tasks.poll();
			if(task!=null){
				
			}
		}
	}
	
	public void put(Class myCls){
//		BytecodeClass cls = new BytecodeClass(myCls);
//		_tasks.offer(cls);
		
	}
	
	public static TaskRepositoryService get(){
		return _instance;
	}
	
	
	private TaskRepositoryService(){
		_threadFactory.execute(this);
		System.out.println("Startup TaskRepository...");
	}
	
}
