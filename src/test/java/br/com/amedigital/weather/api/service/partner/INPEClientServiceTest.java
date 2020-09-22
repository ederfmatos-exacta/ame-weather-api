package br.com.amedigital.weather.api.service.partner;

import br.com.amedigital.weather.api.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest
public class INPEClientServiceTest {

    @Autowired
    private INPEClientService service;

    @Test
    @DisplayName("Should find weather by city")
    public void shouldFindWeatherByCity() {
        StepVerifier.create(service.findWeatherToCity(2242))
                .expectNextMatches(response -> response.getName().equals("Guariba"))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should find weather by city to next week")
    public void shouldFindWeatherByCityToNextWeek() {
        StepVerifier.create(service.findWeatherToCityToNextWeek(2242))
                .expectNextMatches(response -> response.getName().equals("Guariba") && response.getWeather().size() == 7)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should find city by name")
    public void shouldFindCityByName() {
        StepVerifier.create(service.findCityByName("Guariba", null))
                .expectNextMatches(response -> response.getName().equals("Guariba"))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should find wave by city")
    public void shouldFindWaveByCity() {
        StepVerifier.create(service.findWaveByCity(2242))
                .expectNextMatches(response -> response.getName().equals("Guariba"))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should throw error on find inexistent city")
    public void shouldThrowErrorOnFindInexistentCity() {
        StepVerifier.create(service.findCityByName("Cidade que não existe", null))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Should throw error on find with same name")
    public void shouldThrowErrorOnFindCityWithSameName() {
        StepVerifier.create(service.findCityByName("São Francisco", null))
                .expectError(NotFoundException.class)
                .verify();
    }

}
