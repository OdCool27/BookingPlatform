package services;

import dao.PropertyDAO;
import helpers.idGenerators;
import models.*;
import java.util.List;
import static services.ServiceException.Reason.*;

public class PropertyManagementService {
    public List<Property> list(User actor) { AuthService.requireActive(actor); return PropertyDAO.retrieveAllProperties(); }
    public List<Property> mine(User actor) { return viewMyProperties(AuthService.owner(actor).getOwnerID()); }
    public Property find(User actor,String id) { AuthService.requireActive(actor); return required(id); }
    private Property required(String id) {
        Property p = findProperty(id);
        if(p == null) throw new ServiceException(NOT_FOUND,"Property not found");
        return p;
    }
    private void owned(User actor,Property p) {
        if(!AuthService.owner(actor).getOwnerID().equals(p.getOwnerID()))
            throw new ServiceException(FORBIDDEN,"Property belongs to another owner");
    }
    public Property create(User actor,Property p) {
        p.setOwnerID(AuthService.owner(actor).getOwnerID());
        validate(p); p.setPropertyID(idGenerators.propertyIDGenerator(p.getAddress()));
        addProperty(p); return p;
    }
    public Property update(User actor,String id,Property input) {
        Property existing = required(id); owned(actor,existing); validate(input);
        input.setOwnerID(existing.getOwnerID()); input.setPropertyID(id);
        updateProperty(input); return input;
    }
    public void delete(User actor,String id) {
        Property existing = required(id); owned(actor,existing); deleteProperty(id);
    }
    private static void validate(Property p) {
        Validation.text(p.getPropertyName(),"propertyName",100);
        if(p.getDescription() == null || p.getDescription().length() > 2000)
            throw new ServiceException(INVALID,"description must be at most 2000 characters");
        if(p.getAddress() == null) throw new ServiceException(INVALID,"address is required");
        Address a = p.getAddress();
        Validation.text(a.getStreet(),"street",100); Validation.text(a.getCity(),"city",100);
        Validation.text(a.getState(),"state",100); Validation.text(a.getCountry(),"country",100);
        if((a.getSuite() != null && a.getSuite().length() > 100) || (a.getPostalCode() != null && a.getPostalCode().length() > 20))
            throw new ServiceException(INVALID,"Address field too long");
        if(p.getBaseRate() == null || p.getBaseRate().signum() < 0 || p.getBaseRate().precision() > 12 || p.getBaseRate().scale() > 2)
            throw new ServiceException(INVALID,"baseRate must be nonnegative with at most 2 decimal places and 12 digits");
        if(!Double.isFinite(p.getLatitude()) || !Double.isFinite(p.getLongitude()) ||
                Math.abs(p.getLatitude()) > 90 || Math.abs(p.getLongitude()) > 180 ||
                !Double.isFinite(p.getHouseSize()) || !Double.isFinite(p.getLandSize()) ||
                p.getHouseSize() < 0 || p.getLandSize() < 0)
            throw new ServiceException(INVALID,"Invalid coordinates or sizes");
    }
    private void addProperty(Property p) {
        if(!PropertyDAO.saveProperty(p)) throw new IllegalStateException("Property insert failed");
    }
    private void updateProperty(Property p) {
        if(!PropertyDAO.updateProperty(p)) throw new ServiceException(NOT_FOUND,"Property not found");
    }
    private void deleteProperty(String id) {
        if(!PropertyDAO.deleteProperty(id)) throw new ServiceException(NOT_FOUND,"Property not found");
    }
    private List<Property> viewMyProperties(String id) { return PropertyDAO.retrievePropertiesByOwnerID(id); }
    private Property findProperty(String id) { return PropertyDAO.retrievePropertyByPropertyID(id); }
}
