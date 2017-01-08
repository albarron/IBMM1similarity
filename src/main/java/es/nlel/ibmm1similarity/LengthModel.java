package es.nlel.ibmm1similarity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LengthModel {
	private final static String basePath = "/home/lbarron/plagio/crosslingual/dictLearning/corporaJCRNUEVO/train/";
	/**
	 * Calculates Mu and Sigma (mean and standard deviation) of the files lengths between 
	 * languages (translations)	
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		String tLan = "en";
		List<String> sourceFiles = FileIO.getFilesRecursively(new File(basePath+"en"), ".txt");
		List<String> targetFiles = FileIO.getFilesRecursively(new File(basePath+tLan), ".txt");
		List<Double> x = new ArrayList<Double>();				
		int iCount = 0;
		for (String t : targetFiles){			
			String eqEnFile = t.replace("-"+tLan,"-en");
			eqEnFile = eqEnFile.replace(tLan+"/","en/");
			if (sourceFiles.contains(eqEnFile)){
				String s = eqEnFile;
				double lenS = count_characters(new File(s));
				double lenT = count_characters(new File(t));	
				x.add(lengthDivision(lenS, lenT));
				if (++iCount%1000 ==0)
					System.out.println(iCount+ " Currently: " + s.toString()+ " " + t.toString());
			}			
		}
		double Mu = calculateMean(x);
		double Sigma = calculateDesv(x, Mu);
		System.out.println("Number of pair files considered: "+x.size());
		System.out.println("Mean= "+Mu+" Standard deviation= "+Sigma);
	}

	/**
	 * Calculates the mean of the double values in valuesList
	 * @param valuesList	A list containing double elements
	 * @return				Mean of the elements in valuesList
	 */
	public static double calculateMean(List<Double> valuesList){
		double sum = 0.0, count= 0.0;
		for (double value : valuesList){
			count+=1;
			sum+=value;
		}
		return sum/count;
	}
	
	/**
	 * Calculates standard deviation of elements in valuesList given mean
	 * @param valuesList	A list containing double elements
	 * @param mean			The previously calculated mean of valuesList
	 * @return				Standard deviation
	 */
	public static double calculateDesv(List<Double> valuesList, double mean){
		double sum = 0.0, count= 0.0;
		for (double value : valuesList){
			count+=1;
			sum+=Math.pow(value-mean,2);
		}
		double desv = Math.sqrt(sum/count);		
		return desv;		
	}
	
	
	/**
	 * Divides the length of the target text (len_2) by the source text (len_1)
	 * @param len_1	Double value (length source)
	 * @param len_2	Double value (length target)
	 * @return			len_2 / len_1
	 */
	public static double lengthDivision(double len_1, double len_2){
		return len_2/len_1;
	}	
	
	/**
	 * Counts the characters in the text file currentFile
	 * @param currentFile	A text file
	 * @return				Length of the file in characters
	 * @throws IOException
	 */
	public static double count_characters(File currentFile) throws IOException{
		String text = FileIO.fileToString(currentFile);		
		return (double) text.length();
	}
	
	/**
	 * 
	 * @param sFile	Source file
	 * @param tFile	Target file
	 * @param mu		Corresponding mean
	 * @param sigma	Corresponding standard deviation
	 * @return			The length factor between the texts given the mean and sd of the implied languages
	 * @throws IOException
	 */
	public static double lengthFactor(double lenS, double lenT, double mu, double sigma){
/*		double lenS = count_characters(sFile);
		double lenT = count_characters(tFile);*/
		double inner = (lengthDivision(lenS, lenT)-mu)/sigma; 
		double pot = -0.5 * Math.pow(inner, 2); 
		double lf  = Math.exp(pot);
		return lf;
	}
	
}
