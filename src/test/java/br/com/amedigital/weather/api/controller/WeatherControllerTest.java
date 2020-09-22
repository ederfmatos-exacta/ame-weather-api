package br.com.amedigital.weather.api.controller;

import br.com.amedigital.weather.api.controller.request.WeatherNewRequest;
import br.com.amedigital.weather.api.controller.request.WeatherUpdateRequest;
import br.com.amedigital.weather.api.controller.response.WeatherResponse;
import br.com.amedigital.weather.api.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import static com.ederfmatos.mockbean.MockBean.mockBean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class WeatherControllerTest {

    private WebTestClient webTestClient;

    @MockBean
    private WeatherService weatherService;

    @BeforeEach
    public void setUp() {
        webTestClient = WebTestClient
                .bindToController(new WeatherController(weatherService))
                .configureClient()
                .baseUrl("/weather")
                .build();
    }

    @Test
    @DisplayName("Should list weather")
    public void shouldListWeatherWithSuccess() {
        WeatherResponse weatherResponse = mockBean(WeatherResponse.class).build();

        given(weatherService.findWeatherToCity(any())).willReturn(Flux.just(weatherResponse));

        webTestClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(WeatherResponse.class).hasSize(1);
    }

    @Test
    @DisplayName("Should find weather by id")
    public void shouldFindWeatherByIdWithSuccess() {
        WeatherResponse weatherResponse = mockBean(WeatherResponse.class).build();

        given(weatherService.findOneWeather(any())).willReturn(Mono.just(weatherResponse));

        webTestClient.get()
                .uri("/randomid")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("id").isNotEmpty();
    }

    @Test
    @DisplayName("Should create a weather")
    public void shouldCreateWeather() {
        WeatherNewRequest bean = mockBean(WeatherNewRequest.class)
                .build();

        given(weatherService.createWeather(any())).willReturn(Mono.just(bean));

        webTestClient.post()
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(bean)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("id").isNotEmpty()
                .jsonPath("weatherCity").isEqualTo(bean.getWeatherCity());
    }

    @Test
    @DisplayName("Should update a weather")
    public void shouldUpdateWeather() {
        WeatherUpdateRequest bean = mockBean(WeatherUpdateRequest.class).build();

        given(weatherService.updateWeather(any(), any())).willReturn(Mono.create(MonoSink::success));

        webTestClient.put()
                .uri("/randomid")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(bean)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("Should delete a weather")
    public void shouldDeleteWeather() {
        given(weatherService.deleteWeather(any())).willReturn(Mono.create(MonoSink::success));

        webTestClient.delete()
                .uri("/randomid")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("Should list weather to next week")
    public void shouldListWeatherToNextWeekWithSuccess() {
        WeatherResponse weatherResponse = mockBean(WeatherResponse.class).build();

        given(weatherService.findWeatherToCityToNextWeek(any(), any())).willReturn(Flux.just(weatherResponse));

        webTestClient.get()
                .uri("/week?cityName=Guariba")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(WeatherResponse.class).hasSize(1);
    }

}
