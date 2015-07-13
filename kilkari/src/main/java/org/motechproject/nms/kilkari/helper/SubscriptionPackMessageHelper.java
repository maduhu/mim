package org.motechproject.nms.kilkari.helper;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.motechproject.nms.kilkari.domain.Subscription;
import org.motechproject.nms.kilkari.domain.SubscriptionOrigin;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionStatus;
import org.motechproject.nms.kilkari.repository.SubscriptionPackMessageDataService;
import org.motechproject.nms.props.domain.DayOfTheWeek;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * helper class to fetch messages for a subscription
 */
@Component
public class SubscriptionPackMessageHelper {

    private static final String weekIdFormat = "w%d_%d";
    private static final int firstWeekId = 1;
    private static final int firstMessageId = 1;
    private static final int secondMessageId = 2;
    private static final int DAYS_IN_WEEK = 7;
    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPackMessageHelper.class);


    private SubscriptionPackMessageDataService subscriptionPackMessageDataService;

    @Autowired
    public SubscriptionPackMessageHelper(SubscriptionPackMessageDataService subscriptionPackMessageDataService) {
        this.subscriptionPackMessageDataService = subscriptionPackMessageDataService;
    }

    public SubscriptionPackMessage getNextMessage(Subscription subscription, DateTime currentDate) {

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException(String.format("Subscription with ID %s is not active", subscription.getSubscriptionId()));
        }

        if ((subscription.getOrigin() == SubscriptionOrigin.MCTS_IMPORT) && subscription.getNeedsWelcomeMessage()) {
            // Subscriber has been subscribed via MCTS and may not know what Kilkari is; play welcome message this week
            return getMessage(firstWeekId, firstMessageId);
        }

        DateTime startDate = subscription.getStartDate();
        int daysIntoPack = Days.daysBetween(startDate, currentDate).getDays();

        if (daysIntoPack > 0 && currentDate.isBefore(startDate)) {
            // there is no message due
            throw new IllegalStateException(
                    String.format("Subscription with ID %s is not due for any scheduled message until %s", subscription.getSubscriptionId(), startDate));
        }

        int currentWeek = daysIntoPack / DAYS_IN_WEEK + 1;  // accounting for zero-based

        if (subscription.getSubscriptionPack().getMessagesPerWeek() == 1) {
            LOGGER.debug(String.format("Found 1 msg pack, fetching w%d_%d for subscriptionid: %s", currentWeek, 1, subscription.getSubscriptionId()));
            return getMessage(currentWeek, firstMessageId);
        } else {
            if (DayOfTheWeek.fromDateTime(currentDate) == subscription.getFirstMessageDayOfWeek()) {
                return getMessage(currentWeek, firstMessageId);
            } else {
                return getMessage(currentWeek, secondMessageId);
            }
        }
    }

    public SubscriptionPackMessage getMessage(int weekId, int messageId) {

        return subscriptionPackMessageDataService.byWeekId(String.format(weekIdFormat, weekId, messageId));
    }
}
