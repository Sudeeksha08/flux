package com.brightloop.customer.mapper;

import com.brightloop.customer.dto.CustomerResponse;
import com.brightloop.customer.entity.Customer;

public final class CustomerMapper {
    private CustomerMapper() { }

    public static CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getUser().getId(), customer.getName(),
                customer.getEmail(), customer.getCompanyName(), customer.getPhone(), customer.getAddress(),
                customer.isArchived(), customer.getCreatedAt());
    }
}
