package com.spartaecommerce.user.fixture;

import com.spartaecommerce.user.domain.event.UserCreatedEvent;
import com.spartaecommerce.user.domain.port.out.UserEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fake implementation of UserEventPublisher for testing.
 * <p>
 * Classical Testing Style:
 * - In-memory event storage for verification
 * - State-based testing (not interaction-based)
 * - Supports multiple event queries for comprehensive assertions
 * <p>
 * Big Tech Testing Practices:
 * - Simple, deterministic behavior
 * - No external dependencies
 * - Fast execution (no network/IO)
 * - Easy to reason about test failures
 */
public class FakeUserEventPublisher implements UserEventPublisher {

    private final List<UserCreatedEvent> publishedEvents = new ArrayList<>();
    private boolean shouldFail = false;
    private RuntimeException failureException;

    @Override
    public void publishUserCreated(UserCreatedEvent event) {
        if (shouldFail) {
            throw failureException != null
                ? failureException
                : new RuntimeException("Simulated publish failure");
        }
        publishedEvents.add(event);
    }

    /**
     * Returns all published events.
     * Returns unmodifiable list to prevent test pollution.
     */
    public List<UserCreatedEvent> getPublishedEvents() {
        return Collections.unmodifiableList(publishedEvents);
    }

    /**
     * Returns the most recently published event.
     * Useful for single-event test scenarios.
     */
    public UserCreatedEvent getLastPublishedEvent() {
        if (publishedEvents.isEmpty()) {
            return null;
        }
        return publishedEvents.get(publishedEvents.size() - 1);
    }

    /**
     * Returns the number of events published.
     * Useful for verifying expected event count.
     */
    public int getPublishedEventCount() {
        return publishedEvents.size();
    }

    /**
     * Finds events for a specific user ID.
     * Useful for multi-user test scenarios.
     */
    public List<UserCreatedEvent> getEventsForUser(Long userId) {
        return publishedEvents.stream()
            .filter(event -> event.getUserId().equals(userId))
            .toList();
    }

    /**
     * Checks if any event was published for the given user ID.
     */
    public boolean hasEventForUser(Long userId) {
        return publishedEvents.stream()
            .anyMatch(event -> event.getUserId().equals(userId));
    }

    /**
     * Simulates publish failure for negative test scenarios.
     */
    public void simulateFailure(RuntimeException exception) {
        this.shouldFail = true;
        this.failureException = exception;
    }

    /**
     * Resets the fake to initial state.
     * Useful in @BeforeEach if needed.
     */
    public void clear() {
        publishedEvents.clear();
        shouldFail = false;
        failureException = null;
    }
}
