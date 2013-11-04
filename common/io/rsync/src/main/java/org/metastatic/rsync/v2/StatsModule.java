/* :vim:set tw=78 expandtab tabstop=3 softtabstop=3 shiftwidth=3:
   $Id$

   StastModule: special statistics module.
   Copyright (C) 2003  Casey Marshall <rsdio@metastatic.org>

   This file is a part of Jarsync.

   Jarsync is free software; you can redistribute it and/or modify it
   under the terms of the GNU General Public License as published by the
   Free Software Foundation; either version 2, or (at your option) any
   later version.

   Jarsync is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Jarsync; see the file COPYING.  If not, write to the

      Free Software Foundation Inc.,
      59 Temple Place - Suite 330,
      Boston, MA 02111-1307
      USA  */

package org.metastatic.rsync.v2;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

class StatsModule extends Module {

    long startupTime;
    int currentConnections = 0;
    int numConnections = 0;
    long bytesRead = 0;
    long bytesWritten = 0;

    StatsModule() {
        super("#stats");
        startupTime = System.currentTimeMillis();
        readOnly = true;
        comment = "show this server's statistics";
    }

    String format(Map modules) {
        long uptime = System.currentTimeMillis() - startupTime;
        int days = (int) (uptime / 86400000);
        int hours = (int) (uptime / 3600000 % 24);
        int minutes = (int) (uptime / 60000 % 60);
        int seconds = (int) (uptime / 1000 % 60);
        StringBuilder result = new StringBuilder();
        result.append("JARSYNC SERVER STATISTICS\n\nuptime: up ");
        if (days > 0) {
            result.append(days).append(days != 1 ? " days, " : " day, ");
        }
        if (hours > 0 || days > 0) {
            result.append(hours).append(hours != 1 ? " hours, " : " hour, ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            result.append(minutes).append(minutes != 1 ? " minutes, " : " minute, ");
        }
        result.append(seconds).append(seconds != 1 ? " seconds.\n" : " second.\n");
        result.append("Wrote ").append(bytesWritten).append(" bytes, read ").append(bytesRead).append(" bytes.\n");
        result.append(currentConnections).append(currentConnections != 1
                ? " current connections, " : " current connection, ").append(numConnections).append(numConnections != 1 ? " total connections.\n"
                : " total connection.\n");

        Runtime r = Runtime.getRuntime();
        long usedMem = r.totalMemory() - r.freeMemory();
        result.append("Memory used: ").append(usedMem).append(" bytes.\n\n");
        result.append("Individual module statistics:\n");
        result.append("Module         Connections    Total connections\n");
        Iterator it = new TreeSet(modules.values()).iterator();
        while (it.hasNext()) {
            Module mod = (Module) it.next();
            if (!mod.list) {
                continue;
            }
            StringBuilder line = new StringBuilder(
                    "                                             ");
            line.replace(0, Math.min(14, mod.name.length()), mod.name);
            line.replace(15, Math.min(29,
                    15 + String.valueOf(mod.connections).length()),
                    String.valueOf(mod.connections));
            line.replace(30, Math.min(44,
                    30 + String.valueOf(mod.totalConnections).length()),
                    String.valueOf(mod.totalConnections));
            result.append(line.toString());
            result.append("\n");
        }
        return result.toString();
    }
}
