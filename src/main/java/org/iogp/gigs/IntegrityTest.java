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
package org.iogp.gigs;

import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import org.opengis.util.Factory;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.PrimeMeridian;
import org.iogp.gigs.internal.TestSuite;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.iogp.gigs.internal.geoapi.Assert.assertUnicodeIdentifierEquals;


/**
 * Base class of all GIGS tests.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @version 1.0
 * @since   1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public abstract class IntegrityTest extends ConformanceTest {
    /**
     * Relative tolerance factor from GIGS documentation.
     * This tolerance threshold is typically multiplied
     * by the magnitude of the value being compared.
     */
    static final double TOLERANCE = 1E-10;

    /**
     * Absolute angular tolerance from GIGS documentation.
     * This tolerance threshold is <strong>not</strong>
     * multiplied by the value being compared.
     */
    static final double ANGULAR_TOLERANCE = 1E-7;

    /**
     * The extension which will perform dependency injection. When a constructor an argument of the {@code FooFactory},
     * the {@link TestSuite#resolveParameter resolveParameter(…)} method is invoked for providing the factory instance.
     */
    @RegisterExtension
    static final TestSuite INJECTION = TestSuite.INSTANCE;

    /**
     * Creates a new test.
     */
    IntegrityTest() {
    }

    /**
     * Keeps a reference to the instance of the test which has been executed.
     * It will be used for fetching configuration information if needed.
     */
    @AfterEach
    final void reference() {
        TestSuite.INSTANCE.executing = this;
        TestSuite.INSTANCE.configurationTip = configurationTip;
    }

    /**
     * Verifies that the given factory is not null.
     * If null, the test is ignored.
     *
     * @param  factory  a factory required by the tests to execute.
     */
    static void assumeNotNull(final Factory factory) {
        assumeTrue(factory != null);
    }

    /**
     * Invoked when the implementation does not support one of the codes defined in the GIGS test suite.
     * This method has a behavior equivalent to a call to {@code assumeTrue(false)}, which will cause
     * the test to terminate with the "ignored" status.
     *
     * @param  type  the GeoAPI interface of the tested object.
     * @param  code  the EPSG code or name of the tested object.
     */
    final void unsupportedCode(final Class<?> type, final Object code) {
        final StringBuilder buffer = new StringBuilder(50).append(type.getSimpleName()).append('[');
        final boolean quote = !(code instanceof Number);
        if (quote) buffer.append('"');
        buffer.append(code);
        if (quote) buffer.append('"');
        buffer.append("] not supported.");
        assumeTrue(false, buffer.toString());
    }

    /**
     * Returns the name of the given object, or {@code null} if none.
     *
     * @param  object  the object from which to get the name.
     * @return name of the given object, or {@code null} if none.
     */
    static String getName(final IdentifiedObject object) {
        if (object != null) {
            final Identifier name = object.getName();
            if (name != null) {
                return name.getCode();
            }
        }
        return null;
    }

    /*
     * ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――
     *   Methods below this point were copied from `org.opengis.test.referencing.ReferencingTestCase`
     *   and should be deleted after next GeoAPI release.
     * ――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――
     */

    /**
     * Compares the name, axis lengths and inverse flattening factor of the given ellipsoid against the expected values.
     * This method allows for some flexibilities:
     *
     * <ul>
     *   <li>{@link Ellipsoid#getName()} allows for the same flexibilities than the one documented in
     *       {@link #verifyIdentification verifyIdentification(…)}.</li>
     *   <li>{@link Ellipsoid#getSemiMajorAxis()} does not need to use the unit of measurement given
     *       by the {@code axisUnit} argument. Unit conversion will be applied as needed.</li>
     * </ul>
     *
     * The tolerance thresholds are 0.5 unit of the last digits of the values found in the EPSG database:
     * <ul>
     *   <li>3 decimal digits for {@code semiMajor} values in metres.</li>
     *   <li>9 decimal digits for {@code inverseFlattening} values.</li>
     * </ul>
     *
     * If the given {@code ellipsoid} is {@code null}, then this method does nothing.
     * Deciding if {@code null} datum are allowed or not is {@link org.iogp.gigs.internal.geoapi.Validator}'s job.
     *
     * @param ellipsoid          the ellipsoid to verify, or {@code null} if none.
     * @param name               the expected name (ignoring code space), or {@code null} if unrestricted.
     * @param semiMajor          the expected semi-major axis length, in units given by the {@code axisUnit} argument.
     * @param inverseFlattening  the expected inverse flattening factor.
     * @param axisUnit           the unit of the {@code semiMajor} argument (not necessarily the actual unit of the ellipsoid).
     */
    final void verifyFlattenedSphere(final Ellipsoid ellipsoid, final String name,
            final double semiMajor, final double inverseFlattening, final Unit<Length> axisUnit)
    {
        if (ellipsoid != null) {
            if (name != null) {
                assertUnicodeIdentifierEquals("Ellipsoid.getName().getCode()", name, getName(ellipsoid), true);
            }
            final Unit<Length> actualUnit = ellipsoid.getAxisUnit();
            assertNotNull(actualUnit, "Ellipsoid.getAxisUnit()");
            assertEquals(semiMajor, actualUnit.getConverterTo(axisUnit).convert(ellipsoid.getSemiMajorAxis()),
                         units.metre().getConverterTo(axisUnit).convert(5E-4), "Ellipsoid.getSemiMajorAxis()");
            assertEquals(inverseFlattening, ellipsoid.getInverseFlattening(), 5E-10, "Ellipsoid.getInverseFlattening()");
        }
    }

    /**
     * Compares the name and Greenwich longitude of the given prime meridian against the expected values.
     * This method allows for some flexibilities:
     *
     * <ul>
     *   <li>{@link PrimeMeridian#getName()} allows for the same flexibilities than the one documented in
     *       {@link #verifyIdentification verifyIdentification(…)}.</li>
     *   <li>{@link PrimeMeridian#getGreenwichLongitude()} does not need to use the unit of measurement given
     *       by the {@code angularUnit} argument. Unit conversion will be applied as needed.</li>
     * </ul>
     *
     * The tolerance threshold is 0.5 unit of the last digit of the values found in the EPSG database:
     * <ul>
     *   <li>7 decimal digits for {@code greenwichLongitude} values in degrees.</li>
     * </ul>
     *
     * If the given {@code primeMeridian} is {@code null}, then this method does nothing.
     * Deciding if {@code null} prime meridians are allowed or not is {@link org.iogp.gigs.internal.geoapi.Validator}'s job.
     *
     * @param primeMeridian       the prime meridian to verify, or {@code null} if none.
     * @param name                the expected name (ignoring code space), or {@code null} if unrestricted.
     * @param greenwichLongitude  the expected Greenwich longitude, in units given by the {@code angularUnit} argument.
     * @param angularUnit         the unit of the {@code greenwichLongitude} argument (not necessarily the actual unit of the prime meridian).
     */
    final void verifyPrimeMeridian(final PrimeMeridian primeMeridian, final String name,
            final double greenwichLongitude, final Unit<Angle> angularUnit)
    {
        if (primeMeridian != null) {
            if (name != null) {
                assertUnicodeIdentifierEquals("PrimeMeridian.getName().getCode()", name, getName(primeMeridian), true);
            }
            final Unit<Angle> actualUnit = primeMeridian.getAngularUnit();
            assertNotNull(actualUnit, "PrimeMeridian.getAngularUnit()");
            assertEquals(greenwichLongitude, actualUnit.getConverterTo(angularUnit).convert(primeMeridian.getGreenwichLongitude()),
                         units.degree().getConverterTo(angularUnit).convert(5E-8), "PrimeMeridian.getGreenwichLongitude()");
        }
    }

    /**
     * Compares the type, axis units and directions of the given coordinate system against the expected values.
     * This method does not verify the coordinate system name because it is usually not significant.
     * This method does not verify axis names neither because the names specified by ISO 19111 and ISO 19162 differ.
     *
     * <p>If the given {@code cs} is {@code null}, then this method does nothing.
     * Deciding if {@code null} coordinate systems are allowed or not is {@link org.iogp.gigs.internal.geoapi.Validator}'s job.</p>
     *
     * @param  cs          the coordinate system to verify, or {@code null} if none.
     * @param  type        the expected coordinate system type.
     * @param  directions  the expected axis directions. The length of this array determines the expected {@code cs} dimension.
     * @param  axisUnits   the expected axis units. If the array length is less than the {@code cs} dimension,
     *                     then the last unit is repeated for all remaining dimensions.
     *                     If the array length is greater, than extra units are ignored.
     */
    final void verifyCoordinateSystem(final CoordinateSystem cs, final Class<? extends CoordinateSystem> type,
            final AxisDirection[] directions, final Unit<?>... axisUnits)
    {
        if (cs != null) {
            assertEquals(directions.length, cs.getDimension(), "CoordinateSystem.getDimension()");
            for (int i=0; i<directions.length; i++) {
                final CoordinateSystemAxis axis = cs.getAxis(i);
                assertNotNull(axis, "CoordinateSystem.getAxis(*)");
                assertEquals(directions[i], axis.getDirection(), "CoordinateSystem.getAxis(*).getDirection()");
                assertEquals(axisUnits[Math.min(i, axisUnits.length-1)], axis.getUnit(), "CoordinateSystem.getAxis(*).getUnit()");
            }
        }
    }

    /**
     * Compares the name and identifier of the given {@code object} against the expected values.
     * This method allows for some flexibilities:
     *
     * <ul>
     *   <li>For {@link IdentifiedObject#getName()}:
     *     <ul>
     *       <li>Only the value returned by {@link Identifier#getCode()} is verified.
     *           The code space, authority and version are ignored.</li>
     *       <li>Only the characters that are valid for Unicode identifiers are compared (ignoring case), as documented in
     *           {@link org.iogp.gigs.internal.geoapi.Assert#assertUnicodeIdentifierEquals Assert.assertUnicodeIdentifierEquals(…)}.</li>
     *     </ul>
     *   </li>
     *   <li>For {@link IdentifiedObject#getIdentifiers()}:
     *     <ul>
     *       <li>Only the value returned by {@link Identifier#getCode()} is verified.
     *           The code space, authority and version are ignored.</li>
     *       <li>The identifiers collection can contain more identifiers than the expected one,
     *           and the expected identifier does not need to be first.</li>
     *       <li>The comparison is case-insensitive.</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * If the given {@code object} is {@code null}, then this method does nothing.
     * Deciding if {@code null} objects are allowed or not is {@link org.iogp.gigs.internal.geoapi.Validator}'s job.
     *
     * @param object      the object to verify, or {@code null} if none.
     * @param name        the expected name (ignoring code space), or {@code null} if unrestricted.
     * @param identifier  the expected identifier code (ignoring code space), or {@code null} if unrestricted.
     */
    final void verifyIdentification(final IdentifiedObject object, final String name, final String identifier) {
        if (object != null) {
            if (name != null) {
                assertUnicodeIdentifierEquals("getName().getCode()", name, getName(object), true);
            }
            if (identifier != null) {
                for (final Identifier id : object.getIdentifiers()) {
                    assertNotNull(id, "getName().getIdentifiers()");
                    if (identifier.equalsIgnoreCase(id.getCode())) {
                        return;
                    }
                }
                fail("getName().getIdentifiers(): element “" + identifier + "” not found.");
            }
        }
    }
}
