package org.xyplugin.xycore.api;

import org.xyplugin.xycore.api.attribute.AttributeService;
import org.xyplugin.xycore.api.placeholder.PlaceholderRegistry;

public interface XyCoreApi {
    AttributeService getAttributes();

    PlaceholderRegistry getPlaceholders();

    String getVersion();
}
