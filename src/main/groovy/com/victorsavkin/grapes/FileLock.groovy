package com.victorsavkin.grapes

class FileLock {
	File file

	void withLock(Closure closure){
		def random = null
		def lock = null
		try{
			random = new RandomAccessFile(file, "rw")
			lock = random.channel.lock()
			closure(file)
		}finally{
			lock?.release()
			random?.close()
		}
	}
}
