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
package org.iogp.gigs.runner;

import java.net.URI;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.AbstractMap;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import org.opengis.util.Factory;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.citation.Citation;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.iogp.gigs.internal.TestSuite;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;


/**
 * The result of the execution of a single test. This object contains the test method name,
 * some information about the configuration and the stack trace if an error occurred.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
final class ResultEntry {
    /**
     * The status (success, failure, disabled) of an optional test.
     */
    static enum StatusOptional {
        ENABLED, DISABLED, FAILED
    }

    /**
     * The base URL of {@code geoapi-conformance} javadoc. The trailing slash is mandatory.
     */
    private static final String JAVADOC_BASEURL = "http://www.geoapi.org/conformance/java/";

    /**
     * Typical suffix of test class name. This suffix is not mandatory. But if the suffix
     * is found, it will be omitted from the {@linkplain #simpleClassName simple class name}
     * since it does not provide useful information.
     */
    private static final String CLASSNAME_SUFFIX = "Test";

    /**
     * Typical prefix of test method name. This prefix is not mandatory. But if the prefix
     * is found, it will be omitted from the {@linkplain #simpleMethodName simple method name}
     * since it does not provide useful information.
     */
    private static final String METHODNAME_PREFIX = "test";

    /**
     * Identification of the test.
     */
    final TestIdentifier identifier;

    /**
     * The simplified class name and method name of the test method being run.
     */
    final String simpleClassName, simpleMethodName;

    /**
     * The factories declared in the configuration. Each row in this list is an array of length 4.
     * The array elements are:
     *
     * <ol>
     *   <li>The factory category (i.e. GeoAPI interface)</li>
     *   <li>The implementation simple class name</li>
     *   <li>The vendor name (may be null)</li>
     *   <li>The authority name (may be null)</li>
     * </ol>
     *
     * @see SwingFactoryTableModel
     */
    final List<String[]> factories;

    /**
     * The configuration specified by the implementer.
     */
    final List<Map.Entry<Configuration.Key<?>, StatusOptional>> configuration;

    /**
     * The test status, optionally with the exception.
     */
    final TestExecutionResult result;

    /**
     * An estimation of the test coverage, as a floating point value between 0 and 1.
     */
    private final float coverage;

    /**
     * {@code true} if the tolerance threshold has been relaxed.
     */
    private boolean isToleranceRelaxed;

    /**
     * Creates a new entry for the given result.
     */
    ResultEntry(final TestIdentifier identifier, final TestExecutionResult result) {
        this.identifier = identifier;
        this.result     = result;
        result.getThrowable().ifPresent(ResultEntry::trimStackTrace);
        final TestSource source = identifier.getSource().orElse(null);
        if (source instanceof MethodSource) {
            final MethodSource ms = (MethodSource) source;
            String name = ms.getClassName();
            int length = name.length();
            if (name.endsWith(CLASSNAME_SUFFIX)) {
                length -= CLASSNAME_SUFFIX.length();
            }
            simpleClassName = separateWords(name.substring(name.lastIndexOf('.', length)+1, length), false);
            name = ((MethodSource) source).getMethodName();
            if (name.startsWith(METHODNAME_PREFIX)) {
                name = name.substring(METHODNAME_PREFIX.length());
            }
            simpleMethodName = separateWords(name.replace('_', ':'), false);
        } else {
            simpleClassName = simpleMethodName = "(unnamed)";
        }
        /*
         * Extract information from the configuration:
         *  - Computes an estimation of test coverage as a number between 0 and 1.
         *  - Get the list of factories.
         */
        int numTests=1, numSupported=1;
        final Configuration.Key<Boolean> configurationTip = TestSuite.INSTANCE.configurationTip;
        final List<String[]> factories = new ArrayList<>();
        final List<Map.Entry<Configuration.Key<?>, StatusOptional>> configuration = new ArrayList<>();
        for (Map.Entry<Configuration.Key<?>,Object> entry : TestSuite.INSTANCE.configuration().map().entrySet()) {
            final Configuration.Key<?> key = entry.getKey();
            final String   name  = key.name();
            final Class<?> type  = key.valueType();
            final Object   value = entry.getValue();
            /*
             * Note: we assume that a test with every optional features marked as "unsupported"
             * (`isFooSupported = false`) still do some test, so we unconditionally start the
             * count with 1 supported test.
             */
            if ((type == Boolean.class) && name.startsWith("is")) {
                if (name.endsWith("Supported")) {
                    final StatusOptional so;
                    if (Boolean.FALSE.equals(value)) {
                        so = StatusOptional.DISABLED;
                    } else {
                        numSupported++;
                        so = (key == configurationTip) ? StatusOptional.FAILED : StatusOptional.ENABLED;
                    }
                    configuration.add(new AbstractMap.SimpleImmutableEntry<>(key, so));
                    numTests++;
                } else if (name.equals("isToleranceRelaxed")) {
                    isToleranceRelaxed = (Boolean) value;
                }
            }
            /*
             * Check for factories. See the javadoc of the `factories` field
             * for the meaning of array elements.
             */
            if (Factory.class.isAssignableFrom(type)) {
                String impl = null;
                if (value != null) {
                    Class<?> implType = value.getClass();
                    impl = implType.getSimpleName();
                    while ((implType = implType.getEnclosingClass()) != null) {
                        impl = implType.getSimpleName() + '.' + impl;
                    }
                }
                factories.add(new String[] {
                    separateWords(type.getSimpleName(), false), impl,
                    (value instanceof Factory) ?
                        getIdentifier(((Factory) value).getVendor()) : null,
                    (value instanceof AuthorityFactory) ?
                        getIdentifier(((AuthorityFactory) value).getAuthority()) : null
                });
            }
        }
        coverage = numSupported / ((float) numTests);
        this.factories = Collections.unmodifiableList(factories);
        this.configuration = Collections.unmodifiableList(configuration);
    }

    /**
     * Puts space between words in the given string.
     * The first letter is never modified.
     */
    static String separateWords(final String name, final boolean toLowerCase) {
        StringBuilder buffer = null;
        for (int i = name.length(); i >= 2;) {
            final int c = name.codePointBefore(i);
            final int nc = Character.charCount(c);
            i -= nc;
            if (Character.isUpperCase(c) || Character.isDigit(c)) {
                /*
                 * If we have a lower case letter followed by an upper case letter, unconditionally
                 * insert a space between them. If we have 2 consecutive upper case letters (actually
                 * anything except a space and a lower case letter, followed by an upper case letter),
                 * insert a space only if the next character is lower case. The later rule is an
                 * attempt to handle abbreviations, like "URLEncoding" to "URL Encoding".
                 */
                final int cb = name.codePointBefore(i);
                if (Character.isSpaceChar(cb)) {
                    continue;
                }
                if (!Character.isLowerCase(cb)) {
                    final int next = i + nc;
                    if (next >= name.length() || !Character.isLowerCase(name.codePointAt(next))) {
                        continue;
                    }
                }
                if (buffer == null) {
                    buffer = new StringBuilder(name);
                }
                if (toLowerCase && nc == 1) {
                    final int lowerCase = Character.toLowerCase(c);
                    if (Character.charCount(lowerCase) == 1) { // Paranoiac check.
                        buffer.setCharAt(i, (char) lowerCase);
                    }
                }
                buffer.insert(i, ' ');
            }
        }
        return (buffer != null) ? buffer.toString() : name;
    }

    /**
     * Returns the first identifier of the given citation. If no identifier is found, returns
     * the title or {@code null} if none. We search for identifier first because they are
     * typically more compact than the title.
     */
    private static String getIdentifier(final Citation citation) {
        if (citation != null) {
            final Collection<? extends Identifier> identifiers = citation.getIdentifiers();
            if (identifiers != null) {
                for (final Identifier id : identifiers) {
                    if (id != null) {
                        final String code = id.getCode();
                        if (code != null) {
                            return code;
                        }
                    }
                }
            }
            final CharSequence title = citation.getTitle();
            if (title != null) {
                return title.toString();
            }
        }
        return null;
    }

    /**
     * Trims the stack trace of the given exception and all its cause, removing everything
     * after the last {@code org.iogp.gigs} package which is not this runner package.
     */
    private static void trimStackTrace(Throwable exception) {
        final StackTraceElement[] stackTrace = exception.getStackTrace();
        for (int i=stackTrace.length; --i>=0;) {
            final String className = stackTrace[i].getClassName();
            if (className.startsWith("org.iogp.gigs.") &&
               !className.startsWith("org.iogp.gigs.runner."))
            {
                exception.setStackTrace(Arrays.copyOf(stackTrace, i+1));
                break;
            }
        }
        exception = exception.getCause();
    }

    /**
     * Returns the URL to the javadoc of the test method. Users can follow this URL
     * in order to have more details about the test data or procedure.
     *
     * @return the URI to the javadoc of the test method, or {@code null} if none.
     */
    public URI getJavadocURL() {
        final TestSource source = identifier.getSource().orElse(null);
        if (source instanceof MethodSource) {
            final MethodSource ms = (MethodSource) source;
            String method = ms.getMethodName();
            final int s = method.indexOf('[');
            if (s >= 0) {
                method = method.substring(0, s);
            }
            return URI.create(JAVADOC_BASEURL + ms.getClassName().replace('.', '/') + ".html#" + method + "()");
        }
        return null;
    }

    /**
     * Draws a shape representing the test coverage using the given graphics handler.
     * This method changes the graphics paint, so caller should restore it to whatever
     * paint they want to use after this method call.
     *
     * @param graphics  the graphics where to draw.
     * @param bounds    the region where to draw. <strong>Will be modified by this method</strong>.
     */
    void drawCoverage(final Graphics2D graphics, final Rectangle bounds) {
        final Color color;
        switch (result.getStatus()) {
            case SUCCESSFUL: {
                color = isToleranceRelaxed ? Color.ORANGE : Color.GREEN;
                break;
            }
            case FAILED: {
                color = Color.RED;
                break;
            }
            case ABORTED: {
                color = Color.GRAY;
                break;
            }
            default: {
                return;                         // Do not paint anything.
            }
        }
        graphics.setColor(color.darker());
        graphics.draw(bounds);
        bounds.width = Math.round(bounds.width * coverage);
        graphics.setColor(color);
        graphics.fill(bounds);
    }

    /**
     * Returns a string representation of this entry.
     */
    @Override
    public String toString() {
        return simpleClassName + '.' + simpleMethodName + ": " + result.getStatus();
    }
}
