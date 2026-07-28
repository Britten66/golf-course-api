package com.golfclub.api.service.impl;

import com.golfclub.api.domain.Member;
import com.golfclub.api.domain.Tournament;
import com.golfclub.api.dto.TournamentDto;
import com.golfclub.api.exception.ApiException;
import com.golfclub.api.repository.MemberRepository;
import com.golfclub.api.repository.TournamentRepository;
import com.golfclub.api.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

//The rules for tournaments,
//including registering a member.
@Service
@RequiredArgsConstructor
public class TournamentServiceImpl implements TournamentService {

    private final TournamentRepository tournamentRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public TournamentDto.Response create(TournamentDto.Request request) {
        //An annotation cannot compare two
        //fields, so this check goes here.
        if (request.endDate().isBefore(request.startDate())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "endDate cannot be before startDate");
        }
        Tournament saved = tournamentRepository.save(request.toEntity());
        return TournamentDto.Response.from(saved);
    }

    @Override
    @Transactional
    public TournamentDto.Response getById(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tournament " + id + " was not found"));
        return TournamentDto.Response.from(tournament);
    }

    @Override
    @Transactional
    public List<TournamentDto.Response> getAll() {
        return TournamentDto.Response.fromAll(tournamentRepository.findAll());
    }

    @Override
    @Transactional
    public List<TournamentDto.Response> searchByStartDate(LocalDate startDate) {
        return TournamentDto.Response.fromAll(tournamentRepository.findByStartDate(startDate));
    }

    @Override
    @Transactional
    public List<TournamentDto.Response> searchByLocation(String location) {
        return TournamentDto.Response.fromAll(tournamentRepository.findByLocationContainingIgnoreCase(location));
    }

    //We go through the member because the
    //member owns the join table.
    @Override
    @Transactional
    public TournamentDto.Response registerMember(Long tournamentId, Long memberId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Tournament " + tournamentId + " was not found"));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Member " + memberId + " was not found"));

        if (member.getTournaments().contains(tournament)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Member " + memberId + " is already registered for tournament " + tournamentId);
        }

        member.addTournament(tournament);

        //No save() needed. Inside a transaction
        //Hibernate spots the change and writes
        //it when the method finishes.

        return TournamentDto.Response.from(tournament);
    }
}
