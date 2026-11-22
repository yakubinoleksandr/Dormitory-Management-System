package ua.kpi.dormitory.announcement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class AnnouncementDtos {
    private AnnouncementDtos() { }

    public record CreateAnnouncementRequest(
            @NotNull Long dormitoryId,
            @NotBlank @Size(max = 200) String title,
            @NotBlank String content,
            LocalDateTime expiresAt) { }

    public record AnnouncementResponse(Long id, Long dormitoryId, String title, String content,
                                       LocalDateTime expiresAt, LocalDateTime createdAt) { }
}
