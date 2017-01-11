package es.nlel.cross.ibmm1;

import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class DictionaryPruner {

	
	private final ProbabilisticDictionary dictionary;
	private final String DICT_FILE;
	private final double threshold;
	private boolean withSecondDictionary;
	
	
	public DictionaryPruner(String srcTrgDictFile, double threshold) throws IOException {
		DICT_FILE = srcTrgDictFile;
		dictionary = new ProbabilisticDictionary(DICT_FILE, threshold);		
		System.out.println("Dictionary loaded and pruned");
		this.threshold = threshold;
		withSecondDictionary = false;		
	}
	
	public DictionaryPruner(String srcTrgDictFile, String trgSrcDictFile, double threshold) throws IOException {
		this(srcTrgDictFile, threshold);
		ProbabilisticDictionary trgSrcDictionary = new ProbabilisticDictionary(trgSrcDictFile, threshold);
		System.out.println("Second dictionary loaded and pruned");
//		trgSrcDictionary.prune(threshold);
		dictionary.pruneWithTrg2SrcDictionary(trgSrcDictionary);
		System.out.println("Dictionary pruned wrt second dictionary");
		withSecondDictionary = true;		
	}
	
	public void dumpTo(String outputFile) throws IOException {
		dictionary.dump(outputFile);
		System.out.println("Dictionary dumped to " + outputFile);
	}
	
	public void dump() throws IOException {
		StringBuffer sb = new StringBuffer(); 
		sb.append(DICT_FILE)
		  .append(".")
		  .append(threshold)
		  .append(".");
		
		if (withSecondDictionary) {
			sb.append("withInv.");
		}
		sb.append("prun");
		dictionary.dump(sb.toString());
		System.out.println("Dictionary dumped to " + sb.toString());
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
						"Theshold to discard instances with lower probabilities (default: %f",
						ProbabilisticDictionary.DEFAULT_PRUNING_THRESHOLD));
		options.addOption("o", "output", true, 
				"Output file (optional)");
		
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

	
	public static String getOutputFile(String input) {
		return String.format("%s.%f.pruned", input, ProbabilisticDictionary.DEFAULT_PRUNING_THRESHOLD);		
	}
	
	public static String getOutputFile(String input, double threshold) {
		return String.format("%s.%f.pruned", input, threshold);		
	}

	public static String getOutputFileWinv(String input) {
		return String.format("%s.%f.wInv.pruned", input, ProbabilisticDictionary.DEFAULT_PRUNING_THRESHOLD);		
	}
	
	public static String getOutputFileWinv(String input, double threshold) {
		return String.format("%s.%f.wInv.pruned", input, threshold);		
	}
	
	public static void main(String[] args) throws IOException {
		CommandLine cLine = parseArguments(args);
		
		String srcTrgDictionary = cLine.getOptionValue("d");
				
		double threshold = cLine.hasOption("t") ?
					Double.valueOf(cLine.getOptionValue("t")) :
					ProbabilisticDictionary.DEFAULT_PRUNING_THRESHOLD;	
		
		DictionaryPruner pruner;
					
		if (cLine.hasOption("e")) {
			pruner = new DictionaryPruner(srcTrgDictionary, cLine.getOptionValue("e"), threshold);
		} else {
			pruner = new DictionaryPruner(srcTrgDictionary, threshold);			
		}
			
		
		if (cLine.hasOption("o")) {
			pruner.dumpTo(cLine.getOptionValue("o"));
		} else {
			pruner.dump();
		}	}
	
	
	
	
}
