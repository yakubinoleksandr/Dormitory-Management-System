package ua.kpi.dormitory.announcement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.dormitory.account.AccountRepository;
import ua.kpi.dormitory.common.ApiException;
import ua.kpi.dormitory.inventory.DormitoryRepository;
import ua.kpi.dormitory.security.AccessPolicy;
import ua.kpi.dormitory.security.AuthenticatedUser;

import java.time.LocalDateTime;
import java.util.List;

/** FR-11, FR-12: публікація та перегляд оголошень гуртожитку. */
@Service
public class AnnouncementService {
    private final AnnouncementRepository announcements;
    private final DormitoryRepository dormitories;
    private final AccountRepository accounts;
    private final AccessPolicy access;

    public AnnouncementService(AnnouncementRepository announcements, DormitoryRepository dormitories,
                               AccountRepository accounts, AccessPolicy access) {
        this.announcements = announcements;
        this.dormitories = dormitories;
        this.accounts = accounts;
        this.access = access;
    }

    @Transactional
    public AnnouncementDtos.AnnouncementResponse create(AuthenticatedUser author, AnnouncementDtos.CreateAnnouncementRequest req) {
        if (author.isManager()) {
            access.requireManagerOf(author, req.dormitoryId());
        }
        Announcement a = new Announcement();
        a.setDormitory(dormitories.findById(req.dormitoryId())
                .orElseThrow(() -> ApiException.notFound("Гуртожиток не знайдено")));
        a.setAuthor(accounts.getReferenceById(author.id()));
        a.setTitle(req.title());
        a.setContent(req.content());
        a.setExpiresAt(req.expiresAt());
        a.setCreatedAt(LocalDateTime.now());
        return toResponse(announcements.save(a));
    }

    @Transactional(readOnly = true)
    public List<AnnouncementDtos.AnnouncementResponse> active(AuthenticatedUser user, Long dormitoryId) {
        access.requireCanView(user, dormitoryId);
        return announcements.findActive(dormitoryId, LocalDateTime.now()).stream().map(AnnouncementService::toResponse).toList();
    }

    private static AnnouncementDtos.AnnouncementResponse toResponse(Announcement a) {
        return new AnnouncementDtos.AnnouncementResponse(a.getId(), a.getDormitory().getId(), a.getTitle(),
                a.getContent(), a.getExpiresAt(), a.getCreatedAt());
    }
}
