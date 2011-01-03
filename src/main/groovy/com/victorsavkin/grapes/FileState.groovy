package com.victorsavkin.grapes

import java.security.MessageDigest

class FileState {
	File file
	private state

	void saveState(){
		state = calculateMD5()
	}

	boolean isStateChanged(){
		state != calculateMD5()
	}

	private calculateMD5(){
		def digest = MessageDigest.getInstance("MD5")
		digest.update(file.text.bytes)
		def big = new BigInteger(1, digest.digest())
		big.toString(16).padLeft(32,"0")
	}
}
