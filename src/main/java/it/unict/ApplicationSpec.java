package it.unict;

public class ApplicationSpec {

    // Add Spec information here
    private String name;

    private String namespace;

    private String topologyKey;

    private Integer rescheduleDelay;

    private Integer rescheduleFactor;

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getTopologyKey() { return topologyKey; }

    public Integer getRescheduleDelay() { return rescheduleDelay; }

    public Integer getRescheduleFactor() { return rescheduleFactor; }
}
