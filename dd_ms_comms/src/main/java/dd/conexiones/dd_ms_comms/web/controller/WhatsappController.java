package dd.conexiones.dd_ms_comms.web.controller;

import dd.conexiones.dd_ms_comms.application.service.WhatsappService;
import dd.conexiones.dd_ms_comms.web.dto.IncomingWhatsappWebhookRequest;
import dd.conexiones.dd_ms_comms.web.dto.SendWhatsappRequest;
import dd.conexiones.dd_ms_comms.web.dto.WhatsappMessageDTO;
import dd.conexiones.mscommon.role.RequiresRole;
import dd.conexiones.mscommon.role.Roles;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsappController {

    private final WhatsappService whatsappService;

    @Value("${whatsapp.webhook-verify-token:}")
    private String verifyToken;

    public WhatsappController(WhatsappService whatsappService) {
        this.whatsappService = whatsappService;
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.CARTERA})
    @GetMapping
    public List<WhatsappMessageDTO> findAll() {
        return whatsappService.findAll();
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.OPERADOR, Roles.CARTERA})
    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    public WhatsappMessageDTO send(@Valid @RequestBody SendWhatsappRequest request) {
        return whatsappService.send(request);
    }

    /** Verificacion del webhook exigida por Meta al configurarlo en el dashboard de developers. */
    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping("/webhook")
    public WhatsappMessageDTO receiveWebhook(@Valid @RequestBody IncomingWhatsappWebhookRequest request) {
        return whatsappService.receiveIncoming(request);
    }
}
