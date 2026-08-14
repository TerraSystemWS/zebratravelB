package cv.terrasystem.zebratravelb.settings;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private static final String MAINTENANCE_KEY = "maintenance_mode";
    // Som dos toasts de notificação no ZebraDash — partilhado por todos, como o resto do
    // sistema de notificações (não é uma preferência por browser/utilizador).
    private static final String NOTIFICATION_SOUND_KEY = "notification_sound_enabled";

    private final AppSettingRepository appSettingRepository;

    @GetMapping("/maintenance")
    public Map<String, Integer> getMaintenanceMode() {
        int mode = appSettingRepository.findById(MAINTENANCE_KEY)
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElse(0);
        return Map.of("mode", mode);
    }

    @PutMapping("/maintenance")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Integer> setMaintenanceMode(@RequestBody Map<String, Integer> body) {
        AppSetting setting = appSettingRepository.findById(MAINTENANCE_KEY).orElseGet(() -> {
            AppSetting s = new AppSetting();
            s.setSettingKey(MAINTENANCE_KEY);
            return s;
        });
        setting.setSettingValue(String.valueOf(body.get("mode")));
        appSettingRepository.save(setting);
        return Map.of("mode", body.get("mode"));
    }

    @GetMapping("/notification-sound")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENTE')")
    public Map<String, Integer> getNotificationSound() {
        int enabled = appSettingRepository.findById(NOTIFICATION_SOUND_KEY)
                .map(s -> Integer.parseInt(s.getSettingValue()))
                .orElse(1);
        return Map.of("enabled", enabled);
    }

    @PutMapping("/notification-sound")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Integer> setNotificationSound(@RequestBody Map<String, Integer> body) {
        AppSetting setting = appSettingRepository.findById(NOTIFICATION_SOUND_KEY).orElseGet(() -> {
            AppSetting s = new AppSetting();
            s.setSettingKey(NOTIFICATION_SOUND_KEY);
            return s;
        });
        setting.setSettingValue(String.valueOf(body.get("enabled")));
        appSettingRepository.save(setting);
        return Map.of("enabled", body.get("enabled"));
    }
}
