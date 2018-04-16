package com.mukundsankaran.bookit.config;

import com.mukundsankaran.bookit.model.*;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by mukund on 4/12/18.
 *
 * BookIt In-Memory Data Grid Configuration
 *
 */

@Configuration
public class DataGridConfiguration {

    private final Logger logger = LoggerFactory.getLogger(DataGridConfiguration.class);

    @Bean
    IgniteConfiguration igniteConfiguration() {

        if(logger.isDebugEnabled()){
            logger.debug("Configuring Ignite.");
        }

        IgniteConfiguration igniteConfiguration = new IgniteConfiguration();
        igniteConfiguration.setClientMode(false);

        // Define Logger

        IgniteLogger log = new Slf4jLogger(logger);
        igniteConfiguration.setGridLogger(log);

        // Cache Configuration

        // This cache holds all events at the venue
        CacheConfiguration<Long, Event> eventCacheConfig = new CacheConfiguration<>();
        eventCacheConfig.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        eventCacheConfig.setName(CacheName.EVENTS.name());

        if(logger.isDebugEnabled()){
            logger.debug("Configuring Ignite Cache '{}'.", CacheName.EVENTS.name());
        }

        // This cache holds all rows for an event
        CacheConfiguration<Integer, Row> rowCacheConfiguration = new CacheConfiguration<>();
        rowCacheConfiguration.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        rowCacheConfiguration.setName(CacheName.ROWS.name());

        if(logger.isDebugEnabled()){
            logger.debug("Configuring Ignite Cache '{}'.", CacheName.ROWS.name());
        }

        // This cache holds all ticket holds for an event
        CacheConfiguration<Integer, SeatHold> holdCacheConfiguration = new CacheConfiguration<>();
        holdCacheConfiguration.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        holdCacheConfiguration.setName(CacheName.HOLDS.name());

        if(logger.isDebugEnabled()){
            logger.debug("Configuring Ignite Cache '{}'.", CacheName.HOLDS.name());
        }

        // This cache holds all ticket reservations for an event
        CacheConfiguration<String, Reservation> reservationCacheConfig = new CacheConfiguration<>();
        reservationCacheConfig.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        reservationCacheConfig.setName(CacheName.RESERVATIONS.name());

        if(logger.isDebugEnabled()){
            logger.debug("Configuring Ignite Cache '{}'.", CacheName.RESERVATIONS.name());
        }

        igniteConfiguration.setCacheConfiguration(eventCacheConfig, rowCacheConfiguration, holdCacheConfiguration, reservationCacheConfig);

        if(logger.isDebugEnabled()){
            logger.debug("Ignite Configuration Complete.");
        }

        return igniteConfiguration;
    }

    @Bean(destroyMethod = "close")
    Ignite ignite(IgniteConfiguration igniteConfiguration) throws IgniteException {

        final Ignite ignite = Ignition.getOrStart(igniteConfiguration);

        if(logger.isDebugEnabled()) {
            logger.debug("Ignite Started!");
        }

        return ignite;
    }

}



