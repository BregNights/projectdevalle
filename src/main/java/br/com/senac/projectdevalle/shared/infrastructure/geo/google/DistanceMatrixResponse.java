package br.com.senac.projectdevalle.shared.infrastructure.geo.google;

import java.util.List;

record DistanceMatrixResponse(String status, List<Row> rows) {

    record Row(List<Element> elements) {
    }

    record Element(String status, ValueInMeters distance, ValueInSeconds duration) {
    }

    record ValueInMeters(long value) {
    }

    record ValueInSeconds(long value) {
    }
}
</content>
