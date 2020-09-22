package br.com.amedigital.weather.api.service;

import br.com.amedigital.weather.api.controller.request.WaveRequest;
import br.com.amedigital.weather.api.controller.response.WaveResponse;
import br.com.amedigital.weather.api.entity.WaveEntity;
import br.com.amedigital.weather.api.mapper.WaveMapper;
import br.com.amedigital.weather.api.model.partner.response.INPECityResponse;
import br.com.amedigital.weather.api.model.partner.response.INPEWaveCityResponse;
import br.com.amedigital.weather.api.repository.WaveRepository;
import br.com.amedigital.weather.api.service.partner.INPEClientService;
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

import static com.ederfmatos.mockbean.MockBean.mockBean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest
public class WaveServiceTest {

    @MockBean
    private INPEClientService inpeClientService;

    @MockBean
    private WaveMapper mapper;

    @MockBean
    private WaveRepository waveRepository;

    private WaveService service;

    @BeforeEach
    public void setup() {
        service = new WaveService(mapper, waveRepository, inpeClientService);
    }

    @Test
    @DisplayName("Should wave by city")
    public void shouldBeWaveByCity() {
        WaveRequest waveRequest = mockBean(WaveRequest.class).build();
        WaveResponse waveResponse = mockBean(WaveResponse.class).build();
        WaveEntity waveEntity = mockBean(WaveEntity.class).build();
        INPEWaveCityResponse inpeWaveCityResponse = mockBean(INPEWaveCityResponse.class).build();

        INPECityResponse.City city = mockBean(INPECityResponse.City.class).build();

        doReturn(Mono.just(city)).when(inpeClientService).findCityByName(any(), any());
        doReturn(Mono.just(inpeWaveCityResponse)).when(inpeClientService).findWaveByCity(any());
        doReturn(mockBean(WaveEntity.class).build(3)).when(mapper).INPEWaveCityResponseToEntity(any());
        doReturn(waveResponse).when(mapper).entityToResponse(any());
        doReturn(Flux.just(waveEntity)).when(waveRepository).save(any());

        StepVerifier.create(service.findWaveByCityName(waveRequest))
                .expectNextMatches(response -> response.getCode().equals(waveResponse.getCode()))
                .expectComplete()
                .verify();
    }

}
