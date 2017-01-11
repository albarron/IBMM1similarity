package es.nlel.cross.ibmm1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class ProbabilisticDictionary {

	public static final double DEFAULT_PRUNING_THRESHOLD = 0.01;

	private final String SPLIT_CHARACTER = "\t";

	private final StringEncoder ENCODER;

	private int SIZE;
	
	/** Internal map to store all the translations */
	private Map<Integer, Map<Integer, Double>> TRANSLATIONS;
	
	/**
	 * Constructor with fresh data structures and an empty dictionary.  
	 */
	public ProbabilisticDictionary() {
		ENCODER = new StringEncoder();		
		TRANSLATIONS = new TreeMap<Integer, Map<Integer, Double>>();
		SIZE = 0;
	}
	
	/**
	 * Constructor that loads an existing dictionary from a text file. 
	 * The file is expected to have three tab-separated columns:
	 * <ul>
	 * <li /> source
	 * <li /> target
	 * <li /> p(target | source)
	 * </ul>
	 * @param file
	 * 				The input statistical dictionary file
	 * @throws IOException
	 */
	public ProbabilisticDictionary(String file) throws IOException {
		this();
		load(file);
	}
	
	/**
	 * Constructor that loads an existing dictionary from a text file. 
	 * An entry is loaded only if p(trg|src)>threshold.
	 * The file is expected to have three tab-separated columns:
	 * <ul>
	 * <li /> source
	 * <li /> target
	 * <li /> p(target | source)
	 * </ul>
	 * @param file
	 * 				The input statistical dictionary file
	 * @param threshold
	 * 				Minimum probability to consider an instance
	 * @throws IOException
	 */
	public ProbabilisticDictionary(String file, double threshold) throws IOException {
		this();
		load(file, threshold);
	}
	
	
	/**
	 * Adds a new entry to the dictionary.
	 * @param src
	 * 				source text
	 * @param trg
	 * 				target text 
	 * @param prob
	 * 				p(target | source)
	 */
	public void add(String src, String trg, double prob) {
		ENCODER.addString(src);
		ENCODER.addString(trg);

		if (! TRANSLATIONS.containsKey(ENCODER.getCode(src))) {
			TRANSLATIONS.put(ENCODER.getCode(src), new TreeMap<Integer, Double>());
		}
		
		if (! hasPair(src, trg)) {
			// If hasPair=true, This entry existed already. 
			// Just updating the likelihood and no need to increase the size
			SIZE++;
		}
		TRANSLATIONS.get(ENCODER.getCode(src)).put(ENCODER.getCode(trg), prob);
	}

	/**
	 * @param src
	 * @param trg
	 * @return
	 * 			true if the pair (src,trg) exists in the dictionary
	 */
	public boolean hasPair(String src, String trg) {
		return TRANSLATIONS.containsKey(ENCODER.getCode(src)) &&  
			   TRANSLATIONS.get(ENCODER.getCode(src))
			   			.containsKey(ENCODER.getCode(trg));
	}
	
	
	/**
	 * Dumps the current dictionary into a
	 * @param file
	 * @throws IOException
	 */
	public void dump(String file) throws IOException {
		// Dump to a file
		FileOutputStream fos = new FileOutputStream(new File(file));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		for (int i : TRANSLATIONS.keySet()) {
			for (int j  : TRANSLATIONS.get(i).keySet()) {
				bw.write(ENCODER.getString(i));
				bw.write(SPLIT_CHARACTER);
				bw.write(ENCODER.getString(j));
				bw.write(SPLIT_CHARACTER);
				bw.write(TRANSLATIONS.get(i).get(j).toString());
				bw.write("\n");			
			}
		}
		bw.close();
		fos.close();
	}
	
	/**
	 * Loads a dictionary from an input file. It expects a tab-separated file
	 * with three columns:
	 * <ul>
	 * <li /> src
	 * <li /> trg
	 * <li /> p(trg|src)
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
			splLine = line.split(SPLIT_CHARACTER);			
			add(splLine[0], splLine[1], Double.valueOf(splLine[2]));			
		}
		br.close();
		fis.close();
	}
	
	/**
	 *  Loads a dictionary from an input file. An entry is loaded only if 
	 *  p(trg|src)>threshold. It expects a tab-separated file
	 * with three columns:
	 * <ul>
	 * <li /> src
	 * <li /> trg
	 * <li /> p(trg|src)
	 * </ul>
	 * @param file
	 * 				Dictionary input file
	 * @param threshold
	 * 				Minimum probability to consider an instance
	 * @throws IOException
	 */
	public void load(String file, double threshold) throws IOException {
		FileInputStream fis = new FileInputStream(new File(file));		
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		
		String line;
		double prob;
		String[] splLine = new String[3]; 
		while ((line = br.readLine()) != null) {
			splLine = line.split(SPLIT_CHARACTER);
			prob = Double.valueOf(splLine[2]);
			if (prob > threshold) {
				add(splLine[0], splLine[1], prob);
			}
		}
		br.close();
		fis.close();
	}
	
	public void prune() {
		prune(DEFAULT_PRUNING_THRESHOLD);
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
		if (threshold < 0) {
			System.err.println("I cannot handle negative probabilities");
			System.exit(-1);
		}
		if (threshold > 1) {
			System.err.println("I cannot handle non-probabilities");
			System.exit(-1);
		}
		
		
		for (int i : TRANSLATIONS.keySet()) {
			Iterator<Integer> it = TRANSLATIONS.get(i).keySet().iterator();
			
			while (it.hasNext()) {
				if (TRANSLATIONS.get(i).get(it.next()) <= threshold) {
					it.remove();	
					SIZE--;
				}
			}
		}
	}
	
	public void pruneWithTrg2SrcDictionary(ProbabilisticDictionary trgSrcDictionary) {
		for (int i : TRANSLATIONS.keySet()) {
			Iterator<Integer> it = TRANSLATIONS.get(i).keySet().iterator();
			while (it.hasNext()) {
				if (! trgSrcDictionary.hasPair(ENCODER.getString(it.next()), ENCODER.getString(i))) {
					it.remove();
					SIZE--;
				}
			}
		}
	}
	
//	public void prune (ProbabilisticDictionary trgSrcDictionary, double threshold) {
//	// TODO
//}
	
	/**
	 * Removes the entry (src,trg) if it exists. As a side effect, the 
	 * size of the dictionary decreases by 1. If the entry does not exist, 
	 * nothing is modified.
	 * 
	 * @param src
	 * 				source language text
	 * @param trg
	 * 				target language text
	 * @return 
	 * 				true if something was removed
	 */
	public boolean removeEntry(String src, String trg) {
		if (hasPair(src, trg)) {			
			TRANSLATIONS.get(ENCODER.getCode(src)).remove(ENCODER.getCode(trg));
			SIZE--;
			if (TRANSLATIONS.get(ENCODER.getCode(src)).isEmpty()) {
				TRANSLATIONS.remove(ENCODER.getCode(src));
			}
			return true;
		}
		return false;
		
		

	}
		
	/**
	 * Note that if an entry is added twice, this number could be unnacurate.
	 * @return
	 * 			Number of entries in the dictionary
	 */
	public int size() {
		return SIZE;
	}
	
	public double getProbability(String src, String trg) {		
		try {
			return  TRANSLATIONS.get(ENCODER.getCode(src))
								.get(ENCODER.getCode(trg));
		} catch (Exception e ) {
			System.out.println(e);
			return -1.0;
		}
	}

	/* 
	 * Use just for testing purposes. Don't call with a real dictionary!
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		for (int i : TRANSLATIONS.keySet()) {
			for (int j  : TRANSLATIONS.get(i).keySet()) {
				sb.append(ENCODER.getString(i))
				  .append(SPLIT_CHARACTER)
				  .append(ENCODER.getString(j))
				  .append(SPLIT_CHARACTER)
				  .append(TRANSLATIONS.get(i).get(j))
				  .append("\n");			
			}
		}
		return sb.toString();
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
	 
	
	
}
