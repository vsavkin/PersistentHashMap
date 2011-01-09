package com.victorsavkin.grapes.conflict

import spock.lang.Specification

class MergeConflictResolverSpec extends Specification {

	ConflictResolver resolver

	def setup() {
		resolver = new MergeConflictResolver()
	}

	def 'should add elements to the result map if they are added in mine'() {
		setup:
		def original = [key: 'value']
		def mine = original + [key: 'another value']

		expect:
		resolver.resolve(original, mine, original) == mine
	}

	def 'should add elements to the result map in they are added in theirs'() {
		setup:
		def original = [key: 'value']
		def theirs = original + [key: 'another value']

		expect:
		resolver.resolve(original, original, theirs) == theirs
	}

	def 'should delete elements if they are deleted in mine'() {
		setup:
		def original = [key1: 'value1', key2: 'value2']
		def mine = [key1: 'value1']

		expect:
		resolver.resolve(original, mine, original) == mine
	}

	def 'should delete elements if they are deleted in theirs'() {
		setup:
		def original = [key1: 'value1', key2: 'value2']
		def theirs = [key1: 'value1']

		expect:
		resolver.resolve(original, original, theirs) == theirs
	}

	def 'should not delete elements if they are deleted in theirs but they are changed in mine'() {
		setup:
		def original = [key1: 'k1v1', key2: 'k2v1']
		def theirs = [key1: 'k1v1']
		def mine = [key1: 'k1v1', key2: 'k2v2']

		expect:
		resolver.resolve(original, mine, theirs) == mine
	}

	def 'should override values from theirs using values from mine only if it is different from original values'() {
		setup:
		def original = [key1: 'k1v1', key2: 'k2v1']
		def theirs = [key1: 'k1v2', key2: 'k2v2']
		def mine = [key1: 'k1v1', key2: 'k2v3']

		expect:
		resolver.resolve(original, mine, theirs) == [key1: 'k1v2', key2: 'k2v3']
	}
}
