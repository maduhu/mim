package org.motechproject.nms.kilkari.ut;

import org.motechproject.nms.kilkari.domain.SubscriptionPack;
import org.motechproject.nms.kilkari.domain.SubscriptionPackMessage;
import org.motechproject.nms.kilkari.domain.SubscriptionPackType;

import java.util.ArrayList;
import java.util.List;

/**
 * Test helper to create subscriptions
 * */
public final class SubscriptionTestHelper {

    public static SubscriptionPack createSubscriptionPack(String name, SubscriptionPackType type, int weeks,
                                                    int messagesPerWeek) {
        List<SubscriptionPackMessage> messages = new ArrayList<>();
        for (int week = 1; week <= weeks; week++) {
            messages.add(new SubscriptionPackMessage(String.format("w%s_1", week),
                    String.format("w%s_1.wav", week), 120));

            if (messagesPerWeek == 2) {
                messages.add(new SubscriptionPackMessage(String.format("w%s_2", week),
                        String.format("w%s_2.wav", week), 120));
            }
        }

        return new SubscriptionPack(name, type, weeks, messagesPerWeek, messages);
    }
}
