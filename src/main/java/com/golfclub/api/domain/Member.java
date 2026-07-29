package com.golfclub.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(name = "membership_start_date", nullable = false)
    private LocalDate membershipStartDate;

    @Column(name = "membership_duration", nullable = false)
    private int membershipDuration;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type", nullable = false)
    private MembershipType membershipType;

    @ManyToMany
    @JoinTable(
            name = "member_tournament",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "tournament_id")
    )
    private Set<Tournament> tournaments = new HashSet<>();

    public void addTournament(Tournament tournament) {
        this.tournaments.add(tournament);
        tournament.getParticipatingMembers().add(this);
    }
}
