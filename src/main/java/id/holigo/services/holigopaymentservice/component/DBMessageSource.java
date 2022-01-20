package id.holigo.services.holigopaymentservice.component;

import java.text.MessageFormat;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import id.holigo.services.holigopaymentservice.domain.Language;
import id.holigo.services.holigopaymentservice.repositories.LanguageRepository;
// import lombok.RequiredArgsConstructor;

// @RequiredArgsConstructor
@Component("messageSource")
public class DBMessageSource extends AbstractMessageSource {

    @Autowired
    private LanguageRepository languageRepository;

    private static final String DEFAULT_LOCALE_CODE = "id";

    @Override
    protected MessageFormat resolveCode(String messageKey, Locale locale) {
        Language message = languageRepository.findByMessageKeyAndLocale(messageKey, locale.getLanguage());
        if (message == null) {
            message = languageRepository.findByMessageKeyAndLocale(messageKey, DEFAULT_LOCALE_CODE);
        }
        return new MessageFormat(message.getMessageContent(), locale);
    }

}
