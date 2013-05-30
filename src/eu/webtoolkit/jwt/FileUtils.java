package eu.webtoolkit.jwt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileUtils {
	public static List<Byte> fileHeader(String fileName, int size) {
		List<Byte> header = new ArrayList<Byte>();
		try {
			InputStream is = new FileInputStream(new File(fileName));
			for (int i = 0; i < size; i++) {
				header.add((byte) is.read());
			}
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return header;
	}

	public static boolean isDirectory(String directory) {
		return new File(directory).isDirectory();
	}

	public static boolean exists(String directory) {
		return new File(directory).exists();
	}

	public static void listFiles(String path, List<String> files) {
		File dir = new File(path);
		
		if (!dir.isDirectory())
			throw new RuntimeException("listFiles: \"" + path + "\" is not a directory");

		for (File f : dir.listFiles())
			files.add(f.getAbsolutePath());
	}

	public static String leaf(String path) {
		return new File(path).getName();
	}

	public static String fileToString(String fileName) {
		InputStream is = FileUtils.class.getResourceAsStream(fileName);
		Scanner s = new Scanner(is).useDelimiter("\\Z");
		String str = new String();
		while(s.hasNext())
			str = str + s.next();
		return str;
	}
}
