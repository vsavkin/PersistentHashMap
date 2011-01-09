package com.victorsavkin.grapes

import java.util.concurrent.ConcurrentHashMap

import com.victorsavkin.grapes.conflict.RejectConflictResolver
import com.victorsavkin.grapes.conflict.ConflictResolver

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
	private FileExt fileExt
	private conflictResolver
	private shapshot
	
	@Delegate private Map<K, V> map = new ConcurrentHashMap<K, V>()

	PersistentHashMap(File file, ConflictResolver resolver = new RejectConflictResolver()) {
		this.fileExt = new FileExt(file: file)
		this.conflictResolver = resolver
		reread()
	}

	PersistentHashMap(String file, ConflictResolver resolver = new RejectConflictResolver()) {
		this(new File(file))
	}

	synchronized void flush() {
		withLock {
			if (isStateChangedSinceLastRead()){
				resolveConflict()
			}
			writeMap()
		}
	}

	synchronized void reread() {
		withLock {
			readMap()
		}
	}

	void withLock(Closure c){
		fileExt.withLock c
	}


	//--helper methods
	private writeMap(){
		fileExt.withObjectOutputStream {stream ->
			this.each {k, v ->
				stream << new FileStoreEntry(key: k, value: v)
			}
		}
		this.shapshot = clone(map)
	}

	private readMap(){
		map = returnMapFromFile()
		this.shapshot = clone(map)
	}

	private returnMapFromFile(){
		def map = new ConcurrentHashMap()
		fileExt.withObjectInputStream {stream ->
			stream.eachObject {FileStoreEntry e->
				map[e.key] = e.value
			}
		}
		map
	}

	private clone(map){
		def clonedMap = [:] as HashMap
		map.each{k,v->
			clonedMap[k] = v
		}
		clonedMap
	}

	private resolveConflict(){
		def original = shapshot
		def mine = map
		def theirs = returnMapFromFile()
		map = conflictResolver.resolve(original, mine, theirs)
	}
}