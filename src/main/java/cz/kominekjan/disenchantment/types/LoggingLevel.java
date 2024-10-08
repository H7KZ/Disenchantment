package cz.kominekjan.disenchantment.types;

public enum LoggingLevel {
    INFO("INFO"),
    DEBUG("DEBUG"),
    ;

    private final String level;

    LoggingLevel(String level) {
        this.level = level;
    }

    public String getLevel() {
        return level;
    }
}
