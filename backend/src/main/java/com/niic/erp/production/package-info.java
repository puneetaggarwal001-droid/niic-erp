/**
 * Production module — replaces the legacy app's fprod_* Firestore keys: PC
 * rates, jobs (with colour/size variants), workstations, operations, routing
 * + routing-change requests, daily production entries/edits, QC entries +
 * rework, and workstation-to-workstation transfer challans.
 *
 * Key deviations from the legacy data model (each documented at the relevant
 * class): job numbers and transfer challan numbers are allocated atomically
 * (a DB sequence and a locked counter table, respectively, instead of legacy's
 * racy read-increment-write); job/routing/production-edit approval requests
 * store their proposed payload as JSON staging data rather than duplicating
 * the full relational shape; every job is required to have at least one
 * colour/size (a "Default" colour can stand in for non-variant styles) rather
 * than branching the schema for a colour-less case.
 */
package com.niic.erp.production;
