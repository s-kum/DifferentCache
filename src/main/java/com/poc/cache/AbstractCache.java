package com.poc.cache;

import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AbstractCache {
	protected long maxAge;
	protected TimeUnit unit;
	
}
