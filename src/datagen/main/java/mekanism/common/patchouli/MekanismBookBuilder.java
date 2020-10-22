package mekanism.common.patchouli;

import java.util.function.Consumer;
import mekanism.api.providers.IItemProvider;
import net.minecraft.util.ResourceLocation;
import vazkii.patchouli.api.data.BookBuilder;

/**
 * Created by Thiakil on 20/05/2020.
 */
public class MekanismBookBuilder extends BookBuilder {

    protected MekanismBookBuilder(String modid, String id, String displayName, String landingText) {
        super(modid, id, displayName, landingText);
    }

    protected MekanismBookBuilder(ResourceLocation id, String displayName, String landingText) {
        super(id, displayName, landingText);
    }

    public MekanismCategoryBuilder addCategory(GuideCategory cat, String name, String desc, IItemProvider icon) {
        return super.addCategory(new MekanismCategoryBuilder(cat.id, name, desc, icon.getItemStack(), this));
    }

    public void addCategory(GuideCategory cat, String name, String desc, IItemProvider icon, Consumer<MekanismCategoryBuilder> consumer) {
        consumer.accept(this.addCategory(cat, name, desc, icon));
    }
}
