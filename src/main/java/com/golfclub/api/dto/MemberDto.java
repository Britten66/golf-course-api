package com.golfclub.api.dto;

import com.golfclub.api.domain.Member;
import com.golfclub.api.domain.MembershipType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MemberDto {

    public record Request(

            @NotBlank(message = "name is required")
            String name,

            @NotBlank(message = "address is required")
            String address,

            @NotBlank(message = "email is required")
            @Email(message = "email must be a valid address")
            String email,

            @NotBlank(message = "phone is required")
            String phone,

            @NotNull(message = "membershipStartDate is required")
            LocalDate membershipStartDate,

            @Min(value = 1, message = "membershipDuration must be at least 1 month")
            int membershipDuration,

            @NotNull(message = "membershipType is required (ANNUAL, MONTHLY or LIFETIME)")
            MembershipType membershipType
    ) {
        public Member toEntity() {
            Member member = new Member();
            member.setName(name);
            member.setAddress(address);
            member.setEmail(email);
            member.setPhone(phone);
            member.setMembershipStartDate(membershipStartDate);
            member.setMembershipDuration(membershipDuration);
            member.setMembershipType(membershipType);
            return member;
        }
    }

    public record Response(
            Long id,
            String name,
            String address,
            String email,
            String phone,
            LocalDate membershipStartDate,
            int membershipDuration,
            MembershipType membershipType,
            List<TournamentDto.Summary> tournaments
    ) {
        public static Response from(Member member) {
            List<TournamentDto.Summary> tournaments = new ArrayList<>();
            member.getTournaments().forEach(t -> tournaments.add(TournamentDto.Summary.from(t)));

            return new Response(
                    member.getId(),
                    member.getName(),
                    member.getAddress(),
                    member.getEmail(),
                    member.getPhone(),
                    member.getMembershipStartDate(),
                    member.getMembershipDuration(),
                    member.getMembershipType(),
                    tournaments);
        }

        public static List<Response> fromAll(List<Member> members) {
            List<Response> list = new ArrayList<>();
            members.forEach(m -> list.add(from(m)));
            return list;
        }
    }

    public record Summary(
            Long id,
            String name,
            String email,
            String phone,
            MembershipType membershipType
    ) {
        public static Summary from(Member member) {
            return new Summary(
                    member.getId(),
                    member.getName(),
                    member.getEmail(),
                    member.getPhone(),
                    member.getMembershipType());
        }
    }
}
