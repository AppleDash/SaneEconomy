SaneEconomy
===========

Finally, a **sane** economy plugin for Bukkit.

This project is not dead. It just happens that I am busy and get a lot of difficult feature requests. The code doesn't magically break just by sitting around - even if the last commit was a month ago, the released version of the plugin probably still works fine! :)

## About

I was looking for an economy plugin for a server I administrate, and I noticed something quite strange.

All of the plugins are years outdated, built against Bukkit API versions as old as Minecraft 1.2.4, and often have loads of bug reports,
both in the comments on BukkitDev/SpigotMC, and on GitHub, all with no response from the developer!

I decided that it was time for a change. I wanted a working, updated economy plugin for Bukkit, built against the latest API. So I wrote one myself.

## Components

* SaneEconomyCore - The main economy provider.
* SaneEconomySignShop - A side project written for a specific server. Unsupported.
* SaneEconomyMobKills - Another side project for the same server. Unsupported.

## Development

We manage dependencies with Maven.
We try to stick to [SemVer](http://semver.org/), but it's rather difficult with a Bukkit plugin so in reality our version numbers are almost meaningless.
Generally, new releases should be built against the latest Spigot/Bukkit at the time the release is published.

