package es.nlel.ibmm1similarity;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;


public class TranslationCompareBasque implements Runnable {

	//private final static String basePath = "C:\\Users\\Andreas\\Desktop\\samples\\";
	private final static String basePath = "/corpus/currentWork/euskaraMAL100212/eseu/Consumer";
	//private final static String objPath = "C:\\Users\\Andreas\\Desktop\\samples\\objects\\";
	private final static String objPath  = "/corpus/currentWork/euskaraMAL100212/eseu/Consumer/wik_objects1000/";

	private String currentLang;

	private static HashMap<String, HashSet<String>> sourceVoc;
	private static HashMap<String, Double> sourceLens;

	private Map<String, HashMap<String, Apfloat>> dictionary;

	public final static int prec = 25;//1000000;
	public final static Apfloat epsilon = new Apfloat(-0.1,TranslationCompareBasque.prec);

	private static HashMap<String,Double> mu = new HashMap<String, Double>();
	private static HashMap<String,Double> sigma = new HashMap<String, Double>();

	static {
		Class<TranslationCompareBasque> c = TranslationCompareBasque.class;
		PropertyConfigurator.configure(c.getResource("/translation_log4j.properties"));
		
		mu.put("es", 0.9042);
		mu.put("xx", 1.0);			//xx is English as target

		
		sigma.put("es", 0.27);
		sigma.put("xx", 1.0);		//In fact, it's 0, but it produces n/0 division
	}

	protected static Logger log = Logger.getLogger("translation");

	public TranslationCompareBasque(String lang) throws IOException, ClassNotFoundException {
		File dictFile = new File(basePath+lang+"-en/DICT/dictionary-"+lang+"-en.40.dict");
		//File dictFile = new File(basePath+"dics/dictionary-"+lang+"-en.40.dict");
		this.currentLang=lang;
		log.debug("Loading dictionary");
		this.dictionary = GizaDictTranslator.chargeDictionary(dictFile);
	}

	public boolean checkHashSet(HashSet<String> files, String file) {
		if(files==null) return false;

		for(String s : files)		//s <- en ;  file <- de			
			if(s.equals(file)) return true; 
		return false;
	}

