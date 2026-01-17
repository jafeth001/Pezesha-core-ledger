package com.fintech.pezesha_core_ledger.models;

import com.fintech.pezesha_core_ledger.enums.AccountType;
import com.fintech.pezesha_core_ledger.enums.Currency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * This class defines where money is stored conceptually.
 */

@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_account_code", columnList = "code"),
        @Index(name = "idx_account_type", columnList = "type"),
        @Index(name = "idx_account_parent", columnList = "parent_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Account parent;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Version
    private Long version;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}