package com.victorsavkin.grapes.conflict

import com.victorsavkin.grapes.conflict.ConflictResolver
import com.victorsavkin.grapes.PersistentHashMapException

class RejectConflictResolver implements ConflictResolver {
	
	Map resolve(Map original, Map mine, Map theirs) {
		throw new PersistentHashMapException()
	}
}
