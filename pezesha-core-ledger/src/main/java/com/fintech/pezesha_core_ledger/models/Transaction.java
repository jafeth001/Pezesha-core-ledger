package com.fintech.pezesha_core_ledger.models;

import com.fintech.pezesha_core_ledger.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This represents a complete accounting transaction
 */

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_idempotency", columnList = "idempotency_key"),
        @Index(name = "idx_transaction_posted_at", columnList = "posted_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "reversal_of")
    private String reversalOf;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL)
    private List<TransactionEntry> entries;

    @Version
    private Long version;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

public void addEntry(TransactionEntry entry) {
    if (this.entries == null) {
        this.entries = new ArrayList<>();
    }
    this.entries.add(entry);
    entry.setTransaction(this);
}

}
