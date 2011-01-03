package com.victorsavkin.grapes

import java.util.concurrent.ConcurrentHashMap

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
 */
class PersistentHashMap<K,V> {
	private File file
	@Delegate private Map<K,V> map = new ConcurrentHashMap<K, V>()

	PersistentHashMap(File file) {
		this.file = file
		reread()
	}

	PersistentHashMap(String file) {
		this(new File(file))
	}

	synchronized void flush(){
		file.withObjectOutputStream {stream->
			this.each{k,v->
				stream << new FileStoreEntry(key: k, value: v)
			}
		}
	}

	synchronized void reread(){
		clear()
		try{
			file.withObjectInputStream { stream->
				stream.eachObject {
					this[it.key] = it.value
				}
			}
		}catch(EOFException e){
			createFileIfItDoesntExist()
		}catch(FileNotFoundException e){
			createFileIfItDoesntExist()
		}
	}

	private createFileIfItDoesntExist(){
		file.withObjectOutputStream { }
	}
}

class FileStoreEntry implements Serializable{
	def key
	def value
}