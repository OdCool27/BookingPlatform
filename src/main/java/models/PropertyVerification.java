package models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PropertyVerification {
    private String verificationID;             // Unique ID for this verification record
    private String propertyID;                 // Foreign key to Property
    private VerificationStatus status;         // PENDING, VERIFIED, REJECTED
    private String ownershipDeclaredName;      // Name on the submitted document
    private String documentUploadURL;          // URL/path to uploaded file
    private List<String> supportingDocs;       // Other docs (e.g. utility bills)
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String verifiedByAdminID;          // Admin userID who reviewed it
    private String notes;                      // Admin notes (e.g. reasons for rejection)

    enum VerificationStatus{PENDING, VERIFIED, REJECTED}


    public PropertyVerification(){
        this.verificationID = "";
        this.propertyID = "";
        this.status = VerificationStatus.PENDING;
        this.ownershipDeclaredName = "";
        this.documentUploadURL = "";
        this.supportingDocs = new ArrayList<>();
        this.submittedAt = LocalDateTime.now();
        this.reviewedAt = LocalDateTime.MAX;
        this.verifiedByAdminID = "";
        this.notes = "";

    }


    public PropertyVerification(String verificationID, String propertyID, VerificationStatus status,
                                String ownershipDeclaredName, String documentUploadURL, List<String> supportingDocs,
                                LocalDateTime submittedAt, LocalDateTime reviewedAt, String verifiedByAdminID, String notes) {
        this.verificationID = verificationID;
        this.propertyID = propertyID;
        this.status = status;
        this.ownershipDeclaredName = ownershipDeclaredName;
        this.documentUploadURL = documentUploadURL;
        this.supportingDocs = supportingDocs;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
        this.verifiedByAdminID = verifiedByAdminID;
        this.notes = notes;
    }


    public PropertyVerification(PropertyVerification pV) {
        this.verificationID = pV.verificationID;
        this.propertyID = pV.propertyID;
        this.status = pV.status;
        this.ownershipDeclaredName = pV.ownershipDeclaredName;
        this.documentUploadURL = pV.documentUploadURL;
        this.supportingDocs = pV.supportingDocs;
        this.submittedAt = pV.submittedAt;
        this.reviewedAt = pV.reviewedAt;
        this.verifiedByAdminID = pV.verifiedByAdminID;
        this.notes = pV.notes;
    }


    //ACCESSORS
    public String getVerificationID() {
        return verificationID;
    }

    public String getPropertyID() {
        return propertyID;
    }

    public VerificationStatus getStatus() {
        return status;
    }

    public String getOwnershipDeclaredName() {
        return ownershipDeclaredName;
    }

    public String getDocumentUploadURL() {
        return documentUploadURL;
    }

    public List<String> getSupportingDocs() {
        return supportingDocs;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public String getVerifiedByAdminID() {
        return verifiedByAdminID;
    }

    public String getNotes() {
        return notes;
    }


    //MUTATORS
    public void setVerificationID(String verificationID) {
        this.verificationID = verificationID;
    }

    public void setPropertyID(String propertyID) {
        this.propertyID = propertyID;
    }

    public void setStatus(VerificationStatus status) {
        this.status = status;
    }

    public void setOwnershipDeclaredName(String ownershipDeclaredName) {
        this.ownershipDeclaredName = ownershipDeclaredName;
    }

    public void setDocumentUploadURL(String documentUploadURL) {
        this.documentUploadURL = documentUploadURL;
    }

    public void setSupportingDocs(List<String> supportingDocs) {
        this.supportingDocs = supportingDocs;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public void setVerifiedByAdminID(String verifiedByAdminID) {
        this.verifiedByAdminID = verifiedByAdminID;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }


    //TO STRING


    @Override
    public String toString() {
        return  "verificationID='" + verificationID +
                "\npropertyID='" + propertyID +
                "\nstatus=" + status +
                "\nownershipDeclaredName='" + ownershipDeclaredName +
                "\ndocumentUploadURL='" + documentUploadURL +
                "\nsupportingDocs=" + supportingDocs +
                "\nsubmittedAt=" + submittedAt +
                "\nreviewedAt=" + reviewedAt +
                "\nverifiedByAdminID='" + verifiedByAdminID +
                "\nnotes='" + notes ;
    }


    public void displayPropertyVerification(){
        System.out.println(this);
    }
}




