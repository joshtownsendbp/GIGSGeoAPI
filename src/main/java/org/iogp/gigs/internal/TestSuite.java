/*
 *    GeoAPI - Java interfaces for OGC/ISO standards
 *    http://www.geoapi.org
 *
 *    Copyright (C) 2011-2021 Open Geospatial Consortium, Inc.
 *    All Rights Reserved. http://www.opengeospatial.org/ogc/legal
 *
 *    Permission to use, copy, and modify this software and its documentation, with
 *    or without modification, for any purpose and without fee or royalty is hereby
 *    granted, provided that you include the following on ALL copies of the software
 *    and documentation or portions thereof, including modifications, that you make:
 *
 *    1. The full text of this NOTICE in a location viewable to users of the
 *       redistributed or derivative work.
 *    2. Notice of any changes or modifications to the OGC files, including the
 *       date changes were made.
 *
 *    THIS SOFTWARE AND DOCUMENTATION IS PROVIDED "AS IS," AND COPYRIGHT HOLDERS MAKE
 *    NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 *    TO, WARRANTIES OF MERCHANTABILITY OR FITNESS FOR ANY PARTICULAR PURPOSE OR THAT
 *    THE USE OF THE SOFTWARE OR DOCUMENTATION WILL NOT INFRINGE ANY THIRD PARTY
 *    PATENTS, COPYRIGHTS, TRADEMARKS OR OTHER RIGHTS.
 *
 *    COPYRIGHT HOLDERS WILL NOT BE LIABLE FOR ANY DIRECT, INDIRECT, SPECIAL OR
 *    CONSEQUENTIAL DAMAGES ARISING OUT OF ANY USE OF THE SOFTWARE OR DOCUMENTATION.
 *
 *    The name and trademarks of copyright holders may NOT be used in advertising or
 *    publicity pertaining to the software without specific, written prior permission.
 *    Title to copyright in this software and any associated documentation will at all
 *    times remain with copyright holders.
 */
package org.iogp.gigs.internal;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.ServiceLoader;
import org.opengis.util.InternationalString;
import org.opengis.metadata.citation.Citation;
import org.opengis.util.Factory;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.cs.CSFactory;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperationFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.opengis.referencing.operation.MathTransformFactory;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.iogp.gigs.internal.geoapi.Units;
import org.iogp.gigs.*;


