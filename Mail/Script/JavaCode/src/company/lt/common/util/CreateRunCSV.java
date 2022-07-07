package companyname.lt.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.xml.validation.SchemaFactoryConfigurationError;

public class CreateRunCSV {

	public void Trim(String inputFile, String userId, String outputPath, double totalUser, int userColumn){ 	
		BufferedReader br = null;
		FileReader fr = null;
		try {
			File f = new File(inputFile);
			if (!f.exists())
				throw new IOException("Input File does not exist : "+ inputFile);
			String fileName = f.getName();
			fr = new FileReader(inputFile);
			br = new BufferedReader(fr);
			String sCurrentLine;
			int userCount = 0;
			int breakFlag = 0;
			File outputFile = new File(outputPath, fileName);
			PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
			while ((sCurrentLine = br.readLine()) != null && breakFlag == 0) {
				if (sCurrentLine!= null && !"".equals(sCurrentLine.trim()) && sCurrentLine.contains(","))
				{
					String[] elements = sCurrentLine.split(",");
					String currentUserId = elements[userColumn];
					if((Integer.parseInt(currentUserId.trim()) >= Integer.parseInt(userId.trim()))){					
						writer.println(sCurrentLine);
						System.out.println("Triming by user id " +sCurrentLine);
						userCount++;
						if(userCount==totalUser){
							breakFlag = 1;
						}
					}
				}
				writer.flush();
			}
			if(userCount<totalUser){
				double user;
				user = totalUser - userCount;
				throw new Exception("Selected file donot have enough records of user. We are short of" +user+" user");
			}
			writer.close();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			try {
				if (br != null)
					br.close();
				if (fr != null)
					fr.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		CreateRunCSV obj = new CreateRunCSV();
		if (args.length < 5)
			throw new RuntimeException("Incorrect number of Inputs need InputFile, startingUserId, outputLocation, userRequired, userColumnInCSV");
		String inputFile = args[0];
		String startingUserId = args[1];
		String outputLocation = args[2];
		double userRequired = Double.parseDouble(args[3]);
		int userColumnInCSV = Integer.parseInt(args[4]);
		
		obj.Trim(inputFile, startingUserId, outputLocation, userRequired, userColumnInCSV);		
	}

}
