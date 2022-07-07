package companyname.lt.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.xml.validation.SchemaFactoryConfigurationError;

public class CreateRunCustomerCSV {

	public void Trim(String inputFile, String userId, String outputPath, double totalUser, int userColumn, int rewrite){ 	
		BufferedReader br = null;
		FileReader fr = null;
		System.out.println("Value of rewrite is "+rewrite);
		try {
			File f = new File(inputFile);
			if (!f.exists())
				throw new IOException("Input File does not exist : "+ inputFile);
			String fileName = f.getName();		
			//br = new BufferedReader(fr);
			String sCurrentLine;
			int userCount = 0;
			int breakFlag = 0;
			File outputFile = new File(outputPath, fileName);
			PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
			while(rewrite>0){
				fr = new FileReader(inputFile);
				br = new BufferedReader(fr);
				while ((sCurrentLine = br.readLine()) != null && breakFlag == 0 && rewrite>0) {
					if (sCurrentLine!= null && !"".equals(sCurrentLine.trim()) && sCurrentLine.contains(","))
					{
						String[] elements = sCurrentLine.split(",");
						String currentUserId = elements[userColumn];
						if((Integer.parseInt(currentUserId.trim()) >= Integer.parseInt(userId.trim()))){					
							writer.println(sCurrentLine);
							System.out.println("Triming by user id " +sCurrentLine+ " Rewrite value is " + rewrite);
							userCount++;
							if(userCount==totalUser){
								breakFlag = 1;
							}
						}
					}
					writer.flush();
				}
				breakFlag = 0;
				rewrite--;
				if(rewrite>0){
					userCount = 0;
				}
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
		CreateRunCustomerCSV obj = new CreateRunCustomerCSV();
		if (args.length < 6)
			throw new RuntimeException("Incorrect number of Inputs need InputFile, startingUserId, outputLocation, userRequired, userColumnInCSV, mulFactor");
		String inputFile = args[0];
		String startingUserId = args[1];
		String outputLocation = args[2];
		double userRequired = Double.parseDouble(args[3]);
		int userColumnInCSV = Integer.parseInt(args[4]);
		int cust_multFactor = Integer.parseInt(args[5]);
		System.out.println(" Rewrite value is " + cust_multFactor);
		obj.Trim(inputFile, startingUserId, outputLocation, userRequired, userColumnInCSV, cust_multFactor);		
	}

}
