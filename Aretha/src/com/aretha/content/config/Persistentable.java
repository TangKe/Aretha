package com.aretha.content.config;

public interface Persistentable {
	public String persistent();

	public void depersistent(String persistent);
}
