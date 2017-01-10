package es.nlel.cross.ibmm1;

import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class DictionaryPruner {

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

	
	

	public static void main(String[] args) throws IOException {
		CommandLine cLine = parseArguments(args);
		
		String srcTrgDictionary = cLine.getOptionValue("d");
		
		//Pruning this dictionary
		ProbabilisticDictionary dictionary = new ProbabilisticDictionary(srcTrgDictionary);
		if (cLine.hasOption("t")) {
			dictionary.prune(Double.valueOf(cLine.getOptionValue("t")));
		} else {
			dictionary.prune();
		}
		
		// Pruning against an inverse dictionary
		if (cLine.hasOption("e")) {			
			String trgSrcDictionary = cLine.getOptionValue("e");
			ProbabilisticDictionary pd2 = new ProbabilisticDictionary(trgSrcDictionary);
			if (cLine.hasOption("t")) {			
				pd2.prune(Double.valueOf(cLine.getOptionValue("t")));
			} else {
				dictionary.prune();					
			}
			dictionary.pruneWithTrg2SrcDictionary(pd2);
		}

		String outputFile = cLine.hasOption("o") ? 
				cLine.getOptionValue("o") : 
				String.format("%s.prun", srcTrgDictionary); 
		
		dictionary.dump(outputFile);		
	}
	
	
	
	
}
