package com.ausinformatics.phais.server.interfaces;

import com.ausinformatics.phais.common.events.VisualGameEvent;

public interface EventManager {

    public String toData(VisualGameEvent event);
    
    public VisualGameEvent fromData(String data);
    
}
