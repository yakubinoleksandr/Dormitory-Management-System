package ua.kpi.dormitory.payment;

import org.springframework.web.multipart.MultipartFile;

/** Абстракція сховища файлів (DIP): сервіс оплат не знає, де фізично лежать квитанції. */
public interface FileStorage {
    /** Зберігає файл під випадковим іменем і повертає відносний шлях для payments.receipt_url. */
    String store(MultipartFile file);

    void delete(String relativePath);
}
