package com.gulshansingh.ipscanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArgumentGenerator {
	private Set<String> args = new HashSet<String>();

	public void setArg(String arg, boolean enable) {
		if (enable) {
			args.add(arg);
		} else {
			args.remove(arg);
		}
	}

	public List<String> generateArgumentList() {
		List<String> argList = new ArrayList<String>();
		argList.add("");
		argList.add("");
		argList.addAll(args);
		return argList;
	}
}
