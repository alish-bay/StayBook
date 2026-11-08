package org.akusher.staybook.room.service;

import lombok.RequiredArgsConstructor;
import org.akusher.staybook.dto.RoomRequestDto;
import org.akusher.staybook.dto.RoomResponseDto;
import org.akusher.staybook.exception.DuplicateResourceException;
import org.akusher.staybook.exception.ResourceNotFoundException;
import org.akusher.staybook.room.entity.Room;
import org.akusher.staybook.room.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    @Transactional(transactionManager = "roomTransactionManager", readOnly = true)
    public List<RoomResponseDto> getAllRooms() {
        return roomRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(transactionManager = "roomTransactionManager", readOnly = true)
    public RoomResponseDto getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        return mapToResponse(room);
    }

    @Transactional(transactionManager = "roomTransactionManager")
    public RoomResponseDto createRoom(RoomRequestDto dto) {
        String trimmedRoomNumber = dto.getRoomNumber().trim();
        if (roomRepository.existsByRoomNumber(trimmedRoomNumber)) {
            throw new DuplicateResourceException("Room with number '" + trimmedRoomNumber + "' already exists");
        }

        Room room = Room.builder()
                .roomNumber(trimmedRoomNumber)
                .roomType(dto.getRoomType().trim().toUpperCase())
                .pricePerNight(dto.getPricePerNight())
                .capacity(dto.getCapacity())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        Room savedRoom = roomRepository.save(room);
        return mapToResponse(savedRoom);
    }

    @Transactional(transactionManager = "roomTransactionManager")
    public RoomResponseDto updateRoom(Long id, RoomRequestDto dto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));

        String trimmedRoomNumber = dto.getRoomNumber().trim();
        if (roomRepository.existsByRoomNumberAndIdNot(trimmedRoomNumber, id)) {
            throw new DuplicateResourceException("Room with number '" + trimmedRoomNumber + "' already exists");
        }

        room.setRoomNumber(trimmedRoomNumber);
        room.setRoomType(dto.getRoomType().trim().toUpperCase());
        room.setPricePerNight(dto.getPricePerNight());
        room.setCapacity(dto.getCapacity());
        if (dto.getIsActive() != null) {
            room.setIsActive(dto.getIsActive());
        }

        Room updatedRoom = roomRepository.save(room);
        return mapToResponse(updatedRoom);
    }

    @Transactional(transactionManager = "roomTransactionManager")
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
        roomRepository.delete(room);
    }

    private RoomResponseDto mapToResponse(Room room) {
        return RoomResponseDto.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .pricePerNight(room.getPricePerNight())
                .capacity(room.getCapacity())
                .isActive(room.getIsActive())
                .build();
    }
}
