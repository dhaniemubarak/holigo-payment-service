package id.holigo.services.holigopaymentservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import id.holigo.services.holigopaymentservice.domain.Language;

public interface LanguageRepository extends JpaRepository<Language, Integer> {
    Language findByMessageKeyAndLocale(String messageKey, String locale);
}
