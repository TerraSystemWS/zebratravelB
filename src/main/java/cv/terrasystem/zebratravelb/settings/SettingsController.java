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
}
