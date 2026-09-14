package com.project.payflow.vault.entities;

import com.project.payflow.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "card_token")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "vault_card_id", nullable = false)
    private VaultCard vaultCard;

    private UUID customer;

    @Column(nullable = false)
    private UUID merchant;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

}
