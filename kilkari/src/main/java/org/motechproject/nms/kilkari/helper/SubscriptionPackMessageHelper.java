package org.motechproject.nms.kilkari.helper;

import org.joda.time.DateTime;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.repository.SubscriptionPackMessageDataService;
import org.springframework.stereotype.Component;

/**
 * helper class to fetch messages for a subscription
 */
@Component
public class SubscriptionPackMessageHelper {

    private SubscriptionPackMessageDataService subscriptionPackMessageDataService;

    public SubscriptionPackMessageHelper()

    public SubscriptionPackMessage getNextMessage(Subscription subscription, DateTime dateTime) {

    }
}
