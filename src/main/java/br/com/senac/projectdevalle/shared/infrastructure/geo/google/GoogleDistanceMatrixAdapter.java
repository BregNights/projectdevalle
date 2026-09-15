package br.com.senac.projectdevalle.shared.infrastructure.geo.google;

import br.com.senac.projectdevalle.shared.application.port.DistanceCalculationPort;
import br.com.senac.projectdevalle.shared.application.port.EstimatedDistance;
import br.com.senac.projectdevalle.shared.application.port.GeolocationUnavailableException;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.Locale;

@Component
public class GoogleDistanceMatrixAdapter implements DistanceCalculationPort {

    private final RestClient restClient;
    private final GoogleMapsProperties properties;

    public GoogleDistanceMatrixAdapter(RestClient googleMapsRestClient, GoogleMapsProperties properties) {
        this.restClient = googleMapsRestClient;
        this.properties = properties;
    }

    @Override
    public EstimatedDistance calculate(Coordinates origin, Coordinates destination) {
        DistanceMatrixResponse response;
        try {
            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/distancematrix/json")
                            .queryParam("origins", toLatLng(origin))
                            .queryParam("destinations", toLatLng(destination))
                            .queryParam("key", properties.apiKey())
                            .build())
                    .retrieve()
                    .body(DistanceMatrixResponse.class);
        } catch (RestClientException exception) {
            throw new GeolocationUnavailableException("Google Distance Matrix API request failed", exception);
        }

        if (response == null || !"OK".equals(response.status()) || response.rows().isEmpty()) {
            throw new GeolocationUnavailableException("Google Distance Matrix API returned no data");
        }

        DistanceMatrixResponse.Element element = response.rows().get(0).elements().get(0);
        if (!"OK".equals(element.status())) {
            throw new GeolocationUnavailableException("Google Distance Matrix API could not calculate route");
        }

        double kilometers = element.distance().value() / 1000.0;
        Duration duration = Duration.ofSeconds(element.duration().value());
        return new EstimatedDistance(kilometers, duration);
    }

    private static String toLatLng(Coordinates coordinates) {
        return String.format(Locale.US, "%f,%f", coordinates.latitude(), coordinates.longitude());
    }
}
</content>
