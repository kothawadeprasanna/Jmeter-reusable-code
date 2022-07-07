package companyname.lt.chat;

import java.util.Random;

public class Utilities {
	public String randomizeString(String randWord) {
		int strLenF = randWord.length();
		Random rand = new Random();
		int trimrandWord = rand.nextInt(strLenF - 1);
		if (trimrandWord == 0) {
			trimrandWord = 1;
		}
		int temp = rand.nextInt(10000000) + 10000;
		String temp1 = String.valueOf(temp);
		randWord = randWord.substring(trimrandWord, strLenF) + randWord.substring(0, trimrandWord) + temp1;
		randWord = randWord.replaceAll("&","&amp;");
		randWord = randWord.replaceAll("\"","&quot;");
		randWord = randWord.replaceAll("'","&#39;");
		randWord = randWord.replaceAll("<","&lt;");
		randWord = randWord.replaceAll(">","&gt;");
		return randWord;
	}
}