package com.eaglebank.api.model.Account;


import com.eaglebank.api.model.User.User;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String sortCode = "10-10-10"; // Default value from the spec

    @Enumerated(EnumType.STRING) // Stores the enum as a string (e.g., "PERSONAL")
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO; // Use BigDecimal for currency

    @Column(nullable = false)
    private String currency = "GBP"; // Default value from the spec

    @CreationTimestamp
    private Instant createdTimestamp;

    @UpdateTimestamp
    private Instant updatedTimestamp;

}
