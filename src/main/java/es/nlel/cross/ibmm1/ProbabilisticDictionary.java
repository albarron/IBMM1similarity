package es.nlel.cross.ibmm1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ProbabilisticDictionary {

	public static final double DEFAULT_PRUNING_THRESHOLD = 0.01;

	private final String FILE_SPLIT_CHARACTER = "\t";

	
	private Map<String, Integer> MAPPER;
	
	private Map<Integer, Map<Integer, Double>> TRANSLATIONS;
	
	private int IDX;
	

	
	public ProbabilisticDictionary() {
		MAPPER = new HashMap<String, Integer>();
		TRANSLATIONS = new HashMap<Integer, Map<Integer, Double>>();
		IDX = 0;		
	}
	
	public ProbabilisticDictionary(String file) throws IOException {
		load(file);
	}
	
	
	public void addEntry(String src, String trg, double prob) {
		updateInternalIndex(src);
		updateInternalIndex(trg);
		if (! TRANSLATIONS.containsKey(MAPPER.get(src))) {
			TRANSLATIONS.put(MAPPER.get(src), new HashMap<Integer, Double>());
		}
		
		TRANSLATIONS.get(MAPPER.get(src)).put(MAPPER.get(trg), prob);
	}
	

	
	public void removeEntry(String src, String trg) {
		if (TRANSLATIONS.containsKey(MAPPER.get(src)) && 
			TRANSLATIONS.get(MAPPER.get(trg)).containsKey(trg)) {
			TRANSLATIONS.get(MAPPER.get(src)).remove(trg);
		}
		if (TRANSLATIONS.get(MAPPER.get(src)).isEmpty()) {
			TRANSLATIONS.remove(MAPPER.get(src));
		}
		
	}
	
	public double getProbability(String src, String trg) {		
		try {
			return  TRANSLATIONS.get(MAPPER.get(src)).get(MAPPER.get(trg));
		} catch (Exception e ) {
			System.out.println(e);
			return -1.0;
		}
	}
	
	
	/**
	 * Remove all the entries from the dictionary if their probability is 
	 * lower than the threshold. Use DEFAULT_PRUNING_THRESHOLD for default
	 * value.
	 * 
	 * @param threshold
	 * 				the given threshold;
	 */
	public void prune(double threshold) {
		
		
		//TODO test if this is working
		for (int i : TRANSLATIONS.keySet()) {
			Iterator<Integer> it = TRANSLATIONS.get(i).keySet().iterator();
			
			while (it.hasNext()) {
				if (TRANSLATIONS.get(i).get(it.next()) <= threshold) {
					it.remove();
				}
			}
			
		}
		
	}
	
	
	public void prune(ProbabilisticDictionary trgSrcDictionary) {
		// TODO
	}
	
	public void prune (ProbabilisticDictionary trgSrcDictionary, double threshold) {
		// TODO
	}
	
	/**
	 * Loads a dictionary from an input file. It expects a tab-separated file
	 * with three columns:
	 * <ul>
	 * <li /> src
	 * <li /> trg
	 * <li /> p(src,trg)	TODO confirm if it's joint probability
	 * </ul>
	 * @param file
	 * 				Dictionary input file
	 * @throws IOException
	 */
	public void load(String file) throws IOException {
		FileInputStream fis = new FileInputStream(new File(file));		
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		
		String line;
		String[] splLine = new String[3]; 
		while ((line = br.readLine()) != null) {
			splLine = line.split(FILE_SPLIT_CHARACTER);			
			addEntry(splLine[0], splLine[1], Double.valueOf(splLine[2]));			
		}
	}
	
	public boolean dump(String file) {
		// Dump to a file
		
		return false;
	}
	
	
//	public Map<String, Double> getProbabilities(String src) {
//		Map<String, Double> probs = new HashMap<String, Double> ();
//		try {
//			for (int i : TRANSLATIONS.get(MAPPER.get(src)).keySet()) {
//				probs.put(MAPPER.get(i), 
//						TRANSLATIONS.get(MAPPER.get(src)).get(i)
//				);
//			}
//		} catch (Exception e) {
//			System.out.println(e);
//			return null;
//		}
//		return probs;
//	}
	
	
//	public Map<String, Double> next() {
//		return null;
//	}
//	
//	public boolean hasNext() {
//		return false;
//	}
//	
//	public void remove() {
//		throw new UnsupportedOperationException();
//	}
	 
	private boolean updateInternalIndex(String txt) {
		if (! MAPPER.containsKey(txt)) {
			MAPPER.put(txt, IDX++);
			return true;
		}
		return false;
	}
	
}
