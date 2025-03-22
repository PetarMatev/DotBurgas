package dotburgas.apartment;

import dotburgas.apartment.model.Apartment;
import dotburgas.apartment.model.ApartmentInit;
import dotburgas.apartment.service.ApartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApartmentInitUTest {

    @Mock
    private ApartmentService apartmentService;

    @InjectMocks
    private ApartmentInit apartmentInit;

    @BeforeEach
    void setUp() {
    }

    @Test
    void run_ShouldDoNothing_WhenApartmentsExist() throws Exception {
        // Given
        when(apartmentService.getApartments()).thenReturn(List.of(new Apartment()));

        // When
        apartmentInit.run();

        // Then
        verify(apartmentService, never()).save(any(Apartment.class));
    }

    @Test
    void run_ShouldCreateDefaultApartments_WhenNoApartmentsExist() throws Exception {
        // Given
        when(apartmentService.getApartments()).thenReturn(List.of());

        // When
        apartmentInit.run();

        // Then
        ArgumentCaptor<Apartment> apartmentCaptor = ArgumentCaptor.forClass(Apartment.class);
        verify(apartmentService, times(3)).save(apartmentCaptor.capture());

        List<Apartment> savedApartments = apartmentCaptor.getAllValues();

        assertEquals("Apartment 1", savedApartments.get(0).getName());
        assertEquals("Apartment 2", savedApartments.get(1).getName());
        assertEquals("Apartment 3", savedApartments.get(2).getName());
    }
}
