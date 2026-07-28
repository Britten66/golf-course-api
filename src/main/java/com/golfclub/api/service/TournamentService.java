package com.golfclub.api.service;

import com.golfclub.api.dto.TournamentDto;

import java.time.LocalDate;
import java.util.List;

//Same idea as MemberService.
public interface TournamentService {

    TournamentDto.Response create(TournamentDto.Request request);

    TournamentDto.Response getById(Long id);

    List<TournamentDto.Response> getAll();

    List<TournamentDto.Response> searchByStartDate(LocalDate startDate);

    List<TournamentDto.Response> searchByLocation(String location);

    //Put a member into a tournament.
    TournamentDto.Response registerMember(Long tournamentId, Long memberId);
}
