package com.robotca.ControlApp.Core;

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Interface for objects that can be loaded and saved with Bundles.
 *
 * Created by Nathaniel Stone on 3/26/16.
 */
public interface Savable {

    /**
     * Load from a Bundle.
     * @param bundle The Bundle
     */
    void load(@NonNull Bundle bundle);

    /**
     * Save to a Bundle.
     * @param bundle The Bundle
     */
    void save(@NonNull Bundle bundle);

}
