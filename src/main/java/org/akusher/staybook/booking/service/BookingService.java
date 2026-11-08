package org.akusher.staybook.booking.service;

import lombok.RequiredArgsConstructor;
import org.akusher.staybook.booking.entity.Booking;
import org.akusher.staybook.booking.repository.BookingRepository;
import org.akusher.staybook.dto.*;
import org.akusher.staybook.exception.BadRequestException;
import org.akusher.staybook.exception.ResourceNotFoundException;
import org.akusher.staybook.room.entity.Room;
import org.akusher.staybook.room.repository.RoomRepository;
import org.akusher.staybook.user.entity.User;
import org.akusher.staybook.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    @Transactional(transactionManager = "bookingTransactionManager", readOnly = true)
    public List<BookingResponseDto> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(transactionManager = "bookingTransactionManager", readOnly = true)
    public BookingResponseDto getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return mapToResponse(booking);
    }

    @Transactional(transactionManager = "bookingTransactionManager", readOnly = true)
    public List<BookingResponseDto> getBookingsByUserId(Long userId) {
        // Cross-database verification: ensure user exists in staybook_user_db
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return bookingRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(transactionManager = "bookingTransactionManager")
    public BookingResponseDto createBooking(BookingRequestDto dto) {
        // 1. Cross-database verification: check if user exists in staybook_user_db
        if (!userRepository.existsById(dto.getUserId())) {
            throw new ResourceNotFoundException("Cannot create booking: User not found with id: " + dto.getUserId());
        }

        // 2. Cross-database verification: check if room exists in staybook_room_db
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Cannot create booking: Room not found with id: " + dto.getRoomId()));

        // 3. Business validation: room must be active
        if (Boolean.FALSE.equals(room.getIsActive())) {
            throw new BadRequestException("Room " + room.getRoomNumber() + " is currently inactive or under maintenance");
        }

        // 4. Business validation: check dates logic
        if (dto.getCheckOutDate().isBefore(dto.getCheckInDate()) || dto.getCheckOutDate().isEqual(dto.getCheckInDate())) {
            throw new BadRequestException("Check-out date (" + dto.getCheckOutDate()
                    + ") must be strictly after check-in date (" + dto.getCheckInDate() + ")");
        }

        long numberOfNights = ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());
        BigDecimal calculatedTotalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(numberOfNights));

        Booking booking = Booking.builder()
                .userId(dto.getUserId())
                .roomId(dto.getRoomId())
                .checkInDate(dto.getCheckInDate())
                .checkOutDate(dto.getCheckOutDate())
                .totalPrice(calculatedTotalPrice)
                .status(dto.getStatus() != null && !dto.getStatus().isBlank() ? dto.getStatus().trim().toUpperCase() : "CONFIRMED")
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    @Transactional(transactionManager = "bookingTransactionManager")
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        bookingRepository.delete(booking);
    }

    /**
     * Cross-Database Data Aggregation (Laboratory 8 Final Challenge)
     * Combines data from all 3 independent databases:
     * - User details from staybook_user_db
     * - Bookings list from staybook_booking_db
     * - Room details for each booking from staybook_room_db
     * Calculates total nights stayed, total amount spent, and average booking cost.
     */
    @Transactional(readOnly = true)
    public UserSummaryDto getUserSummary(Long userId) {
        // 1. Fetch user from staybook_user_db
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // 2. Fetch all bookings for this user from staybook_booking_db
        List<Booking> userBookings = bookingRepository.findByUserId(userId);

        List<BookingDetailsDto> bookingDetailsList = new ArrayList<>();
        BigDecimal totalAmountSpent = BigDecimal.ZERO;
        long totalNightsStayed = 0;
        int confirmedBookings = 0;

        // 3. For each booking, fetch corresponding room details from staybook_room_db
        for (Booking booking : userBookings) {
            long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
            if (nights <= 0) {
                nights = 1;
            }

            Room room = roomRepository.findById(booking.getRoomId()).orElse(null);

            RoomSummaryDto roomSummary = null;
            if (room != null) {
                roomSummary = RoomSummaryDto.builder()
                        .roomId(room.getId())
                        .roomNumber(room.getRoomNumber())
                        .roomType(room.getRoomType())
                        .pricePerNight(room.getPricePerNight())
                        .capacity(room.getCapacity())
                        .build();
            }

            BookingDetailsDto detailsDto = BookingDetailsDto.builder()
                    .bookingId(booking.getId())
                    .checkInDate(booking.getCheckInDate())
                    .checkOutDate(booking.getCheckOutDate())
                    .numberOfNights(nights)
                    .totalPrice(booking.getTotalPrice())
                    .status(booking.getStatus())
                    .createdAt(booking.getCreatedAt())
                    .room(roomSummary)
                    .build();

            bookingDetailsList.add(detailsDto);

            totalAmountSpent = totalAmountSpent.add(booking.getTotalPrice());
            totalNightsStayed += nights;
            if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
                confirmedBookings++;
            }
        }

        BigDecimal averageBookingCost = BigDecimal.ZERO;
        if (!userBookings.isEmpty()) {
            averageBookingCost = totalAmountSpent.divide(BigDecimal.valueOf(userBookings.size()), 2, RoundingMode.HALF_UP);
        }

        return UserSummaryDto.builder()
                .userId(user.getId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .totalBookings(userBookings.size())
                .confirmedBookings(confirmedBookings)
                .totalNightsStayed(totalNightsStayed)
                .totalAmountSpent(totalAmountSpent)
                .averageBookingCost(averageBookingCost)
                .bookings(bookingDetailsList)
                .build();
    }

    private BookingResponseDto mapToResponse(Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .userId(booking.getUserId())
                .roomId(booking.getRoomId())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