/**
 * Collection of all GIGS tests.
 * This {@code TestSuite} class provides some static fields for specifying explicitly which factories to use.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
public final class TestSuite implements ParameterResolver {
    /**
     * The type of factories to inject, in priority order. The order matter only if
     * an argument type is assignable from more than one of the types listed here.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})    // Generic array creation.
    private static final Class<? extends Factory>[] FACTORY_TYPES = new Class[] {
        CRSAuthorityFactory.class,
        CRSFactory.class,
        CSAuthorityFactory.class,
        CSFactory.class,
        DatumAuthorityFactory.class,
        DatumFactory.class,
        CoordinateOperationAuthorityFactory.class,
        CoordinateOperationFactory.class,
        MathTransformFactory.class
    };

    /**
     * All factories found. May contain null elements.
     */
    private final Factory[] factories;

    /**
     * The test under execution, or {@code null} if none.
     * This is set by {@link IntegrityTest#reference()} after test execution.
     */
    public volatile IntegrityTest executing;

    /**
     * If a test failure occurred in an optional test, the configuration key for disabling that test.
     * Otherwise {@code null}. This is set by {@link IntegrityTest#reference()} after test execution.
     */
    public volatile Configuration.Key<Boolean> configurationTip;

    /**
     * Creates a new suite.
     */
    private TestSuite() {
        factories = new Factory[FACTORY_TYPES.length];
    }

    /**
     * The singleton instance of this test suite. Ideally we should not have this
     * static field, but I did not yet found another way to get this reference.
     */
    public static final TestSuite INSTANCE = new TestSuite();

    /**
     * Specifies the JAR files containing the implementation to test, then runs tests.
     *
     * @param  listener  the listener which will collect test results.
     * @param  jarFiles  JAR files of the implementation to test.
     * @throws MalformedURLException if a file can not be converted to a URL.
     */
    public void run(final TestExecutionListener listener, final File... jarFiles) throws MalformedURLException {
        /*
         * Prepare the tests plan. We use the GIGS class loader here,
         * not yet the class loader for the factories to be tested.
         */
        final Class<?>[] tests = {
            Test2001.class, Test2002.class, Test2003.class, Test2004.class, Test2005.class,
            Test2006.class, Test2007.class, Test2008.class, Test2009.class,
            Test3002.class, Test3003.class, Test3004.class, Test3005.class
        };
        final ClassSelector[] selectors = new ClassSelector[tests.length];
        for (int i=0; i<selectors.length; i++) {
            selectors[i] = DiscoverySelectors.selectClass(tests[i]);
        }
        final LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request().selectors(selectors).build();
        final Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        /*
         * Prepare a class loader for the JAR files specified by the caller.
         * This class loader is used for finding the factories.
         */
        final URL[] urls = new URL[jarFiles.length];
        for (int i=0; i < urls.length; i++) {
            urls[i] = jarFiles[i].toURI().toURL();
        }
        final ClassLoader loader = new URLClassLoader(urls, TestSuite.class.getClassLoader());
        try {
            /*
             * Find factories. If an authority factory is for a name space other than EPSG,
             * skip that factory.
             */
            for (int i=0; i < FACTORY_TYPES.length; i++) {
                for (final Factory factory : ServiceLoader.load(FACTORY_TYPES[i], loader)) {
                    if (factory instanceof AuthorityFactory) {
                        if (isNotEPSG(((AuthorityFactory) factory).getAuthority())) {
                            continue;
                        }
                    }
                    factories[i] = factory;
                    break;
                }
            }
            Units.setInstance(loader);
            launcher.execute(request);
        } finally {
            Units.setInstance(null);
            Arrays.fill(factories, null);
        }
    }

    /**
     * Tests whether the given citation is for an authority other than EPSG.
     * If not specified, conservatively assumes {@code false}.
     */
    private static boolean isNotEPSG(final Citation citation) {
        if (citation == null || isEPSG(citation.getTitle())) {
            return false;
        }
        boolean hasOtherTitle = false;
        for (final InternationalString title : citation.getAlternateTitles()) {
            if (isEPSG(title)) return false;
            hasOtherTitle = true;
        }
        return hasOtherTitle;
    }

    /**
     * Returns {@code true} if the given title is "EPSG".
     */
    private static boolean isEPSG(final InternationalString title) {
        return (title != null) && title.toString().contains("EPSG");
    }

    /**
     * Determine if this resolver supports resolution of an argument.
     * This is used for dependency injection.
     *
     * @param  pc  the context for the parameter for which an argument should be resolved.
     * @param  ec  the extension context (ignored).
     * @return whether this resolver can resolve the parameter.
     */
    @Override
    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
        return Factory.class.isAssignableFrom(pc.getParameter().getType());
    }

    /**
     * Resolves an argument.
     * This is used for dependency injection.
     *
     * @param  pc  the context for the parameter for which an argument should be resolved.
     * @param  ec  the extension context (ignored).
     * @return the argument value (may be null).
     */
    @Override
    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
        final Class<?> type = pc.getParameter().getType();
        for (int i=0; i < FACTORY_TYPES.length; i++) {
            if (type.isAssignableFrom(FACTORY_TYPES[i])) {
                return factories[i];
            }
        }
        return null;
    }

    /**
     * Returns the configuration associated to the test under execution.
     * We use reflection for avoiding to put the configuration in public API (for now).
     *
     * @return configuration of last executed test.
     */
    public Configuration configuration() {
        try {
            Method m = IntegrityTest.class.getSuperclass().getDeclaredMethod("configuration");
            m.setAccessible(true);
            return (Configuration) m.invoke(executing);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);                    // Should never happen.
        }
    }
}
