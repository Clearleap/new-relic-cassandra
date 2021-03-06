package com.tylerhoersch.nr.cassandra.templates;

import com.newrelic.metrics.publish.util.Logger;
import com.tylerhoersch.nr.cassandra.JMXRunner;
import com.tylerhoersch.nr.cassandra.JMXTemplate;
import com.tylerhoersch.nr.cassandra.Metric;

import javax.management.MBeanServerConnection;
import java.lang.Long;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Cassandra2xMetrics implements JMXTemplate<List<Metric>> {

    private static final Logger logger = Logger.getLogger(Cassandra2xMetrics.class);

    private static final String READ_LATENCY_INSTANCE = "Cassandra/hosts/%s/Latency/Reads";
    private static final String WRITE_LATENCY_INSTANCE = "Cassandra/hosts/%s/Latency/Writes";
    private static final String READ_LATENCY_GLOBAL = "Cassandra/global/Latency/Reads";
    private static final String WRITE_LATENCY_GLOBAL = "Cassandra/global/Latency/Writes";
    private static final String READ_TIMEOUTS = "Cassandra/hosts/%s/Timeouts/Reads";
    private static final String WRITE_TIMEOUTS = "Cassandra/hosts/%s/Timeouts/Writes";
    private static final String READ_LATENCY_TOTAL_INSTANCE = "Cassandra/hosts/%s/LatencyTotal/Reads";
    private static final String WRITE_LATENCY_TOTAL_INSTANCE = "Cassandra/hosts/%s/LatencyTotal/Writes";
    private static final String READ_UNAVAILABLE_REQUESTS_INSTANCE = "Cassandra/hosts/%s/Unavailables/Reads";
    private static final String WRITE_UNAVAILABLE_REQUESTS_INSTANCE = "Cassandra/hosts/%s/Unavailables/Writes";

    private static final String COMPACTION_PENDING_TASKS = "Cassandra/hosts/%s/Compaction/PendingTasks";
    private static final String MEMTABLE_PENDING_TASKS = "Cassandra/hosts/%s/MemtableFlush/PendingTasks";

    private static final String STORAGE_LOAD_INSTANCE = "Cassandra/hosts/%s/Storage/Data";
    private static final String STORAGE_LOAD_GLOBAL = "Cassandra/global/Storage/Data";
    private static final String COMMIT_LOG_INSTANCE = "Cassandra/hosts/%s/Storage/CommitLog";
    private static final String COMMIT_LOG_GLOBAL = "Cassandra/global/Storage/CommitLog";

    private static final String KEY_CACHE_HIT_RATE_INSTANCE = "Cassandra/hosts/%s/Cache/KeyCache/HitRate";
    private static final String KEY_CACHE_HIT_RATE_GLOBAL = "Cassandra/global/Cache/KeyCache/HitRate";
    private static final String KEY_CACHE_SIZE_INSTANCE = "Cassandra/hosts/%s/Cache/KeyCache/Size";
    private static final String KEY_CACHE_SIZE_GLOBAL = "Cassandra/global/Cache/KeyCache/Size";
    private static final String KEY_CACHE_ENTRIES_INSTANCE = "Cassandra/hosts/%s/Cache/KeyCache/Entries";
    private static final String KEY_CACHE_ENTRIES_GLOBAL = "Cassandra/global/Cache/KeyCache/Entries";
    private static final String KEY_CACHE_REQUESTS_INSTANCE = "Cassandra/hosts/%s/Cache/KeyCache/Requests";
    private static final String ROW_CACHE_HIT_RATE_INSTANCE = "Cassandra/hosts/%s/Cache/RowCache/HitRate";
    private static final String ROW_CACHE_HIT_RATE_GLOBAL = "Cassandra/global/Cache/RowCache/HitRate";
    private static final String ROW_CACHE_SIZE_INSTANCE = "Cassandra/hosts/%s/Cache/RowCache/Size";
    private static final String ROW_CACHE_SIZE_GLOBAL = "Cassandra/global/Cache/RowCache/Size";
    private static final String ROW_CACHE_ENTRIES_INSTANCE = "Cassandra/hosts/%s/Cache/RowCache/Entries";
    private static final String ROW_CACHE_ENTRIES_GLOBAL = "Cassandra/global/Cache/RowCache/Entries";
    private static final String ROW_CACHE_REQUESTS_INSTANCE = "Cassandra/hosts/%s/Cache/RowCache/Requests";

    private static final String MILLIS = "millis";
    private static final String RATE = "rate";
    private static final String BYTES = "bytes";
    private static final String COUNT = "count";

    private final String instance;

    public Cassandra2xMetrics(String instance) {
        this.instance = instance;
    }

    @Override
    public List<Metric> execute(MBeanServerConnection connection, JMXRunner jmxRunner) throws Exception {
        List<Metric> metrics = new ArrayList<>();

        try {
            metrics.addAll(getLatencyMetrics(connection, jmxRunner));
            metrics.addAll(getSystemMetrics(connection, jmxRunner));
            metrics.addAll(getStorageMetrics(connection, jmxRunner));
            metrics.addAll(getCacheMetrics(connection, jmxRunner));
        } catch (Exception e) {
            logger.error("Error polling for metrics:", e);
        }

        return metrics;
    }

    private List<Metric> getCacheMetrics(MBeanServerConnection connection, JMXRunner jmxRunner) throws Exception {
        List<Metric> metrics = new ArrayList<>();

        Double keyCacheHitRate = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "HitRate", "Cache", "KeyCache", "Value");
        metrics.add(new Metric(String.format(KEY_CACHE_HIT_RATE_INSTANCE, instance), RATE, keyCacheHitRate));
        metrics.add(new Metric(KEY_CACHE_HIT_RATE_GLOBAL, RATE, keyCacheHitRate));

        Long keyCacheSize = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "Size", "Cache", "KeyCache", "Value");
        metrics.add(new Metric(String.format(KEY_CACHE_SIZE_INSTANCE, instance), BYTES, keyCacheSize));
        metrics.add(new Metric(KEY_CACHE_SIZE_GLOBAL, BYTES, keyCacheSize));

        Integer keyCacheEntries = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "Entries", "Cache", "KeyCache", "Value");
        metrics.add(new Metric(String.format(KEY_CACHE_ENTRIES_INSTANCE, instance), COUNT, keyCacheEntries));
        metrics.add(new Metric(KEY_CACHE_ENTRIES_GLOBAL, COUNT, keyCacheEntries));

        Double keyCacheRequestsRate = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "Requests", "Cache", "KeyCache", "OneMinuteRate");
        metrics.add(new Metric(String.format(KEY_CACHE_REQUESTS_INSTANCE, instance), RATE, keyCacheRequestsRate));

        Double rowCacheHitRate = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "HitRate", "Cache", "RowCache", "Value");
        metrics.add(new Metric(String.format(ROW_CACHE_HIT_RATE_INSTANCE, instance), RATE, rowCacheHitRate));
        metrics.add(new Metric(ROW_CACHE_HIT_RATE_GLOBAL, RATE, rowCacheHitRate));

        Long rowCacheSize = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "Size", "Cache", "RowCache", "Value");
        metrics.add(new Metric(String.format(ROW_CACHE_SIZE_INSTANCE, instance), BYTES, rowCacheSize));
        metrics.add(new Metric(ROW_CACHE_SIZE_GLOBAL, BYTES, rowCacheSize));

        Integer rowCacheEntries = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "Entries", "Cache", "RowCache", "Value");
        metrics.add(new Metric(String.format(ROW_CACHE_ENTRIES_INSTANCE, instance), COUNT, rowCacheEntries));
        metrics.add(new Metric(ROW_CACHE_ENTRIES_GLOBAL, COUNT, rowCacheEntries));

        Double rowCacheRequestsRate = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "Requests", "Cache", "RowCache", "OneMinuteRate");
        metrics.add(new Metric(String.format(ROW_CACHE_REQUESTS_INSTANCE, instance), RATE, rowCacheRequestsRate));

        return metrics;
    }

    private List<Metric> getStorageMetrics(MBeanServerConnection connection, JMXRunner jmxRunner) throws Exception {
        List<Metric> metrics = new ArrayList<>();

        String loadString = jmxRunner.getAttribute(connection, "org.apache.cassandra.db", null, "StorageService", null, "LoadString");
        String[] parts = loadString.split(" ");
        Double load = toBytes(Double.parseDouble(parts[0]), parts[1]);
        metrics.add(new Metric(String.format(STORAGE_LOAD_INSTANCE, instance), BYTES, load));
        metrics.add(new Metric(STORAGE_LOAD_GLOBAL, BYTES, load));

        Long commitLogSize = jmxRunner.getAttribute(connection, "org.apache.cassandra.db", null, "Commitlog", null, "ActiveOnDiskSize");
        metrics.add(new Metric(String.format(COMMIT_LOG_INSTANCE, instance), BYTES, commitLogSize));
        metrics.add(new Metric(COMMIT_LOG_GLOBAL, BYTES, commitLogSize));

        return metrics;
    }

    private List<Metric> getSystemMetrics(MBeanServerConnection connection, JMXRunner jmxRunner) throws Exception {
        List<Metric> metrics = new ArrayList<>();

        Integer compactionPendingTasks = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "PendingTasks", "Compaction", null, "Value");
        Long memtableFlushPendingTasks = jmxRunner.getAttribute(connection, "org.apache.cassandra.internal", null, "MemtablePostFlusher", null, "PendingTasks");
        metrics.add(new Metric(String.format(COMPACTION_PENDING_TASKS, instance), COUNT, compactionPendingTasks));
        metrics.add(new Metric(String.format(MEMTABLE_PENDING_TASKS, instance), COUNT, memtableFlushPendingTasks));

        return metrics;
    }

    private List<Metric> getLatencyMetrics(MBeanServerConnection connection, JMXRunner jmxRunner) throws Exception {
        List<Metric> metrics = new ArrayList<>();

        Double readMean = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "Latency", "ClientRequest", "Read", "MeanRate");
        String sreadMeanUnits = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "Latency", "ClientRequest", "Read", "DurationUnit");
        TimeUnit readMeanUnits = TimeUnit.valueOf(sreadMeanUnits.toUpperCase());
        metrics.add(new Metric(String.format(READ_LATENCY_INSTANCE, instance), MILLIS, toMillis(readMean, readMeanUnits)));
        metrics.add(new Metric(READ_LATENCY_GLOBAL, MILLIS, toMillis(readMean, readMeanUnits)));

        Double writeMean = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "Latency", "ClientRequest", "Write", "MeanRate");
        String swriteMeanUnits = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "Latency", "ClientRequest", "Write", "DurationUnit");
        TimeUnit writeMeanUnits = TimeUnit.valueOf(sreadMeanUnits.toUpperCase());
        metrics.add(new Metric(String.format(WRITE_LATENCY_INSTANCE, instance), MILLIS, toMillis(writeMean, writeMeanUnits)));
        metrics.add(new Metric(WRITE_LATENCY_GLOBAL, MILLIS, toMillis(writeMean, writeMeanUnits)));

        Long readTimeouts = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "Timeouts", "ClientRequest", "Read", "Count");
        metrics.add(new Metric(String.format(READ_TIMEOUTS, instance), COUNT, readTimeouts));

        Long writeTimeouts = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "Timeouts", "ClientRequest", "Write", "Count");
        metrics.add(new Metric(String.format(WRITE_TIMEOUTS, instance), COUNT, writeTimeouts));

        Long totalReadLatency = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "TotalLatency", "ClientRequest", "Read", "Count");
        metrics.add(new Metric(String.format(READ_LATENCY_TOTAL_INSTANCE, instance), MILLIS, totalReadLatency * 0.001));

        Long totalWriteLatency = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "TotalLatency", "ClientRequest", "Write", "Count");
        metrics.add(new Metric(String.format(WRITE_LATENCY_TOTAL_INSTANCE, instance), MILLIS, totalWriteLatency * 0.001));

        Double readUnavailableRequests = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "Unavailables", "ClientRequest", "Read", "MeanRate");
        TimeUnit readUnavailableRequestsUnits = TimeUnit.valueOf("SECONDS");
        metrics.add(new Metric(String.format(READ_UNAVAILABLE_REQUESTS_INSTANCE, instance), MILLIS, toMillis(readUnavailableRequests, readUnavailableRequestsUnits)));

        Double writeUnavailableRequests = jmxRunner.getAttribute(connection, "org.apache.cassandra.metrics", "Unavailables", "ClientRequest", "Write", "MeanRate");
        TimeUnit writeUnavailableRequestsUnits = TimeUnit.valueOf("SECONDS");
        metrics.add(new Metric(String.format(WRITE_UNAVAILABLE_REQUESTS_INSTANCE, instance), MILLIS, toMillis(writeUnavailableRequests, writeUnavailableRequestsUnits)));


        // for(Metric metric : metrics) {
        //     logger.error(String.format( metric.getName(), "(",metric.getValueType(),") = ", metric.getValue() ));
        // }

        return metrics;
    }

    private Double toMillis(Double sourceValue, TimeUnit sourceUnit) {

        if(sourceUnit == null || sourceValue == null){
            return null;
        }

        switch (sourceUnit) {
            case DAYS:
                return sourceValue * 86400000;
            case MICROSECONDS:
                return sourceValue * 0.001;
            case HOURS:
                return sourceValue * 3600000;
            case MILLISECONDS:
                return sourceValue;
            case MINUTES:
                return sourceValue * 60000;
            case NANOSECONDS:
                return sourceValue * 1.0e-6;
            case SECONDS:
                return sourceValue * 1000;
            default:
                return sourceValue;
        }
    }

    private Double toBytes(Double sourceValue, String sourceUnit) {

        if(sourceUnit == null || sourceValue == null){
            return null;
        }

        Double mult;
        switch (sourceUnit) {
         case "MB": return (Math.pow(1000, 2) * sourceValue);
         case "GB": return (Math.pow(1000, 3) * sourceValue);
         case "TB": return (Math.pow(1000, 4) * sourceValue);
         default:   return sourceValue;
        }
    }
}
