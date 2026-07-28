package com.golfclub.api.service;

import com.golfclub.api.domain.MembershipType;
import com.golfclub.api.dto.MemberDto;

import java.time.LocalDate;
import java.util.List;

//The controller talks to this interface,
//not to MemberServiceImpl directly.
public interface MemberService {

    MemberDto.Response create(MemberDto.Request request);

    MemberDto.Response getById(Long id);

    List<MemberDto.Response> getAll();

    List<MemberDto.Response> searchByName(String name);

    List<MemberDto.Response> searchByMembershipType(MembershipType membershipType);

    List<MemberDto.Response> searchByPhone(String phone);

    //Members in a tournament starting on this date.
    List<MemberDto.Response> searchByTournamentStartDate(LocalDate startDate);
}
