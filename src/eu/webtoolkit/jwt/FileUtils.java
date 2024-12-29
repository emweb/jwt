package eu.webtoolkit.jwt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
	public static List<Byte> fileHeader(String fileName, int size) {
		List<Byte> header = new ArrayList<Byte>();
		try {
			InputStream is = getResourceAsStream(fileName);
			for (int i = 0; i < size; i++) {
				header.add((byte) is.read());
			}
			is.close();
		} catch (IOException e) {
			logger.warn("Exception while accessing file {}", fileName, e);
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
		InputStream is = null;
		Scanner s = null;
		try {
			is = getResourceAsStream(path);
			s = new Scanner(is, "UTF-8");
			s.useDelimiter("\\Z");
			String str = new String();
			while (s.hasNext())
				str = str + s.next();
			return str;
		} catch (IOException e) {
			System.err.println("resourceToString: " + e.getMessage());
			return null;
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				logger.info("resourceToString failed for {}", path, e);
			}
			
			if (s != null)
				s.close();
		}
	}

	public static String fileToString(String path) {
		return resourceToString(path);
	}

    public static void appendFile(String srcPath, String targetPath) {
    	try {
    		FileInputStream fis = new FileInputStream(srcPath);
    		FileOutputStream fos = new FileOutputStream(targetPath, true);
    		
    		int nbBytes = 0;
    		byte[] buffer = new byte[1024];
    		while ( (nbBytes = fis.read(buffer)) != -1 ) {
    			fos.write(buffer, 0, nbBytes);
    		}

    		fis.close();
    		fos.close();
    	} catch (IOException e) {
    		logger.info("appendFile failed for {}", srcPath, e);
    	}
    }
}
