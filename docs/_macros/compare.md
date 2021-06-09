---
layout: default
class: Macro
title: compare STRING STRING
summary: Compare two strings by using the String method compareTo. <astring>.compareTo(<bstring>)
---

	static String	_compareHelp  = ${compare;<astring>;<bstring>}";

	public int _compare(String[] args) throws Exception {
		verifyCommand(args, _compareHelp, null, 3, 3);
		int n = args[1].compareTo(args[2]);
		return Integer.signum(n);
	}
