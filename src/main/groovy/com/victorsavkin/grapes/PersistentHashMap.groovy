package com.victorsavkin.grapes

import java.util.concurrent.ConcurrentHashMap
import java.security.MessageDigest

/**
 * PersistentHashMap implements Map interface and adds two additional operations to it.
 * reread() - reads a hash map from a file.
 * flush() - saves a hash map to a file.
 *
 * All operations are thread safe.
 *
 * Basic use case: 
 * def map = new PersistentHashMap(file)
 * map['key'] = 'value'
 * map.flush()
 *
 * It uses objectStream under the hood. As a result all the objects must implement
 * Serializable to be saved.
 *
 * If an external process changes the file flush will throw an exception.
 */
class PersistentHashMap<K, V> {
	private fileLock
	private fileState
	private md5
	@Delegate private Map<K, V> map = new ConcurrentHashMap<K, V>()

	PersistentHashMap(File file) {
		this.fileLock = new FileLock(file: file)
		this.fileState = new FileState(file: file)
		reread()
	}

	PersistentHashMap(String file) {
		this(new File(file))
	}

	synchronized void flush() {
		if (isFileStateChanged())
			throw new PersistentHashMapException()

		fileLock.withLock {file ->
			file.withObjectOutputStream {stream ->
				this.each {k, v ->
					stream << new FileStoreEntry(key: k, value: v)
				}
			}
			saveFileState()
		}
	}

	synchronized void reread() {
		fileLock.withLock {file ->
			this.clear()
			try {
				file.withObjectInputStream { stream ->
					stream.eachObject {
						this[it.key] = it.value
					}
				}
			} catch (EOFException e) {
				createFileIfItDoesntExist(file)
			} catch (FileNotFoundException e) {
				createFileIfItDoesntExist(file)
			}
			saveFileState()
		}
	}

	private isFileStateChanged(){
		fileState.isStateChanged()
	}

	private saveFileState(){
		fileState.saveState()
	}

	private createFileIfItDoesntExist(file) {
		file.withObjectOutputStream { }
	}
}

class FileStoreEntry implements Serializable {
	def key
	def value
}