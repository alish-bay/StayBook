package org.akusher.staybook.room;

import org.akusher.staybook.dto.RoomRequestDto;
import org.akusher.staybook.dto.RoomResponseDto;
import org.akusher.staybook.exception.DuplicateResourceException;
import org.akusher.staybook.exception.ResourceNotFoundException;
import org.akusher.staybook.room.entity.Room;
import org.akusher.staybook.room.repository.RoomRepository;
import org.akusher.staybook.room.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    private Room sampleRoom;
    private RoomRequestDto requestDto;

    @BeforeEach
    void setUp() {
        sampleRoom = Room.builder()
                .id(1L)
                .roomNumber("101")
                .roomType("STANDARD")
                .pricePerNight(new BigDecimal("100.00"))
                .capacity(2)
                .isActive(true)
                .build();

        requestDto = RoomRequestDto.builder()
                .roomNumber("101")
                .roomType("STANDARD")
                .pricePerNight(new BigDecimal("100.00"))
                .capacity(2)
                .isActive(true)
                .build();
    }

    @Test
    void createRoom_Success() {
        when(roomRepository.existsByRoomNumber("101")).thenReturn(false);
        when(roomRepository.save(any(Room.class))).thenReturn(sampleRoom);

        RoomResponseDto response = roomService.createRoom(requestDto);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getRoomNumber()).isEqualTo("101");
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void createRoom_DuplicateRoomNumber_ThrowsDuplicateResourceException() {
        when(roomRepository.existsByRoomNumber("101")).thenReturn(true);

        assertThatThrownBy(() -> roomService.createRoom(requestDto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void getRoomById_NotFound_ThrowsResourceNotFoundException() {
        when(roomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.getRoomById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Room not found");
    }
}
