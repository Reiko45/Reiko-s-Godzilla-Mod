package com.reiko45.godzilla.entity;

/** Machines knocked out by a MUTO's electromagnetic pulse. */
public interface EmpSensitive {
    void applyEmp(int ticks);
}
