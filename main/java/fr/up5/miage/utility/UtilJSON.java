/* Links that helped for coding:
 * http://www.oracle.com/technetwork/articles/java/json-1973242.html
 */

package fr.up5.miage.utility;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

/**
 * This utility class provides data recovery features for JSON text
 */
public class UtilJSON{

	/**
	 * This method recovers the values of all metrics
	 * @param input is the StringBuilder of JSON text
	 * @return a HashMap where key's field is the name of the metrics and the value's field is the metrics values
	 */
	public static HashMap<String,Float> getValueOfAllMetrics(StringBuilder input){
		HashMap<String,Float> mapMetrics = new HashMap<String,Float>();
		String metricName;
		try (JsonParser pars = Json.createParser(new StringReader(input.toString()))){
			while (pars.hasNext()){
				if (pars.next().equals(Event.KEY_NAME) && "metric".equals(pars.getString())){
					pars.next();
					metricName = pars.getString();
					pars.next();
					pars.next();
					try{
						mapMetrics.put(metricName, Float.parseFloat(pars.getString()));
					}
					catch (NumberFormatException e){};
				}
			}
		}
		return mapMetrics;
	}


	/**
	 * This method retrieves the set of values of a specific field
	 * @param input is the StringBuilder of JSON text
	 * @param fieldName the field's name concerned
	 * @return a List of all values for this specific field
	 */
	public static List<String> getValueOfAllSpecificField(StringBuilder input, String fieldName){
		ArrayList<String> listFields = new ArrayList<String>();
		try (JsonParser pars = Json.createParser(new StringReader(input.toString()))){
			while (pars.hasNext()){
				if (pars.next().equals(Event.KEY_NAME) && pars.getString().equals(fieldName)){
					pars.next();
					listFields.add(pars.getString());
				}
			}
		}
		return listFields;
	}

	/**
	 * Get id of a quality profile
	 * @param input is the StringBuilder of JSON text
	 * @return the String that represents the id of the quality profile
	 */
	public static String getIdQualityProfile(StringBuilder input){
		try (JsonParser pars = Json.createParser(new StringReader(input.toString()))){
			while (pars.hasNext()){
				if (pars.next().equals(Event.KEY_NAME) && "key".equals(pars.getString())){
					pars.next();
					return pars.getString();
				}
			}
			return null;
		}
	}
}

