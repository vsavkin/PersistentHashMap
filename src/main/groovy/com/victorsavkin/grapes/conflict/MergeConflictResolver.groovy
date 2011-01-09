package com.victorsavkin.grapes.conflict

class MergeConflictResolver implements ConflictResolver {
	
	Map resolve(Map original, Map mine, Map theirs) {
		def res = theirs
		addNewValuesFromMine mine, original, res
		removeValuesThatWereDeletedInMine res, mine
		res
	}

	private addNewValuesFromMine(Map mine, Map original, res) {
		mine.each {k, v ->
			if (v != original[k]) {
				res[k] = v
			}
		}
	}

	private removeValuesThatWereDeletedInMine(Map res, Map mine) {
		(res - mine).each {k, v ->
			if (!mine.containsKey(k)) {
				res.remove k
			}
		}
	}
}
