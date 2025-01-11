package ru.seller_support.assignment.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RoleChangeException extends RuntimeException {

    public RoleChangeException(String message) {
        super(message);
    }
}
