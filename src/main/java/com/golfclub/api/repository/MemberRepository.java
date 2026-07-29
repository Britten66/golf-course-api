package com.golfclub.api.repository;

import com.golfclub.api.domain.Member;
import com.golfclub.api.domain.MembershipType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByNameContainingIgnoreCase(String name);

    List<Member> findByMembershipType(MembershipType membershipType);

    List<Member> findByPhoneContaining(String phone);

    @Query("select distinct m from Member m join m.tournaments t where t.startDate = :startDate")
    List<Member> findByTournamentStartDate(@Param("startDate") LocalDate startDate);

    boolean existsByEmailIgnoreCase(String email);
}
