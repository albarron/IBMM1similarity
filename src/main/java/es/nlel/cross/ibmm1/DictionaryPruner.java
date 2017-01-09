package es.nlel.cross.ibmm1;

import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


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
	
	private static CommandLine parseArguments(String[] args) {
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cLine = null;
		Options options = new Options();
		CommandLineParser parser = new BasicParser();
		
		options.addOption("d", "dictionary", true,
				"File with the dictionary to be pruned (src-trg)");
		options.addOption("e", "dictionary2", true,
				"Second dictionary to pruen from (trg-src) [optional]");
		options.addOption("t", "threshold", true, 
				String.format(
						"Theshold to discard instances with lower probabilities (default: %d",
						ProbabilisticDictionary.DEFAULT_PRUNING_THRESHOLD));
		
		try {
			cLine = parser.parse(options, args);
		} catch (ParseException exp) {
			System.err.println("Unexpected exception: " + exp.getMessage());
		}
		
		if (cLine == null || !(cLine.hasOption("d"))) {
			System.err.println("Please, set the input dictionary");
			formatter.printHelp(DictionaryPruner.class.getSimpleName(), options);
			System.exit(-1);
		}
		
		if (cLine.hasOption("h")) {
			formatter.printHelp(DictionaryPruner.class.getSimpleName(), options);
			System.exit(0);
		}
		return cLine;
	}
	
	public static void main(String[] args) throws IOException {
		CommandLine cLine = parseArguments(args);
		
		ProbabilisticDictionary dictionary = new ProbabilisticDictionary(cLine.getOptionValue("d"));
		
		if (cLine.hasOption("e")) {
			//TODO this is repetitive. Use some functions and fix it
			ProbabilisticDictionary pd2 = new ProbabilisticDictionary(cLine.getOptionValue("e"));
			if (cLine.hasOption("t")) {
				pd2.prune(Double.valueOf(cLine.getOptionValue("t")));
				dictionary.prune(Double.valueOf(cLine.getOptionValue("t")));
			} else {
				pd2.prune();
				dictionary.prune();
			}
			dictionary.pruneWithTrg2SrcDictionary(pd2);
			
			
			
		} else { 
			if (cLine.hasOption("t")) {
				dictionary.prune(Double.valueOf(cLine.getOptionValue("t")));
			} else {
				dictionary.prune();
			}
			
		}
				
		//TODO at the end dump the dictionary
		
	}
	
	
	
	
}
