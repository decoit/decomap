/* 
 * Copyright 2015 DECOIT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.simu.decomap.util;

import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;

import de.simu.decomap.main.IfMapClient;

/**
 * class containing some helper-functions that do not fit anywhere else
 * 
 * @version 0.2
 * @author Dennis Dunekacke, DECOIT GmbH
 */
public class Toolbox {

	// application start time
	public static final String clientStartTime = getNowDateAsString("yyyy-MM-dd HH:mm:ss");

	public static final String dateFormatString = "yyyy-MM-dd'T'kk:mm:ss'Z'";

	public static SimpleDateFormat dateFormatNoYearNoDay = new SimpleDateFormat(
			"MMM dd HH:mm:ss", Locale.ENGLISH);
	
	public static SimpleDateFormat dateFormatNoDay = new SimpleDateFormat(
			"MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
	
	public static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH);

	public static final SimpleDateFormat ifmapTimeStyle = new SimpleDateFormat(
			dateFormatString);

	public static SimpleDateFormat calenderFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	// ********************************************************
	// * REGULAR EXPRESSIONS
	// ********************************************************

	// HashMap containing all regular expressions from config
	public static HashMap<String, Pattern> regExMap = new HashMap<String, Pattern>();

	/**
	 * build regular expressions hashmap from regex.properties file
	 * 
	 * @param pr
	 *            regex-properties
	 */
	public static void loadAndPrepareRegExFromFile(final String path) {
		Properties props = new Properties();
		try {
			FileInputStream in;
			in = new FileInputStream(path);
			props.load(in);
			in.close();
		} catch (Exception e) {
			IfMapClient.criticalError(e);
		}

		Enumeration<Object> em = props.keys();
		while (em.hasMoreElements()) {
			String str = (String) em.nextElement();
			if (!isNullOrEmpty(str)
					&& !isNullOrEmpty(props.get(str).toString())
					&& str.startsWith("regex")) {
				regExMap.put(str, Pattern.compile(props.get(str).toString()));
			}
		}
	}

	/**
	 * get regular expressions pattern from regex.hashmap
	 * 
	 * @param key
	 *            key for the value to return
	 * 
	 * @return Pattern for passed in key
	 */
	public static Pattern getRegExPattern(String key) {
		if (regExMap == null) {
			return null;
		}
		return regExMap.get(key);
	}

	public static Config loadConfig(String path,
			Class<? extends Config> configClass) {
		ConfigFactory.setProperty("filename", path);
		return ConfigFactory.create(configClass);
	}

	// ********************************************************
	// * DATE-RELATED
	// ********************************************************

	/**
	 * get current date-time as string. format of the returned string is
	 * determined by the passed in format-string
	 * 
	 * @param dateFormat
	 *            format-string
	 * 
	 * @return current date as string
	 */
	public static String getNowDateAsString(final String dateFormat) {
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
		return formatter.format(currentDate.getTime());
	}

	/**
	 * get current date-time as date-object. format is determined by the passed
	 * in format-string
	 * 
	 * @param dateFormat
	 * 
	 * @return date-object containing current date-time
	 */
	public static Date getNowDate(final String dateFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Date date = null;
		try {
			date = sdf.parse(getNowDateAsString(dateFormat));
		} catch (ParseException e) {
			IfMapClient.criticalError(e);
		}
		return date;
	}

	/**
	 * get calendar-object from passed in string using the passed in date format
	 * string
	 * 
	 * @param dateString
	 *            date of calendar
	 * @param dateFormat
	 *            format of the passed in date
	 * 
	 * @return calendar-object set to passed in date
	 */
	public static Calendar getCalendarFromString(final String dateString,
			final String dateFormat, final Locale loc) {
		DateFormat formatter;
		Date date = null;
		if (loc == null) {
			formatter = new SimpleDateFormat(dateFormat);
		} else {
			formatter = new SimpleDateFormat(dateFormat, loc);
		}
		try {
			date = formatter.parse(dateString);
		} catch (ParseException e) {
			IfMapClient.criticalError(e);
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		return cal;
	}

	/**
	 * convert the passed in timestamp-string to string in IFMAP-compatible
	 * format
	 * 
	 * @param dateString
	 *            timestamp-string to be converted
	 * @param dateSplitter
	 *            splitter separating date values (e.g YYYY/MM/DD)
	 * @param dateTimeSplitter
	 *            splitter separating the date from the time-values
	 * 
	 * @return timestamp-string as specified by IFMAP
	 */
	public static String convertTimestampToIfMapFormat(final String dateString,
			final String dateSplitter, final String dateTimeSplitter) {
		String[] timestamp = dateString.split(dateTimeSplitter);

		// YYYY/MM/DD => [0]Year [1] Month [2] Day
		String[] date = timestamp[0].split(dateSplitter);

		// build new timestamp-string
		return date[0] + "-" + date[1] + "-" + date[2] + "T" + timestamp[1]
				+ "Z";
	}

	/**
	 * get HashMap containing mapping of alphanumeric month values (e.g. Feb) to
	 * integer values
	 * 
	 * @return HashMap
	 */
	public static HashMap<String, String> MonthValues = new HashMap<String, String>();

	public static HashMap<String, String> getAplhaNumericMonthMap() {
		if (MonthValues.isEmpty()) {
			MonthValues.put("Jan", "01");
			MonthValues.put("Feb", "02");

			MonthValues.put("Mar", "03");
			MonthValues.put("Apr", "04");
			MonthValues.put("May", "05");
			MonthValues.put("Jun", "06");
			MonthValues.put("Jul", "07");
			MonthValues.put("Aug", "08");
			MonthValues.put("Sep", "09");
			MonthValues.put("Oct", "10");
			MonthValues.put("Nov", "11");
			MonthValues.put("Dec", "12");
		}
		return MonthValues;
	}

	// ********************************************************
	// * MISC
	// ********************************************************

	/**
	 * check if passed in string is null or empty
	 * 
	 * @param test
	 *            string to be tested
	 * 
	 * @return true if string is null or empty
	 */
	public static boolean isNullOrEmpty(final String test) {
		if (test == null) {
			return true;
		}
		if (test.trim().isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * convert passed in ip6-address-string to IFMAP-compatible
	 * ip6-address-string. this means deleting all leading zeros from every
	 * address-part
	 * 
	 * @param String
	 *            adr address-string to convert
	 * 
	 * @return IFMAP-compatible ip6-address-string
	 */
	public static String convertIP6AddressToIFMAPIP6AddressPattern(
			final String adr) {
		String[] singleEntrys = adr.split(":");
		String convertedAddress = new String();

		for (int i = 0; i < singleEntrys.length; i++) {

			// delete "leading" zeros
			singleEntrys[i] = singleEntrys[i].replaceFirst("0*", "");

			// is string is empty after deleting leading zeros, add a zero-char
			if (singleEntrys[i].length() < 1) {
				singleEntrys[i] = "0";
			}

			// add ":" to address, leave out last entry
			if (i != singleEntrys.length - 1) {
				singleEntrys[i] += ":";
			}

			// add entry to converted-address-string
			convertedAddress += singleEntrys[i];
		}

		return convertedAddress;
	}

}