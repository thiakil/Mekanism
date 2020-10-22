package mekanism.common.patchouli;

import java.util.function.Consumer;
import mekanism.api.providers.IItemProvider;
import net.minecraft.item.ItemStack;
import vazkii.patchouli.api.data.CategoryBuilder;
import vazkii.patchouli.api.data.EntryBuilder;

/**
 * Created by Thiakil on 20/05/2020.
 */
public class MekanismCategoryBuilder extends CategoryBuilder {

    protected final MekanismBookBuilder mekanismBookBuilder;

    protected MekanismCategoryBuilder(String id, String name, String description, ItemStack icon, MekanismBookBuilder bookBuilder) {
        super(id, name, description, icon, bookBuilder);
        this.mekanismBookBuilder = bookBuilder;
    }

    protected MekanismCategoryBuilder(String id, String name, String description, String icon, MekanismBookBuilder bookBuilder) {
        super(id, name, description, icon, bookBuilder);
        this.mekanismBookBuilder = bookBuilder;
    }

    public CategoryBuilder addSubCategory(GuideCategory cat, String title, String desc, IItemProvider icon, Consumer<MekanismCategoryBuilder> consumer) {
        MekanismCategoryBuilder categoryBuilder = this.mekanismBookBuilder.addCategory(cat, title, desc, icon);
        categoryBuilder.setParent(this.id.toString());
        consumer.accept(categoryBuilder);
        return categoryBuilder;
    }

    public EntryBuilder addEntry(GuideEntry guideEntry, String title,  IItemProvider icon) {
        return this.addEntry(guideEntry.getEntryId(), title, icon.getItemStack());
    }

    public void addEntry(GuideEntry guideEntry, String title, IItemProvider icon, Consumer<EntryBuilder> consumer) {
        consumer.accept(this.addEntry(guideEntry, title, icon));
    }
}
