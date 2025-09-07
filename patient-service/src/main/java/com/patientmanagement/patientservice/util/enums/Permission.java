package com.patientmanagement.patientservice.util.enums;

/**
 * Represents the specific actions a user can be granted on a module.
 * These correspond to the checkboxes in your UI (Create, Edit, Delete, List, View).
 */
public enum Permission {
    /**
     * Permission to create a new entity (e.g., a new Patient).
     */
    CREATE,

    /**
     * Permission to view the details of a single entity (e.g., view one Patient's record).
     * Often used for GET /api/resource/{id}
     */
    VIEW,

    /**
     * Permission to modify an existing entity (e.g., update a Patient's address).
     */
    EDIT,

    /**
     * Permission to remove an entity.
     */
    DELETE,

    /**
     * Permission to view a collection of entities (e.g., get a list of all Patients).
     * Often used for GET /api/resource
     */
    LIST
}
