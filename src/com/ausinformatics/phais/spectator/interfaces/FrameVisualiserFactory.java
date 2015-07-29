package com.ausinformatics.phais.spectator.interfaces;

public interface FrameVisualiserFactory<S> {
    public FrameVisualisationHandler<S> createHandler();
}
