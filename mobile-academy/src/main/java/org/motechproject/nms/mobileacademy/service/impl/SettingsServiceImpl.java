package org.motechproject.nms.mobileacademy.service.impl;

import org.motechproject.nms.mobileacademy.service.SettingsService;
import org.motechproject.server.config.SettingsFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * helper service class created to, yep, help facilitate ITs.
 */
@Service("settingsService")
public class SettingsServiceImpl implements SettingsService {

    private SettingsFacade settingsFacade;

    @Autowired
    public SettingsServiceImpl(SettingsFacade settingsFacade) {

        this.settingsFacade = settingsFacade;
    }


    public SettingsFacade getSettingsFacade() {
        return settingsFacade;
    }
}
