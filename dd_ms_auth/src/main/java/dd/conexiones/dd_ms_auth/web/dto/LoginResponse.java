package dd.conexiones.dd_ms_auth.web.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn,
        UserSummaryDTO user
) {
    public static LoginResponse bearer(String token, long expiresIn, UserSummaryDTO user) {
        return new LoginResponse(token, "Bearer", expiresIn, user);
    }
}
