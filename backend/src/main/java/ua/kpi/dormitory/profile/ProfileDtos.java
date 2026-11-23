package ua.kpi.dormitory.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ProfileDtos {
    private ProfileDtos() { }

    public record UpdateProfileRequest(
            @NotBlank @Size(max = 150) String fullName,
            Integer course,
            @Size(max = 20) String groupName,
            @Size(max = 20) String phone,
            String benefitsInfo) { }

    public record ProfileResponse(Long accountId, String fullName, Integer course, String groupName,
                                  String phone, String benefitsInfo) { }
}
