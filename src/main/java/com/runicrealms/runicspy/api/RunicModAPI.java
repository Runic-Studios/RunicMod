package com.runicrealms.runicspy.api;

import com.runicrealms.runicspy.RunicMod;
import org.jetbrains.annotations.NotNull;

/**
 * A utility class for the runic mod module
 *
 * @author BoBoBalloon
 * @since 6/28/23
 */
public final class RunicModAPI {
    private RunicModAPI() {

    }

    /**
     * A method that returns the instance of the spy api
     *
     * @return the instance of the spy api
     */
    @NotNull
    public static SpyAPI getSpyAPI() {
        return RunicMod.getInstance().getSpyAPI();
    }
}
