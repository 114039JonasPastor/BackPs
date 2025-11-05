package ar.edu.utn.frc.tup.app.controllers;

import ar.edu.utn.frc.tup.app.services.StreamChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat", description = "Endpoints para gestión de chat en tiempo real")
public class ChatController {

    private final StreamChatService streamChatService;

    @PostMapping("/token")
    @Operation(summary = "Generar token de autenticación para usuario")
    public ResponseEntity<Map<String, String>> generateToken(@RequestParam String userId) {
        log.info("Generando token para usuario: {}", userId);
        String token = streamChatService.createUserToken(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "token", token));
    }

    @PostMapping("/users")
    @Operation(summary = "Crear o actualizar usuario en Stream Chat")
    public ResponseEntity<Map<String, String>> createUser(@RequestBody Map<String, String> userData) {
        String userId = userData.get("userId");
        String nombre = userData.get("nombre");
        String email = userData.get("email");
        String imageUrl = userData.get("imageUrl");

        streamChatService.createOrUpdateUser(userId, nombre, email, imageUrl);
        return ResponseEntity.ok(Map.of("status", "success", "userId", userId));
    }

    @PostMapping("/channels")
    @Operation(summary = "Crear canal de chat")
    public ResponseEntity<Map<String, String>> createChannel(@RequestBody Map<String, Object> channelData) {
        String channelType = (String) channelData.get("channelType");
        String channelId = (String) channelData.get("channelId");
        String creatorId = (String) channelData.get("creatorId");

        streamChatService.createChannel(channelType, channelId, creatorId, channelData);
        return ResponseEntity.ok(Map.of("status", "success", "channelId", channelId));
    }

    @PostMapping("/channels/members")
    @Operation(summary = "Agregar miembros a canal")
    public ResponseEntity<Map<String, String>> addMembers(@RequestBody Map<String, Object> request) {
        String channelType = (String) request.get("channelType");
        String channelId = (String) request.get("channelId");
        List<String> userIds = (List<String>) request.get("userIds");

        streamChatService.addMembersToChannel(channelType, channelId, userIds);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Miembros agregados"));
    }

    @PostMapping("/messages")
    @Operation(summary = "Enviar mensaje a canal")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody Map<String, String> messageData) {
        String channelType = messageData.get("channelType");
        String channelId = messageData.get("channelId");
        String userId = messageData.get("userId");
        String message = messageData.get("message");

        streamChatService.sendMessage(channelType, channelId, userId, message);
        return ResponseEntity.ok(Map.of("status", "success", "message", "Mensaje enviado"));
    }
}
