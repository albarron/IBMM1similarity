package es.nlel.ibmm1similarity;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;




public class ExtractResults {
	private final static String basePath = "/home/lbarron/plagio/crosslingual/dictLearning/corporaJCRNUEVO/test/out1000/";	
	
	static Integer[] integerArray;

	static {
	  integerArray= new Integer[] {
	    new Integer(40),
	    new Integer(60),
	    new Integer(80),
	    new Integer(95),
	    };
	}
	/*
	public static void generateOutput(String srcDir, String trgDir) throws IOException{
		List<String> files = FileIO.getFilesRecursively(new File(srcDir), ".out");
		for (String f : files){
			if (f.substring(9,11) == "lf")
				createOutFiles ( new File(f), trgDir + "tm-lf");
			else
				createOutFiles ( new File(f), trgDir + "tm");
			
			
		}
		
	}
	
	public static void createOutFiles(File src, String trg) throws IOException{
		Apfloat similarity;
		String[] splLine;
		String srcIdentifier, trgIdentifier; 
		
		String file = FileIO.fileToString(src);
		Map<String, HashMap<Apfloat, List<String>>> globalMap = new HashMap<String, HashMap<Apfloat, List<String>>>();
		
		List<String> files = FileIO.getFilesRecursively(src, ".txt");
		
		for (String line : file.split("\n")){
			splLine = line.split(" ");
			similarity = new Apfloat(Double.parseDouble(splLine[0]),NewTranslationCompare.prec);
			srcIdentifier = splLine[1];
			trgIdentifier = splLine[2];
			if (globalMap.containsKey(srcIdentifier))
				globalMap.put(srcIdentifier, refreshLocalMap(globalMap.get(srcIdentifier), similarity, trgIdentifier));
		}
		writeFiles(globalMap);
	}
	
	public static HashMap<Apfloat, List<String>> refreshLocalMap (HashMap<Apfloat, List<String>> currentLocalMap, Apfloat sim, String trgID){
		List<String> currentIDs = new LinkedList<String>();
		if (currentLocalMap.containsKey(sim))
			currentIDs = currentLocalMap.get(sim);
		currentIDs.add(trgID);		
		currentLocalMap.put(sim, currentIDs);
		return currentLocalMap;			
	}
	
	public static void writeFiles(Map<String, HashMap<Apfloat, List<String>>> output){
		Set<Apfloat> similarities = new TreeSet<Apfloat>();
		
		Map<Apfloat, List<String>> localMap = new HashMap<Apfloat,List<String>>(); 
		for (String fileID : output.keySet()){
			localMap = output.get(fileID);
			similarities = localMap.keySet();
			for (Apfloat currSim : similarities){
				System.out.println(currSim + " contains: " +  localMap.get(currSim));
				
			}
			
			
		}
	}*/
	public static void sortFile(String f) throws IOException{
		//double sim;
		//String src, trg;
		String[] splLine; 
		TreeSet<FileLine> elementsList = new TreeSet<FileLine>();
		String inputText = FileIO.fileToString(new File(f));
		for (String inputLine : inputText.split("\n")){
			splLine = inputLine.split(" ");
			elementsList.add(new FileLine(Double.valueOf(splLine[0]), splLine[1], splLine[2]));			
		}
		
		for (FileLine x : elementsList){
			System.out.println(x);
		}
		
	}
	
	public static void main (String[] args) throws Exception{
		String folder_tm, folder_lf;
		
		//List<String> langs = Arrays.asList("de","es","fr","nl","pl");
		List<String> langs = Arrays.asList("de");
		for (String lang : langs){
			for (int i : integerArray ){
				folder_tm = basePath + lang + "/out"+i;			
				folder_lf = basePath + lang + "/out"+i;
				System.out.println(folder_tm);
				for (String currentFile : FileIO.getFilesRecursively(new File(folder_tm), ".txt")){
					sortFile(currentFile);
				}
				
		/*		targetDir = sourceDir+"out"+i;
				generateOutput(sourceDir, targetDir);*/
				
			}
		}
		
	}
}
