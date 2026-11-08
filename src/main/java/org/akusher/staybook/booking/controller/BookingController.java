package org.akusher.staybook.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.akusher.staybook.booking.service.BookingService;
import org.akusher.staybook.dto.BookingRequestDto;
import org.akusher.staybook.dto.BookingResponseDto;
import org.akusher.staybook.dto.UserSummaryDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponseDto>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<BookingResponseDto> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponseDto> createBooking(@Valid @RequestBody BookingRequestDto dto) {
        BookingResponseDto created = bookingService.createBooking(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    // Equivalent to GET /api/grades/student/{id} in Lab 8
    @GetMapping("/bookings/user/{userId}")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    // Equivalent to GET /api/students/{id}/transcript in Lab 8 (Final Challenge)
    // Aggregates User info (user_db) + Bookings (booking_db) + Room info (room_db)
    @GetMapping("/users/{userId}/summary")
    public ResponseEntity<UserSummaryDto> getUserSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getUserSummary(userId));
    }
}
