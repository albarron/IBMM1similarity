package es.nlel.ibmm1similarity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FileIO {
	public final static String separator = System.getProperty("file.separator");
	
	static final int BUFF_SIZE = 100000;
	static final byte[] buffer = new byte[BUFF_SIZE];


	public static String md5(File file) throws FileNotFoundException, NoSuchAlgorithmException, IOException {
		MessageDigest digest = MessageDigest.getInstance("MD5");

		FileInputStream input = new FileInputStream(file);
		FileChannel inputChannel = input.getChannel();
		try {
			ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4096);
			byte[] buf = new byte[4096];
			int len;
			long c = 0;
			while ((len = inputChannel.read(inputBuffer)) >= 0) {
				inputBuffer.flip();
				inputBuffer.get(buf, 0, len);
				digest.update(buf, 0, len);
				inputBuffer.flip();
				c += len;
			}
			if (c != inputChannel.size()) {
				throw new IllegalStateException(c + " != " + len);
			}
		}finally{
			inputChannel.close();
			input.close();
		}

		String result = (new BigInteger(1, digest.digest())).toString(16);
		return result.length()==31 ? "0"+result : result;
	}

	public static List<String> getFilesRecursively(File dir, final String ext) {
		return getFilesRecursively(dir, ext, Long.MIN_VALUE, Long.MAX_VALUE);
	}

	public static void writeObject(Object x, File f) throws IOException {
		FileOutputStream fos = new FileOutputStream(f);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(x);
		oos.close();
	}

	public static Object readObject(File f) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(fis);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Object result = ois.readObject();
		ois.close();
		bis.close();
		fis.close();
		return result;
	}

	public static List<String> getFilesRecursively(File dir, final String ext, long timeMin, long timeMax) {
		List<String> fileList = null;
		if (dir.isDirectory() && dir.canRead()) {
			fileList = new LinkedList<String>();
			String[] list = dir.list();

			for (String s : list) {
				String fileName = dir.getAbsolutePath() + separator + s;
				File f = new File(fileName);
				Long lastMod = f.lastModified();

				if (f.isDirectory()) {
					fileList.addAll(getFilesRecursively(f, ext,timeMin,timeMax));
				} else if((s.toLowerCase().endsWith(ext) && !s.toLowerCase().startsWith("stat") && timeMin<=lastMod && timeMax>=lastMod)) {
					fileList.add(dir.getAbsolutePath()+separator+s);
				}
			}
		}
		if(fileList!=null)
			Collections.sort(fileList, String.CASE_INSENSITIVE_ORDER);

		return fileList;
	}

	public static String fileToString(File f) throws IOException {
		ByteArrayOutputStream baos =   new ByteArrayOutputStream();  		
		FileInputStream input = new FileInputStream(f);
		FileChannel inputChannel = input.getChannel();
		try {
			ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4096);
			byte[] buf = new byte[4096];

			int len;
			long c = 0;
			while ((len = inputChannel.read(inputBuffer)) >= 0) {
				inputBuffer.flip();
				inputBuffer.get(buf, 0, len);
				// Dont read UTF-8 Header
				if(c==0 && buf[0]==-17 && buf[1]==-69 && buf[2]==-65)
					baos.write(buf,3,len-3);
				else				
					baos.write(buf, 0, len);
				inputBuffer.flip();
				c += len;
			}
			if (c != inputChannel.size()) {
				throw new IllegalStateException(c + " != " + len);
			}			
		}finally{
			inputChannel.close();
			input.close();
		}

		String result = baos.toString("UTF-8");
		baos.close();
		return result;
	}

	public static synchronized void stringToFile(File f, String text, boolean append) throws IOException {
		if(!(new File(f.getParent()).exists())) new File(f.getParent()).mkdirs();

		ByteArrayOutputStream baos = new ByteArrayOutputStream () ;
		if(!append || (append && f.length()==0))
			baos.write(new byte[] {-17,-69,-65}); // Write UTF-8 BOM
		
		PrintStream printer = new PrintStream(baos);
		printer.print(text);
		printer.close();
		FileOutputStream fos = new FileOutputStream (f,append) ; 
		baos.writeTo(fos) ;
		baos.close();
		fos.close();
	}

	public static void copy(File from, File to) throws IOException {
		if(!(new File(to.getParent()).exists())) new File(to.getParent()).mkdirs();

		InputStream in = null;
		OutputStream out = null; 
		try {
			in = new FileInputStream(from);
			out = new FileOutputStream(to);
			while (true) {
				synchronized (buffer) {
					int amountRead = in.read(buffer);
					if (amountRead == -1) {
						break;
					}
					out.write(buffer, 0, amountRead); 
				}
			} 
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

	public static void move(File in, File out) throws IOException {
		new File(out.getAbsolutePath().substring(0,out.getAbsolutePath().lastIndexOf(separator))).mkdirs();
		copy(in, out);
		in.delete();
	}

	/**
	 * Deletes all files and subdirectories under "dir".
	 * @param dir Directory to be deleted
	 * @return boolean Returns "true" if all deletions were successful.
	 *                 If a deletion fails, the method stops attempting to
	 *                 delete and returns "false".
	 */
	public static boolean deleteDir(File dir) {

		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
} 