package es.nlel.cross.ibmm1;

import java.io.IOException;


public class DictionaryPruner {


	private ProbabilisticDictionary DICTIONARY;
		
//	private final String SRC_TRG_FILE;
//	
//	private final String TRG_SRC_FILE;
	
	
	
	
	public DictionaryPruner(String srcTrgDictionaryFile) throws IOException {
		DICTIONARY = loadDictionary(srcTrgDictionaryFile);		
	}

	private ProbabilisticDictionary loadDictionary(String file) throws IOException {
		ProbabilisticDictionary dictionary = new ProbabilisticDictionary();
		
		
		
		
		return dictionary;
	}
	
	public void prune(String trgSrcDictionary) throws IOException {
		//ProbabilisticDictionary dictionary = loadDictionary(trgSrcDictionary);
		
		
		
	}
	
	
	public ProbabilisticDictionary getDictionary() {
		return DICTIONARY;
	}
	
	
	public static void main(String[] args) throws IOException {
		
		ProbabilisticDictionary pd = new ProbabilisticDictionary();
		pd.load("source dictionary");
		pd.prune(0.01);
		
		ProbabilisticDictionary pd2 = new ProbabilisticDictionary();
		pd2.load("trg dictionary");
		pd.pruneWithTrg2SrcDictionary(pd2);
		
		DictionaryPruner dp = new DictionaryPruner("");
		dp.prune("");
		
	}
	
	
	
	
}
