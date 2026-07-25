package org.xyplugin.xycore.api.placeholder;

public interface PlaceholderRegistry {
    void register(PlaceholderProvider provider);

    void unregister(String namespace);
}
