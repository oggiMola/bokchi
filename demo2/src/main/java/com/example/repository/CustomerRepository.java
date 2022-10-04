package com.example.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.example.dto.Customer;

import reactor.core.publisher.Flux;

@Repository
public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {

    Flux<Customer> findByFirstName(
            String firstName);

    Flux<Customer> findByLastName(
            String lastName);

}
// public interface CustomerRepository extends MongoRepository<Customer, String>
// {

// public Customer findByFirstName(String firstName);

// public List<Customer> findByLastName(String lastName);

// }