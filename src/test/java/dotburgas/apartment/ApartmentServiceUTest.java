package dotburgas.apartment;


import dotburgas.apartment.service.ApartmentService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ApartmentServiceUTest {

    @InjectMocks
    private ApartmentService apartmentService;
}
