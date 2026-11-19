package dd.conexiones.dd_ms_billing.web.controller;

import dd.conexiones.dd_ms_billing.application.service.PaymentService;
import dd.conexiones.dd_ms_billing.web.dto.ManualPaymentRequest;
import dd.conexiones.dd_ms_billing.web.dto.PaymentDTO;
import dd.conexiones.mscommon.module.RequiresModule;
import dd.conexiones.mscommon.role.RequiresRole;
import dd.conexiones.mscommon.role.Roles;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.CARTERA, Roles.OPERADOR})
    @RequiresModule("billing")
    @GetMapping
    public List<PaymentDTO> findAll() {
        return paymentService.findAll();
    }

    @RequiresRole({Roles.ADMIN_ISP, Roles.CARTERA, Roles.OPERADOR})
    @RequiresModule("billing")
    @GetMapping("/{id}")
    public PaymentDTO findById(@PathVariable UUID id) {
        return paymentService.findById(id);
    }

    /** Efectivo o Nequi recibido a mano; el tenant es el de la sesion. */
    @RequiresRole({Roles.ADMIN_ISP, Roles.CARTERA})
    @RequiresModule("billing")
    @PostMapping("/manual")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDTO registerManual(@Valid @RequestBody ManualPaymentRequest request) {
        return paymentService.registerManual(request);
    }

    /**
     * Publico (sin JWT): lo llama la pasarela. El cuerpo llega crudo porque la
     * firma se calcula sobre los bytes exactos; la verificacion vive en PaymentGateway.
     * Spring entrega las cabeceras con su nombre en minusculas.
     */
    @PostMapping("/webhook")
    public PaymentDTO webhook(@RequestBody String rawBody, @RequestHeader Map<String, String> headers) {
        return paymentService.processGatewayWebhook(rawBody, headers);
    }
}
