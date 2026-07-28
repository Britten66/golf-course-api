package com.golfclub.api.dto;

import com.golfclub.api.domain.Tournament;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//Same idea as MemberDto.
public class TournamentDto {

    public record Request(

            @NotNull(message = "startDate is required")
            LocalDate startDate,

            @NotNull(message = "endDate is required")
            LocalDate endDate,

            @NotBlank(message = "location is required")
            String location,

            @NotNull(message = "entryFee is required")
            @DecimalMin(value = "0.0", message = "entryFee cannot be negative")
            BigDecimal entryFee,

            @NotNull(message = "cashPrize is required")
            @DecimalMin(value = "0.0", message = "cashPrize cannot be negative")
            BigDecimal cashPrize
    ) {
        public Tournament toEntity() {
            Tournament tournament = new Tournament();
            tournament.setStartDate(startDate);
            tournament.setEndDate(endDate);
            tournament.setLocation(location);
            tournament.setEntryFee(entryFee);
            tournament.setCashPrize(cashPrize);
            return tournament;
        }
    }

    public record Response(
            Long id,
            LocalDate startDate,
            LocalDate endDate,
            String location,
            BigDecimal entryFee,
            BigDecimal cashPrize,
            List<MemberDto.Summary> participatingMembers
    ) {
        public static Response from(Tournament tournament) {
            List<MemberDto.Summary> members = new ArrayList<>();
            tournament.getParticipatingMembers().forEach(m -> members.add(MemberDto.Summary.from(m)));

            return new Response(
                    tournament.getId(),
                    tournament.getStartDate(),
                    tournament.getEndDate(),
                    tournament.getLocation(),
                    tournament.getEntryFee(),
                    tournament.getCashPrize(),
                    members);
        }

        public static List<Response> fromAll(List<Tournament> tournaments) {
            List<Response> list = new ArrayList<>();
            tournaments.forEach(t -> list.add(from(t)));
            return list;
        }
    }

    //No members in here, so the JSON stops.
    public record Summary(
            Long id,
            LocalDate startDate,
            LocalDate endDate,
            String location,
            BigDecimal entryFee,
            BigDecimal cashPrize
    ) {
        public static Summary from(Tournament tournament) {
            return new Summary(
                    tournament.getId(),
                    tournament.getStartDate(),
                    tournament.getEndDate(),
                    tournament.getLocation(),
                    tournament.getEntryFee(),
                    tournament.getCashPrize());
        }
    }
}
