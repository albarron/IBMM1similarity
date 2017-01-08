package es.nlel.ibmm1similarity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.BreakIterator;
import java.util.*;		//Map, HashMap, etc

import org.apfloat.Apfloat;


public class GizaDictTranslator {
	
		static final Apfloat epsilon = new Apfloat(-0.1,NewTranslationCompare.prec);
		
		/**
		 * Translates a Giza++ numerical translation dictionary to a string dictionary based on the 
		 * corresponding vocabularies.
		 * @param sourceDict	source dictionary en-de.t3final (numbers)
		 * @param targetDict	target dictionary en-de.t3final (words)
		 * @param vocE			Language e vocabulary
		 * @param vocF			Language f vocabulary
		 * @throws IOException
		 */
		public static void translateDictionary(String sourceDict, String targetDict, String vocE, String vocF) throws IOException{
			String cL, wordE, wordF, trans_probability, printLine;
			String triplet[] ;
			System.out.println("Loading vocabulary e...");
			Map<String, String> mapE= file2map(vocE);
			System.out.println("Loading vocabulary f...");
			Map<String, String> mapF= file2map(vocF);
			
			mapE.put("0", "###null###");	//Adding the null string 		
			
			System.out.println("Translating dictionary inputs...");
			FileReader fr = new FileReader(sourceDict);
			BufferedReader in = new BufferedReader(fr);
			
			FileOutputStream fout = new FileOutputStream (targetDict);
			PrintStream p = new PrintStream( fout );
			
			while((cL = in.readLine())!= null){
				triplet = splitTriplet(cL);
				wordE = mapE.get(triplet[0]);
				wordF = mapF.get(triplet[1]);
				trans_probability = triplet[2];
				printLine = wordE + " " + wordF + " "+trans_probability;
				p.println(printLine);
			}			
			in.close(); fr.close(); 
			p.close(); fout.close();			
		}
		
		/**
		 * Opens a vocabulary file (such as en_dee.vcb) and creates a map containing the numerical
		 * values and string translations
		 * @param sourceDic		File containing the vocabulary file
		 * @return				a map key=num_id, value= string
		 * @throws IOException
		 */
		
		public static Map<String, String> file2map (String vocFile) throws IOException{
			String line;
			String currentTrans[];
			Map<String, String> fileMap = new HashMap<String, String>();
			try {			
				//FileReader in = new FileReader(sampleFile);
				FileReader fr = new FileReader(vocFile);
				BufferedReader in = new BufferedReader(fr);						
				while((line = in.readLine())!= null){
					currentTrans = splitTriplet(line);
					fileMap.put(currentTrans[0], currentTrans[1]);
				}
				fr.close();			in.close();			
			} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();			
			}		
			return fileMap;
		}
		
		/**
		 * Given a triplet of the kind number_id string_word frequency (the one in the vocabularies files
		 * generated by Giza, returns an array with [number_id, string_word]
		 * @param triplet	the current triplet in the dictionary
		 * @return			an array containing [number_id, string_word]
		 */
		private static String[] splitTriplet (String triplet){
			String[] splTriplet = triplet.split(" ");		
			return splTriplet;		
		}
		
		
		/**
		 * 	Opens a dictionary file and charges a Map dictionary.
		 * @param sourceFile File containing a dictionary of the type word_e word_f prob
		 * @return			 A nested map relating source and target words with the associated probability
		 * @throws IOException
		 */
		public static Map<String, HashMap<String, Apfloat>> chargeDictionary(File dictFile) throws IOException{
			String[] t;
			Apfloat p;
		
			String text = FileIO.fileToString(dictFile);
			Map<String, HashMap<String, Apfloat>> dMap = new HashMap<String, HashMap<String, Apfloat>>();
			for(String line : text.split("\n")) {
				t = line.split(" ");
				p = new Apfloat(Double.parseDouble(t[2]),NewTranslationCompare.prec);
				insertIntoMap(dMap,t[0],t[1], p);			
			}
			return dMap;			
		}
	
		/**
		 * Insert a new words pair with the associated probability.
		 * @param cMap	Current dictionary
		 * @param ffT	Current e word
		 * @param kkF	Current f word
		 * @param pp	Probability associated to the pair kkE kkF
		 */
		private static void insertIntoMap (Map<String, HashMap<String, Apfloat>> cMap, String ffT, String kkF, Apfloat pp){
			HashMap<String, Apfloat> subMap= new HashMap<String, Apfloat>();
			if (cMap.containsKey(ffT))
				subMap= cMap.get(ffT);
			subMap.put(kkF, pp);
			cMap.put(ffT, subMap);
		}
		
		/**
		 * Charges the vocabulary of the textFile into a map. 
		 * @param textFile An input text file containing text in any language (previously tokenized)
		 * @return			A map containing the vocabulary of the file
		 * @throws IOException
		 */
		public static HashSet<String> chargeTextFile(File textFile) throws IOException{
			String text = FileIO.fileToString(textFile);
			BreakIterator boundary = BreakIterator.getWordInstance();
			HashSet<String> tokenList = new HashSet<String>(5000,1.0f);		
			boundary.setText(text);

	        int start = boundary.first();
	        //String part;
	        for (int end = boundary.next();
	             end != BreakIterator.DONE;
	             start = end, end = boundary.next()) {
	        	//part = text.substring(start,end);
	        	tokenList.add(text.substring(start, end));		//no ignora números ni puntos
	        	//if(end-start>1 && !part.matches(".*[0-9].*"))
	        	//	tokenList.add(part);
	        }

			return tokenList;
		}
		
		
