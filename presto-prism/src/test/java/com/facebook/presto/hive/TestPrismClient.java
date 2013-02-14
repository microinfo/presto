package com.facebook.presto.hive;

import com.facebook.presto.spi.ImportClient;
import com.facebook.swift.service.guice.ThriftClientModule;
import com.facebook.swift.smc.SmcClientModule;
import com.google.inject.Injector;
import io.airlift.bootstrap.Bootstrap;
import io.airlift.bootstrap.LifeCycleManager;
import io.airlift.discovery.client.testing.TestingDiscoveryModule;
import io.airlift.json.JsonModule;
import io.airlift.node.testing.TestingNodeModule;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "hive")
public class TestPrismClient
        extends AbstractTestHiveClient
{
    private LifeCycleManager lifeCycleManager;

    @BeforeMethod
    public void setup()
            throws Exception
    {
        Bootstrap app = new Bootstrap(
                new TestingNodeModule(),
                new TestingDiscoveryModule(),
                new JsonModule(),
                new HiveClientModule(),
                new ThriftClientModule(),
                new SmcClientModule(),
                new PrismClientModule());

        Injector injector = app
                .strictConfig()
                .setRequiredConfigurationProperty("prism.allowed-regions", "west")
                .doNotInitializeLogging()
                .initialize();

        lifeCycleManager = injector.getInstance(LifeCycleManager.class);

        PrismImportClientFactory factory = injector.getInstance(PrismImportClientFactory.class);

        this.client = factory.createClient("prism");
    }

    @AfterMethod
    public void tearDown()
            throws Exception
    {
        if (lifeCycleManager != null) {
            lifeCycleManager.stop();
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnsupportedRegion()
            throws Exception
    {
        Bootstrap app = new Bootstrap(
                new TestingNodeModule(),
                new TestingDiscoveryModule(),
                new JsonModule(),
                new HiveClientModule(),
                new ThriftClientModule(),
                new SmcClientModule(),
                new PrismClientModule());

        Injector injector = app
                .strictConfig()
                .setRequiredConfigurationProperty("prism.allowed-regions", "fuu, bar")
                .doNotInitializeLogging()
                .initialize();

        LifeCycleManager localLifeCycleManager = injector.getInstance(LifeCycleManager.class);
        try {
            PrismImportClientFactory factory = injector.getInstance(PrismImportClientFactory.class);
            ImportClient prism = factory.createClient("prism");
            prism.getTableNames(AbstractTestHiveClient.DATABASE);
        }
        finally {
            localLifeCycleManager.stop();
        }
    }
}