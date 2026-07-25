package org.xyplugin.xycore.api.attribute;

import java.util.List;
import org.bukkit.entity.LivingEntity;

public interface AttributeService {
    boolean isAvailable();

    String getProviderName();

    boolean addSource(LivingEntity entity, String source, List<String> attributeLines);

    boolean removeSource(LivingEntity entity, String source);
}
