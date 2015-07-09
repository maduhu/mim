package org.motechproject.nms.kilkari.repository;

import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;

public interface SubscriptionPackMessageDataService extends MotechDataService<SubscriptionPackMessage> {

    @Lookup
    SubscriptionPackMessage byWeekId(@LookupField(name = "weekId") String weekId);
}
