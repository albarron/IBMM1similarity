package es.nlel.ibmm1similarity;

import org.apfloat.Apfloat;



public class FileLine {
	
	public static Apfloat similarity;
	public static String sourceFile;
	public static String targetFile;
	
	public FileLine(double similarity, String sourceFile, String targetFile){
	this.similarity = new Apfloat(similarity, NewTranslationCompare.prec);
	this.sourceFile = sourceFile;
	this.targetFile = targetFile;
		
	}
	
	
}