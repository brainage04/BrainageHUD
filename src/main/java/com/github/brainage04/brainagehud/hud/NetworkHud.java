package com.github.brainage04.brainagehud.hud;

import com.github.brainage04.brainagehud.config.hud.basic.NetworkHudConfig;
import com.github.brainage04.brainagehud.hud.core.BasicHudElement;
import com.github.brainage04.brainagehud.util.TimerUtils;

import java.util.ArrayList;
import java.util.List;

import static com.github.brainage04.brainagehud.util.ConfigUtils.getConfig;
import static com.github.brainage04.brainagehud.util.MathUtils.roundDecimalPlaces;

public class NetworkHud implements BasicHudElement<NetworkHudConfig> {
    // Credits:
    // https://github.com/vladmarica/better-ping-display-fabric
    @Override
    public List<String> getLines() {
        List<String> lines = new ArrayList<>(List.of());

        if (getElementConfig().showPing) {
            lines.add(
                    String.format(
                            "Ping: %sms",
                            TimerUtils.ping
                    )
            );
        }

        if (getElementConfig().showTps) {
            lines.add(
                    String.format(
                            "TPS: %s",
                            roundDecimalPlaces(TimerUtils.tps, getElementConfig().tpsDecimalPlaces)
                    )
            );
        }

        return lines;
    }

    @Override
    public NetworkHudConfig getElementConfig() {
        return getConfig().networkHudConfig;
    }
}