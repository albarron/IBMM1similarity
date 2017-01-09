package es.nlel.cross.ibmm1;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ProbabilisticDictionaryTest extends TestCase {


	private ProbabilisticDictionary dictionary;
	
	protected void setUp() throws Exception {
		super.setUp();
		dictionary = new ProbabilisticDictionary();
		dictionary.add("hello", "hola", 0.65);
		dictionary.add("hi", "adios", 0.35);
		dictionary.add("low", "baja", 0.001);
	}

//	protected void tearDown() throws Exception {
//		super.tearDown();
//	}
	
	public void testSize() {		
		Assert.assertEquals("The dictionary should have 3 entries", 
				3, dictionary.size());
	}
	
	public void testAddEntry() {
		dictionary.add("low", "bajo", 0.5);
		Assert.assertEquals("After adding one entry, the dictionary should have 4 entries", 
				4, dictionary.size());
	}

	public void testRemoveEntry() {
		dictionary.removeEntry("low", "bajo");
		Assert.assertEquals("No match. No change should occur", 
				3, dictionary.size());
		
		dictionary.removeEntry("low", "baja");
		Assert.assertEquals("After removing one entry, the dictionary should 2 entries", 
				2, dictionary.size());
	}

	public void testGetProbability() {
		Assert.assertEquals("p(hi, adios) should be 0.35", 
				0.35, dictionary.getProbability("hi", "adios"));
	}

	public void testPruneDouble() {		
		dictionary.prune(ProbabilisticDictionary.DEFAULT_PRUNING_THRESHOLD);
//		System.out.println(dictionary);
		Assert.assertEquals("After prunning, the dictionary should have 2 entries", 
				2, dictionary.size());
	}

	public void testPruneProbabilisticDictionary() {
		ProbabilisticDictionary d2 = new ProbabilisticDictionary();
		d2.add("hola", "hello", 0.5);
		d2.add("hey", "hi", 0.3);
		d2.add("baja", "low", 0.6);
		dictionary.pruneWithTrg2SrcDictionary(d2);
		
		System.out.println(dictionary);
		Assert.assertEquals("After prunning, the dictionary should have 1 entry", 
				2, dictionary.size());
	}

	public void testHasPair() {
		Assert.assertTrue("(hello,hola) do exist", dictionary.hasPair("hello", "hola"));
		Assert.assertFalse("(hello,hey) does not exist", dictionary.hasPair("hello", "hey"));
	}

}
