package com.golfclub.api.service;

import com.golfclub.api.dto.TournamentDto;

import java.time.LocalDate;
import java.util.List;

public interface TournamentService {

    TournamentDto.Response create(TournamentDto.Request request);

    TournamentDto.Response getById(Long id);

    List<TournamentDto.Response> getAll();

    List<TournamentDto.Response> searchByStartDate(LocalDate startDate);

    List<TournamentDto.Response> searchByLocation(String location);

    TournamentDto.Response registerMember(Long tournamentId, Long memberId);
}
