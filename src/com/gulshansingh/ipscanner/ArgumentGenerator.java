package com.gulshansingh.ipscanner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArgumentGenerator {
	private Set<Argument> args = new HashSet<Argument>();

	public void setArg(Argument arg, boolean enable) {
		if (enable) {
			args.add(arg);
		} else {
			args.remove(arg);
		}
	}

	public void clear() {
		args = new HashSet<Argument>();
	}

	public List<String> generateArgumentList() {
		List<String> argList = new ArrayList<String>();
		for (Argument arg : args) {
			addArgument(argList, arg);
		}
		return argList;
	}

	private void addArgument(List<String> l, Argument arg) {
		l.add(arg.getName());
		String argVal = arg.getArg();
		if (argVal != null) {
			l.add(argVal);
		}
	}
}
