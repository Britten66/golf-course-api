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

//A club member.
//@Entity means this class becomes a table.
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

    //EnumType.STRING saves the word "ANNUAL".
    //Without it JPA saves a number instead.
    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type", nullable = false)
    private MembershipType membershipType;

    //The many to many link.
    //@JoinTable is on this side, so this is
    //the side that actually saves the rows.
    @ManyToMany
    @JoinTable(
            name = "member_tournament",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "tournament_id")
    )
    private Set<Tournament> tournaments = new HashSet<>();

    //Adds the link on both objects.
    //JPA does not do this for us.
    public void addTournament(Tournament tournament) {
        this.tournaments.add(tournament);
        tournament.getParticipatingMembers().add(this);
    }
}
