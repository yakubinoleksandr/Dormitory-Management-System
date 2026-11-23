package ua.kpi.dormitory.search;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.dormitory.common.ApiException;
import ua.kpi.dormitory.inventory.Dormitory;
import ua.kpi.dormitory.inventory.DormitoryRepository;
import ua.kpi.dormitory.profile.DormitoryManagerRepository;
import ua.kpi.dormitory.security.AuthenticatedUser;

import java.util.List;
import java.util.Locale;

/** FR-17: пошук за ПІБ, номером кімнати, договором або назвою гуртожитку в межах дозволених корпусів. */
@Service
public class SearchService {
    private final SearchRepository repository;
    private final DormitoryRepository dormitories;
    private final DormitoryManagerRepository managers;

    public SearchService(SearchRepository repository, DormitoryRepository dormitories, DormitoryManagerRepository managers) {
        this.repository = repository;
        this.dormitories = dormitories;
        this.managers = managers;
    }

    @Transactional(readOnly = true)
    public SearchDtos.SearchResponse search(AuthenticatedUser user, String query) {
        String q = query == null ? "" : query.replace("%", " ").replace("_", " ").trim().toLowerCase(Locale.ROOT);
        if (q.length() < 2) {
            throw ApiException.badRequest("Запит має містити щонайменше 2 символи");
        }
        List<Long> ids = user.isAdmin()
                ? dormitories.findAll().stream().map(Dormitory::getId).toList()
                : managers.findDormitoryIdsByAccountId(user.id());
        if (ids.isEmpty()) {
            return new SearchDtos.SearchResponse(List.of(), List.of(), List.of());
        }
        String like = "%" + q + "%";
        return new SearchDtos.SearchResponse(repository.residents(like, ids), repository.rooms(like, ids),
                repository.dormitories(like, ids));
    }
}
