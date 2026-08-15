package cv.terrasystem.zebratravelb.invoice;

import cv.terrasystem.zebratravelb.settings.AppSetting;
import cv.terrasystem.zebratravelb.settings.AppSettingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Dados fiscais da ZebraTravel usados no cabeçalho das faturas (nome, nome fiscal/razão social,
// NIF, morada, email, logótipo) — configuráveis pelo ADMIN em Contas > Configuração, guardados
// como chaves soltas em app_settings (mesmo mecanismo já usado no Modo de Manutenção e no Som
// de Notificações). Os valores de application.properties/env continuam a servir de default
// enquanto o ADMIN não configurar nada, mesmo comportamento de sempre — só passou a ser
// editável sem precisar de redeploy.
@Service
public class InvoiceCompanyProfileService {

    private static final String NAME_KEY = "invoice_company_name";
    private static final String LEGAL_NAME_KEY = "invoice_company_legal_name";
    private static final String NIF_KEY = "invoice_company_nif";
    private static final String ADDRESS_KEY = "invoice_company_address";
    private static final String EMAIL_KEY = "invoice_company_email";
    private static final String LOGO_KEY = "invoice_company_logo";

    private final AppSettingRepository appSettingRepository;
    private final String defaultName;
    private final String defaultAddress;
    private final String defaultNif;
    private final String defaultEmail;

    public InvoiceCompanyProfileService(
            AppSettingRepository appSettingRepository,
            @Value("${app.invoice.company-name}") String defaultName,
            @Value("${app.invoice.company-address}") String defaultAddress,
            @Value("${app.invoice.company-nif}") String defaultNif,
            @Value("${app.invoice.company-email}") String defaultEmail
    ) {
        this.appSettingRepository = appSettingRepository;
        this.defaultName = defaultName;
        this.defaultAddress = defaultAddress;
        this.defaultNif = defaultNif;
        this.defaultEmail = defaultEmail;
    }

    public InvoiceCompanyProfile get() {
        return new InvoiceCompanyProfile(
                setting(NAME_KEY, defaultName),
                setting(LEGAL_NAME_KEY, ""),
                setting(NIF_KEY, defaultNif),
                setting(ADDRESS_KEY, defaultAddress),
                setting(EMAIL_KEY, defaultEmail),
                appSettingRepository.findById(LOGO_KEY).map(AppSetting::getSettingValue).filter(v -> !v.isBlank()).orElse(null)
        );
    }

    public InvoiceCompanyProfile update(String name, String legalName, String nif, String address, String email) {
        save(NAME_KEY, name);
        save(LEGAL_NAME_KEY, legalName);
        save(NIF_KEY, nif);
        save(ADDRESS_KEY, address);
        save(EMAIL_KEY, email);
        return get();
    }

    public void setLogoPath(String path) {
        save(LOGO_KEY, path);
    }

    private String setting(String key, String fallback) {
        return appSettingRepository.findById(key)
                .map(AppSetting::getSettingValue)
                .filter(v -> !v.isBlank())
                .orElse(fallback);
    }

    private void save(String key, String value) {
        AppSetting setting = appSettingRepository.findById(key).orElseGet(() -> {
            AppSetting s = new AppSetting();
            s.setSettingKey(key);
            return s;
        });
        setting.setSettingValue(value != null ? value : "");
        appSettingRepository.save(setting);
    }
}
