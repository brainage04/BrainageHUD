package com.github.brainage04.brainagehud.hud.core;

import java.util.List;

public interface HUDElement {
    List<String> getLines();
    void updateLines();
}
