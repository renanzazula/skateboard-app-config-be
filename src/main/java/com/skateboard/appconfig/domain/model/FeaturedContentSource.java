package com.skateboard.appconfig.domain.model;

/**
 * Logical content source a {@link HomeFeaturedPlayerConfig} can reference.
 * Only PODCAST exists today (backed by skateboard-podcast-be); new sources
 * are added here as they're onboarded — this service never depends on a
 * source's own IDs/schema, only this opaque source+contentId pair.
 */
public enum FeaturedContentSource {
    PODCAST
}
