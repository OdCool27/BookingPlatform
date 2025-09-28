package helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dao.PropertyDAO;
import models.Address;
import models.Property;

public class idGenerators {
	
	//HELPS WITH CREATING UNIQUE IDS
	public static int randomNumberGenerator() {
		Random rand = new Random();
		int randNum = rand.nextInt(90000) + 10000;
		return randNum;
	}
	
	//GENERATES PROPERTY ID
	public static String propertyIDGenerator(Address address) {

		String output;
		List<Property> propertyList = PropertyDAO.retrieveAllProperties();
		List<String> propIdList = new ArrayList<>();

		for (Property prop : propertyList) {
			propIdList.add(prop.getPropertyID());
		}

		do{
			output = "";

			//Based on Country
			switch (address.getCountry().toUpperCase()) {

				case "JAMAICA":
					output += "JA";
					break;

				case "TRINIDAD"://An Example of another country
					output += "TT";
					break;

				default:
					output += "YY";
					break;
			}

			//BREAK
			output += "-";


			//Based On State
			String parish = address.getState().trim().toLowerCase().replace(".", "");

			// Handle missing spaces in "St" parishes
			if (parish.startsWith("st") && parish.length() > 2 && parish.charAt(2) != ' ') {
				parish = "st " + parish.substring(2);
			}

			switch (parish) {
				case "kingston":
					output += "KS";
					break;
				case "st andrew":
					output += "SA";
					break;
				case "st thomas":
					output += "ST";
					break;
				case "portland":
					output += "PL";
					break;
				case "st mary":
					output += "SM";
					break;
				case "st ann":
					output += "SN";
					break;
				case "trelawny":
					output += "TR";
					break;
				case "st james":
					output += "SJ";
					break;
				case "hanover":
					output += "HN";
					break;
				case "westmoreland":
					output += "WM";
					break;
				case "st elizabeth":
					output += "SE";
					break;
				case "manchester":
					output += "MN";
					break;
				case "clarendon":
					output += "CL";
					break;
				case "st catherine":
					output += "SC";
					break;
				default:
					output += "XX"; // Unknown or invalid parish
					break;
			}

			//BREAK
			output += "-";


			//ADDS RANDOM NUMBER AT THE END
			output += randomNumberGenerator();

		}while (propIdList.contains(output));

		return output;

	}
	
	
	//GENERATES OWNER IDS
	public static String ownerIDGenerator(String firstName, String lastName) {
		String output = "PO-";
		output += firstName.charAt(0);
		output += firstName.charAt(1);
		output += lastName.charAt(0);
		output += lastName.charAt(1);
		output += "-";
		output += randomNumberGenerator();
		
		return output.toUpperCase();
		
		
	}
	
	
	//GENERATES GUEST IDS
	public static String guestIDGenerator(String firstName, String lastName) {
			String output = "G-";
			output += firstName.charAt(0);
			output += firstName.charAt(1);
			output += lastName.charAt(0);
			output += lastName.charAt(1);
			output += "-";
			output += randomNumberGenerator();
			
			return output.toUpperCase();
			
			
		}
	
	
	//GENERATES BOOKING IDS
	public static String bookingIDGenerator(int lastIDNum){
		return "B-" + (lastIDNum + 1);
	}


	//GENERATE REVIEW IDS
	public static String reviewIDGenerator(int lastIDNum){
		return "R-" + (lastIDNum + 1);
	}
	

}
