package com.poc.cache;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TestCond {
	Lock l = new ReentrantLock();
	
	Condition c = l.newCondition();
	public static void main(String[] args) {
		TestCond t = new TestCond();
		t.testM();
			
		
	}
	
	void testM(){
		l.lock();
		c.signalAll();
		l.unlock();
	
	}
}
