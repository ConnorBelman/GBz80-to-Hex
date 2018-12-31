package asmToHex;
import java.util.ArrayList;
import java.util.Arrays;

public class Hex {
	
	private String value;
	
	private String[] hexDigits = new String[] {
			"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"
			};	
	
	ArrayList<String> hexList = new ArrayList<String>(Arrays.asList(hexDigits));
	
	public Hex (int dec) {
		this.value = decToHex(dec);
	}
	
	public Hex (String hexValue) {
		for(int i = 0; i < hexValue.length(); i++) {
			if(hexList.indexOf(String.valueOf(hexValue.charAt(i))) == -1) {
				throw new ArithmeticException("Invalid Input: " + hexValue.charAt(i));
			}
		}
		this.value = hexValue;
	}
	
	private String decToHex(int dec) {
		String curStr = "";
		int curDec = dec;
		int remainder;
		if(dec == 0) {
			return "0";
		}
		while (curDec != 0) {
			remainder = curDec % 16;
			curDec = curDec / 16;
			curStr = hexDigits[remainder] + curStr;
		}
		return curStr;
	}
	
	private int hexToDec(String hex) {
		int dec = 0;
		int digit = 0;
		if(hex == "") {
			return 0;
		}
		for(int i = hex.length() - 1; i >= 0; i--) {
			dec += (hexList.indexOf(String.valueOf(hex.charAt(i))) * Math.pow(16, digit));
			digit++;
		}
		return dec;
	}
	
	public void add(Hex addend) {
		String sum = "";
		String addStr = addend.getValue();
		int digit;
		int carry = 0;
		int places = value.length();
		if (addStr.length() - 1 > places) {
			places = addStr.length();
		}
		for(int i = 0; i < places; i++) {
			if(value.length() - 1 < i) {
				digit = hexList.indexOf(String.valueOf
						(addStr.charAt(addStr.length() - i - 1))) + carry;
				if(digit > 15) {
					digit -= 16;
					carry = 1;
				}
				else {
					carry = 0;
				}
				sum = hexDigits[digit] + sum;
			}
			else if(addStr.length() - 1 < i) {
				digit = hexList.indexOf(String.valueOf
						(value.charAt(value.length() - i - 1))) + carry;
				if(digit > 15) {
					digit -= 16;
					carry = 1;
				}
				else {
					carry = 0;
				}
				sum = hexDigits[digit] + sum;
			}
			else {
				int dec1 = hexList.indexOf(String.valueOf(value.charAt(value.length() - i - 1)));
				int dec2 = hexList.indexOf(String.valueOf(addStr.charAt(addStr.length() - i - 1)));
				digit = dec1 + dec2 + carry;
				carry = 0;
				if(digit > 15) {
					digit -= 16;
					carry = 1;
				}
				sum = hexDigits[digit] + sum;
			}
		}
		if (carry == 1) {
			sum = "1" + sum;
		}
		value = sum;
	}
	
	public void subtract(Hex subtrahend) {
		String difference = "";
		String subStr = removeZeroPadding(subtrahend.getValue());
		String minStr = removeZeroPadding(value);
		int digit;
		int carry = 0;
		if(subStr.length() > minStr.length()) {
			throw new ArithmeticException("Underflow Error");
		}
		if(subStr.length() == minStr.length()) {
			for(int i = 0; i < minStr.length(); i++) {
				if(hexList.indexOf(String.valueOf(minStr.charAt(i))) > hexList.indexOf(String.valueOf(subStr.charAt(i)))) {
					break;
				}
				else if(hexList.indexOf(String.valueOf(minStr.charAt(i))) < hexList.indexOf(String.valueOf(subStr.charAt(i)))) {
					throw new ArithmeticException("Underflow Error");
				}
			}
		}
		for(int i = 0; i < minStr.length(); i++) {
			if(subStr.length() - 1 < i) {
				digit = hexList.indexOf(String.valueOf
						(minStr.charAt(minStr.length() - i - 1))) - carry;
				difference = hexDigits[digit] + difference;
				carry = 0;
			}
			else {
				int dec1 = hexList.indexOf(String.valueOf(minStr.charAt(minStr.length() - i - 1)));
				int dec2 = hexList.indexOf(String.valueOf(subStr.charAt(subStr.length() - i - 1)));
				if(dec2 > dec1) {
					dec1 += 16;
					digit = dec1 - dec2 - carry;
					carry = 1;
				}
				else {
					digit = dec1 - dec2 - carry;
					carry = 0;
				}
				difference = hexDigits[digit] + difference;
			}
			value = difference;
		}
	}
	
	public void inc() {
		add(new Hex("1"));
	}
	
	public boolean isHex(String hex) {
		if(hex == ""){
				return false;
		}
		for(int i = 0; i < hex.length(); i++) {
			if(hexList.indexOf(String.valueOf(hex.charAt(i))) == -1) {
				return false;
			}
		}
		return true;
	}
	
	private String zeroPadding(String unpaddedHex, int size) {
		String paddedHex = unpaddedHex;
		boolean padded = false;
		if(unpaddedHex.length() >= size) {
			padded = true;
		}
		while(!padded) {
			if(paddedHex.length() < size) {
				paddedHex = "0" + paddedHex;
			}
			else {
				padded = true;
			}
		}
		return paddedHex;
	}
	
	private String removeZeroPadding(String paddedHex) {
		String unpaddedHex = paddedHex;
		boolean padded = true;
		int digit = 0;
		if(Integer.valueOf(paddedHex) == 0) {
			return "0";
		}
		while(padded) {
			if(unpaddedHex.charAt(digit) == '0') {
				unpaddedHex = unpaddedHex.substring(digit + 1);
			}
			else {
				padded = false;
			}
		}
		return unpaddedHex;
	}
	
	public String getValue() {
		return value;
	}
	
	public String toString() {
		return "$" + value;
	}
	
	public int length() {
		return value.length();
	}
	
	public Hex copy() {
		String copyValue = new String(value);
		return new Hex(copyValue);
	}

}
