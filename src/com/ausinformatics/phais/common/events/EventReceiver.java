package com.ausinformatics.phais.common.events;

import java.util.List;

public interface EventReceiver {
    public void giveEvents(List<VisualGameEvent> events);
}
