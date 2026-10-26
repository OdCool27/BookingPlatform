package dto;

import models.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

/** Explicit allowlists: models contain password hashes and internal fields that must never be serialized. */
public final class ApiDtos {
    private ApiDtos() {}
    public record Register(String accountType,String email,String password,String firstName,String lastName,
                           User.Gender gender,String contactNum,String nationality,LocalDate dob,List<String> preferences) {}
    public record Login(String email,String password) {}
    public record Activation(String email,String password,String code) {}
    public record Account(int userId,String email,String firstName,String lastName,boolean activated) {
        public static Account from(User u) { return new Account(u.getUserID(),u.getEmail(),u.getFirstName(),u.getLastName(),u.isActivated()); }
    }
    public record Message(String message) {}
    public record Error(String code,String message) {}
    public record AddressData(String street,String suite,String city,String state,String postalCode,String country) {
        public Address model() { return new Address(street,suite,city,state,postalCode,country); }
        public static AddressData from(Address a) { return new AddressData(a.getStreet(),a.getSuite(),a.getCity(),a.getState(),a.getPostalCode(),a.getCountry()); }
    }
    public record PropertyInput(String propertyName,String description,AddressData address,Double latitude,Double longitude,
                                Double houseSize,Double landSize,BigDecimal baseRate) {
        public Property model() {
            if(latitude == null || longitude == null || houseSize == null || landSize == null)
                throw new services.ServiceException(services.ServiceException.Reason.INVALID,"Coordinates and sizes are required");
            Property p = new Property();
            p.setPropertyName(propertyName); p.setDescription(description);
            p.setAddress(address == null ? null : address.model());
            p.setLatitude(latitude); p.setLongitude(longitude); p.setHouseSize(houseSize); p.setLandSize(landSize);
            p.setBaseRate(baseRate); return p;
        }
    }
    public record PropertyView(String propertyId,String propertyName,String description,AddressData address,
                               double latitude,double longitude,double houseSize,double landSize,BigDecimal baseRate) {
        public static PropertyView from(Property p) { return new PropertyView(p.getPropertyID(),p.getPropertyName(),p.getDescription(),
                AddressData.from(p.getAddress()),p.getLatitude(),p.getLongitude(),p.getHouseSize(),p.getLandSize(),p.getBaseRate()); }
    }
    public record BookingInput(String propertyId,LocalDate arrivalDate,LocalDate departureDate) {}
    public record Dates(LocalDate arrivalDate,LocalDate departureDate) {}
    public record BookingView(String bookingId,String propertyId,LocalDate arrivalDate,LocalDate departureDate,
                              BigDecimal baseCharge,BigDecimal gct,BigDecimal tef,BigDecimal gart,BigDecimal totalCharge,
                              Booking.Status status,Booking.PaymentStatus paymentStatus,LocalDateTime createdAt,LocalDateTime updatedAt) {
        public static BookingView from(Booking b) { return new BookingView(b.getBookingID(),b.getPropertyID(),b.getArrivalDate(),b.getDepartureDate(),
                b.getBaseCharge(),b.getGct(),b.getTef(),b.getGart(),b.getTotalCharge(),b.getStatus(),b.getPayStatus(),b.getCreatedAt(),b.getUpdatedAt()); }
    }
}
