package tests;

import java.text.ParseException;
import java.util.HashMap;

import org.meta.plugin.TCP.AMP.AMPAskParser;
import org.meta.plugin.TCP.AMP.AMPFactory;

public class AMPAskParserTest {

	public static void main(String[] args) {
		byte[] message = {	0x00,0x04,0x5F,0x61,0x73,0x6B,0x00,0x02,0x32,0x33,
							0x00,0x08,0x5F,0x63,0x6F,0x6D,0x6D,0x61,0x6E,0x64,
							0x00,0x03,0x53,0x75,0x6D,0x00,0x01,0x61,0x00,0x02,
							0x31,0x33,0x00,0x01,0x62,0x00,0x02,0x38,0x31,0x00,
							0x00};
		try {
			AMPAskParser parser = new AMPAskParser(message);
			System.out.println(parser.getAsk());
			System.out.println(parser.getCommand());
			System.out.println(parser.getHash());
			
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("_ask", "23");
			map.put("_command", "toto");
			map.put("_hash", "cacahuete");
			AMPFactory factory = new AMPFactory(map);
			
			System.out.println(factory.getMessage());
			
			parser = new AMPAskParser(factory.getMessage());
			
			System.out.println(parser.getAsk());
			System.out.println(parser.getCommand());
			System.out.println(parser.getHash());
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}
