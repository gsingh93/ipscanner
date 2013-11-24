package com.gulshansingh.ipscanner;

public class Argument {
	private String mName;
	private String mArg;

	public Argument(String name) {
		mName = name;
	}

	public Argument(String name, String arg) {
		mName = name;
		mArg = arg;
	}

	public String getName() {
		return mName;
	}

	public String getArg() {
		return mArg;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mArg == null) ? 0 : mArg.hashCode());
		result = prime * result + ((mName == null) ? 0 : mName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		Argument arg = (Argument) obj;

		if (mArg == null) {
			if (arg.mArg != null)
				return false;
		} else if (!mArg.equals(arg.mArg))
			return false;
		if (mName == null) {
			if (arg.mName != null)
				return false;
		} else if (!mName.equals(arg.mName))
			return false;
		return true;
	}
}
