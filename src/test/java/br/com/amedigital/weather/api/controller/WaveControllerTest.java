package br.com.amedigital.weather.api.controller;

import br.com.amedigital.weather.api.controller.response.WaveResponse;
import br.com.amedigital.weather.api.controller.response.WeatherResponse;
import br.com.amedigital.weather.api.service.WaveService;
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
import reactor.core.publisher.Mono;

import static com.ederfmatos.mockbean.MockBean.mockBean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class WaveControllerTest {

    private WebTestClient webTestClient;

    @MockBean
    private WaveService waveService;

    @BeforeEach
    public void setUp() {
        webTestClient = WebTestClient
                .bindToController(new WaveController(waveService))
                .configureClient()
                .baseUrl("/wave")
                .build();
    }

    @Test
    @DisplayName("Should find wave by city")
    public void shouldFindWaveByCity() {
        WaveResponse waveResponse = mockBean(WaveResponse.class).build();

        given(waveService.findWaveByCityName(any())).willReturn(Mono.just(waveResponse));

        webTestClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(WeatherResponse.class).hasSize(1);
    }

}
