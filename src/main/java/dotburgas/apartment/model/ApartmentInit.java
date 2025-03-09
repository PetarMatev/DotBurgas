package dotburgas.apartment.model;

import dotburgas.apartment.service.ApartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ApartmentInit implements CommandLineRunner {  // Fixed class name

    private final ApartmentService apartmentService;
    private final BigDecimal APARTMENT_ONE_PRICE = BigDecimal.valueOf(100.00);
    private final BigDecimal APARTMENT_TWO_PRICE = BigDecimal.valueOf(100.00);
    private final BigDecimal APARTMENT_THREE_PRICE = BigDecimal.valueOf(100.00);

    @Autowired
    public ApartmentInit(ApartmentService apartmentService) {
        this.apartmentService = apartmentService;
    }

    @Override
    public void run(String... args) throws Exception {

        if (!apartmentService.getApartments().isEmpty()) {
            // If apartments exist, do nothing.
            return;
        }


        // Create default apartments
        List<Apartment> defaultApartments = List.of(
                new Apartment(null, "Apartment 1", "Downtown, City of Burgas",
                        "Cozy environment with modern amenities.", APARTMENT_ONE_PRICE,
                        List.of("/img/ap30/1.jpg", "/img/ap30/2.jpg", "/img/ap30/3.jpg"), null),
                new Apartment(null, "Apartment 2", "Near the Central Park, City of Burgas",
                        "Spacious apartment with a city skyline view.", APARTMENT_TWO_PRICE,
                        List.of("/img/ap25/1.jpg", "/img/ap25/2.jpg", "/img/ap25/3.jpg"), null),
                new Apartment(null, "Apartment 3", "Beachside, City of Burgas",
                        "Luxurious suite with breathtaking views.", APARTMENT_THREE_PRICE,
                        List.of("/img/ap4/1.jpg", "/img/ap4/2.jpg", "/img/ap4/3.jpg"), null)
        );

        defaultApartments.forEach(apartmentService::save);
    }
}
