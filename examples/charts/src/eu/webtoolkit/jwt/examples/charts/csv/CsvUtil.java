/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.charts.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import eu.webtoolkit.jwt.Orientation;
import eu.webtoolkit.jwt.WAbstractItemModel;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WStandardItemModel;
import eu.webtoolkit.jwt.WString;
import eu.webtoolkit.jwt.WText;

public class CsvUtil {
	public static void readFromCsv(BufferedReader reader, WAbstractItemModel model) {
		try {
			String line = null;
			int row = 0;
			while ((line = reader.readLine()) != null) {
				List<String> fields = splitHandleQuotes(line);

				if (row != 0) {
					model.insertRow(model.getRowCount());
				}

				for (int col = 0; col < fields.size(); col++) {
					if (col >= model.getColumnCount())
						model.insertColumns(model.getColumnCount(), col + 1 - model.getColumnCount());

					String value = fields.get(col);
					if (row == 0)
						model.setHeaderData(col, Orientation.Horizontal, value);
					else {
						int dataRow = row - 1;

						if (dataRow >= model.getRowCount())
							model.insertRows(model.getRowCount(), dataRow + 1 - model.getRowCount());

						try {
							Double d = Double.valueOf(value);
							model.setData(dataRow, col, d);
						} catch (NumberFormatException e) {
							model.setData(dataRow, col, value);
						}						
					}
				}
				row++;
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private static List<String> splitHandleQuotes(String s) {
		ArrayList<String> results = new ArrayList<String>();
		char delimiter = ',';
		char quoteChar = '"';
		char escapeChar = '\\';

		StringBuffer current = new StringBuffer("");
		boolean inQuotation = false;
		boolean escaping = false;

		for (int i = 0; i < s.length(); ++i) {
			if (escaping) {
				if (s.charAt(i) == quoteChar)
					current.append(quoteChar);
				else {
					current.append(escapeChar);
					current.append(quoteChar);
				}
				escaping = false;
			} else {
				if (s.charAt(i) == quoteChar) {
					inQuotation = !inQuotation;
				} else
					if (s.charAt(i) == escapeChar) {
						escaping = true;
					} else
						if (!inQuotation)
							if (s.charAt(i) == delimiter) {
								results.add(new String(current));
								current = new StringBuffer("");
							} else
								current.append(s.charAt(i));
						else
							current.append(s.charAt(i));
			}
		}

		results.add(new String(current));

		return results;
	}

	public static WAbstractItemModel readCsvFile(String fname, WContainerWidget parent) {
		WStandardItemModel model = new WStandardItemModel(0, 0, parent);

		InputStream is = model.getClass().getResourceAsStream("/eu/webtoolkit/jwt/examples/charts/data/"+fname);

		if (is!=null) {
			readFromCsv(new BufferedReader(new InputStreamReader(is)), model);
			return model;
		} else {
			WString error = WString.tr("error-missing-data");
			error.arg(fname);
			new WText(error, parent);
			return null;
		}
	}
}
