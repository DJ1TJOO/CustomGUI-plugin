package me.dj1tjoo.customgui.titles;

import me.dj1tjoo.customgui.MinecraftHelpers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class TitleShownManager {

    private static TitleShownManager INSTANCE;

    public static TitleShownManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TitleShownManager();
        }

        return INSTANCE;
    }

    private final Map<UUID, TitleShownEntry> entries;
    private final Map<UUID, BukkitTask> entryRemovals;

    private TitleShownManager() {
        entries = new HashMap<>();
        entryRemovals = new HashMap<>();
    }

    public void showTitleTo(UUID playerUUID, TitleShownEntry entry, Plugin plugin) {
        TitleShownEntry oldEntry = entries.get(playerUUID);
        if (oldEntry != null && oldEntry.title.getUuid().equals(entry.title.getUuid())) {
            removeEntryRemovalTask(playerUUID);
        } else {
            remove(playerUUID);
        }

        entries.put(playerUUID, entry);
        BukkitTask task = Bukkit.getScheduler()
            .runTaskLater(plugin, () -> remove(entry.title, true), MinecraftHelpers.secondsToTicks(
                Duration.between(entry.completedShownAt(), Instant.now()).getSeconds()) + 1);
        entryRemovals.put(playerUUID, task);
    }

    public void remove(BasicTitle title) {
        remove(title, false);
    }

    public void remove(BasicTitle title, boolean requireCompletedShown) {
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, TitleShownEntry> entry : entries.entrySet()) {
            if (!entry.getValue().title.getUuid().equals(title.getUuid())) {
                continue;
            }
            if (requireCompletedShown && !entry.getValue().completedShown()) {
                continue;
            }
            toRemove.add(entry.getKey());
        }

        for (UUID uuid : toRemove) {
            remove(uuid, requireCompletedShown);
        }
    }

    public void remove(UUID playerUUID) {
        remove(playerUUID, false);
    }

    public void remove(UUID playerUUID, boolean requireCompletedShown) {
        if (entries.containsKey(playerUUID)) {
            if (requireCompletedShown && !entries.get(playerUUID).completedShown()) {
                return;
            }

            HumanEntity humanEntity = Bukkit.getPlayer(playerUUID);
            if (humanEntity != null) {
                humanEntity.showTitle(Title.title(Component.empty(), Component.empty(), Title.Times.times(
                    Duration.ZERO,Duration.ZERO,Duration.ZERO)));
            }

            entries.get(playerUUID).title.closed(humanEntity);
            entries.remove(playerUUID);
        }

        removeEntryRemovalTask(playerUUID);
    }

    private void removeEntryRemovalTask(UUID playerUUID) {
        if (entryRemovals.containsKey(playerUUID)) {
            entryRemovals.get(playerUUID).cancel();
            entryRemovals.remove(playerUUID);
        }
    }

    public boolean isShowingTo(UUID playerUUID, BasicTitle title) {
        TitleShownEntry entry = getShowingTo(playerUUID);
        if (entry == null) {
            return false;
        }

        return entry.title.getUuid().equals(title.getUuid());
    }

    public TitleShownEntry getShowingTo(UUID playerUUID) {
        return entries.get(playerUUID);
    }

    public record TitleShownEntry(BasicTitle title, Instant shownAt, Title.Times times) {
        public Instant completedShownAt() {
            Instant showingDoneAt = shownAt.plus(times.fadeIn());
            showingDoneAt = showingDoneAt.plus(times.stay());
            showingDoneAt = showingDoneAt.plus(times.fadeOut());

            return showingDoneAt;
        }

        public boolean completedShown() {
            return completedShownAt().isBefore(Instant.now());
        }

        public static TitleShownEntry fromTitle(BasicTitle basicTitle, Title title) {
            return new TitleShownEntry(basicTitle, Instant.now(), title.times());
        }
    }
}
