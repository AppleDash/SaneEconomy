package org.appledash.saneeconomy;

import org.appledash.saneeconomy.economy.EconomyManager;
import org.appledash.saneeconomy.economy.logger.TransactionLogger;
import org.appledash.saneeconomy.vault.VaultHook;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by appledash on 9/18/16.
 * Blackjack is best pony.
 *
 * This interface represent's SaneEconomy's public API.
 * Anything not exposed in some way by this interface should be considered unstable, and may change at any time.
 */
public interface ISaneEconomy {
    /**
     * Get the active EconomyManager
     * @return EconomyManager
     */
    EconomyManager getEconomyManager();

    /**
     * Get the active TransactionLogger
     * @return TransactionLogger, if there is one. Otherwise, Optional.empty()
     */
    Optional<TransactionLogger> getTransactionLogger();

    VaultHook getVaultHook();

    String getLastName(UUID uuid);
}
