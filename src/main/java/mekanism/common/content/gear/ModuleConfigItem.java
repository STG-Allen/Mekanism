package mekanism.common.content.gear;

import java.util.function.Consumer;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.base.ILangEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class ModuleConfigItem<TYPE> {

    private Module module;
    private String name;
    private ILangEntry description;
    private ConfigData<TYPE> data;

    public ModuleConfigItem(Module module, String name, ILangEntry description, ConfigData<TYPE> data, TYPE def) {
        this.module = module;
        this.name = name;
        this.description = description;
        this.data = data;
        data.set(def);
    }

    public ILangEntry getDescription() {
        return description;
    }

    public ConfigData<TYPE> getData() {
        return data;
    }

    public TYPE get() {
        return data.get();
    }

    public void set(TYPE type, Consumer<ItemStack> callback) {
        data.set(type);
        // validity checks
        for (Module m : Modules.loadAll(module.getContainer())) {
            // disable other exclusive modules
            if (name.equals(Module.ENABLED_KEY) && type == Boolean.TRUE && module.getData().isExclusive()) {
                if (m.getData().isExclusive() && m.getData() != module.getData()) {
                    m.setDisabledForce();
                }
            }
            // turn off mode change handling for other modules
            if (name.equals(Module.HANDLE_MODE_CHANGE_KEY) && type == Boolean.TRUE && module.handlesModeChange()) {
                if (m.handlesModeChange() && m.getData() != module.getData()) {
                    m.setModeHandlingDisabledForce();
                }
            }
        }
        // finally, save this specific module with the callback (to send a packet)
        module.save(callback);
    }

    public void read(CompoundNBT tag) {
        if (tag.contains(name)) {
            data.read(name, tag);
        }
    }

    public void write(CompoundNBT tag) {
        data.write(name, tag);
    }

    public String getName() {
        return name;
    }

    public interface ConfigData<TYPE> {
        TYPE get();
        void set(TYPE val);
        void read(String name, CompoundNBT tag);
        void write(String name, CompoundNBT tag);
    }

    public static class BooleanData implements ConfigData<Boolean> {

        private boolean value;

        public BooleanData() {
            this(true);
        }

        public BooleanData(boolean def) {
            value = def;
        }

        @Override
        public Boolean get() {
            return value;
        }

        @Override
        public void set(Boolean val) {
            value = val;
        }

        @Override
        public void read(String name, CompoundNBT tag) {
            value = tag.getBoolean(name);
        }

        @Override
        public void write(String name, CompoundNBT tag) {
            tag.putBoolean(name, value);
        }
    }

    public static class EnumData<TYPE extends Enum<TYPE> & IHasTextComponent> implements ConfigData<TYPE> {

        private Class<TYPE> enumClass;
        private TYPE value;
        private int selectableCount;
        private float textScale;

        public EnumData(Class<TYPE> enumClass) {
            this(enumClass, enumClass.getEnumConstants().length);
        }

        public EnumData(Class<TYPE> enumClass, int selectableCount) {
            this.enumClass = enumClass;
            this.selectableCount = selectableCount;
        }

        public EnumData<TYPE> withScale(float textScale) {
            this.textScale = textScale;
            return this;
        }

        @Override
        public TYPE get() {
            return value;
        }

        @Override
        public void set(TYPE val) {
            value = val;
        }

        @Override
        public void read(String name, CompoundNBT tag) {
            int index = Math.min(tag.getInt(name), selectableCount-1);
            value = enumClass.getEnumConstants()[index];
        }

        @Override
        public void write(String name, CompoundNBT tag) {
            tag.putInt(name, value.ordinal());
        }

        public TYPE[] getEnums() {
            return enumClass.getEnumConstants();
        }

        public int getSelectableCount() {
            return selectableCount;
        }

        public float getTextScale() {
            return textScale;
        }
    }
}
