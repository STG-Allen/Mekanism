package buildcraft.api.enums;

import net.minecraft.util.IStringSerializable;

import org.apache.commons.lang3.StringUtils;

public enum EnumEnergyStage implements IStringSerializable {
    BLUE,
    GREEN,
    YELLOW,
    RED,
    OVERHEAT,
    BLACK;
    public static final EnumEnergyStage[] VALUES = values();

    public String getModelName() {
        return StringUtils.lowerCase(name());
    }

    @Override
    public String getName() {
        return getModelName();
    }
}
