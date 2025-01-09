package kr.hhplus.be.server.domain.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Embedded
    private Address address;

    public User(Long id, String username, Address address) {
        this(username, address);
        this.id = id;
    }

    public User(String username, Address address) {
        this.username = username;
        this.address = address;
    }

    public String getAddressLine1() {
        return address.getAddressLine1();
    }

    public String getAddressLine2() {
        return address.getAddressLine2();
    }

    public String getPostalCode() {
        return address.getPostalCode();
    }
}

