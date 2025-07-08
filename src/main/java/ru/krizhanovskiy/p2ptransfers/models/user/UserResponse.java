package ru.krizhanovskiy.p2ptransfers.models.user;

import lombok.Data;

@Data
public class UserResponse {
    long id;
    String email;
    String firstName;
    String lastName;
    String middleName;

    UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.middleName = user.getMiddleName();
    }
}