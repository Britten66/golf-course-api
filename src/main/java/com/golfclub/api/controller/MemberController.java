package com.golfclub.api.controller;

import com.golfclub.api.domain.MembershipType;
import com.golfclub.api.dto.MemberDto;
import com.golfclub.api.service.MemberService;
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
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberDto.Response create(@Valid @RequestBody MemberDto.Request request) {
        return memberService.create(request);
    }

    @GetMapping
    public List<MemberDto.Response> getAll() {
        return memberService.getAll();
    }

    @GetMapping("/{id}")
    public MemberDto.Response getById(@PathVariable Long id) {
        return memberService.getById(id);
    }

    @GetMapping("/search/by-name")
    public List<MemberDto.Response> searchByName(@RequestParam String name) {
        return memberService.searchByName(name);
    }

    @GetMapping("/search/by-type")
    public List<MemberDto.Response> searchByType(@RequestParam MembershipType type) {
        return memberService.searchByMembershipType(type);
    }

    @GetMapping("/search/by-phone")
    public List<MemberDto.Response> searchByPhone(@RequestParam String phone) {
        return memberService.searchByPhone(phone);
    }

    @GetMapping("/search/by-tournament-date")
    public List<MemberDto.Response> searchByTournamentStartDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        return memberService.searchByTournamentStartDate(startDate);
    }
}
