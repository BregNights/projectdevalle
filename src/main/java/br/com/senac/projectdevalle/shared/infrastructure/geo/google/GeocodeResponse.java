package br.com.senac.projectdevalle.shared.infrastructure.geo.google;

import java.util.List;

record GeocodeResponse(String status, List<Result> results) {

    record Result(Geometry geometry) {
    }

    record Geometry(Location location) {
    }

    record Location(double lat, double lng) {
    }
}
