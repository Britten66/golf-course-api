package com.golfclub.api.service.impl;

import com.golfclub.api.domain.Member;
import com.golfclub.api.domain.MembershipType;
import com.golfclub.api.dto.MemberDto;
import com.golfclub.api.exception.ApiException;
import com.golfclub.api.repository.MemberRepository;
import com.golfclub.api.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

//The rules live here.
//Controllers only deal with HTTP and
//repositories only deal with the database.
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public MemberDto.Response create(MemberDto.Request request) {
        if (memberRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "A member with email " + request.email() + " already exists");
        }
        Member saved = memberRepository.save(request.toEntity());
        return MemberDto.Response.from(saved);
    }

    //@Transactional keeps the database
    //connection open while we read the
    //tournaments list, which is loaded lazily.
    @Override
    @Transactional
    public MemberDto.Response getById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Member " + id + " was not found"));
        return MemberDto.Response.from(member);
    }

    @Override
    @Transactional
    public List<MemberDto.Response> getAll() {
        return MemberDto.Response.fromAll(memberRepository.findAll());
    }

    @Override
    @Transactional
    public List<MemberDto.Response> searchByName(String name) {
        return MemberDto.Response.fromAll(memberRepository.findByNameContainingIgnoreCase(name));
    }

    @Override
    @Transactional
    public List<MemberDto.Response> searchByMembershipType(MembershipType membershipType) {
        return MemberDto.Response.fromAll(memberRepository.findByMembershipType(membershipType));
    }

    @Override
    @Transactional
    public List<MemberDto.Response> searchByPhone(String phone) {
        return MemberDto.Response.fromAll(memberRepository.findByPhoneContaining(phone));
    }

    @Override
    @Transactional
    public List<MemberDto.Response> searchByTournamentStartDate(LocalDate startDate) {
        return MemberDto.Response.fromAll(memberRepository.findByTournamentStartDate(startDate));
    }
}
