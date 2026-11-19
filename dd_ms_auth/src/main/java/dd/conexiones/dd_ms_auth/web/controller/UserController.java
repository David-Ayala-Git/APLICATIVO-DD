package dd.conexiones.dd_ms_auth.web.controller;

import dd.conexiones.dd_ms_auth.application.service.UserService;
import dd.conexiones.dd_ms_auth.web.dto.CreateUserRequest;
import dd.conexiones.dd_ms_auth.web.dto.UserSummaryDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserSummaryDTO> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserSummaryDTO findById(@PathVariable UUID id) {
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserSummaryDTO create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }
}
