package com.techie.microservices.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    private String skuCode;
    private BigDecimal price;
    private Integer quantity;
    private UserDetails userDetails;

    public void setUserDetails(String firstName, String lastName, String email) {
        this.userDetails = new UserDetails(firstName, lastName, email);
    }

}
