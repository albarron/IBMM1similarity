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


public class TranslationCompPenal implements Runnable {

	
	//private final static String basePath = "/home/lbarron/plagio/crosslingual/dictLearning/corporaJCRNUEVO/";
	//private final static String objPath  = "/home/lbarron/plagio/crosslingual/dictLearning/corporaJCRNUEVO/objects/";
	private final static String basePath = "/home/lbarron/plagio/crosslingual/dictLearning/corporaJCRNUEVO/";
	private final static String objPath  = "/home/lbarron/plagio/crosslingual/dictLearning/corporaJCRNUEVO/objects/";
	
	private String currentLang;

	private static HashMap<String, HashSet<String>> sourceVoc;
	private static HashMap<String, Double> sourceLens;

	private Map<String, HashMap<String, Apfloat>> dictionary;

	public final static int prec = 25;//1000000;
	public final static Apfloat epsilon = new Apfloat(-0.1,TranslationCompPenal.prec);

	private static HashMap<String,Double> mu = new HashMap<String, Double>();
	private static HashMap<String,Double> sigma = new HashMap<String, Double>();

	static {
		Class<TranslationCompPenal> c = TranslationCompPenal.class;
		PropertyConfigurator.configure(c.getResource("/translation_log4j.properties"));
		mu.put("de", 1.0898);
		mu.put("fr", 1.0934);
		mu.put("nl", 1.1437);
		mu.put("pl", 1.2165);
		mu.put("es", 1.1385);
		mu.put("xx", 1.0);			//xx is English as target

		sigma.put("de", 0.2682);
		sigma.put("fr", 0.1573);
		sigma.put("nl", 1.8852);
		sigma.put("pl", 6.3994);
		sigma.put("es", 0.6314);
		sigma.put("xx", 1.0);		//In fact, it's 0, but it produces n/0 division
	}

	protected static Logger log = Logger.getLogger("translation");

	public TranslationCompPenal(String lang) throws IOException, ClassNotFoundException {
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
		Apfloat penelizacionTotal = epsilon.multiply(new Apfloat(targetVoc.size(),prec));
		
		for(String file : sourceLens.keySet())
			results.put(file, penelizacionTotal);
		
		Apfloat transValue;
		HashSet<String> found = new HashSet<String>();
		for(String word : targetVoc) {
			found.clear();
			translations = dictionary.get(word);		//Obtains the possible translations of the word
			if(translations!=null) {
				for(String wordTrans : translations.keySet()) {
					if(sourceVoc.containsKey(wordTrans)) {
						transValue = translations.get(wordTrans); 
						for(String file : sourceVoc.get(wordTrans)) {
							if(!found.contains(file)) {
								results.put(file, results.get(file).subtract(epsilon).add(transValue));
								found.add(file);
							} else
								results.put(file, results.get(file).add(transValue));
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
		Apfloat alf = new Apfloat(lf,TranslationCompPenal.prec);
		return alf;//ApfloatMath.product(alf, simil);
	}

	public void run() {
		try {
			Apfloat lenSimilarity, lf;
			String outFile, lenFile;
			HashMap<String, Apfloat> simMap;

			//List<String> targetFiles = FileIO.getFilesRecursively(new File(basePath+"test/"+currentLang+"/tok"), ".txt");
			//HashMap<String, Double> targetLens = getLengths(new File(objPath+currentLang+"_len.object"), 
			//		new File(basePath+"test/"+currentLang+"/tok"));
			List<String> targetFiles = FileIO.getFilesRecursively(new File(basePath+"test/"+currentLang+"/tok"), ".txt");
			HashMap<String, Double> targetLens = getLengths(new File(objPath+currentLang+"_len.object"), 
					new File(basePath+"test/"+currentLang+"/tok"));
			
			long totalLength=0;
			for(String s : targetLens.keySet())
				totalLength+=targetLens.get(s);

			long countLength = 0;

			long start = System.currentTimeMillis();

			for(String t : targetFiles) {
				log.debug(t);				
				simMap = this.getSimilarity(t);
				StringBuffer resultOut = new StringBuffer();
				StringBuffer resultLen = new StringBuffer();

				for(String s : simMap.keySet()) {
					lf = this.calcLenFactor(sourceLens.get(s), targetLens.get(t), mu.get(currentLang), sigma.get(currentLang));
					lenSimilarity = simMap.get(s).multiply(lf);
					resultOut.append(formatLine(simMap.get(s), s, t));
					resultLen.append(formatLine(lenSimilarity, s, t));					
				}
				//outFile = basePath+"general_"+currentLang+".out";
				//lenFile = basePath+"general_"+currentLang+".lf.out";
				outFile = basePath+"test/pen_out1000/"+currentLang+"/trans.40.out";
				lenFile = basePath+"test/pen_out1000/"+currentLang+"/trans.40.lf.out";
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
		List<String> langs = Arrays.asList("de","es","fr","nl","pl","xx");
		//List<String> langs = Arrays.asList("fr");
		String sourcePath =basePath+"test/en/tok1000";//tok1000";
		//String sourcePath =basePath+"en";			

		sourceLens = getLengths(new File(objPath+"en_len.object"), 
				new File(sourcePath));
		sourceVoc  = getVocabulary(new File(objPath+"en_vocabulary.object"),
				new File(sourcePath));

		ArrayList<Thread> proc = new ArrayList<Thread>();
		for(String currentLang : langs) {
			Thread thread = new Thread(new TranslationCompPenal(currentLang),currentLang);
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

