package com.victorsavkin.grapes

import spock.lang.Specification

class PersistentHashMapSpec extends Specification{
	File file

	def setup(){
		file = File.createTempFile('fileStoreSpec', 'store')
	}

	def cleanup(){
		file.delete()
	}
	
	def 'should save strings'(){
		setup:
		def map = new PersistentHashMap(file)

		when:
		map['mystring'] = 'value'

		then:
		map['mystring'] == 'value'
	}

	def 'should return the size'(){
		setup:
		def map = new PersistentHashMap(file)

		when:
		map['mystring'] = 'value'

		then:
		map.size() == 1
	}

	def 'should flush state to a file after flushing'(){
		setup:
		def map1 = new PersistentHashMap(file)
		map1['key'] = 'value'
		map1.flush()

		when: 'open another store'
		def map2 = new PersistentHashMap(file)

		then: 'records should be there'
		map1['key'] == 'value'
		map2['key'] == 'value'
	}

	def 'should reread the state from a file'(){
		setup:
		def map1 = new PersistentHashMap(file)
		def map2 = new PersistentHashMap(file)

		when:
		map1['key'] = 'value'
		map1.flush()

		then:
		!map2.containsKey('key')

		when:
		map2.reread()

		then:
		map2['key'] == 'value'
	}

	def 'should delete a record from a file'(){
		setup:
		def map1 = new PersistentHashMap(file)
		map1['key'] = 'value'
		map1.flush()

		map1.remove('key')
		map1.flush()

		when: 'open another store'
		def map2 = new PersistentHashMap(file)

		then: 'records should be there'
		!map2.containsKey('key1')
	}

	def 'should throw an exception if files was changed by external process'(){
		setup:
		def map1 = new PersistentHashMap(file)
		def map2 = new PersistentHashMap(file)
		map2['external_key'] = 'value'
		map2.flush()

		when:
		map1['key'] = 'value'
		map1.flush()

		then:
		thrown(PersistentHashMapException)
	}
}
