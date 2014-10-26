package com.alexecollins.docker.orchestration.util;


import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Comparator;
import java.util.Properties;
import java.util.TreeSet;

public final class Filters {
    private Filters() {
    }

    public static void filter(File file, FileFilter fileFilter, Properties properties) throws IOException {

        if (file == null) {
            throw new IllegalArgumentException("file is null");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("file " + file + " does not exist");
        }
        if (fileFilter == null) {
            throw new IllegalArgumentException("fileFilter is null");
        }
        if (properties == null) {
            throw new IllegalArgumentException("properties is null");
        }

        if (file.isDirectory()) {
            //noinspection ConstantConditions
            for (File child : file.listFiles()) {
                filter(child, fileFilter, properties);
            }
        } else if (fileFilter.accept(file)) {
            final File outFile = new File(file + ".tmp");
            final TokenReplacingReader in = new TokenReplacingReader(new BufferedReader(new FileReader(file)),
                    new PropertiesTokenResolver(properties));
            try {
                final FileWriter out = new FileWriter(outFile);
                try {
                    IOUtils.copy(in, out);
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }

            move(outFile, file);
        }
    }

    public static String filter(String l, Properties properties) {
        try {
            return IOUtils.toString(new TokenReplacingReader(
                    new StringReader(l),
                    new PropertiesTokenResolver(properties)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void move(File from, File to) throws IOException {
        //renaming over an existing file fails under Windows.
        to.delete();
        if (!from.renameTo(to)) {
            throw new IOException("failed to move " + from + " to " + to);
        }
    }

    static int maxKeyLength(Properties properties) {
        final TreeSet<Object> t = new TreeSet<Object>(new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        t.addAll(properties.keySet());
        return t.last().toString().length();
    }
}
