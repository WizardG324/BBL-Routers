package com.benbenlaw.routers.transfers;

import com.benbenlaw.routers.Routers;
import com.benbenlaw.routers.api.TransferModule;
import com.benbenlaw.routers.api.transfers.EnergyTransfer;
import com.benbenlaw.routers.api.transfers.FluidTransfer;
import com.benbenlaw.routers.api.transfers.ItemTransfer;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

// Every kind of resource routers can move is a TransferModule registered here. The registry id doubles as the
// id of the resource's filter button, so these three keep the ids "item", "fluid" and "energy".
public class RoutersTransfers {

    public static final ResourceKey<Registry<TransferModule<?>>> TRANSFER_MODULE_KEY =
        ResourceKey.createRegistryKey(Routers.identifier("transfer_modules"));

    public static final DeferredRegister<TransferModule<?>> TRANSFER_MODULES =
        DeferredRegister.create(TRANSFER_MODULE_KEY, "routers");

    public static final Registry<TransferModule<?>> TRANSFER_MODULES_REGISTRY = TRANSFER_MODULES.makeRegistry(builder ->
            builder.sync(true)
    );

    public static final DeferredHolder<TransferModule<?>, ItemTransfer> ITEMS =
        TRANSFER_MODULES.register("item", ItemTransfer::new);

    public static final DeferredHolder<TransferModule<?>, FluidTransfer> FLUIDS =
        TRANSFER_MODULES.register("fluid", FluidTransfer::new);

    public static final DeferredHolder<TransferModule<?>, EnergyTransfer> ENERGY =
        TRANSFER_MODULES.register("energy", EnergyTransfer::new);

    // in registration order
    public static List<TransferModule<?>> modules() {
        List<TransferModule<?>> modules = new ArrayList<>();
        for (TransferModule<?> module : TRANSFER_MODULES_REGISTRY) modules.add(module);
        return modules;
    }

    // A flag for this resource in the manager chart's bit masks. Same on both sides because the registry is synced.
    public static int bit(Identifier id) {
        TransferModule<?> module = TRANSFER_MODULES_REGISTRY.getValue(id);
        if (module == null) return 0;

        int index = TRANSFER_MODULES_REGISTRY.getId(module);
        return index >= 0 && index < 31 ? 1 << index : 0;
    }
}
