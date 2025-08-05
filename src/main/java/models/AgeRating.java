package models;

public class AgeRating {
    private String ageRatingCode;
    private String description;

    public AgeRating() {
    }

    public AgeRating(String ageRatingCode, String description) {
        this.ageRatingCode = ageRatingCode;
        this.description = description;
    }

    public String getAgeRatingCode() {
        return ageRatingCode;
    }

    public void setAgeRatingCode(String ageRatingCode) {
        this.ageRatingCode = ageRatingCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

