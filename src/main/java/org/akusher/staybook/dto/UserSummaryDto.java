package org.akusher.staybook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryDto {
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String role;

    // Aggregated statistics across all 3 databases
    private int totalBookings;
    private int confirmedBookings;
    private long totalNightsStayed;
    private BigDecimal totalAmountSpent;
    private BigDecimal averageBookingCost;

    // Detailed bookings enriched with room details
    private List<BookingDetailsDto> bookings;
}
