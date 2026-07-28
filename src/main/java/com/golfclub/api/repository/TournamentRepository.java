package com.golfclub.api.repository;

import com.golfclub.api.domain.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

//Same idea as MemberRepository.
@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {

    //Search by start date.
    List<Tournament> findByStartDate(LocalDate startDate);

    //Search by location, partial and any case.
    List<Tournament> findByLocationContainingIgnoreCase(String location);
}
