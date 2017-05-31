package hmacHelper;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 * <h1>Create an HMAC code</h1>
 * Create an HMAC code suitable for use with the Adyen Payment Provider system
 * 
 * @author notahelpdesk
 * @version 1.0
 * @since 2016-12-15
 */
public class HmacHelper {
	private String secret;
	
	public HmacHelper() {
		this.secret = "";
	}
	
	public void setSecret(String secret)
	{
		this.secret = secret;
	}
	
	/**
	 * Constructor
	 * @param secret The secret used to generate the HMAC hash value
	 */
	public HmacHelper(String secret)
	{
		this.secret = secret;
	}
	
	/**
	 * Turn a string of hex characters representing the characters of a phrase into a byte array
	 * @param input The hex character representation of the phrase
	 * @return byte array
	 * @throws Exception
	 */
	private byte[] getBytes(String input) throws Exception {
		int numberChars = input.length();
		if (numberChars == 0 || numberChars % 2 != 0)
		{
			throw new Exception("Must be greater than 0 chars and be an even length");
		}
		byte[] bytes = new byte[numberChars/2];
		for (int i = 0; (i <= (numberChars - 1)); i = (i + 2)) 
		{
			bytes[i / 2] = (byte)((Character.digit(input.charAt(i), 16) << 4)
                    + Character.digit(input.charAt(i+1), 16));
		}
	    return bytes;
	}
	
	/**
	 * Helper for generating an URL-encoded version of the generated HMAC
	 * @param hmac The HMAC created using ComputeHmac()
	 * @return String The URL-encoded version of the HMAC string
	 * @throws Exception
	 */
	public String GetUrlEncodedHmac(String hmac) throws Exception
	{
		return URLEncoder.encode(hmac, "UTF-8");
	}
	
	/**
	 * Create an HMAC hash using the data created by CreateDataString() and the secret used to instantiate the class
	 * @param data Correctly formatted string of data to hash
	 * @return String The HMAC hash
	 * @throws Exception
	 */
	public String ComputeHmac(String data) throws Exception
	{
		//Charset charset = Charset.forName("UTF-8");
		if (secret == "")
			throw new Exception("The secret must be specified before the HMAC can be created");
		
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		byte[] secretBytes = getBytes(secret);
		
		SecretKeySpec secret_key = new SecretKeySpec(secretBytes, "HmacSHA256");
		sha256_HMAC.init(secret_key);

		return Base64.encodeBase64String(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
	}
	
	/**
	 * This is specific to the Adyen Payment Provider implementation
	 * @param authResult Authorisation Result, e.g. "AUTHORISED"
	 * @param pspReference The payment provider's reference number, e.g. "1234567890"
	 * @param merchantReference The merchant reference, i.e. the reference number supplied by the merchant to the payment provider, e.g. "ABC123FED098"
	 * @param skinCode The code used to specify the appearance and configuration of the payment provider interface, e.g. "asdfghj"
	 * @param shopperLocale The locale code, e.g. "en_GB"
	 * @param paymentMethod The code to identify the payment type, e.g. "mc" for MasterCard 
	 * @return
	 */
	public String CreateDataString(String authResult, String pspReference, String merchantReference, String skinCode, String shopperLocale, String paymentMethod)
	{
		Map<String, String> dict = new HashMap<String, String>();
        dict.put("authResult", authResult);
        dict.put("pspReference", pspReference);
        dict.put("merchantReference", merchantReference);
        dict.put("skinCode", skinCode);
        dict.put("shopperLocale", shopperLocale);
        dict.put("paymentMethod", paymentMethod);
        return ComputeDataString(dict);
	}
	
	/**
	 * Mechanism used to arrange the data in the correct fashion for putting into the HMAC
	 * Note that this implementation is specific to the Payment Provider
	 * @param keyValuePairs
	 * @return String formatted and ready for HMAC generation
	 */
	private static String ComputeDataString(Map<String, String> keyValuePairs) 
	{
		Object[] keys = keyValuePairs.keySet().toArray();
		List<String> valuesInSortedKeyOrder = new ArrayList<String>();
		List<String> keysInSortedOrder = new ArrayList<String>();
		Arrays.sort(keys);
		// sort the keys and store the values for the sorted keys in order
		for(Object key : keys)
		{
			if (key.toString() == "sig" || key.toString() == "merchantSig" || key.toString().startsWith("ignore."))
				keyValuePairs.remove(key);
			else
			{
				valuesInSortedKeyOrder.add(keyValuePairs.get(key));
				keysInSortedOrder.add(key.toString());
			}
		}
		List<String> components = new ArrayList<String>();
		
		components.addAll(ReplaceSpecials(keysInSortedOrder));
		components.addAll(ReplaceSpecials(valuesInSortedKeyOrder));
		
		return String.join(":", components);
	}
	
	/**
	 * Escape characters that need to be escaped
	 * @param inputs A list of values to escape
	 * @return List<String>
	 */
	private static List<String> ReplaceSpecials(List<String> inputs) 
	{
		List<String> ret = new ArrayList<String>();
		for(String input : inputs){
			ret.add(ReplaceSpecials(input));
		}
		return ret;
	}
	
	/**
	 * Escape characters that need to be escaped
	 * @param input A string to make safe
	 * @return String
	 */
	private static String ReplaceSpecials(String input)
	{
		return input.replace("\\", "\\\\").replaceAll(":", "\\:");
	}
		
	/**
	 * Main method for testing purposes
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length < 6)
		{
			System.out.println("Usage java --jar HmacHelper.jar 'secret' 'authResult' 'pspReference', 'merchantReference', 'skinCode', 'shopperLocale', 'paymentMethod'");
			System.exit(1);
		}
		HmacHelper hh = new HmacHelper(args[0]);
		String data = hh.CreateDataString(args[1], args[2], args[3], args[4], args[5], args[6]);
		
		String hmac = "";
		try {
			hmac = hh.ComputeHmac(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.printf("Generated HMAC: %s", hmac);
	}

}
