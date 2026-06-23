package de.mainwetten.legal;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "app.legal")
public class LegalProperties {

    @NotBlank
    private String fullName;

    @NotBlank
    private String street;

    @NotBlank
    private String postalCode;

    @NotBlank
    private String city;

    @NotBlank
    private String country;

    @NotBlank
    private String email;

    @NotBlank
    private String supervisoryAuthorityName;

    @NotBlank
    private String supervisoryAuthorityAddress;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSupervisoryAuthorityName() {
        return supervisoryAuthorityName;
    }

    public void setSupervisoryAuthorityName(
            String supervisoryAuthorityName
    ) {
        this.supervisoryAuthorityName =
                supervisoryAuthorityName;
    }

    public String getSupervisoryAuthorityAddress() {
        return supervisoryAuthorityAddress;
    }

    public void setSupervisoryAuthorityAddress(
            String supervisoryAuthorityAddress
    ) {
        this.supervisoryAuthorityAddress =
                supervisoryAuthorityAddress;
    }
}