	public HashMap<String,Apfloat> getSimilarity(String targetFile) throws IOException {
		HashSet<String> targetVoc = GizaDictTranslator.chargeTextFile(new File(targetFile));
		HashMap<String, Apfloat> translations = new HashMap<String, Apfloat>();
		HashMap<String, Apfloat> results = new HashMap<String, Apfloat>(11000,1.0f);
		Apfloat transValue;
		for(String word : targetVoc) {
			translations = dictionary.get(word);		//Obtains the possible translations of the word
			if(translations!=null) {
				for(String wordTrans : translations.keySet()) {
					if(sourceVoc.containsKey(wordTrans)) {
						transValue = translations.get(wordTrans); 
						for(String file : sourceVoc.get(wordTrans)) {
							if(results.containsKey(file))
								results.put(file, ApfloatMath.sum(results.get(file),transValue));
							else
								results.put(file, transValue);
						}
					}	
				}
			}			
		}
		return results;
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, HashSet<String>> getVocabulary(File objFile, File srcFolder) 
	throws IOException, ClassNotFoundException{
		if (objFile.exists()) {
			log.debug("Loading voc data from "+objFile.getName());
			return (HashMap<String, HashSet<String>>) FileIO.readObject(objFile);
		} else {
			log.debug("Generating voc data... ");
			objFile.getParentFile().mkdirs();
			List<String> files = FileIO.getFilesRecursively(srcFolder, ".txt");
			HashMap<String, HashSet<String>> m = getFilesVocabulary(files);
			FileIO.writeObject(m, objFile);
			return m;
		}		
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, Double> getLengths(File objFile, File srcFolder) 
	throws IOException, ClassNotFoundException{
		if (objFile.exists()) {
			log.debug("Loading len data from "+objFile.getName());
			return (HashMap<String, Double>) FileIO.readObject(objFile);
		} else {
			log.debug("Generating len data... ");
			objFile.getParentFile().mkdirs();
			List<String> files = FileIO.getFilesRecursively(srcFolder, ".txt");
			HashMap<String, Double> m = getFilesLen(files);
			FileIO.writeObject(m, objFile);
			return m;
		}		
	}


	public static HashMap<String, HashSet<String>> getFilesVocabulary(List<String> fileList) throws IOException{
		HashMap<String,HashSet<String>> resultMap = new HashMap<String, HashSet<String>>();

		HashSet<String> fileVoc = new HashSet<String>();
		HashSet<String> newFilelist = new HashSet<String>();		
		for(String s : fileList) {
			fileVoc = GizaDictTranslator.chargeTextFile(new File(s));
			log.debug(s);
			for (String word : fileVoc) {
				if(resultMap.containsKey(word))
					newFilelist = resultMap.get(word);	
				else
					newFilelist = new HashSet<String>();

				newFilelist.add(s);
				resultMap.put(word, newFilelist);
			}
		}		
		return resultMap;
	}

	public static HashMap<String, Double> getFilesLen(List<String> fileList) throws IOException{
		HashMap<String,Double> resultMap = new HashMap<String, Double>();
		double l;		
		for(String s : fileList) {
			l = LengthModel.count_characters(new File(s));
			resultMap.put(s, l);
		}		
		return resultMap;
	}

	private static String formatLine(Apfloat s, String src, String trg){
		StringBuffer result = new StringBuffer(s.toString());
		result.append(" ");
		result.append(src.substring(src.lastIndexOf(FileIO.separator)+1));
		result.append(" ");
		result.append(trg.substring(trg.lastIndexOf(FileIO.separator)+1));
		result.append("\n");
		return result.toString();
	}

	public Apfloat calcLenFactor(Double sLen, Double tLen, Double m, Double s){
		double lf = LengthModel.lengthFactor(sLen, tLen, m, s);
		//double lf = LengthModel.lengthFactor(sFile, tFile, m, s);
		Apfloat alf = new Apfloat(lf,TranslationCompareBasque.prec);
		return alf;//ApfloatMath.product(alf, simil);
	}

	public void run() {
		try {
			Apfloat lenSimilarity, lf;
			String outFile, lenFile;
			HashMap<String, Apfloat> simMap;

			//List<String> targetFiles = FileIO.getFilesRecursively(new File(basePath+currentLang), ".txt");
			List<String> targetFiles = FileIO.getFilesRecursively(new File(basePath+"test/wik_"+currentLang+"/tok"), ".txt");
			HashMap<String, Double> targetLens = getLengths(new File(objPath+currentLang+"_len.object"), 
					new File(basePath+"test/wik_"+currentLang+"/tok"));

			long totalLength=0;
			for(String s : sourceLens.keySet())
				totalLength+=sourceLens.get(s);

			long countLength = 0;

			long start = System.currentTimeMillis();

			for(String t : targetFiles) {
				log.debug(t);				
				simMap = this.getSimilarity(t);
				StringBuffer resultOut = new StringBuffer();
				StringBuffer resultLen = new StringBuffer();

				for(String s : simMap.keySet()) {
					lf = this.calcLenFactor(sourceLens.get(s), targetLens.get(t), mu.get(currentLang), sigma.get(currentLang));
					lenSimilarity =ApfloatMath.product(lf, simMap.get(s));
					resultOut.append(formatLine(simMap.get(s), s, t));
					resultLen.append(formatLine(lenSimilarity, s, t));					
				}
				//outFile = basePath+"general_"+currentLang+".out";
				//lenFile = basePath+"general_"+currentLang+".lf.out";
				outFile = basePath+"test/wik_out1000/"+currentLang+"/trans.KK.out";
				lenFile = basePath+"test/wik_out1000/"+currentLang+"/trans.KK.lf.out";
				FileIO.stringToFile(new File(outFile), resultOut.toString(),true);
				FileIO.stringToFile(new File(lenFile), resultLen.toString(),true);

				countLength += targetLens.get(t);
				Calendar cal = new GregorianCalendar();
				cal.add(Calendar.SECOND, (int)((double)countLength/(double)(System.currentTimeMillis()-start)*(double)totalLength/1000.0));
				SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy HH:mm");
				String date = sdf.format(cal.getTime());

				log.debug("Estimated end: " + date);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main (String[] args) throws Exception{
		List<String> langs = Arrays.asList("de");//,"es","fr","nl","pl");
		//List<String> langs = Arrays.asList("xx");	//List<String> langs = Arrays.asList("de");
		//xx is English as target
		//String sourcePath =basePath+"en/minitok";//tok1000";			
		String sourcePath =basePath+"test/wik_en/tok1000";//tok1000";

		sourceLens = getLengths(new File(objPath+"en_len.object"), 
				new File(sourcePath));
		sourceVoc  = getVocabulary(new File(objPath+"en_vocabulary.object"),
				new File(sourcePath));

		ArrayList<Thread> proc = new ArrayList<Thread>();
		for(String currentLang : langs) {
			Thread thread = new Thread(new TranslationCompareBasque(currentLang),currentLang);
			thread.start();
			proc.add(thread);
		}

		ArrayList<String> running = new ArrayList<String>();
		do {
			running = new ArrayList<String>();
			for(Thread x : proc)
				if(x.isAlive())
					running.add(x.getName());
				else
					System.out.println("Thread finished: " + x.getName());
			Thread.sleep(10000);
		} while(running.size()>0);
	}
}

