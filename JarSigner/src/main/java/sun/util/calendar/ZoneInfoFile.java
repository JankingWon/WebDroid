/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.util.calendar;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sun.security.action.GetPropertyAction;

/**
 * Loads TZDB time-zone rules for j.u.TimeZone
 * <p>
 * @since 1.8
 */
public final class ZoneInfoFile {

    /**
     * Gets all available IDs supported in the Java run-time.
     *
     * @return a set of time zone IDs.
     */
    public static String[] getZoneIds() {
        int len = regions.length + oldMappings.length;
        if (!USE_OLDMAPPING) {
            len += 3;    // EST/HST/MST not in tzdb.dat
        }
        String[] ids = Arrays.copyOf(regions, len);
        int i = regions.length;
        if (!USE_OLDMAPPING) {
            ids[i++] = "EST";
            ids[i++] = "HST";
            ids[i++] = "MST";
        }
        for (int j = 0; j < oldMappings.length; j++) {
            ids[i++] = oldMappings[j][0];
        }
        return ids;
    }


    /**
     * Returns a Map from alias time zone IDs to their standard
     * time zone IDs.
     *
     * @return an unmodified alias mapping
     */
    public static Map<String, String> getAliasMap() {
        return Collections.unmodifiableMap(aliases);
    }

    /**
     * Gets the version of this tz data.
     *
     * @return the tzdb version
     */
    public static String getVersion() {
        return versionId;
    }

    ///////////////////////////////////////////////////////////
    private ZoneInfoFile() {
    }

    private static String versionId;
    private final static Map<String, ZoneInfo> zones = new ConcurrentHashMap<>();
    private static Map<String, String> aliases = new HashMap<>();

    private static byte[][] ruleArray;
    private static String[] regions;
    private static int[] indices;

    // Flag for supporting JDK backward compatible IDs, such as "EST".
    private static final boolean USE_OLDMAPPING;

    private static String[][] oldMappings = new String[][] {
        { "ACT", "Australia/Darwin" },
        { "AET", "Australia/Sydney" },
        { "AGT", "America/Argentina/Buenos_Aires" },
        { "ART", "Africa/Cairo" },
        { "AST", "America/Anchorage" },
        { "BET", "America/Sao_Paulo" },
        { "BST", "Asia/Dhaka" },
        { "CAT", "Africa/Harare" },
        { "CNT", "America/St_Johns" },
        { "CST", "America/Chicago" },
        { "CTT", "Asia/Shanghai" },
        { "EAT", "Africa/Addis_Ababa" },
        { "ECT", "Europe/Paris" },
        { "IET", "America/Indiana/Indianapolis" },
        { "IST", "Asia/Kolkata" },
        { "JST", "Asia/Tokyo" },
        { "MIT", "Pacific/Apia" },
        { "NET", "Asia/Yerevan" },
        { "NST", "Pacific/Auckland" },
        { "PLT", "Asia/Karachi" },
        { "PNT", "America/Phoenix" },
        { "PRT", "America/Puerto_Rico" },
        { "PST", "America/Los_Angeles" },
        { "SST", "Pacific/Guadalcanal" },
        { "VST", "Asia/Ho_Chi_Minh" },
    };

    static {
        String oldmapping = AccessController.doPrivileged(
            new GetPropertyAction("sun.timezone.ids.oldmapping", "false")).toLowerCase(Locale.ROOT);
        USE_OLDMAPPING = (oldmapping.equals("yes") || oldmapping.equals("true"));
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    String libDir = System.getProperty("java.home") + File.separator + "lib";
                    try (DataInputStream dis = new DataInputStream(
                             new BufferedInputStream(new FileInputStream(
                                 new File(libDir, "tzdb.dat"))))) {
                        load(dis);
                    }
                } catch (Exception x) {
                    throw new Error(x);
                }
                return null;
            }
        });
    }

    private static void addOldMapping() {
        for (String[] alias : oldMappings) {
            aliases.put(alias[0], alias[1]);
        }
        if (USE_OLDMAPPING) {
            aliases.put("EST", "America/New_York");
            aliases.put("MST", "America/Denver");
            aliases.put("HST", "Pacific/Honolulu");
        } else {
            zones.put("EST", new ZoneInfo("EST", -18000000));
            zones.put("MST", new ZoneInfo("MST", -25200000));
            zones.put("HST", new ZoneInfo("HST", -36000000));
        }
    }

    /**
     * Loads the rules from a DateInputStream
     *
     * @param dis  the DateInputStream to load, not null
     * @throws Exception if an error occurs
     */
    private static void load(DataInputStream dis) throws ClassNotFoundException, IOException {
        if (dis.readByte() != 1) {
            throw new StreamCorruptedException("File format not recognised");
        }
        // group
        String groupId = dis.readUTF();
        if ("TZDB".equals(groupId) == false) {
            throw new StreamCorruptedException("File format not recognised");
        }
        // versions, only keep the last one
        int versionCount = dis.readShort();
        for (int i = 0; i < versionCount; i++) {
            versionId = dis.readUTF();

        }
        // regions
        int regionCount = dis.readShort();
        String[] regionArray = new String[regionCount];
        for (int i = 0; i < regionCount; i++) {
            regionArray[i] = dis.readUTF();
        }
        // rules
        int ruleCount = dis.readShort();
        ruleArray = new byte[ruleCount][];
        for (int i = 0; i < ruleCount; i++) {
            byte[] bytes = new byte[dis.readShort()];
            dis.readFully(bytes);
            ruleArray[i] = bytes;
        }
        // link version-region-rules, only keep the last version, if more than one
        for (int i = 0; i < versionCount; i++) {
            regionCount = dis.readShort();
            regions = new String[regionCount];
            indices = new int[regionCount];
            for (int j = 0; j < regionCount; j++) {
                regions[j] = regionArray[dis.readShort()];
                indices[j] = dis.readShort();
            }
        }
        // remove the following ids from the map, they
        // are exclued from the "old" ZoneInfo
        zones.remove("ROC");
        for (int i = 0; i < versionCount; i++) {
            int aliasCount = dis.readShort();
            aliases.clear();
            for (int j = 0; j < aliasCount; j++) {
                String alias = regionArray[dis.readShort()];
                String region = regionArray[dis.readShort()];
                aliases.put(alias, region);
            }
        }
        // old us time-zone names
        addOldMapping();
    }


    static final int DAYS_PER_CYCLE = 146097;
}
