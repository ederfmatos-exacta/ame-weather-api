package br.com.amedigital.weather.api.service;

import br.com.amedigital.weather.api.controller.request.WeatherNewRequest;
import br.com.amedigital.weather.api.controller.request.WeatherRequest;
import br.com.amedigital.weather.api.controller.request.WeatherUpdateRequest;
import br.com.amedigital.weather.api.controller.response.WeatherResponse;
import br.com.amedigital.weather.api.entity.WeatherEntity;
import br.com.amedigital.weather.api.mapper.WeatherMapper;
import br.com.amedigital.weather.api.model.partner.response.INPECityResponse;
import br.com.amedigital.weather.api.model.partner.response.INPEWeatherCityResponse;
import br.com.amedigital.weather.api.repository.WeatherRepository;
import br.com.amedigital.weather.api.service.partner.INPEClientService;
import br.com.amedigital.weather.api.utils.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static com.ederfmatos.mockbean.MockBean.mockBean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest
public class WeatherServiceTest {

    @MockBean
    private INPEClientService inpeClientService;

    @MockBean
    private WeatherMapper mapper;

    @MockBean
    private WeatherRepository weatherRepository;

    private WeatherService service;

    @BeforeEach
    public void setup() {
        service = new WeatherService(inpeClientService, weatherRepository, mapper);
    }

    @Test
    @DisplayName("Should list all weather")
    public void shouldBeListAllWeathers() {
        WeatherResponse weatherResponse = mockBean(WeatherResponse.class)
                .with("weatherCity", "Guariba")
                .build();

        WeatherRequest weatherRequest = new WeatherRequest();

        doReturn(Flux.just(weatherResponse)).when(weatherRepository).findAllWeather(weatherRequest);

        StepVerifier.create(service.findWeatherToCity(weatherRequest))
                .expectNextMatches(response -> response.getWeatherCity().equals("Guariba"))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should list weather by name")
    public void shouldBeListWeatherByName() {
        WeatherResponse weatherResponse = mockBean(WeatherResponse.class)
                .with("weatherCity", "Guariba")
                .build();

        WeatherRequest weatherRequest = new WeatherRequest();
        weatherRequest.setCityName("Guariba");

        INPECityResponse.City city = mockBean(INPECityResponse.City.class).build();
        INPEWeatherCityResponse inpeWeatherCityResponse = mockBean(INPEWeatherCityResponse.class).build();

        doReturn(Mono.just(city)).when(inpeClientService).findCityByName(any(), any());
        doReturn(Flux.just(mockBean(WeatherEntity.class).build())).when(weatherRepository).findByCityCode(any(), any());
        doReturn(Mono.just(inpeWeatherCityResponse)).when(inpeClientService).findWeatherToCity(any());
        doReturn(weatherResponse).when(mapper).entityToResponse(any());

        StepVerifier.create(service.findWeatherToCity(weatherRequest))
                .expectNextMatches(response -> response.getWeatherCity().equals("Guariba"))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should list weather by name with all weather in database")
    public void shouldBeListWeatherByNameWithAllWeatherInDatabase() {
        WeatherResponse weatherResponse = mockBean(WeatherResponse.class)
                .with("weatherCity", "Guariba")
                .build();

        WeatherRequest weatherRequest = new WeatherRequest();
        weatherRequest.setCityName("Guariba");

        INPECityResponse.City city = mockBean(INPECityResponse.City.class).build();
        INPEWeatherCityResponse inpeWeatherCityResponse = mockBean(INPEWeatherCityResponse.class).build();
        List<WeatherEntity> weatherEntities = mockBean(WeatherEntity.class).build(4);

        doReturn(Mono.just(city)).when(inpeClientService).findCityByName(any(), any());
        doReturn(Flux.fromIterable(weatherEntities)).when(weatherRepository).findByCityCode(any(), any());
        doReturn(Mono.just(inpeWeatherCityResponse)).when(inpeClientService).findWeatherToCity(any());
        doReturn(weatherResponse, weatherResponse).when(mapper).entityToResponse(any());

        StepVerifier.create(service.findWeatherToCity(weatherRequest))
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should list weather by name to next week")
    public void shouldBeListWeatherByNameToNextWeek() {
        WeatherResponse weatherResponse = mockBean(WeatherResponse.class).build();
        INPECityResponse.City city = mockBean(INPECityResponse.City.class).build();
        INPEWeatherCityResponse inpeWeatherCityResponse = mockBean(INPEWeatherCityResponse.class).build();
        WeatherEntity weatherEntity = mockBean(WeatherEntity.class)
                .without("id")
                .build();

        List<WeatherEntity> weatherEntities = mockBean(WeatherEntity.class)
                .without("id")
                .with("date", LocalDate.now().plusDays(2))
                .build(7);

        doReturn(Mono.just(city)).when(inpeClientService).findCityByName(any(), any());
        doReturn(Flux.empty()).when(weatherRepository).findByCityCode(any(), any());
        doReturn(Mono.just(weatherEntity)).when(weatherRepository).save(any());
        doReturn(Mono.just(inpeWeatherCityResponse)).when(inpeClientService).findWeatherToCityToNextWeek(any());
        doReturn(weatherResponse).when(mapper).entityToResponse(any());
        doReturn(weatherEntities).when(mapper).INPEWeatherCityResponseToEntity(any());

        StepVerifier.create(service.findWeatherToCityToNextWeek("Guariba", "SP"))
                .expectNextCount(7)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should find weather by id")
    public void shouldFindWeatherById() {
        WeatherResponse weatherResponse = mockBean(WeatherResponse.class).build();
        doReturn(Mono.just(weatherResponse)).when(weatherRepository).findById(any());

        StepVerifier.create(service.findOneWeather(""))
                .expectNextMatches(response -> response.getId().equals(weatherResponse.getId()))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should be delete weather")
    public void shouldDeleteWeather() {
        WeatherResponse weatherResponse = mockBean(WeatherResponse.class).build();
        doReturn(Mono.just(weatherResponse)).when(weatherRepository).findById(any());
        doReturn(Mono.empty()).when(weatherRepository).delete(any());

        StepVerifier.create(service.deleteWeather(""))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should be update weather")
    public void shouldUpdateWeather() {
        WeatherResponse weatherResponse = mockBean(WeatherResponse.class).build();
        WeatherEntity weatherEntity = mockBean(WeatherEntity.class).build();

        doReturn(Mono.just(weatherResponse)).when(weatherRepository).findById(any());
        doReturn(Mono.empty()).when(weatherRepository).update(any());
        doReturn(weatherEntity).when(mapper).responseToEntity(any());

        StepVerifier.create(service.updateWeather("", new WeatherUpdateRequest()))
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("Should be create weather")
    public void shouldCreateWeather() {
        WeatherNewRequest weatherNewRequest = mockBean(WeatherNewRequest.class)
                .without("id")
                .build();
        WeatherEntity weatherEntity = mockBean(WeatherEntity.class).build();

        INPECityResponse.City city = mockBean(INPECityResponse.City.class).build();

        doReturn(Mono.just(city)).when(inpeClientService).findCityByName(any(), any());
        doReturn(Mono.just(weatherEntity)).when(weatherRepository).save(any());

        StepVerifier.create(service.createWeather(weatherNewRequest))
                .expectNextMatches(response -> StringUtils.isNotEmpty(response.getId()))
                .expectComplete()
                .verify();
    }

}
