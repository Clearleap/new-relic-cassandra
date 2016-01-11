package com.tylerhoersch.nr.cassandra;

import org.junit.Before;
import org.junit.Test;

import javax.management.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class JMXRunnerTest {
    private MBeanServerConnection mBeanServerConnection = mock(MBeanServerConnection.class);
    private JMXRunner jmxRunner;

    @Before
    public void setup() {
        jmxRunner = new JMXRunner(new ArrayList<String>() {{ add("127.0.0.1");}}, "7199", "username", "password");
    }

    @Test
    public void verifyQueryWithNoMBeansReturnsNull() throws Exception {
        when(mBeanServerConnection.queryMBeans(any(ObjectName.class), any(QueryExp.class))).thenReturn(new HashSet<ObjectInstance>());
        JMXRequest request = new JMXRequest.JMXRequestBuilder("domain").name("name").type("type").scope("scope").attribute("attribute").build();
        Object attribute = jmxRunner.getAttribute(mBeanServerConnection, request);

        assertNull(attribute);
    }

    @Test
    public void verifyObjectNameCreation() throws Exception {
        JMXRequest request = new JMXRequest.JMXRequestBuilder("domain").name("name").type("type").scope("scope").attribute("attribute").build();
        Object attribute = jmxRunner.getAttribute(mBeanServerConnection, request);
        when(mBeanServerConnection.queryMBeans(any(ObjectName.class), any(QueryExp.class))).then(invocation -> {
            ObjectName objectName = (ObjectName) invocation.getArguments()[0];
            assertEquals("name", objectName.getKeyProperty("name"));
            assertEquals("type", objectName.getKeyProperty("type"));
            assertEquals("scope", objectName.getKeyProperty("scope"));
            return null;
        });
    }

    @Test
    public void verifyQueryWithBeansCallsForAttributes() throws Exception {
        final JMXRequest request = new JMXRequest.JMXRequestBuilder("domain").name("value").attribute("attribute").build();
        final ObjectName objectName = new ObjectName("domain", "name", "value");
        Set<ObjectInstance> instances = new HashSet<>();
        instances.add(new ObjectInstance(objectName, "Object"));
        when(mBeanServerConnection.queryMBeans(any(ObjectName.class), any(QueryExp.class))).thenReturn(instances);
        jmxRunner.getAttribute(mBeanServerConnection, request);

        verify(mBeanServerConnection, times(1)).queryMBeans(objectName, null);
        verify(mBeanServerConnection, times(1)).getAttribute(objectName, "attribute");
    }

    @Test
    public void verifyRunnerTriesOtherHostsOnFailure() throws Exception {
        jmxRunner = new JMXRunner(new ArrayList<String>() {{ add("1.2.3.4"); add("127.0.0.1"); add("127.0.0.1");}}, "7199", "username", "password");

        JMXTemplate template = mock(JMXTemplate.class);

        try {
            jmxRunner.run(template);
            fail("Did not fail on all hosts");
        } catch (Exception e) {
            assertEquals("All hosts (1.2.3.4,127.0.0.1,127.0.0.1) tried and failed to connect.", e.getMessage());
        }

        verify(template, times(0)).execute(any(MBeanServerConnection.class), any(JMXRunner.class));
    }
}
