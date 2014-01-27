package eu.webtoolkit.jwt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class FileUtils {
	public static List<Byte> fileHeader(String fileName, int size) {
		List<Byte> header = new ArrayList<Byte>();
		try {
			InputStream is = getResourceAsStream(fileName);
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

	public static InputStream getResourceAsStream(String path) throws IOException {
		URL url = FileUtils.class.getResource(path);
		
		if (url == null) {
			WApplication app = WApplication.getInstance();
			if (app != null) {
				try {
					ServletContext context = app.getEnvironment().getServer().getServletContext();
					url = context.getResource(path);
				} catch (Exception e) {
					url = null;
				}
			}
			
			if (url == null) {			
				try {
					url = new URL(path);
				} catch (Exception e) {
					url = new File(path).toURI().toURL();
				}
			}
		}

		return url.openStream();
	}

	/*
	 * path can be a:
	 *  - resource path (within the WAR)
	 *  - a URL string
	 *  - a file path
	 */
	public static String resourceToString(String path) {
		try {
			InputStream is = getResourceAsStream(path);
			Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\Z");
			String str = new String();
			while (s.hasNext())
				str = str + s.next();
			return str;
		} catch (IOException e) {
			System.err.println("resourceToString: " + e.getMessage());
			return null;
		}
	}

	public static String fileToString(String path) {
		return resourceToString(path);
	}
}
