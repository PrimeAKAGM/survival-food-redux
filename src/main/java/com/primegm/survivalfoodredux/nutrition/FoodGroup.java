package com.primegm.survivalfoodredux.nutrition;

/**
 * Food groups for the nutrition system.
 * Each food item can be assigned to one group.
 */
public enum FoodGroup {
    PROTEIN,
    FIBER,
    SUGAR,
    FAT,
    UNKNOWN;

    /**
     * Check if this is a valid (known) food group.
     *
     * @return true if not UNKNOWN
     */
    public boolean isValid() {
        return this != UNKNOWN;
    }
}
