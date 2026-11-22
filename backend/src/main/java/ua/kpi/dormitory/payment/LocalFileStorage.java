package ua.kpi.dormitory.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ua.kpi.dormitory.common.ApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Component
public class LocalFileStorage implements FileStorage {
    private static final Map<String, String> EXTENSIONS = Map.of(
            "application/pdf", ".pdf", "image/jpeg", ".jpg", "image/png", ".png");
    private final Path root;

    public LocalFileStorage(@Value("${app.storage.receipts-dir}") String dir) {
        this.root = Path.of(dir).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file) {
        String ext = EXTENSIONS.get(file.getContentType());
        if (ext == null) {
            throw ApiException.badRequest("Дозволені типи квитанцій: PDF, JPEG, PNG");
        }
        String name = UUID.randomUUID() + ext;   // ім'я не залежить від імені, надісланого користувачем
        try {
            Files.createDirectories(root);
            file.transferTo(root.resolve(name));
        } catch (IOException e) {
            throw new IllegalStateException("Не вдалося зберегти файл", e);
        }
        return name;
    }

    @Override
    public void delete(String relativePath) {
        try {
            Files.deleteIfExists(root.resolve(relativePath).normalize());
        } catch (IOException ignored) {
            // файл без запису в БД не критичний; очищується обслуговуванням
        }
    }
}
