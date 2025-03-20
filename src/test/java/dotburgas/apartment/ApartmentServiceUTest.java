package dotburgas.apartment;


import dotburgas.apartment.model.Apartment;
import dotburgas.apartment.repository.ApartmentRepository;
import dotburgas.apartment.service.ApartmentService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApartmentServiceUTest {

    @Mock
    private ApartmentRepository apartmentRepository;

    @InjectMocks
    private ApartmentService apartmentService;

    // 01. findApartmentNameByID
    @Test
    void givenApartmentIdThatDoesNotExistInTheDatabase_thenThrowException() {

        // Given
        UUID apartmentId = UUID.randomUUID();
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> apartmentService.findApartmentNameByID(apartmentId));
        verify(apartmentRepository, times(1)).findById(apartmentId);
    }

    @Test
    void givenApartmentIdThatDoesExistInTheDatabase_thenReturnApartmentName() {

        // Given
        UUID apartmentId = UUID.randomUUID();
        String apartmentName = "apartment_test";
        Apartment apartment = Apartment.builder()
                .id(apartmentId)
                .name(apartmentName)
                .build();
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));

        // When
        String apartmentNameByID = apartmentService.findApartmentNameByID(apartmentId);

        // Then (Expected vs Actual)
        assertEquals(apartment.getName(), apartmentNameByID);
        verify(apartmentRepository, times(1)).findById(apartmentId);
    }

    // 02. getById
    @Test
    void givenApartmentIdThatDoesNotExistInTheDatabase_thenThrowsNewException() {

        // Given
        UUID apartmentId = UUID.randomUUID();
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> apartmentService.getById(apartmentId));
        verify(apartmentRepository, times(1)).findById(apartmentId);
    }

    @Test
    void givenApartmentIdExistInTheDatabase_returnApartmentEntity() {

        // Given
        UUID apartmentId = UUID.randomUUID();
        String apartmentName = "apartment_test";
        Apartment apartment = Apartment.builder()
                .id(apartmentId)
                .name(apartmentName)
                .build();
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));

        // When
        Apartment resultApartment = apartmentService.getById(apartmentId);

        // Then
        assertEquals(apartment.getName(), resultApartment.getName());
        assertEquals(apartment, resultApartment);
        verify(apartmentRepository, times(1)).findById(apartmentId);
    }

    // 03. getApartments
    @Test
    void givenListOfApartments_thenReturnAllApartments() {

        // Given
        List<Apartment> apartmentList = List.of(Apartment.builder().build(), Apartment.builder().build());
        when(apartmentRepository.findAll()).thenReturn(apartmentList);

        // When
        List<Apartment> actualApartments = apartmentService.getApartments();

        // Then
        assertEquals(apartmentList.size(), actualApartments.size());
        verify(apartmentRepository, times(1)).findAll();
    }

    // 04. Save

    @Test
    void givenApartmentNameIsNull_thenThrowException() {

        // Given
        String apartmentName = "";
        Apartment apartment = Apartment.builder()
                .name(apartmentName)
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> apartmentService.save(apartment), "Apartment name cannot be empty!");
        verify(apartmentRepository, never()).save(any(Apartment.class));
    }

    @Test
    void givenApartmentNameIsBlank_thenThrowException() {

        // Given
        String apartmentName = " ";
        Apartment apartment = Apartment.builder()
                .name(apartmentName)
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> apartmentService.save(apartment), "Apartment name cannot be empty!");
        verify(apartmentRepository, never()).save(any(Apartment.class));
    }

    @Test
    void givenApartmentNameIsPresent_thenSaveApartmentInTheDatabase() {

        // Given
        String apartmentName = "apartmentTest";
        Apartment apartment = Apartment.builder()
                .id(UUID.randomUUID())
                .name(apartmentName)
                .location("Burgas")
                .description("Seaside flat")
                .pricePerNight(BigDecimal.valueOf(100))
                .build();

        // When

        apartmentService.save(apartment);

        // Then
        assertEquals(apartmentName, apartment.getName(), "Apartment name should match the expected name");
        assertNotNull(apartment.getName(), "Apartment name cannot be empty!");
        verify(apartmentRepository, times(1)).save(any(Apartment.class));
    }
}
