package br.com.senac.projectdevalle.shared.application.port;

import java.time.Duration;

public record EstimatedDistance(double kilometers, Duration duration) {

    public EstimatedDistance {
        if (kilometers < 0) {
            throw new IllegalArgumentException("kilometers must not be negative");
        }
    }
}
</content>
