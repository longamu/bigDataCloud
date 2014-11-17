package com.program.search;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONObject;
import com.mongodb.DBObject;

/**
 
 * @author wendy926

 */
final class PutDataToMap{
    private static Map<String,String> receiveMap =
            new HashMap<String, String>();
    public static String putDataToMap(String dbObject) {
        String string = "";
	JSONObject jsonObject = new JSONObject(dbObject);
	String key = "";
	String value = "";
	Iterator<String> iterator = jsonObject.keys();
	while (iterator.hasNext()) {
	    key = iterator.next();
	    value = jsonObject.get(key).toString();
	    receiveMap.put(key, value);
	}
	for (String keydetail : receiveMap.keySet()) {
	    String valuedetail = receiveMap.get(keydetail);
	    string += keydetail + "\t" + valuedetail + "\r\n";
	}
	return string;
    }
}
