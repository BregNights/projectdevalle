package br.com.senac.projectdevalle.shared.application.port;

// Exceção do contrato das portas de geolocalização (GeocodingPort/DistanceCalculationPort).
// Qualquer adapter (ex.: Google Maps) deve traduzir falhas de infraestrutura (timeout, erro HTTP,
// resposta inesperada) para esta exceção, nunca deixando o tipo de exceção do provedor escapar.
public class GeolocationUnavailableException extends RuntimeException {

    public GeolocationUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public GeolocationUnavailableException(String message) {
        super(message);
    }
}
