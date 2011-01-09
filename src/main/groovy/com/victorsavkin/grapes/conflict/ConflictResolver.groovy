package com.victorsavkin.grapes.conflict

interface ConflictResolver {
	Map resolve(Map original, Map mine, Map theirs)
}
