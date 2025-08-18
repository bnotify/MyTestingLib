package com.example.mycustomlib.constants

/**
 * Enum representing different types of notifications with their specific purposes.
 */
enum class NotificationType {
    /**
     * Used for marketing purposes such as sales, special offers, and campaigns.
     * Typically sent to promote products or services.
     */
    PROMOTIONAL,

    /**
     * Triggered by a user action or transaction.
     * Examples: order confirmations, payment receipts, shipping updates.
     */
    TRANSACTIONAL,

    /**
     * Provides general updates or news information.
     * Examples: app updates, new content uploads, newsletter.
     */
    INFORMATIONAL,

    /**
     * Indicates urgent matters requiring immediate attention.
     * Examples: low balance alerts, failed login attempts, system errors.
     */
    ALERT,

    /**
     * Similar to [ALERT], indicates important warnings.
     * Examples: service interruptions, security warnings, critical updates.
     */
    WARNING,

    /**
     * For scheduled or time-based reminders.
     * Examples: calendar events, medication reminders, subscription renewals.
     */
    REMINDER,

    /**
     * Related to social network activity.
     * Examples: likes, comments, friend requests, mentions.
     */
    SOCIAL,

    /**
     * Pertains to account security and activity.
     * Examples: password changes, logins from new devices, 2FA codes.
     */
    SECURITY
}