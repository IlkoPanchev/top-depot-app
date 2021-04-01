package warehouse.statistics;

import java.time.LocalDateTime;

public class UnauthorizedError {
    private String URI;
    private String username;
    private LocalDateTime localDateTime;

    public UnauthorizedError(String URI, String username, LocalDateTime localDateTime) {
        this.URI = URI;
        this.username = username;
        this.localDateTime = localDateTime;
    }

    public String getURI() {
        return URI;
    }

    public UnauthorizedError setURI(String URI) {
        this.URI = URI;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UnauthorizedError setUsername(String username) {
        this.username = username;
        return this;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public UnauthorizedError setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
        return this;
    }
}
