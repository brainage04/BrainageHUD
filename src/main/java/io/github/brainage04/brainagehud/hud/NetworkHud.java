package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.NetworkHudConfig;
import io.github.brainage04.brainagehud.util.TimerUtils;
import io.github.brainage04.hudrendererlib.hud.core.BasicHudElement;

import java.util.ArrayList;
import java.util.List;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;
import static io.github.brainage04.brainagehud.util.MathUtils.roundDecimalPlaces;

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