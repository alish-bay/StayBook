package org.akusher.staybook.booking;

import org.akusher.staybook.booking.entity.Booking;
import org.akusher.staybook.booking.repository.BookingRepository;
import org.akusher.staybook.booking.service.BookingService;
import org.akusher.staybook.dto.BookingRequestDto;
import org.akusher.staybook.dto.BookingResponseDto;
import org.akusher.staybook.dto.UserSummaryDto;
import org.akusher.staybook.exception.BadRequestException;
import org.akusher.staybook.exception.ResourceNotFoundException;
import org.akusher.staybook.room.entity.Room;
import org.akusher.staybook.room.repository.RoomRepository;
import org.akusher.staybook.user.entity.User;
import org.akusher.staybook.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private BookingService bookingService;

    private User sampleUser;
    private Room sampleRoom;
    private Booking sampleBooking;
    private BookingRequestDto requestDto;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+1-555-0101")
                .role("GUEST")
                .build();

        sampleRoom = Room.builder()
                .id(10L)
                .roomNumber("201")
                .roomType("DELUXE")
                .pricePerNight(new BigDecimal("150.00"))
                .capacity(3)
                .isActive(true)
                .build();

        sampleBooking = Booking.builder()
                .id(100L)
                .userId(1L)
                .roomId(10L)
                .checkInDate(LocalDate.of(2026, 12, 1))
                .checkOutDate(LocalDate.of(2026, 12, 4))
                .totalPrice(new BigDecimal("450.00"))
                .status("CONFIRMED")
                .createdAt(LocalDateTime.now())
                .build();

        requestDto = BookingRequestDto.builder()
                .userId(1L)
                .roomId(10L)
                .checkInDate(LocalDate.of(2026, 12, 1))
                .checkOutDate(LocalDate.of(2026, 12, 4))
                .status("CONFIRMED")
                .build();
    }

    @Test
    void createBooking_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(roomRepository.findById(10L)).thenReturn(Optional.of(sampleRoom));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        BookingResponseDto response = bookingService.createBooking(requestDto);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getTotalPrice()).isEqualByComparingTo(new BigDecimal("450.00"));
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBooking_UserNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.existsById(999L)).thenReturn(false);
        requestDto.setUserId(999L);

        assertThatThrownBy(() -> bookingService.createBooking(requestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_RoomNotFound_ThrowsResourceNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());
        requestDto.setRoomId(999L);

        assertThatThrownBy(() -> bookingService.createBooking(requestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Room not found with id: 999");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_InvalidDates_ThrowsBadRequestException() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(roomRepository.findById(10L)).thenReturn(Optional.of(sampleRoom));

        // Check-out before check-in
        requestDto.setCheckInDate(LocalDate.of(2026, 12, 10));
        requestDto.setCheckOutDate(LocalDate.of(2026, 12, 5));

        assertThatThrownBy(() -> bookingService.createBooking(requestDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must be strictly after check-in date");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getUserSummary_CombinesDataFromAllThreeDatabases() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of(sampleBooking));
        when(roomRepository.findById(10L)).thenReturn(Optional.of(sampleRoom));

        UserSummaryDto summary = bookingService.getUserSummary(1L);

        assertThat(summary).isNotNull();
        assertThat(summary.getUserId()).isEqualTo(1L);
        assertThat(summary.getFullName()).isEqualTo("John Doe");
        assertThat(summary.getTotalBookings()).isEqualTo(1);
        assertThat(summary.getConfirmedBookings()).isEqualTo(1);
        assertThat(summary.getTotalNightsStayed()).isEqualTo(3);
        assertThat(summary.getTotalAmountSpent()).isEqualByComparingTo(new BigDecimal("450.00"));
        assertThat(summary.getAverageBookingCost()).isEqualByComparingTo(new BigDecimal("450.00"));

        assertThat(summary.getBookings()).hasSize(1);
        assertThat(summary.getBookings().get(0).getRoom()).isNotNull();
        assertThat(summary.getBookings().get(0).getRoom().getRoomNumber()).isEqualTo("201");
        assertThat(summary.getBookings().get(0).getRoom().getRoomType()).isEqualTo("DELUXE");
    }
}
