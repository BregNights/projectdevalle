package br.com.senac.projectdevalle.shared.infrastructure.geo.google;

import br.com.senac.projectdevalle.shared.application.port.GeocodingPort;
import br.com.senac.projectdevalle.shared.application.port.GeolocationUnavailableException;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Coordinates;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class GoogleGeocodingAdapter implements GeocodingPort {

    private final RestClient restClient;
    private final GoogleMapsProperties properties;

    public GoogleGeocodingAdapter(RestClient googleMapsRestClient, GoogleMapsProperties properties) {
        this.restClient = googleMapsRestClient;
        this.properties = properties;
    }

    @Override
    public Coordinates geocode(Address address) {
        GeocodeResponse response;
        try {
            response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/geocode/json")
                            .queryParam("address", address.toFullAddressString())
                            .queryParam("key", properties.apiKey())
                            .build())
                    .retrieve()
                    .body(GeocodeResponse.class);
        } catch (RestClientException exception) {
            throw new GeolocationUnavailableException("Google Geocoding API request failed", exception);
        }

        if (response == null || !"OK".equals(response.status()) || response.results().isEmpty()) {
            throw new GeolocationUnavailableException(
                    "Google Geocoding API could not resolve address: " + address.toFullAddressString());
        }

        GeocodeResponse.Location location = response.results().get(0).geometry().location();
        return new Coordinates(location.lat(), location.lng());
    }
}
</content>
