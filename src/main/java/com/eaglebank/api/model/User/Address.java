package com.eaglebank.api.model.User;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Embeddable
public class Address {
    private String line1;
    private String postcode;
    private String city;
    private String country;
}
