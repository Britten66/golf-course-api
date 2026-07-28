package com.golfclub.api.repository;

import com.golfclub.api.domain.Member;
import com.golfclub.api.domain.MembershipType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

//We only write the method names here.
//Spring Data works out the SQL from the
//name and writes the code for us.
//JpaRepository already gives us save,
//findById, findAll and deleteById.
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    //Search by name, partial and any case.
    List<Member> findByNameContainingIgnoreCase(String name);

    //Search by membership type.
    List<Member> findByMembershipType(MembershipType membershipType);

    //Search by phone, partial match.
    List<Member> findByPhoneContaining(String phone);

    //Search members by tournament start date.
    //This one needs a join, so it is easier
    //to just write the query out.
    //distinct stops a member showing twice.
    @Query("select distinct m from Member m join m.tournaments t where t.startDate = :startDate")
    List<Member> findByTournamentStartDate(@Param("startDate") LocalDate startDate);

    boolean existsByEmailIgnoreCase(String email);
}
