package com.golfclub.api.controller;

import com.golfclub.api.dto.TournamentDto;
import com.golfclub.api.service.TournamentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TournamentDto.Response create(@Valid @RequestBody TournamentDto.Request request) {
        return tournamentService.create(request);
    }

    @GetMapping
    public List<TournamentDto.Response> getAll() {
        return tournamentService.getAll();
    }

    @GetMapping("/{id}")
    public TournamentDto.Response getById(@PathVariable Long id) {
        return tournamentService.getById(id);
    }

    @GetMapping("/search/by-start-date")
    public List<TournamentDto.Response> searchByStartDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return tournamentService.searchByStartDate(startDate);
    }

    @GetMapping("/search/by-location")
    public List<TournamentDto.Response> searchByLocation(@RequestParam String location) {
        return tournamentService.searchByLocation(location);
    }

    @PostMapping("/{tournamentId}/members/{memberId}")
    public TournamentDto.Response registerMember(@PathVariable Long tournamentId,
                                                 @PathVariable Long memberId) {
        return tournamentService.registerMember(tournamentId, memberId);
    }
}
