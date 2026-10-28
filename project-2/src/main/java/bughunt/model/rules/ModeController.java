package bughunt.model.rules;

import bughunt.model.actors.Bug;
import bughunt.model.core.Match;
import bughunt.model.modes.BugMode;
import bughunt.model.modes.Chase;
import bughunt.model.modes.Scatter;

/**
 * The bugs' clock: alternates scatter and chase waves and switches on the
 * vulnerable mode when a breakpoint is picked up.
 * The wave clock stands still during a breakpoint.
 */
public class ModeController {
    private int[] waves = {Integer.MAX_VALUE};
    private int waveIndex;
    private int ticksInWave;
    private int breakpointTicks;
    private int breakpointDuration;
    private int flashingTicks;

    public void start(LevelConfig config) {
        waves = config.wavesInTicks();
        waveIndex = 0;
        ticksInWave = 0;
        breakpointTicks = 0;
        breakpointDuration = config.breakpointDuration();
        flashingTicks = config.flashingTicks();
    }

    /** Even waves are scatter; odd waves are chase. */
    public BugMode newGlobalMode() {
        return waveIndex % 2 == 0 ? new Scatter() : new Chase();
    }

    public void update(Match match) {
        if (breakpointTicks > 0) {
            if (--breakpointTicks == 0) {
                for (Bug bug : match.bugs()) {
                    bug.onBreakpointEnd(newGlobalMode());
                }
            }
            return;
        }
        if (waveIndex < waves.length - 1 && ++ticksInWave >= waves[waveIndex]) {
            waveIndex++;
            ticksInWave = 0;
            for (Bug bug : match.bugs()) {
                bug.onWaveChange(newGlobalMode());
            }
        }
    }

    public void activateBreakpoint(Match match) {
        breakpointTicks = match.config().breakpointDuration();
        for (Bug bug : match.bugs()) {
            bug.onBreakpointStart();
        }
    }

    public boolean isBreakpointActive() {
        return breakpointTicks > 0;
    }

    public boolean isVulnerableEnding() {
        return breakpointTicks > 0 && breakpointTicks <= flashingTicks;
    }

    public int breakpointTicksLeft() {
        return breakpointTicks;
    }

    /** The full length of a breakpoint at this level (for the HUD bar). */
    public int breakpointDurationTicks() {
        return breakpointDuration;
    }

    public String waveName() {
        return waveIndex % 2 == 0 ? "SCATTER" : "CHASE";
    }
}
