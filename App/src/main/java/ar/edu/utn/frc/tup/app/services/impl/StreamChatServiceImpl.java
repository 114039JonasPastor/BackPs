package ar.edu.utn.frc.tup.app.services.impl;

import ar.edu.utn.frc.tup.app.services.StreamChatService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
public class StreamChatServiceImpl implements StreamChatService {

    @Value("${stream.chat.api.key}")
    private String apiKey;

    @Value("${stream.chat.api.secret}")
    private String apiSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String createUserToken(String userId) {
        try {
            log.info("Generando token para usuario: {}", userId);

            String token = Jwts.builder()
                    .claim("user_id", userId)
                    .signWith(SignatureAlgorithm.HS256, apiSecret.getBytes(StandardCharsets.UTF_8))
                    .compact();

            log.info("✅ Token generado exitosamente para usuario: {}", userId);
            return token;

        } catch (Exception e) {
            log.error("Error al generar token para usuario: {}", userId, e);
            throw new RuntimeException("Error al crear token de usuario", e);
        }
    }

    @Override
    public void createOrUpdateUser(String userId, String nombre, String email, String imageUrl) {
        try {
            log.info("Creando/actualizando usuario en Stream: {}", userId);

            // ✅ URL correcta con api_key
            String url = String.format("https://chat.stream-io-api.com/users?api_key=%s", apiKey);

            Map<String, Object> user = new HashMap<>();
            user.put("id", userId);
            user.put("name", nombre);
            if (email != null && !email.isEmpty()) {
                user.put("email", email);
            }
            if (imageUrl != null && !imageUrl.isEmpty()) {
                user.put("image", imageUrl);
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("users", Map.of(userId, user));

            HttpHeaders headers = createServerAuthHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            log.info("✅ Usuario creado/actualizado en Stream: {} - Status: {}", userId, response.getStatusCode());

        } catch (Exception e) {
            log.error("Error al crear/actualizar usuario en Stream: {}", userId, e);
            throw new RuntimeException("Error al crear usuario en Stream Chat: " + e.getMessage(), e);
        }
    }

    @Override
    public String createChannel(String channelType, String channelId, String creatorId, Map<String, Object> additionalData) {
        try {
            log.info("Creando canal: {} de tipo: {}", channelId, channelType);

            String url = String.format("https://chat.stream-io-api.com/channels/%s/%s/query?api_key=%s",
                    channelType, channelId, apiKey);

            Map<String, Object> channelData = new HashMap<>();
            if (additionalData != null) {
                channelData.putAll(additionalData);
            }
            channelData.put("created_by_id", creatorId);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("data", channelData);

            HttpHeaders headers = createServerAuthHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            restTemplate.postForEntity(url, request, String.class);

            log.info("✅ Canal creado exitosamente: {}", channelId);
            return channelId;

        } catch (Exception e) {
            log.error("Error al crear canal: {}", channelId, e);
            throw new RuntimeException("Error al crear canal en Stream Chat: " + e.getMessage(), e);
        }
    }

    @Override
    public void addMembersToChannel(String channelType, String channelId, List<String> userIds) {
        try {
            log.info("Agregando {} miembros al canal: {}", userIds.size(), channelId);

            String url = String.format("https://chat.stream-io-api.com/channels/%s/%s?api_key=%s",
                    channelType, channelId, apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("add_members", userIds);

            HttpHeaders headers = createServerAuthHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            restTemplate.postForEntity(url, request, String.class);

            log.info("✅ Miembros agregados exitosamente al canal: {}", channelId);

        } catch (Exception e) {
            log.error("Error al agregar miembros al canal: {}", channelId, e);
            throw new RuntimeException("Error al agregar miembros al canal: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendMessage(String channelType, String channelId, String userId, String messageText) {
        try {
            log.info("Enviando mensaje al canal: {} por usuario: {}", channelId, userId);

            String url = String.format("https://chat.stream-io-api.com/channels/%s/%s/message?api_key=%s",
                    channelType, channelId, apiKey);

            Map<String, Object> message = new HashMap<>();
            message.put("text", messageText);
            message.put("user_id", userId);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("message", message);

            HttpHeaders headers = createServerAuthHeaders();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            restTemplate.postForEntity(url, request, String.class);

            log.info("✅ Mensaje enviado exitosamente al canal: {}", channelId);

        } catch (Exception e) {
            log.error("Error al enviar mensaje al canal: {}", channelId, e);
            throw new RuntimeException("Error al enviar mensaje: " + e.getMessage(), e);
        }
    }

    private HttpHeaders createServerAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // ✅ Autenticación de servidor con JWT
        String serverToken = Jwts.builder()
                .claim("server", true)
                .signWith(SignatureAlgorithm.HS256, apiSecret.getBytes(StandardCharsets.UTF_8))
                .compact();

        headers.set("Authorization", serverToken);
        headers.set("Stream-Auth-Type", "jwt");

        return headers;
    }
}
