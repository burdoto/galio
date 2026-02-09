package de.kaleidox.galio.util;

import lombok.NoArgsConstructor;
import lombok.Value;
import org.comroid.api.func.ext.Context;
import org.comroid.api.func.util.RootContextSource;

import static de.kaleidox.galio.util.ApplicationContextProvider.*;

@Value
@NoArgsConstructor
public class RootContextSupplier implements RootContextSource {
    @Override
    public Context getRootContext() {
        return bean(ApplicationContextProvider.class);
    }
}
