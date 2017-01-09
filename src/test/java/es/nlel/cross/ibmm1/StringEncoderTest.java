package es.nlel.cross.ibmm1;

import junit.framework.Assert;
import junit.framework.TestCase;

public class StringEncoderTest extends TestCase {
	
	private StringEncoder encoder;

	protected void setUp() throws Exception {
		super.setUp();
		encoder = new StringEncoder();
		encoder.addString("zero");
		encoder.addString("one");
		encoder.addString("two");		
	}

	public void testGetCode() {
		Assert.assertEquals("The code for one should be 1", 
				1, encoder.getCode("one"));		
	}

	public void testGetString() {
		Assert.assertEquals("The string for code 2 should be 'two'", 
				"two", encoder.getString(2));
		Assert.assertNull("Null as code 5 does not exist", 
				encoder.getString(5));
	}

	public void testAddString() {
		Assert.assertTrue("Adding a new entry should return true", 
				encoder.addString("three"));
		Assert.assertFalse("Adding an existing entry should return false", 
				encoder.addString("zero"));
	}

}
