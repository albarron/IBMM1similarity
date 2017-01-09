package es.nlel.cross.ibmm1;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple string-to-integer encoder. It is intended to be used to process
 * integers instead of strings in order to require less space and make comparisons
 * more agile. 
 * 
 * Loot at {@code es.nlel.cross.ibmm1.ProbabilisticDictionary} for an example.
 * 
 * @author alberto
 *
 */
public class StringEncoder {

	/** Stores the mappings from string to integer */
	private final Map<String, Integer> MAPPER_STR2INT;
	
	/** Stores the mappings from integer to string */
	private final Map<Integer, String> MAPPER_INT2STR;
	
	/** Last used index for the encoding to integer */
	private int IDX;

	/** The constructor initialises the data structures and index */
	public StringEncoder() {
		MAPPER_STR2INT = new HashMap<String, Integer>();
		MAPPER_INT2STR = new HashMap<Integer, String>();
		IDX = 0;
	}
	
	/**
	 * Obtains the int code for the given string. If no code exists for it, 
	 * it is added. 
	 * 
	 * @param str
	 * @return
	 * 		Unique numerical code associared to the string
	 */
	public int getCode(String str) {
		if (! MAPPER_STR2INT.containsKey(str)) {
			addString(str);
		}		
		return MAPPER_STR2INT.get(str);
	}
	
	/**
	 * Obtains the string associated to the given code. 
	 * Null if the code does not exist.
	 * 
	 * TODO decide if this should just crash the process
	 * @param code
	 * 			a numerical code
	 * @return
	 * 			the string associated (null if does not exist)
	 */
	public String getString(int code) {	
		return MAPPER_INT2STR.get(code);
	}
	
	/**
	 * Add a new string to the encoder and assigns it a unique numerical value.
	 * @param txt
	 * @return
	 * 			true if the str was added; false if it existed already
	 */
	public boolean addString(String txt) {
		if (! MAPPER_STR2INT.containsKey(txt)) {
			MAPPER_INT2STR.put(IDX, txt);
			MAPPER_STR2INT.put(txt, IDX++);
			return true;
		}
		return false;
	}
	
	
	
}
