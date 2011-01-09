package com.victorsavkin.grapes

import java.security.MessageDigest

class FileExt {
	File file
	private md5

	void withLock(Closure closure){
		def random = null
		def lock = null
		try{
			random = new RandomAccessFile(file, "rw")
			lock = random.channel.lock()
			closure.delegate = this
			closure()
		}finally{
			lock?.release()
			random?.close()
		}
	}

	void withObjectOutputStream(Closure c){
		file.withObjectOutputStream c
		md5 = calculateMD5()
	}

	void withObjectInputStream (Closure c){
		try {
			file.withObjectInputStream c
		} catch (EOFException e) {
			createFileIfItDoesntExist(file)
		} catch (FileNotFoundException e) {
			createFileIfItDoesntExist(file)
		}
		md5 = calculateMD5()
	}

	private createFileIfItDoesntExist(file) {
		file.withObjectOutputStream { }
	}

	boolean isStateChangedSinceLastRead(){
		md5 != calculateMD5()
	}

	private calculateMD5(){
		def digest = MessageDigest.getInstance("MD5")
		digest.update(file.text.bytes)
		def big = new BigInteger(1, digest.digest())
		big.toString(16).padLeft(32,"0")
	}
}
