package org.motechproject.nms.kilkari.domain;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.nms.props.domain.DayOfTheWeek;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Unique;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@Entity(maxFetchDepth = -1, tableName = "nms_subscriptions")
@Index(name = "status_endDate_composite_idx", members = { "status", "endDate" })
public class Subscription {

    private static final int DAYS_IN_WEEK = 7;
    private static final int FOUR_DAYS_LATER = 4;
    private static final int TWO_MESSAGES_PER_WEEK = 2;

    @Field
    @Unique
    @Column(allowsNull = "false", length = 36)
    @NotNull
    private String subscriptionId;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private Subscriber subscriber;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private SubscriptionPack subscriptionPack;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private SubscriptionStatus status;

    @Field
    @Column(allowsNull = "false")
    @NotNull
    private SubscriptionOrigin origin;

    @Field
    private DateTime startDate;

    @Field
    private DateTime endDate;

    @Field
    private DayOfTheWeek firstMessageDayOfWeek;

    @Field
    private DayOfTheWeek secondMessageDayOfWeek;

    @Field
    private DeactivationReason deactivationReason;

    @Field
    private boolean needsWelcomeMessage;

    public Subscription(Subscriber subscriber, SubscriptionPack subscriptionPack, SubscriptionOrigin origin) {
        this(subscriber, subscriptionPack, origin, null);
    }

    public Subscription(Subscriber subscriber, SubscriptionPack subscriptionPack, SubscriptionOrigin subscriptionOrigin, DateTime startDate) {
        this.subscriptionId = UUID.randomUUID().toString();
        this.subscriber = subscriber;
        this.subscriptionPack = subscriptionPack;
        this.origin = subscriptionOrigin;
        if (origin == SubscriptionOrigin.MCTS_IMPORT) {
            needsWelcomeMessage = true;
        }
        this.startDate = startDate;
        this.firstMessageDayOfWeek = DayOfTheWeek.fromDateTime(startDate);

        if (subscriptionPack.getMessagesPerWeek() == TWO_MESSAGES_PER_WEEK) {
            this.secondMessageDayOfWeek = DayOfTheWeek.fromInt(
                    (startDate.getDayOfWeek() + FOUR_DAYS_LATER) % DAYS_IN_WEEK);
        }
    }

    public String getSubscriptionId() { return subscriptionId; }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    public SubscriptionPack getSubscriptionPack() {
        return subscriptionPack;
    }

    public void setSubscriptionPack(SubscriptionPack subscriptionPack) {
        this.subscriptionPack = subscriptionPack;
    }

    public SubscriptionStatus getStatus() { return status; }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;

        if (this.status == SubscriptionStatus.DEACTIVATED || this.status == SubscriptionStatus.COMPLETED) {
            setEndDate(new DateTime());
        } else {
            setEndDate(null);
        }
    }

    public SubscriptionOrigin getOrigin() { return origin; }

    public void setOrigin(SubscriptionOrigin origin) { this.origin = origin; }

    public DateTime getStartDate() { return startDate; }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
        this.firstMessageDayOfWeek = DayOfTheWeek.fromInt(startDate.getDayOfWeek());
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    public DayOfTheWeek getFirstMessageDayOfWeek() {
        return firstMessageDayOfWeek;
    }

    public void setFirstMessageDayOfWeek(DayOfTheWeek firstMessageDayOfWeek) {
        this.firstMessageDayOfWeek = firstMessageDayOfWeek;
    }

    public DayOfTheWeek getSecondMessageDayOfWeek() {
        return secondMessageDayOfWeek;
    }

    public void setSecondMessageDayOfWeek(DayOfTheWeek secondMessageDayOfWeek) {
        this.secondMessageDayOfWeek = secondMessageDayOfWeek;
    }

    public DeactivationReason getDeactivationReason() { return deactivationReason; }

    public void setDeactivationReason(DeactivationReason deactivationReason) {
        this.deactivationReason = deactivationReason;
    }

    public boolean getNeedsWelcomeMessage() {
        return needsWelcomeMessage;
    }

    public void setNeedsWelcomeMessage(boolean needsWelcomeMessage) {
        this.needsWelcomeMessage = needsWelcomeMessage;
    }

    /**
     * Helper method to be called by the CDR processor to determine whether to mark subscription status as COMPLETED
     * @param date The date for which subscription status is to be evaluated
     * @return true if the subscription should be marked completed, false otherwise
     */
    public boolean hasCompleted(DateTime date) {
        return hasCompletedForStartDate(startDate, date, subscriptionPack);
    }

    public static boolean hasCompletedForStartDate(DateTime startDate, DateTime today, SubscriptionPack pack) {
        int totalDaysInPack = pack.getWeeks() * DAYS_IN_WEEK;
        int daysSinceStartDate = Days.daysBetween(startDate, today).getDays();

        return totalDaysInPack < daysSinceStartDate;
    }


    /**
     * Helper method which determines if the given contentFileName corresponds to the last message of this
     * subscription's message pack
     *
     * @param contentFileName
     * @return true if contentFileName is the last message for this subscription's message pack
     */
    public boolean isLastPackMessage(String contentFileName) {
        List<SubscriptionPackMessage> messages = subscriptionPack.getMessages();
        return messages.get(messages.size() - 1).getMessageFileName().equals(contentFileName);
    }

    /**
     * Helper method that determines the subscription status based on its start date, subscription pack length
     * and current status (deactivated subscriptions remain deactivated)
     * @param subscription subscription for which the status is to be determined
     * @param today today's date
     * @return status determined for the provided subscription
     */
    public static SubscriptionStatus getStatus(Subscription subscription, DateTime today) {
        if (subscription.getStatus() == SubscriptionStatus.DEACTIVATED) {
            return SubscriptionStatus.DEACTIVATED;
        } else {
            int daysInPack = subscription.getSubscriptionPack().getWeeks() * DAYS_IN_WEEK;
            DateTime startDate = subscription.getStartDate();
            DateTime completionDate = startDate.plusDays(daysInPack);

            if (today.isBefore(startDate)) {
                return SubscriptionStatus.PENDING_ACTIVATION;
            } else if (today.isAfter(startDate) && today.isBefore(completionDate)) {
                return SubscriptionStatus.ACTIVE;
            } else {
                return SubscriptionStatus.COMPLETED;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Subscription that = (Subscription) o;

        return !(subscriptionId != null ? !subscriptionId
                .equals(that.subscriptionId) : that.subscriptionId != null);

    }

    @Override
    public int hashCode() {
        return subscriptionId != null ? subscriptionId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "subscriptionId='" + subscriptionId + '\'' +
                //todo: put back subscriptionPack when the getDetachedField bug is fixed...
                ", status=" + status +
                ", origin=" + origin +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
