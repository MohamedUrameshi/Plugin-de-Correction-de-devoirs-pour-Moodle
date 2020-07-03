package fr.up5.miage.configuration;

import org.junit.Assert;
import org.junit.Test;

/**
 * This test cases is used to test the ModelValue class.
 */
public class ModelValueTest{

	/**
	 * Test the constructor and the getter for a ModelValue object composed by one value
	 */
	@Test
	public void modelValueOneValueTest(){
		ModelValue mdv = new ModelValue(50f);
		Assert.assertEquals(mdv.getFirstValue(), new Float(50));
	}

	/**
	 *  Test the constructor and the getter for a ModelValue object composed by two value
	 */
	@Test
	public void modelValueTwoValueTest(){
		ModelValue mdv = new ModelValue(3f, 4f);
		Assert.assertEquals(mdv.getFirstValue(), new Float(3));
		Assert.assertEquals(mdv.getSecondValue(), new Float(4));
	}

	/**
	 *  Test the constructor and the getter for a ModelValue object composed by three value
	 */
	@Test
	public void modelValueThreeValueTest(){
		ModelValue mdv = new ModelValue(3f, 4f, 2f);
		Assert.assertEquals(mdv.getFirstValue(), new Float(3));
		Assert.assertEquals(mdv.getSecondValue(), new Float(4));
		Assert.assertEquals(mdv.getThirdValue(), new Float(2));
	}
}
