package es.nlel.ibmm1similarity;

public class FileValueMap<T extends Comparable<T>> implements Comparable<FileValueMap<T>>{

	private String fileName;
	private T value;
	
	public FileValueMap(String fileName, T newValue) {
		this.fileName = fileName;
		this.value = newValue;
	}
	
	public String getName() { return this.fileName; }
	public T getValue() { return this.value; }
	
	public String toString() {
		return "(" + this.fileName + " : " + this.value + ")";
	}
	public int compareTo(FileValueMap<T> o) {
		return this.value.compareTo(o.getValue());
	}
}