/*		public static Apfloat calculateSimilarity(Map<String, HashMap<String, Apfloat>> dictionary, 
									HashSet<String> srcTokens, 
									HashMap<String, HashSet<File>> trgTokens){
			Apfloat similarity = new Apfloat(1.0,NewTranslationCompare.prec);
			Apfloat sum;			
			for(String trg : trgTokens.keySet()) {
				sum = new Apfloat(0.0,NewTranslationCompare.prec);
				HashMap<String, Apfloat> translations = dictionary.get(trg);
				if(translations!=null) {
					for(String key : translations.keySet()) {
						if(srcTokens.contains(key))
							sum = ApfloatMath.sum(sum,translations.get(key));
					}
				}
				
				//if (sum.compareTo(epsilon)==1)
				if (sum.compareTo(new Apfloat(0,NewTranslationCompare.prec))!=0)
					similarity = ApfloatMath.sum(similarity, sum);
					//similarity = ApfloatMath.product(similarity,sum);
				else
					//similarity = ApfloatMath.product(similarity,epsilon);
					similarity = ApfloatMath.sum(similarity, epsilon);
			}
			return similarity;			
		}*/
		
/*		private static Apfloat getPairProbability(Map<String, HashMap<String, Apfloat>> d, String s, String t){
			Apfloat p = new Apfloat(0.0,NewTranslationCompare.prec);
			if (d.containsKey(s)){
				HashMap <String, Apfloat> sMap = d.get(s);	
				if (sMap.containsKey(t))
					p = sMap.get(t);
			}
			return p;
		}*/
		
		public static void main (String[] args) throws IOException{
			String path ="/home/lbarron/plagio/crosslingual/dictLearning/corporaJCRNUEVO/xx-en/MT/";
			translateDictionary(path+"en-en.t3.final", path+"dictionary-en-en.dict", 
					path+"en-ene.vcb", path+"en-enf.vcb");
			//String path ="/home/lbarron/tempoEDA/MT/";
			//translateDictionary(path+"en-es.t3.final", path+"dictionary-en-es.dict", 
			//					path+"en-ese.vcb", path+"en-esf.vcb");
						
			
		}
		
}