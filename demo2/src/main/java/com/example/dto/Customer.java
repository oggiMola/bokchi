package com.example.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "customer")
public class Customer {
    @Id
    public String id;

    public String firstName;

    public String lastName;

    public Object subCustomer;

    public Customer() {
    }

    public Customer(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Customer(String firstName, Object subCustomer) {
        this.firstName = firstName;
        this.subCustomer = subCustomer;
    }

    // @Override
    // public String toString() {
    // return String.format(
    // "Customer[id=%s, firstName='%s', lastName='%s']",
    // id, firstName, lastName);
    // }
}
