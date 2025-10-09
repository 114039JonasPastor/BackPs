package ar.edu.utn.frc.tup.app.services;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OpenStreetMapService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OpenStreetMapService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader("User-Agent", "SpringBootApp/1.0")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String buscarPorDireccion(String direccion) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", direccion)
                            .queryParam("format", "json")
                            .queryParam("limit", "10")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error al buscar dirección: " + e.getMessage());
        }
    }

    public String buscarPorBarrio(String barrio, String ciudad) {
        String query = barrio + ", " + ciudad;
        return buscarPorDireccion(query);
    }

    public String buscarCoordenadas(double lat, double lon) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/reverse")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Error al buscar coordenadas: " + e.getMessage());
        }
    }
}
