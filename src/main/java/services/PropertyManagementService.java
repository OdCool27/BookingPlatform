package services;

import dao.PropertyDAO;
import models.Property;

import java.util.ArrayList;
import java.util.List;

public class PropertyManagementService {

    private final PropertyDAO pDAO = new PropertyDAO();

    public void addProperty(Property p){
        pDAO.saveProperty(p);
        System.out.println("Property: " + p.getPropertyName() + " has been saved");
    }

    public void updateProperty(Property p){
        if(pDAO.updateProperty(p)){
            System.out.println("Property has been updated successfully!");
        }else{
            System.out.println("Update has failed.");
        }

    }

    public void deleteProperty(String propertyID) {

        if(pDAO.deleteProperty(propertyID)){
            System.out.println("Property has been deleted.");
        }else{
            System.out.println("Deletion failed.");
        }
    }

    public List<Property> viewMyProperties(String ownerID) {
       List<Property> propList = pDAO.retrievePropertiesByOwnerID(ownerID);
       if (propList!=null) {
           System.out.println(propList.size() + " Properties found.\n");
           return propList;
       }else{
           System.out.println("No Properties Found.");
           return new ArrayList<Property>();
       }

    }

    public Property findProperty(String propertyID){
        Property p = pDAO.retrievePropertyByPropertyID(propertyID);

        if (p!=null) {
            System.out.println("Property found!\n");
            return p;
        }

        return null;

    }
}
