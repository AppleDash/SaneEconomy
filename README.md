SaneEconomy
===========

Finally, a **sane** economy plugin for Bukkit.

## About

I was looking for an economy plugin for a server I administrate, and I noticed something quite strange.

All of the plugins are years outdated, built against Bukkit API versions as old as Minecraft 1.2.4, and often have loads of bug reports,
both in the comments on BukkitDev/SpigotMC, and on GitHub, all with no response from the developer!

I decided that it was time for a change. I wanted a working, updated economy plugin for Bukkit, built against the latest API. So I wrote one myself.

## Development

Our dependencies are managed with Gradle instead of Maven. Other than that, it's a pretty standard development workflow.
We try to stick to [SemVer](http://semver.org/), but it's rather difficult with a Bukkit plugin so in reality our version numbers are almost meaningless.
Generally, new releases should be built against the latest Spigot/Bukkit at the time the release is published.
