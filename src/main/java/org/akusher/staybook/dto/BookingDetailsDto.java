package org.akusher.staybook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailsDto {
    private Long bookingId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private long numberOfNights;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private RoomSummaryDto room;
}
