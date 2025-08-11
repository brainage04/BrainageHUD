package io.github.brainage04.brainagehud.hud;

import io.github.brainage04.brainagehud.config.hud.basic.NetworkHudConfig;
import io.github.brainage04.brainagehud.util.TimerUtils;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.util.TextList;

import static io.github.brainage04.brainagehud.util.ConfigUtils.getConfig;
import static io.github.brainage04.brainagehud.util.MathUtils.roundDecimalPlaces;

public class NetworkHud implements BasicCoreHudElement<NetworkHudConfig> {
    // Credits:
    // https://github.com/vladmarica/better-ping-display-fabric
    @Override
    public TextList getLines() {
        TextList lines = new TextList();

        if (getElementConfig().showPing) {
            lines.add("Ping: %sms".formatted(TimerUtils.ping));
        }

        if (getElementConfig().showTps) {
            lines.add("TPS: %s".formatted(
                    roundDecimalPlaces(TimerUtils.tps, getElementConfig().tpsDecimalPlaces)
            ));
        }

        return lines;
    }

    @Override
    public NetworkHudConfig getElementConfig() {
        return getConfig().networkHudConfig;
    }
}