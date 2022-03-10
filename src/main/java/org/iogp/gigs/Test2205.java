/*
 * GIGS - Geospatial Integrity of Geoscience Software
 * https://gigs.iogp.org/
 *
 * Copyright (C) 2022 International Association of Oil and Gas Producers.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.iogp.gigs;

import org.opengis.util.FactoryException;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.crs.GeodeticCRS;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.iogp.gigs.internal.geoapi.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.iogp.gigs.internal.geoapi.Assert.assertAxisDirectionsEqual;


/**
 * Verifies geodetic reference systems bundled with the geoscience software.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @author  Alexis Manin (Geomatys)
 * @version 1.0
 * @since   1.0
 */
@DisplayName("Geodetic CRS")
public class Test2205 extends Series2000<GeodeticCRS> {
    /**
     * The expected axis directions of two-dimensional geographic CRS with longitude first.
     * This axis order does not appear in the EPSG database, but appears often in user-defined CRS.
     */
    static final AxisDirection[] GEOGRAPHIC_XY = {
        AxisDirection.EAST,
        AxisDirection.NORTH
    };

    /**
     * The expected axis directions of two-dimensional geographic CRS.
     */
    static final AxisDirection[] GEOGRAPHIC_2D = {
        AxisDirection.NORTH,
        AxisDirection.EAST
    };

    /**
     * The expected axis directions of three-dimensional geographic CRS.
     */
    static final AxisDirection[] GEOGRAPHIC_3D = {
        AxisDirection.NORTH,
        AxisDirection.EAST,
        AxisDirection.UP
    };

    /**
     * The expected axis directions of geocentric CRS.
     */
    static final AxisDirection[] GEOCENTRIC = {
        AxisDirection.GEOCENTRIC_X,
        AxisDirection.GEOCENTRIC_Y,
        AxisDirection.GEOCENTRIC_Z
    };

    /**
     * Whether the CRS to create is geocentric.
     * Otherwise it is assumed geographic.
     */
    private boolean isGeocentric;

    /**
     * EPSG code of the datum associated to the CRS.
     */
    private int datum;

    /**
     * The CRS created by the factory,
     * or {@code null} if not yet created or if CRS creation failed.
     *
     * @see #crsAuthorityFactory
     */
    private GeodeticCRS crs;

    /**
     * Factory to use for building {@link GeodeticCRS} instances, or {@code null} if none.
     */
    protected final CRSAuthorityFactory crsAuthorityFactory;

    /**
     * Creates a new test using the given factory. If a given factory is {@code null},
     * then the tests which depend on it will be skipped.
     *
     * @param crsFactory  factory for creating {@link GeodeticCRS} instances.
     */
    public Test2205(final CRSAuthorityFactory crsFactory) {
        crsAuthorityFactory = crsFactory;
    }

    /**
     * Returns information about the configuration of the test which has been run.
     * This method returns a map containing:
     *
     * <ul>
     *   <li>All the following values associated to the {@link org.opengis.test.Configuration.Key} of the same name:
     *     <ul>
     *       <li>{@link #isStandardNameSupported}</li>
     *       <li>{@link #isStandardAliasSupported}</li>
     *       <li>{@link #isDependencyIdentificationSupported}</li>
     *       <li>{@link #isDeprecatedObjectCreationSupported}</li>
     *       <li>{@link #crsAuthorityFactory}</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return the configuration of the test being run.
     */
    @Override
    Configuration configuration() {
        final Configuration op = super.configuration();
        assertNull(op.put(Configuration.Key.crsAuthorityFactory, crsAuthorityFactory));
        return op;
    }

    /**
     * Returns the CRS instance to be tested. When this method is invoked for the first time,
     * it creates the CRS to test by invoking the
     * {@link CRSAuthorityFactory#createGeographicCRS(String)} or
     * {@link CRSAuthorityFactory#createGeocentricCRS(String)} method
     * with the current {@link #code} value in argument.
     * The created object is then cached and returned in all subsequent invocations of this method.
     *
     * @return the CRS instance to test.
     * @throws FactoryException if an error occurred while creating the CRS instance.
     */
    @Override
    public GeodeticCRS getIdentifiedObject() throws FactoryException {
        if (crs == null) {
            assumeNotNull(crsAuthorityFactory);
            try {
                if (isGeocentric) {
                    crs = crsAuthorityFactory.createGeocentricCRS(String.valueOf(code));
                } else {
                    crs = crsAuthorityFactory.createGeographicCRS(String.valueOf(code));
                }
            } catch (NoSuchAuthorityCodeException e) {
                unsupportedCode(GeodeticCRS.class, code);
                throw e;
            }
        }
        return crs;
    }

    /**
     * Verifies the geographic or geocentric CRS.
     *
     * @param  expectedDirections  either {@link #GEOGRAPHIC_2D}, {@link #GEOGRAPHIC_3D} or {@link #GEOCENTRIC}.
     * @throws FactoryException if an error occurred while creating the CRS instance.
     */
    private void verifyGeodeticCRS(final AxisDirection[] expectedDirections) throws FactoryException {
        final GeodeticCRS crs = getIdentifiedObject();
        assertNotNull(crs, "GeodeticCRS");
        validators.validate(crs);

        // Geodetic CRS identifier.
        assertContainsCode("GeodeticCRS.getIdentifiers()", "EPSG", code, crs.getIdentifiers());

        // Geodetic CRS name.
        if (isStandardNameSupported) {
            configurationTip = Configuration.Key.isStandardNameSupported;
            assertEquals(name, getVerifiableName(crs), "GeodeticCRS.getName()");
            configurationTip = null;
        }

        // Geodetic CRS datum.
        final GeodeticDatum crsDatum = crs.getDatum();
        assertNotNull(crsDatum, "GeodeticCRS.getDatum()");
        validators.validate(crsDatum);
        if (isDependencyIdentificationSupported) {
            configurationTip = Configuration.Key.isDependencyIdentificationSupported;
            assertContainsCode("GeodeticCRS.getDatum().getIdentifiers()", "EPSG", datum, crsDatum.getIdentifiers());
            configurationTip = null;
        }

        // Geodetic CRS coordinate system.
        final CoordinateSystem cs = crs.getCoordinateSystem();
        assertNotNull(cs, "GeodeticCRS.getCoordinateSystem()");
        assertEquals(expectedDirections.length, cs.getDimension(), "GeodeticCRS.getCoordinateSystem().getDimension()");
        assertAxisDirectionsEqual("GeodeticCRS.getCoordinateSystem().getAxis(*)", cs, expectedDirections);
    }

    /**
     * Tests “Abidjan 1987” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4143</b></li>
     *   <li>EPSG CRS name: <b>Abidjan 1987</b></li>
     *   <li>Alias(es) given by EPSG: <b>Côte d'Ivoire</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6143</b></li>
     *   <li>EPSG Usage Extent: <b>Cote d'Ivoire (Ivory Coast)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Abidjan 1987")
    public void EPSG_4143() throws FactoryException {
        code  = 4143;
        name  = "Abidjan 1987";
        datum = 6143;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Accra” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4168</b></li>
     *   <li>EPSG CRS name: <b>Accra</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6168</b></li>
     *   <li>EPSG Usage Extent: <b>Ghana</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Accra")
    public void EPSG_4168() throws FactoryException {
        code  = 4168;
        name  = "Accra";
        datum = 6168;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Adindan” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4201</b></li>
     *   <li>EPSG CRS name: <b>Adindan</b></li>
     *   <li>Alias(es) given by EPSG: <b>Blue Nile 1958</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6201</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Eritrea; Ethiopia; South Sudan and Sudan</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Adindan")
    public void EPSG_4201() throws FactoryException {
        code  = 4201;
        name  = "Adindan";
        datum = 6201;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Afgooye” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4205</b></li>
     *   <li>EPSG CRS name: <b>Afgooye</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6205</b></li>
     *   <li>EPSG Usage Extent: <b>Somalia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Afgooye")
    public void EPSG_4205() throws FactoryException {
        code  = 4205;
        name  = "Afgooye";
        datum = 6205;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Agadez” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4206</b></li>
     *   <li>EPSG CRS name: <b>Agadez</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6206</b></li>
     *   <li>EPSG Usage Extent: <b>Niger</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Agadez")
    public void EPSG_4206() throws FactoryException {
        code  = 4206;
        name  = "Agadez";
        datum = 6206;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “AGD66” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4202</b></li>
     *   <li>EPSG CRS name: <b>AGD66</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6202</b></li>
     *   <li>EPSG Usage Extent: <b>Australasia - Australia and PNG - AGD66</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("AGD66")
    public void EPSG_4202() throws FactoryException {
        code  = 4202;
        name  = "AGD66";
        datum = 6202;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “AGD84” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4203</b></li>
     *   <li>EPSG CRS name: <b>AGD84</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6203</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - AGD84</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("AGD84")
    public void EPSG_4203() throws FactoryException {
        code  = 4203;
        name  = "AGD84";
        datum = 6203;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Ain el Abd” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4204</b></li>
     *   <li>EPSG CRS name: <b>Ain el Abd</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6204</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Bahrain; Kuwait and Saudi Arabia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Ain el Abd")
    public void EPSG_4204() throws FactoryException {
        code  = 4204;
        name  = "Ain el Abd";
        datum = 6204;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Albanian 1987” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4191</b></li>
     *   <li>EPSG CRS name: <b>Albanian 1987</b></li>
     *   <li>Alias(es) given by EPSG: <b>ALB86</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6191</b></li>
     *   <li>EPSG Usage Extent: <b>Albania - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Albanian 1987")
    public void EPSG_4191() throws FactoryException {
        code  = 4191;
        name  = "Albanian 1987";
        datum = 6191;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “American Samoa 1962” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4169</b></li>
     *   <li>EPSG CRS name: <b>American Samoa 1962</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6169</b></li>
     *   <li>EPSG Usage Extent: <b>American Samoa - 2 main island groups</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("American Samoa 1962")
    public void EPSG_4169() throws FactoryException {
        code  = 4169;
        name  = "American Samoa 1962";
        datum = 6169;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Amersfoort” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4289</b></li>
     *   <li>EPSG CRS name: <b>Amersfoort</b></li>
     *   <li>Alias(es) given by EPSG: <b>RD Bessel</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6289</b></li>
     *   <li>EPSG Usage Extent: <b>Netherlands - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Amersfoort")
    public void EPSG_4289() throws FactoryException {
        code  = 4289;
        name  = "Amersfoort";
        datum = 6289;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Ammassalik 1958” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4196</b></li>
     *   <li>EPSG CRS name: <b>Ammassalik 1958</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6196</b></li>
     *   <li>EPSG Usage Extent: <b>Greenland - Ammassalik area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Ammassalik 1958")
    public void EPSG_4196() throws FactoryException {
        code  = 4196;
        name  = "Ammassalik 1958";
        datum = 6196;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Anguilla 1957” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4600</b></li>
     *   <li>EPSG CRS name: <b>Anguilla 1957</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6600</b></li>
     *   <li>EPSG Usage Extent: <b>Anguilla - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Anguilla 1957")
    public void EPSG_4600() throws FactoryException {
        code  = 4600;
        name  = "Anguilla 1957";
        datum = 6600;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Antigua 1943” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4601</b></li>
     *   <li>EPSG CRS name: <b>Antigua 1943</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6601</b></li>
     *   <li>EPSG Usage Extent: <b>Antigua - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Antigua 1943")
    public void EPSG_4601() throws FactoryException {
        code  = 4601;
        name  = "Antigua 1943";
        datum = 6601;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Aratu” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4208</b></li>
     *   <li>EPSG CRS name: <b>Aratu</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6208</b></li>
     *   <li>EPSG Usage Extent: <b>Brazil - Aratu</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Aratu")
    public void EPSG_4208() throws FactoryException {
        code  = 4208;
        name  = "Aratu";
        datum = 6208;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Arc 1950” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4209</b></li>
     *   <li>EPSG CRS name: <b>Arc 1950</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6209</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Botswana; Malawi; Zambia; Zimbabwe</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Arc 1950")
    public void EPSG_4209() throws FactoryException {
        code  = 4209;
        name  = "Arc 1950";
        datum = 6209;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Arc 1960” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4210</b></li>
     *   <li>EPSG CRS name: <b>Arc 1960</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6210</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Burundi; Kenya; Rwanda; Tanzania and Uganda</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Arc 1960")
    public void EPSG_4210() throws FactoryException {
        code  = 4210;
        name  = "Arc 1960";
        datum = 6210;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Ascension Island 1958” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4712</b></li>
     *   <li>EPSG CRS name: <b>Ascension Island 1958</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6712</b></li>
     *   <li>EPSG Usage Extent: <b>St Helena - Ascension Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Ascension Island 1958")
    public void EPSG_4712() throws FactoryException {
        code  = 4712;
        name  = "Ascension Island 1958";
        datum = 6712;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Astro DOS 71” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4710</b></li>
     *   <li>EPSG CRS name: <b>Astro DOS 71</b></li>
     *   <li>Alias(es) given by EPSG: <b>ASTRO DOS 71/4</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6710</b></li>
     *   <li>EPSG Usage Extent: <b>St Helena - St Helena Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Astro DOS 71")
    public void EPSG_4710() throws FactoryException {
        code  = 4710;
        name  = "Astro DOS 71";
        datum = 6710;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “ATF (Paris)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4901</b></li>
     *   <li>EPSG CRS name: <b>ATF (Paris)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6901</b></li>
     *   <li>EPSG Usage Extent: <b>France - mainland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ATF (Paris)")
    public void EPSG_4901() throws FactoryException {
        code  = 4901;
        name  = "ATF (Paris)";
        datum = 6901;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “ATS77” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4122</b></li>
     *   <li>EPSG CRS name: <b>ATS77</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6122</b></li>
     *   <li>EPSG Usage Extent: <b>Canada - Maritime Provinces</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ATS77")
    public void EPSG_4122() throws FactoryException {
        code  = 4122;
        name  = "ATS77";
        datum = 6122;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Australian Antarctic” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4176</b></li>
     *   <li>EPSG CRS name: <b>Australian Antarctic</b></li>
     *   <li>Alias(es) given by EPSG: <b>AAD98</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6176</b></li>
     *   <li>EPSG Usage Extent: <b>Antarctica - Australian sector</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Australian Antarctic")
    public void EPSG_4176() throws FactoryException {
        code  = 4176;
        name  = "Australian Antarctic";
        datum = 6176;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Australian Antarctic” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4931</b></li>
     *   <li>EPSG CRS name: <b>Australian Antarctic</b></li>
     *   <li>Alias(es) given by EPSG: <b>AAD98</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6176</b></li>
     *   <li>EPSG Usage Extent: <b>Antarctica - Australian sector</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Australian Antarctic")
    public void EPSG_4931() throws FactoryException {
        code  = 4931;
        name  = "Australian Antarctic";
        datum = 6176;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “Australian Antarctic” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4930</b></li>
     *   <li>EPSG CRS name: <b>Australian Antarctic</b></li>
     *   <li>Alias(es) given by EPSG: <b>AAD98</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6176</b></li>
     *   <li>EPSG Usage Extent: <b>Antarctica - Australian sector</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Australian Antarctic")
    public void EPSG_4930() throws FactoryException {
        code  = 4930;
        name  = "Australian Antarctic";
        datum = 6176;
        isGeocentric = true;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Ayabelle Lighthouse” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4713</b></li>
     *   <li>EPSG CRS name: <b>Ayabelle Lighthouse</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6713</b></li>
     *   <li>EPSG Usage Extent: <b>Djibouti</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Ayabelle Lighthouse")
    public void EPSG_4713() throws FactoryException {
        code  = 4713;
        name  = "Ayabelle Lighthouse";
        datum = 6713;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Azores Central 1948” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4183</b></li>
     *   <li>EPSG CRS name: <b>Azores Central 1948</b></li>
     *   <li>Alias(es) given by EPSG: <b>Graciosa</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6183</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - Azores C - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Azores Central 1948")
    public void EPSG_4183() throws FactoryException {
        code  = 4183;
        name  = "Azores Central 1948";
        datum = 6183;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Azores Central 1995” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4665</b></li>
     *   <li>EPSG CRS name: <b>Azores Central 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>Base SW</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6665</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - Azores C - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Azores Central 1995")
    public void EPSG_4665() throws FactoryException {
        code  = 4665;
        name  = "Azores Central 1995";
        datum = 6665;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Azores Occidental 1939” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4182</b></li>
     *   <li>EPSG CRS name: <b>Azores Occidental 1939</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6182</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - Azores W - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Azores Occidental 1939")
    public void EPSG_4182() throws FactoryException {
        code  = 4182;
        name  = "Azores Occidental 1939";
        datum = 6182;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Azores Oriental 1940” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4184</b></li>
     *   <li>EPSG CRS name: <b>Azores Oriental 1940</b></li>
     *   <li>Alias(es) given by EPSG: <b>Sao Braz</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6184</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - Azores E - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Azores Oriental 1940")
    public void EPSG_4184() throws FactoryException {
        code  = 4184;
        name  = "Azores Oriental 1940";
        datum = 6184;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Azores Oriental 1995” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4664</b></li>
     *   <li>EPSG CRS name: <b>Azores Oriental 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>Sao Braz</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6664</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - Azores E - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Azores Oriental 1995")
    public void EPSG_4664() throws FactoryException {
        code  = 4664;
        name  = "Azores Oriental 1995";
        datum = 6664;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Barbados 1938” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4212</b></li>
     *   <li>EPSG CRS name: <b>Barbados 1938</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6212</b></li>
     *   <li>EPSG Usage Extent: <b>Barbados - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Barbados 1938")
    public void EPSG_4212() throws FactoryException {
        code  = 4212;
        name  = "Barbados 1938";
        datum = 6212;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Batavia” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4211</b></li>
     *   <li>EPSG CRS name: <b>Batavia</b></li>
     *   <li>Alias(es) given by EPSG: <b>Genuk</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6211</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Java; Java Sea and western Sumatra</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Batavia")
    public void EPSG_4211() throws FactoryException {
        code  = 4211;
        name  = "Batavia";
        datum = 6211;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Batavia (Jakarta)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4813</b></li>
     *   <li>EPSG CRS name: <b>Batavia (Jakarta)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Genuk (Jakarta)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6813</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Bali; Java and western Sumatra onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Batavia (Jakarta)")
    public void EPSG_4813() throws FactoryException {
        code  = 4813;
        name  = "Batavia (Jakarta)";
        datum = 6813;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “BDA2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4762</b></li>
     *   <li>EPSG CRS name: <b>BDA2000</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6762</b></li>
     *   <li>EPSG Usage Extent: <b>Bermuda</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("BDA2000")
    public void EPSG_4762() throws FactoryException {
        code  = 4762;
        name  = "BDA2000";
        datum = 6762;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “BDA2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4887</b></li>
     *   <li>EPSG CRS name: <b>BDA2000</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6762</b></li>
     *   <li>EPSG Usage Extent: <b>Bermuda</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("BDA2000")
    public void EPSG_4887() throws FactoryException {
        code  = 4887;
        name  = "BDA2000";
        datum = 6762;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “BDA2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4886</b></li>
     *   <li>EPSG CRS name: <b>BDA2000</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6762</b></li>
     *   <li>EPSG Usage Extent: <b>Bermuda</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("BDA2000")
    public void EPSG_4886() throws FactoryException {
        code  = 4886;
        name  = "BDA2000";
        datum = 6762;
        isGeocentric = true;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Beduaram” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4213</b></li>
     *   <li>EPSG CRS name: <b>Beduaram</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6213</b></li>
     *   <li>EPSG Usage Extent: <b>Niger - southeast</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Beduaram")
    public void EPSG_4213() throws FactoryException {
        code  = 4213;
        name  = "Beduaram";
        datum = 6213;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Beijing 1954” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4214</b></li>
     *   <li>EPSG CRS name: <b>Beijing 1954</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6214</b></li>
     *   <li>EPSG Usage Extent: <b>China</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Beijing 1954")
    public void EPSG_4214() throws FactoryException {
        code  = 4214;
        name  = "Beijing 1954";
        datum = 6214;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Belge 1950” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4215</b></li>
     *   <li>EPSG CRS name: <b>Belge 1950</b></li>
     *   <li>Alias(es) given by EPSG: <b>BD 50</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6215</b></li>
     *   <li>EPSG Usage Extent: <b>Belgium - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Belge 1950")
    public void EPSG_4215() throws FactoryException {
        code  = 4215;
        name  = "Belge 1950";
        datum = 6215;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Belge 1950 (Brussels)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4809</b></li>
     *   <li>EPSG CRS name: <b>Belge 1950 (Brussels)</b></li>
     *   <li>Alias(es) given by EPSG: <b>BD 50 (Brussels)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6809</b></li>
     *   <li>EPSG Usage Extent: <b>Belgium - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Belge 1950 (Brussels)")
    public void EPSG_4809() throws FactoryException {
        code  = 4809;
        name  = "Belge 1950 (Brussels)";
        datum = 6809;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Belge 1972” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4313</b></li>
     *   <li>EPSG CRS name: <b>Belge 1972</b></li>
     *   <li>Alias(es) given by EPSG: <b>BD 72</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6313</b></li>
     *   <li>EPSG Usage Extent: <b>Belgium - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Belge 1972")
    public void EPSG_4313() throws FactoryException {
        code  = 4313;
        name  = "Belge 1972";
        datum = 6313;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Bellevue” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4714</b></li>
     *   <li>EPSG CRS name: <b>Bellevue</b></li>
     *   <li>Alias(es) given by EPSG: <b>Bellevue (IGN)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6714</b></li>
     *   <li>EPSG Usage Extent: <b>Vanuatu - southern islands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Bellevue")
    public void EPSG_4714() throws FactoryException {
        code  = 4714;
        name  = "Bellevue";
        datum = 6714;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Bermuda 1957” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4216</b></li>
     *   <li>EPSG CRS name: <b>Bermuda 1957</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6216</b></li>
     *   <li>EPSG Usage Extent: <b>Bermuda - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Bermuda 1957")
    public void EPSG_4216() throws FactoryException {
        code  = 4216;
        name  = "Bermuda 1957";
        datum = 6216;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Bern 1898 (Bern)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4801</b></li>
     *   <li>EPSG CRS name: <b>Bern 1898 (Bern)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6801</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Liechtenstein and Switzerland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Bern 1898 (Bern)")
    public void EPSG_4801() throws FactoryException {
        code  = 4801;
        name  = "Bern 1898 (Bern)";
        datum = 6801;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Bern 1938” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4306</b></li>
     *   <li>EPSG CRS name: <b>Bern 1938</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6306</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Liechtenstein and Switzerland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Bern 1938")
    public void EPSG_4306() throws FactoryException {
        code  = 4306;
        name  = "Bern 1938";
        datum = 6306;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Bissau” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4165</b></li>
     *   <li>EPSG CRS name: <b>Bissau</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6165</b></li>
     *   <li>EPSG Usage Extent: <b>Guinea-Bissau - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Bissau")
    public void EPSG_4165() throws FactoryException {
        code  = 4165;
        name  = "Bissau";
        datum = 6165;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Bogota 1975” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4218</b></li>
     *   <li>EPSG CRS name: <b>Bogota 1975</b></li>
     *   <li>Alias(es) given by EPSG: <b>Bogota</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6218</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - mainland and offshore Caribbean</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Bogota 1975")
    public void EPSG_4218() throws FactoryException {
        code  = 4218;
        name  = "Bogota 1975";
        datum = 6218;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Bogota 1975 (Bogota)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4802</b></li>
     *   <li>EPSG CRS name: <b>Bogota 1975 (Bogota)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6802</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia - mainland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Bogota 1975 (Bogota)")
    public void EPSG_4802() throws FactoryException {
        code  = 4802;
        name  = "Bogota 1975 (Bogota)";
        datum = 6802;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Bukit Rimpah” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4219</b></li>
     *   <li>EPSG CRS name: <b>Bukit Rimpah</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6219</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Banga &amp; Belitung Islands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Bukit Rimpah")
    public void EPSG_4219() throws FactoryException {
        code  = 4219;
        name  = "Bukit Rimpah";
        datum = 6219;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Camacupa 1948” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4220</b></li>
     *   <li>EPSG CRS name: <b>Camacupa 1948</b></li>
     *   <li>Alias(es) given by EPSG: <b>Camacupa</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6220</b></li>
     *   <li>EPSG Usage Extent: <b>Angola - Angola proper</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Camacupa 1948")
    public void EPSG_4220() throws FactoryException {
        code  = 4220;
        name  = "Camacupa 1948";
        datum = 6220;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Camp Area Astro” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4715</b></li>
     *   <li>EPSG CRS name: <b>Camp Area Astro</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6715</b></li>
     *   <li>EPSG Usage Extent: <b>Antarctica - Camp McMurdo area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Camp Area Astro")
    public void EPSG_4715() throws FactoryException {
        code  = 4715;
        name  = "Camp Area Astro";
        datum = 6715;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Campo Inchauspe” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4221</b></li>
     *   <li>EPSG CRS name: <b>Campo Inchauspe</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6221</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - mainland onshore and offshore TdF</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Campo Inchauspe")
    public void EPSG_4221() throws FactoryException {
        code  = 4221;
        name  = "Campo Inchauspe";
        datum = 6221;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Cape” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4222</b></li>
     *   <li>EPSG CRS name: <b>Cape</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6222</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Botswana; Eswatini; Lesotho and South Africa</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Cape")
    public void EPSG_4222() throws FactoryException {
        code  = 4222;
        name  = "Cape";
        datum = 6222;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Cape Canaveral” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4717</b></li>
     *   <li>EPSG CRS name: <b>Cape Canaveral</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6717</b></li>
     *   <li>EPSG Usage Extent: <b>North America - Bahamas and USA - Florida - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Cape Canaveral")
    public void EPSG_4717() throws FactoryException {
        code  = 4717;
        name  = "Cape Canaveral";
        datum = 6717;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Carthage” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4223</b></li>
     *   <li>EPSG CRS name: <b>Carthage</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6223</b></li>
     *   <li>EPSG Usage Extent: <b>Tunisia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Carthage")
    public void EPSG_4223() throws FactoryException {
        code  = 4223;
        name  = "Carthage";
        datum = 6223;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Carthage (Paris)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4816</b></li>
     *   <li>EPSG CRS name: <b>Carthage (Paris)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6816</b></li>
     *   <li>EPSG Usage Extent: <b>Tunisia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Carthage (Paris)")
    public void EPSG_4816() throws FactoryException {
        code  = 4816;
        name  = "Carthage (Paris)";
        datum = 6816;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “CH1903” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4149</b></li>
     *   <li>EPSG CRS name: <b>CH1903</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6149</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Liechtenstein and Switzerland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("CH1903")
    public void EPSG_4149() throws FactoryException {
        code  = 4149;
        name  = "CH1903";
        datum = 6149;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “CH1903+” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4150</b></li>
     *   <li>EPSG CRS name: <b>CH1903+</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6150</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Liechtenstein and Switzerland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("CH1903+")
    public void EPSG_4150() throws FactoryException {
        code  = 4150;
        name  = "CH1903+";
        datum = 6150;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Chatham Islands 1971” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4672</b></li>
     *   <li>EPSG CRS name: <b>Chatham Islands 1971</b></li>
     *   <li>Alias(es) given by EPSG: <b>CI1971</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6672</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand - Chatham Islands group</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Chatham Islands 1971")
    public void EPSG_4672() throws FactoryException {
        code  = 4672;
        name  = "Chatham Islands 1971";
        datum = 6672;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Chatham Islands 1979” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4673</b></li>
     *   <li>EPSG CRS name: <b>Chatham Islands 1979</b></li>
     *   <li>Alias(es) given by EPSG: <b>CI1979</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6673</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand - Chatham Islands group</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Chatham Islands 1979")
    public void EPSG_4673() throws FactoryException {
        code  = 4673;
        name  = "Chatham Islands 1979";
        datum = 6673;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Chos Malal 1914” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4160</b></li>
     *   <li>EPSG CRS name: <b>Chos Malal 1914</b></li>
     *   <li>Alias(es) given by EPSG: <b>Quini-Huao</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6160</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - Mendoza and Neuquen</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Chos Malal 1914")
    public void EPSG_4160() throws FactoryException {
        code  = 4160;
        name  = "Chos Malal 1914";
        datum = 6160;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “CHTRF95” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4151</b></li>
     *   <li>EPSG CRS name: <b>CHTRF95</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6151</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Liechtenstein and Switzerland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("CHTRF95")
    public void EPSG_4151() throws FactoryException {
        code  = 4151;
        name  = "CHTRF95";
        datum = 6151;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “CHTRF96” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4933</b></li>
     *   <li>EPSG CRS name: <b>CHTRF96</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6151</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Liechtenstein and Switzerland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("CHTRF96")
    public void EPSG_4933() throws FactoryException {
        code  = 4933;
        name  = "CHTRF96";
        datum = 6151;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “CHTRF97” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4932</b></li>
     *   <li>EPSG CRS name: <b>CHTRF97</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6151</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Liechtenstein and Switzerland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("CHTRF97")
    public void EPSG_4932() throws FactoryException {
        code  = 4932;
        name  = "CHTRF97";
        datum = 6151;
        isGeocentric = true;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Chua” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4224</b></li>
     *   <li>EPSG CRS name: <b>Chua</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6224</b></li>
     *   <li>EPSG Usage Extent: <b>South America - Brazil - south of 18°S and west of 54°W + DF; N Paraguay</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Chua")
    public void EPSG_4224() throws FactoryException {
        code  = 4224;
        name  = "Chua";
        datum = 6224;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Cocos Islands 1965” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4708</b></li>
     *   <li>EPSG CRS name: <b>Cocos Islands 1965</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6708</b></li>
     *   <li>EPSG Usage Extent: <b>Cocos (Keeling) Islands - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Cocos Islands 1965")
    public void EPSG_4708() throws FactoryException {
        code  = 4708;
        name  = "Cocos Islands 1965";
        datum = 6708;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Combani 1950” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4632</b></li>
     *   <li>EPSG CRS name: <b>Combani 1950</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6632</b></li>
     *   <li>EPSG Usage Extent: <b>Mayotte - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Combani 1950")
    public void EPSG_4632() throws FactoryException {
        code  = 4632;
        name  = "Combani 1950";
        datum = 6632;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Conakry 1905” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4315</b></li>
     *   <li>EPSG CRS name: <b>Conakry 1905</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6315</b></li>
     *   <li>EPSG Usage Extent: <b>Guinea - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Conakry 1905")
    public void EPSG_4315() throws FactoryException {
        code  = 4315;
        name  = "Conakry 1905";
        datum = 6315;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Corrego Alegre 1970-72” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4225</b></li>
     *   <li>EPSG CRS name: <b>Corrego Alegre 1970-72</b></li>
     *   <li>Alias(es) given by EPSG: <b>Corrego Alegre</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6225</b></li>
     *   <li>EPSG Usage Extent: <b>Brazil - Corrego Alegre 1970-1972</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Corrego Alegre 1970-72")
    public void EPSG_4225() throws FactoryException {
        code  = 4225;
        name  = "Corrego Alegre 1970-72";
        datum = 6225;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “CSG67” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4623</b></li>
     *   <li>EPSG CRS name: <b>CSG67</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6623</b></li>
     *   <li>EPSG Usage Extent: <b>French Guiana - coastal area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("CSG67")
    public void EPSG_4623() throws FactoryException {
        code  = 4623;
        name  = "CSG67";
        datum = 6623;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Dabola 1981” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4155</b></li>
     *   <li>EPSG CRS name: <b>Dabola 1981</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6155</b></li>
     *   <li>EPSG Usage Extent: <b>Guinea - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Dabola 1981")
    public void EPSG_4155() throws FactoryException {
        code  = 4155;
        name  = "Dabola 1981";
        datum = 6155;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Datum 73” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4274</b></li>
     *   <li>EPSG CRS name: <b>Datum 73</b></li>
     *   <li>Alias(es) given by EPSG: <b>D73</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6274</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - mainland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Datum 73")
    public void EPSG_4274() throws FactoryException {
        code  = 4274;
        name  = "Datum 73";
        datum = 6274;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Dealul Piscului 1930” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4316</b></li>
     *   <li>EPSG CRS name: <b>Dealul Piscului 1930</b></li>
     *   <li>Alias(es) given by EPSG: <b>Dealul Piscului 1933</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6316</b></li>
     *   <li>EPSG Usage Extent: <b>Romania - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Dealul Piscului 1930")
    public void EPSG_4316() throws FactoryException {
        code  = 4316;
        name  = "Dealul Piscului 1930";
        datum = 6316;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Deception Island” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4736</b></li>
     *   <li>EPSG CRS name: <b>Deception Island</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6736</b></li>
     *   <li>EPSG Usage Extent: <b>Antarctica - Deception Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Deception Island")
    public void EPSG_4736() throws FactoryException {
        code  = 4736;
        name  = "Deception Island";
        datum = 6736;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Deir ez Zor” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4227</b></li>
     *   <li>EPSG CRS name: <b>Deir ez Zor</b></li>
     *   <li>Alias(es) given by EPSG: <b>Levant</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6227</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Lebanon and Syria onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Deir ez Zor")
    public void EPSG_4227() throws FactoryException {
        code  = 4227;
        name  = "Deir ez Zor";
        datum = 6227;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “DGN95” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4755</b></li>
     *   <li>EPSG CRS name: <b>DGN95</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6755</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95")
    public void EPSG_4755() throws FactoryException {
        code  = 4755;
        name  = "DGN95";
        datum = 6755;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “DGN95” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4898</b></li>
     *   <li>EPSG CRS name: <b>DGN95</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6755</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95")
    public void EPSG_4898() throws FactoryException {
        code  = 4898;
        name  = "DGN95";
        datum = 6755;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “DGN95” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4897</b></li>
     *   <li>EPSG CRS name: <b>DGN95</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGD95</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6755</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("DGN95")
    public void EPSG_4897() throws FactoryException {
        code  = 4897;
        name  = "DGN95";
        datum = 6755;
        isGeocentric = true;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “DHDN” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4314</b></li>
     *   <li>EPSG CRS name: <b>DHDN</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6314</b></li>
     *   <li>EPSG Usage Extent: <b>Germany - West Germany all states</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("DHDN")
    public void EPSG_4314() throws FactoryException {
        code  = 4314;
        name  = "DHDN";
        datum = 6314;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Diego Garcia 1969” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4724</b></li>
     *   <li>EPSG CRS name: <b>Diego Garcia 1969</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6724</b></li>
     *   <li>EPSG Usage Extent: <b>British Indian Ocean Territory - Diego Garcia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Diego Garcia 1969")
    public void EPSG_4724() throws FactoryException {
        code  = 4724;
        name  = "Diego Garcia 1969";
        datum = 6724;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Dominica 1945” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4602</b></li>
     *   <li>EPSG CRS name: <b>Dominica 1945</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6602</b></li>
     *   <li>EPSG Usage Extent: <b>Dominica - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Dominica 1945")
    public void EPSG_4602() throws FactoryException {
        code  = 4602;
        name  = "Dominica 1945";
        datum = 6602;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Douala 1948” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4192</b></li>
     *   <li>EPSG CRS name: <b>Douala 1948</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6192</b></li>
     *   <li>EPSG Usage Extent: <b>Cameroon - coastal area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Douala 1948")
    public void EPSG_4192() throws FactoryException {
        code  = 4192;
        name  = "Douala 1948";
        datum = 6192;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Easter Island 1967” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4719</b></li>
     *   <li>EPSG CRS name: <b>Easter Island 1967</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6719</b></li>
     *   <li>EPSG Usage Extent: <b>Chile - Easter Island onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Easter Island 1967")
    public void EPSG_4719() throws FactoryException {
        code  = 4719;
        name  = "Easter Island 1967";
        datum = 6719;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “ED50” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4230</b></li>
     *   <li>EPSG CRS name: <b>ED50</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6230</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - ED50 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ED50")
    public void EPSG_4230() throws FactoryException {
        code  = 4230;
        name  = "ED50";
        datum = 6230;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “ED50(ED77)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4154</b></li>
     *   <li>EPSG CRS name: <b>ED50(ED77)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6154</b></li>
     *   <li>EPSG Usage Extent: <b>Iran</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ED50(ED77)")
    public void EPSG_4154() throws FactoryException {
        code  = 4154;
        name  = "ED50(ED77)";
        datum = 6154;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “ED79” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4668</b></li>
     *   <li>EPSG CRS name: <b>ED79</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6668</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - west</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ED79")
    public void EPSG_4668() throws FactoryException {
        code  = 4668;
        name  = "ED79";
        datum = 6668;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “ED87” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4231</b></li>
     *   <li>EPSG CRS name: <b>ED87</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6231</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - west</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ED87")
    public void EPSG_4231() throws FactoryException {
        code  = 4231;
        name  = "ED87";
        datum = 6231;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Egypt 1907” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4229</b></li>
     *   <li>EPSG CRS name: <b>Egypt 1907</b></li>
     *   <li>Alias(es) given by EPSG: <b>Old Egyptian</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6229</b></li>
     *   <li>EPSG Usage Extent: <b>Egypt</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Egypt 1907")
    public void EPSG_4229() throws FactoryException {
        code  = 4229;
        name  = "Egypt 1907";
        datum = 6229;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Egypt 1930” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4199</b></li>
     *   <li>EPSG CRS name: <b>Egypt 1930</b></li>
     *   <li>Alias(es) given by EPSG: <b>New Egyptian</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6199</b></li>
     *   <li>EPSG Usage Extent: <b>Egypt - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Egypt 1930")
    public void EPSG_4199() throws FactoryException {
        code  = 4199;
        name  = "Egypt 1930";
        datum = 6199;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Egypt Gulf of Suez S-650 TL” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4706</b></li>
     *   <li>EPSG CRS name: <b>Egypt Gulf of Suez S-650 TL</b></li>
     *   <li>Alias(es) given by EPSG: <b>S-650 TL</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6706</b></li>
     *   <li>EPSG Usage Extent: <b>Egypt - Gulf of Suez</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Egypt Gulf of Suez S-650 TL")
    public void EPSG_4706() throws FactoryException {
        code  = 4706;
        name  = "Egypt Gulf of Suez S-650 TL";
        datum = 6706;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “ELD79” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4159</b></li>
     *   <li>EPSG CRS name: <b>ELD79</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6159</b></li>
     *   <li>EPSG Usage Extent: <b>Libya</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ELD79")
    public void EPSG_4159() throws FactoryException {
        code  = 4159;
        name  = "ELD79";
        datum = 6159;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “EST92” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4133</b></li>
     *   <li>EPSG CRS name: <b>EST92</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6133</b></li>
     *   <li>EPSG Usage Extent: <b>Estonia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("EST92")
    public void EPSG_4133() throws FactoryException {
        code  = 4133;
        name  = "EST92";
        datum = 6133;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “EST97” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4180</b></li>
     *   <li>EPSG CRS name: <b>EST97</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6180</b></li>
     *   <li>EPSG Usage Extent: <b>Estonia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("EST97")
    public void EPSG_4180() throws FactoryException {
        code  = 4180;
        name  = "EST97";
        datum = 6180;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “EST98” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4935</b></li>
     *   <li>EPSG CRS name: <b>EST98</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6180</b></li>
     *   <li>EPSG Usage Extent: <b>Estonia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("EST98")
    public void EPSG_4935() throws FactoryException {
        code  = 4935;
        name  = "EST98";
        datum = 6180;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “EST99” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4834</b></li>
     *   <li>EPSG CRS name: <b>EST99</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6180</b></li>
     *   <li>EPSG Usage Extent: <b>Estonia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("EST99")
    public void EPSG_4834() throws FactoryException {
        code  = 4834;
        name  = "EST99";
        datum = 6180;
        isGeocentric = true;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “ETRS89” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4258</b></li>
     *   <li>EPSG CRS name: <b>ETRS89</b></li>
     *   <li>Alias(es) given by EPSG: <b>ETRS89-GRS80</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6258</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - ETRF by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ETRS89")
    public void EPSG_4258() throws FactoryException {
        code  = 4258;
        name  = "ETRS89";
        datum = 6258;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “ETRS89” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4937</b></li>
     *   <li>EPSG CRS name: <b>ETRS89</b></li>
     *   <li>Alias(es) given by EPSG: <b>ETRS89</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6258</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - ETRF by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ETRS89")
    public void EPSG_4937() throws FactoryException {
        code  = 4937;
        name  = "ETRS89";
        datum = 6258;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “ETRS89” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4936</b></li>
     *   <li>EPSG CRS name: <b>ETRS89</b></li>
     *   <li>Alias(es) given by EPSG: <b>ETRS89 / (X Y Z)</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6258</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - ETRF by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ETRS89")
    public void EPSG_4936() throws FactoryException {
        code  = 4936;
        name  = "ETRS89";
        isGeocentric = true;
        datum = 6258;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Fahud” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4232</b></li>
     *   <li>EPSG CRS name: <b>Fahud</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6232</b></li>
     *   <li>EPSG Usage Extent: <b>Oman - mainland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Fahud")
    public void EPSG_4232() throws FactoryException {
        code  = 4232;
        name  = "Fahud";
        datum = 6232;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Fatu Iva 72” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4688</b></li>
     *   <li>EPSG CRS name: <b>Fatu Iva 72</b></li>
     *   <li>Alias(es) given by EPSG: <b>MHEFO 55</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6688</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia - Marquesas Islands - Fatu Hiva</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Fatu Iva 72")
    public void EPSG_4688() throws FactoryException {
        code  = 4688;
        name  = "Fatu Iva 72";
        datum = 6688;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “FD54” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4741</b></li>
     *   <li>EPSG CRS name: <b>FD54</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6741</b></li>
     *   <li>EPSG Usage Extent: <b>Faroe Islands - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("FD54")
    public void EPSG_4741() throws FactoryException {
        code  = 4741;
        name  = "FD54";
        datum = 6741;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “FD58” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4132</b></li>
     *   <li>EPSG CRS name: <b>FD58</b></li>
     *   <li>Alias(es) given by EPSG: <b>Final Datum 1958 (Iran)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6132</b></li>
     *   <li>EPSG Usage Extent: <b>Iran - FD58</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("FD58")
    public void EPSG_4132() throws FactoryException {
        code  = 4132;
        name  = "FD58";
        datum = 6132;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Fiji 1956” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4721</b></li>
     *   <li>EPSG CRS name: <b>Fiji 1956</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6721</b></li>
     *   <li>EPSG Usage Extent: <b>Fiji - main islands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Fiji 1956")
    public void EPSG_4721() throws FactoryException {
        code  = 4721;
        name  = "Fiji 1956";
        datum = 6721;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Fiji 1986” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4720</b></li>
     *   <li>EPSG CRS name: <b>Fiji 1986</b></li>
     *   <li>Alias(es) given by EPSG: <b>FGD 1986</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6720</b></li>
     *   <li>EPSG Usage Extent: <b>Fiji - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Fiji 1986")
    public void EPSG_4720() throws FactoryException {
        code  = 4720;
        name  = "Fiji 1986";
        datum = 6720;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “fk89” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4753</b></li>
     *   <li>EPSG CRS name: <b>fk89</b></li>
     *   <li>Alias(es) given by EPSG: <b>FD54a</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6753</b></li>
     *   <li>EPSG Usage Extent: <b>Faroe Islands - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("fk89")
    public void EPSG_4753() throws FactoryException {
        code  = 4753;
        name  = "fk89";
        datum = 6753;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Fort Marigot” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4621</b></li>
     *   <li>EPSG CRS name: <b>Fort Marigot</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6621</b></li>
     *   <li>EPSG Usage Extent: <b>Guadeloupe - St Martin and St Barthelemy - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Fort Marigot")
    public void EPSG_4621() throws FactoryException {
        code  = 4621;
        name  = "Fort Marigot";
        datum = 6621;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Gan 1970” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4684</b></li>
     *   <li>EPSG CRS name: <b>Gan 1970</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6684</b></li>
     *   <li>EPSG Usage Extent: <b>Maldives - onshore</b></li>
     * </ul>
     *
     * Remarks: In some references incorrectly named Gandajika 1970.
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Gan 1970")
    public void EPSG_4684() throws FactoryException {
        code  = 4684;
        name  = "Gan 1970";
        datum = 6684;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Garoua” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4197</b></li>
     *   <li>EPSG CRS name: <b>Garoua</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6197</b></li>
     *   <li>EPSG Usage Extent: <b>Cameroon - Garoua area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Garoua")
    public void EPSG_4197() throws FactoryException {
        code  = 4197;
        name  = "Garoua";
        datum = 6197;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “GDA94” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4283</b></li>
     *   <li>EPSG CRS name: <b>GDA94</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6283</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - GDA</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("GDA94")
    public void EPSG_4283() throws FactoryException {
        code  = 4283;
        name  = "GDA94";
        datum = 6283;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “GDA95” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4939</b></li>
     *   <li>EPSG CRS name: <b>GDA95</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6283</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - GDA</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("GDA95")
    public void EPSG_4939() throws FactoryException {
        code  = 4939;
        name  = "GDA95";
        datum = 6283;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “GDA96” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4938</b></li>
     *   <li>EPSG CRS name: <b>GDA96</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6283</b></li>
     *   <li>EPSG Usage Extent: <b>Australia - GDA</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("GDA96")
    public void EPSG_4938() throws FactoryException {
        code  = 4938;
        name  = "GDA96";
        isGeocentric = true;
        datum = 6283;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “GDM2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4742</b></li>
     *   <li>EPSG CRS name: <b>GDM2000</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6742</b></li>
     *   <li>EPSG Usage Extent: <b>Malaysia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("GDM2000")
    public void EPSG_4742() throws FactoryException {
        code  = 4742;
        name  = "GDM2000";
        datum = 6742;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “GDM2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4921</b></li>
     *   <li>EPSG CRS name: <b>GDM2000</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6742</b></li>
     *   <li>EPSG Usage Extent: <b>Malaysia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("GDM2000")
    public void EPSG_4921() throws FactoryException {
        code  = 4921;
        name  = "GDM2000";
        datum = 6742;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “GDM2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4920</b></li>
     *   <li>EPSG CRS name: <b>GDM2000</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6742</b></li>
     *   <li>EPSG Usage Extent: <b>Malaysia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("GDM2000")
    public void EPSG_4920() throws FactoryException {
        code  = 4920;
        name  = "GDM2000";
        isGeocentric = true;
        datum = 6742;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “GGRS87” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4121</b></li>
     *   <li>EPSG CRS name: <b>GGRS87</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6121</b></li>
     *   <li>EPSG Usage Extent: <b>Greece - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("GGRS87")
    public void EPSG_4121() throws FactoryException {
        code  = 4121;
        name  = "GGRS87";
        datum = 6121;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “GR96” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4747</b></li>
     *   <li>EPSG CRS name: <b>GR96</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6747</b></li>
     *   <li>EPSG Usage Extent: <b>Greenland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("GR96")
    public void EPSG_4747() throws FactoryException {
        code  = 4747;
        name  = "GR96";
        datum = 6747;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “GR96” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4909</b></li>
     *   <li>EPSG CRS name: <b>GR96</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6747</b></li>
     *   <li>EPSG Usage Extent: <b>Greenland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("GR96")
    public void EPSG_4909() throws FactoryException {
        code  = 4909;
        name  = "GR96";
        datum = 6747;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “GR96” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4908</b></li>
     *   <li>EPSG CRS name: <b>GR96</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6747</b></li>
     *   <li>EPSG Usage Extent: <b>Greenland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("GR96")
    public void EPSG_4908() throws FactoryException {
        code  = 4908;
        name  = "GR96";
        isGeocentric = true;
        datum = 6747;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Grand Cayman 1959” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4723</b></li>
     *   <li>EPSG CRS name: <b>Grand Cayman 1959</b></li>
     *   <li>Alias(es) given by EPSG: <b>Grand Cayman 1959</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6723</b></li>
     *   <li>EPSG Usage Extent: <b>Cayman Islands - Grand Cayman</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Grand Cayman 1959")
    public void EPSG_4723() throws FactoryException {
        code  = 4723;
        name  = "Grand Cayman 1959";
        datum = 6723;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Grand Comoros” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4646</b></li>
     *   <li>EPSG CRS name: <b>Grand Comoros</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGN50</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6646</b></li>
     *   <li>EPSG Usage Extent: <b>Comoros - Njazidja (Grande Comore)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Grand Comoros")
    public void EPSG_4646() throws FactoryException {
        code  = 4646;
        name  = "Grand Comoros";
        datum = 6646;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Greek” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4120</b></li>
     *   <li>EPSG CRS name: <b>Greek</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6120</b></li>
     *   <li>EPSG Usage Extent: <b>Greece - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Greek")
    public void EPSG_4120() throws FactoryException {
        code  = 4120;
        name  = "Greek";
        datum = 6120;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Greek (Athens)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4815</b></li>
     *   <li>EPSG CRS name: <b>Greek (Athens)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6815</b></li>
     *   <li>EPSG Usage Extent: <b>Greece - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Greek (Athens)")
    public void EPSG_4815() throws FactoryException {
        code  = 4815;
        name  = "Greek (Athens)";
        datum = 6815;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Grenada 1953” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4603</b></li>
     *   <li>EPSG CRS name: <b>Grenada 1953</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6603</b></li>
     *   <li>EPSG Usage Extent: <b>Grenada and southern Grenadines - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Grenada 1953")
    public void EPSG_4603() throws FactoryException {
        code  = 4603;
        name  = "Grenada 1953";
        datum = 6603;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Guadeloupe 1948” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4622</b></li>
     *   <li>EPSG CRS name: <b>Guadeloupe 1948</b></li>
     *   <li>Alias(es) given by EPSG: <b>Sainte Anne</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6622</b></li>
     *   <li>EPSG Usage Extent: <b>Guadeloupe - Grande-Terre and surrounding islands - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Guadeloupe 1948")
    public void EPSG_4622() throws FactoryException {
        code  = 4622;
        name  = "Guadeloupe 1948";
        datum = 6622;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Guam 1963” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4675</b></li>
     *   <li>EPSG CRS name: <b>Guam 1963</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6675</b></li>
     *   <li>EPSG Usage Extent: <b>Pacific - Guam and NMI - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Guam 1963")
    public void EPSG_4675() throws FactoryException {
        code  = 4675;
        name  = "Guam 1963";
        datum = 6675;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Gulshan 303” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4682</b></li>
     *   <li>EPSG CRS name: <b>Gulshan 303</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6682</b></li>
     *   <li>EPSG Usage Extent: <b>Bangladesh</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Gulshan 303")
    public void EPSG_4682() throws FactoryException {
        code  = 4682;
        name  = "Gulshan 303";
        datum = 6682;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Hanoi 1972” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4147</b></li>
     *   <li>EPSG CRS name: <b>Hanoi 1972</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6147</b></li>
     *   <li>EPSG Usage Extent: <b>Vietnam - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Hanoi 1972")
    public void EPSG_4147() throws FactoryException {
        code  = 4147;
        name  = "Hanoi 1972";
        datum = 6147;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Hartebeesthoek94” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4148</b></li>
     *   <li>EPSG CRS name: <b>Hartebeesthoek94</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6148</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - South Africa; Lesotho and Eswatini.</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Hartebeesthoek94")
    public void EPSG_4148() throws FactoryException {
        code  = 4148;
        name  = "Hartebeesthoek94";
        datum = 6148;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Hartebeesthoek94” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4941</b></li>
     *   <li>EPSG CRS name: <b>Hartebeesthoek94</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6148</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - South Africa; Lesotho and Eswatini.</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Hartebeesthoek94")
    public void EPSG_4941() throws FactoryException {
        code  = 4941;
        name  = "Hartebeesthoek94";
        datum = 6148;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “Hartebeesthoek94” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4940</b></li>
     *   <li>EPSG CRS name: <b>Hartebeesthoek94</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6148</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - South Africa; Lesotho and Eswatini.</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Hartebeesthoek94")
    public void EPSG_4940() throws FactoryException {
        code  = 4940;
        name  = "Hartebeesthoek94";
        isGeocentric = true;
        datum = 6148;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “HD1909” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>3819</b></li>
     *   <li>EPSG CRS name: <b>HD1909</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>1024</b></li>
     *   <li>EPSG Usage Extent: <b>Hungary</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("HD1909")
    public void EPSG_3819() throws FactoryException {
        code  = 3819;
        name  = "HD1909";
        datum = 1024;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “HD72” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4237</b></li>
     *   <li>EPSG CRS name: <b>HD72</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6237</b></li>
     *   <li>EPSG Usage Extent: <b>Hungary</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("HD72")
    public void EPSG_4237() throws FactoryException {
        code  = 4237;
        name  = "HD72";
        datum = 6237;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Helle 1954” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4660</b></li>
     *   <li>EPSG CRS name: <b>Helle 1954</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6660</b></li>
     *   <li>EPSG Usage Extent: <b>Jan Mayen - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Helle 1954")
    public void EPSG_4660() throws FactoryException {
        code  = 4660;
        name  = "Helle 1954";
        datum = 6660;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Herat North” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4255</b></li>
     *   <li>EPSG CRS name: <b>Herat North</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6255</b></li>
     *   <li>EPSG Usage Extent: <b>Afghanistan</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Herat North")
    public void EPSG_4255() throws FactoryException {
        code  = 4255;
        name  = "Herat North";
        datum = 6255;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Hito XVIII 1963” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4254</b></li>
     *   <li>EPSG CRS name: <b>Hito XVIII 1963</b></li>
     *   <li>Alias(es) given by EPSG: <b>Hito XVIII</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6254</b></li>
     *   <li>EPSG Usage Extent: <b>South America - Tierra del Fuego</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Hito XVIII 1963")
    public void EPSG_4254() throws FactoryException {
        code  = 4254;
        name  = "Hito XVIII 1963";
        datum = 6254;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Hjorsey 1955” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4658</b></li>
     *   <li>EPSG CRS name: <b>Hjorsey 1955</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6658</b></li>
     *   <li>EPSG Usage Extent: <b>Iceland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Hjorsey 1955")
    public void EPSG_4658() throws FactoryException {
        code  = 4658;
        name  = "Hjorsey 1955";
        datum = 6658;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Hong Kong 1963” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4738</b></li>
     *   <li>EPSG CRS name: <b>Hong Kong 1963</b></li>
     *   <li>Alias(es) given by EPSG: <b>HK63</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6738</b></li>
     *   <li>EPSG Usage Extent: <b>China - Hong Kong</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Hong Kong 1963")
    public void EPSG_4738() throws FactoryException {
        code  = 4738;
        name  = "Hong Kong 1963";
        datum = 6738;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Hong Kong 1963(67)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4739</b></li>
     *   <li>EPSG CRS name: <b>Hong Kong 1963(67)</b></li>
     *   <li>Alias(es) given by EPSG: <b>HK63(67)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6739</b></li>
     *   <li>EPSG Usage Extent: <b>China - Hong Kong</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Hong Kong 1963(67)")
    public void EPSG_4739() throws FactoryException {
        code  = 4739;
        name  = "Hong Kong 1963(67)";
        datum = 6739;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Hong Kong 1980” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4611</b></li>
     *   <li>EPSG CRS name: <b>Hong Kong 1980</b></li>
     *   <li>Alias(es) given by EPSG: <b>HK1980</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6611</b></li>
     *   <li>EPSG Usage Extent: <b>China - Hong Kong</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Hong Kong 1980")
    public void EPSG_4611() throws FactoryException {
        code  = 4611;
        name  = "Hong Kong 1980";
        datum = 6611;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “HTRS96” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4761</b></li>
     *   <li>EPSG CRS name: <b>HTRS96</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6761</b></li>
     *   <li>EPSG Usage Extent: <b>Croatia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("HTRS96")
    public void EPSG_4761() throws FactoryException {
        code  = 4761;
        name  = "HTRS96";
        datum = 6761;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “HTRS96” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4889</b></li>
     *   <li>EPSG CRS name: <b>HTRS96</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6761</b></li>
     *   <li>EPSG Usage Extent: <b>Croatia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("HTRS96")
    public void EPSG_4889() throws FactoryException {
        code  = 4889;
        name  = "HTRS96";
        datum = 6761;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “HTRS96” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4888</b></li>
     *   <li>EPSG CRS name: <b>HTRS96</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6761</b></li>
     *   <li>EPSG Usage Extent: <b>Croatia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("HTRS96")
    public void EPSG_4888() throws FactoryException {
        code  = 4888;
        name  = "HTRS96";
        isGeocentric = true;
        datum = 6761;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Hu Tzu Shan 1950” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4236</b></li>
     *   <li>EPSG CRS name: <b>Hu Tzu Shan 1950</b></li>
     *   <li>Alias(es) given by EPSG: <b>Hu Tzu Shan</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6236</b></li>
     *   <li>EPSG Usage Extent: <b>Taiwan - onshore - mainland and Penghu</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Hu Tzu Shan 1950")
    public void EPSG_4236() throws FactoryException {
        code  = 4236;
        name  = "Hu Tzu Shan 1950";
        datum = 6236;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “ID74” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4238</b></li>
     *   <li>EPSG CRS name: <b>ID74</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6238</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ID74")
    public void EPSG_4238() throws FactoryException {
        code  = 4238;
        name  = "ID74";
        datum = 6238;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “IGC 1962 6th Parallel South” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4697</b></li>
     *   <li>EPSG CRS name: <b>IGC 1962 6th Parallel South</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGC 1962</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6697</b></li>
     *   <li>EPSG Usage Extent: <b>Congo DR (Zaire) - 6th parallel south</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("IGC 1962 6th Parallel South")
    public void EPSG_4697() throws FactoryException {
        code  = 4697;
        name  = "IGC 1962 6th Parallel South";
        datum = 6697;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “IGCB 1955” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4701</b></li>
     *   <li>EPSG CRS name: <b>IGCB 1955</b></li>
     *   <li>Alias(es) given by EPSG: <b>Bas Congo 1955</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6701</b></li>
     *   <li>EPSG Usage Extent: <b>Congo DR (Zaire) - Bas Congo</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("IGCB 1955")
    public void EPSG_4701() throws FactoryException {
        code  = 4701;
        name  = "IGCB 1955";
        datum = 6701;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “IGM95” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4670</b></li>
     *   <li>EPSG CRS name: <b>IGM95</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6670</b></li>
     *   <li>EPSG Usage Extent: <b>Italy - including San Marino and Vatican</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("IGM95")
    public void EPSG_4670() throws FactoryException {
        code  = 4670;
        name  = "IGM95";
        datum = 6670;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “IGM95” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4983</b></li>
     *   <li>EPSG CRS name: <b>IGM95</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6670</b></li>
     *   <li>EPSG Usage Extent: <b>Italy - including San Marino and Vatican</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("IGM95")
    public void EPSG_4983() throws FactoryException {
        code  = 4983;
        name  = "IGM95";
        datum = 6670;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “IGM95” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4982</b></li>
     *   <li>EPSG CRS name: <b>IGM95</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6670</b></li>
     *   <li>EPSG Usage Extent: <b>Italy - including San Marino and Vatican</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("IGM95")
    public void EPSG_4982() throws FactoryException {
        code  = 4982;
        name  = "IGM95";
        isGeocentric = true;
        datum = 6670;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “IGN 1962 Kerguelen” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4698</b></li>
     *   <li>EPSG CRS name: <b>IGN 1962 Kerguelen</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6698</b></li>
     *   <li>EPSG Usage Extent: <b>French Southern Territories - Kerguelen onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("IGN 1962 Kerguelen")
    public void EPSG_4698() throws FactoryException {
        code  = 4698;
        name  = "IGN 1962 Kerguelen";
        datum = 6698;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “IGN53 Mare” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4641</b></li>
     *   <li>EPSG CRS name: <b>IGN53 Mare</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6641</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia - Mare</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("IGN53 Mare")
    public void EPSG_4641() throws FactoryException {
        code  = 4641;
        name  = "IGN53 Mare";
        datum = 6641;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “IGN56 Lifou” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4633</b></li>
     *   <li>EPSG CRS name: <b>IGN56 Lifou</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6633</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia - Lifou</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("IGN56 Lifou")
    public void EPSG_4633() throws FactoryException {
        code  = 4633;
        name  = "IGN56 Lifou";
        datum = 6633;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “IGN63 Hiva Oa” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4689</b></li>
     *   <li>EPSG CRS name: <b>IGN63 Hiva Oa</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6689</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia - Marquesas Islands - Hiva Oa and Tahuata</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("IGN63 Hiva Oa")
    public void EPSG_4689() throws FactoryException {
        code  = 4689;
        name  = "IGN63 Hiva Oa";
        datum = 6689;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “IGN72 Grande Terre” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4662</b></li>
     *   <li>EPSG CRS name: <b>IGN72 Grande Terre</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6634</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia - Grande Terre</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("IGN72 Grande Terre")
    public void EPSG_4662() throws FactoryException {
        code  = 4662;
        name  = "IGN72 Grande Terre";
        datum = 6634;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “IGN72 Nuku Hiva” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4630</b></li>
     *   <li>EPSG CRS name: <b>IGN72 Nuku Hiva</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6630</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia - Marquesas Islands - Nuku Hiva; Ua Huka and Ua Pou</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("IGN72 Nuku Hiva")
    public void EPSG_4630() throws FactoryException {
        code  = 4630;
        name  = "IGN72 Nuku Hiva";
        datum = 6630;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “IGN Astro 1960” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4700</b></li>
     *   <li>EPSG CRS name: <b>IGN Astro 1960</b></li>
     *   <li>Alias(es) given by EPSG: <b>Mauritanian Mining Cadastre 1999</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6700</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritania - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("IGN Astro 1960")
    public void EPSG_4700() throws FactoryException {
        code  = 4700;
        name  = "IGN Astro 1960";
        datum = 6700;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “IKBD-92” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4667</b></li>
     *   <li>EPSG CRS name: <b>IKBD-92</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6667</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Iraq-Kuwait boundary</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("IKBD-92")
    public void EPSG_4667() throws FactoryException {
        code  = 4667;
        name  = "IKBD-92";
        datum = 6667;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Indian 1954” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4239</b></li>
     *   <li>EPSG CRS name: <b>Indian 1954</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6239</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Myanmar and Thailand onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Indian 1954")
    public void EPSG_4239() throws FactoryException {
        code  = 4239;
        name  = "Indian 1954";
        datum = 6239;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Indian 1960” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4131</b></li>
     *   <li>EPSG CRS name: <b>Indian 1960</b></li>
     *   <li>Alias(es) given by EPSG: <b>Indian (DMA Reduced)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6131</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Cambodia and Vietnam - onshore &amp; Cuu Long basin</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Indian 1960")
    public void EPSG_4131() throws FactoryException {
        code  = 4131;
        name  = "Indian 1960";
        datum = 6131;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Indian 1975” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4240</b></li>
     *   <li>EPSG CRS name: <b>Indian 1975</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6240</b></li>
     *   <li>EPSG Usage Extent: <b>Thailand - onshore and Gulf of Thailand</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Indian 1975")
    public void EPSG_4240() throws FactoryException {
        code  = 4240;
        name  = "Indian 1975";
        datum = 6240;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “IRENET95” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4173</b></li>
     *   <li>EPSG CRS name: <b>IRENET95</b></li>
     *   <li>Alias(es) given by EPSG: <b>ETRS89</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6173</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Ireland (Republic and Ulster) - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("IRENET95")
    public void EPSG_4173() throws FactoryException {
        code  = 4173;
        name  = "IRENET95";
        datum = 6173;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “IRENET96” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4943</b></li>
     *   <li>EPSG CRS name: <b>IRENET96</b></li>
     *   <li>Alias(es) given by EPSG: <b>ETRS89</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6173</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Ireland (Republic and Ulster) - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("IRENET96")
    public void EPSG_4943() throws FactoryException {
        code  = 4943;
        name  = "IRENET96";
        datum = 6173;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “IRENET97” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4942</b></li>
     *   <li>EPSG CRS name: <b>IRENET97</b></li>
     *   <li>Alias(es) given by EPSG: <b>ETRS89</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6173</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Ireland (Republic and Ulster) - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("IRENET97")
    public void EPSG_4942() throws FactoryException {
        code  = 4942;
        name  = "IRENET97";
        isGeocentric = true;
        datum = 6173;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “ISN93” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4659</b></li>
     *   <li>EPSG CRS name: <b>ISN93</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6659</b></li>
     *   <li>EPSG Usage Extent: <b>Iceland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ISN93")
    public void EPSG_4659() throws FactoryException {
        code  = 4659;
        name  = "ISN93";
        datum = 6659;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “ISN93” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4945</b></li>
     *   <li>EPSG CRS name: <b>ISN93</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6659</b></li>
     *   <li>EPSG Usage Extent: <b>Iceland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ISN93")
    public void EPSG_4945() throws FactoryException {
        code  = 4945;
        name  = "ISN93";
        datum = 6659;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “ISN93” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4944</b></li>
     *   <li>EPSG CRS name: <b>ISN93</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6659</b></li>
     *   <li>EPSG Usage Extent: <b>Iceland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ISN93")
    public void EPSG_4944() throws FactoryException {
        code  = 4944;
        name  = "ISN93";
        isGeocentric = true;
        datum = 6659;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Israel 1993” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4141</b></li>
     *   <li>EPSG CRS name: <b>Israel 1993</b></li>
     *   <li>Alias(es) given by EPSG: <b>Israel</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6141</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Israel and Palestine Territory onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Israel 1993")
    public void EPSG_4141() throws FactoryException {
        code  = 4141;
        name  = "Israel 1993";
        datum = 6141;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Iwo Jima 1945” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4709</b></li>
     *   <li>EPSG CRS name: <b>Iwo Jima 1945</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6709</b></li>
     *   <li>EPSG Usage Extent: <b>Japan - Iwo Jima</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Iwo Jima 1945")
    public void EPSG_4709() throws FactoryException {
        code  = 4709;
        name  = "Iwo Jima 1945";
        datum = 6709;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “JAD2001” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4758</b></li>
     *   <li>EPSG CRS name: <b>JAD2001</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6758</b></li>
     *   <li>EPSG Usage Extent: <b>Jamaica</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("JAD2001")
    public void EPSG_4758() throws FactoryException {
        code  = 4758;
        name  = "JAD2001";
        datum = 6758;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “JAD2001” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4895</b></li>
     *   <li>EPSG CRS name: <b>JAD2001</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6758</b></li>
     *   <li>EPSG Usage Extent: <b>Jamaica</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("JAD2001")
    public void EPSG_4895() throws FactoryException {
        code  = 4895;
        name  = "JAD2001";
        datum = 6758;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “JAD2001” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4894</b></li>
     *   <li>EPSG CRS name: <b>JAD2001</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6758</b></li>
     *   <li>EPSG Usage Extent: <b>Jamaica</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("JAD2001")
    public void EPSG_4894() throws FactoryException {
        code  = 4894;
        name  = "JAD2001";
        isGeocentric = true;
        datum = 6758;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “JAD69” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4242</b></li>
     *   <li>EPSG CRS name: <b>JAD69</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6242</b></li>
     *   <li>EPSG Usage Extent: <b>Jamaica - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("JAD69")
    public void EPSG_4242() throws FactoryException {
        code  = 4242;
        name  = "JAD69";
        datum = 6242;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Jamaica 1875” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4241</b></li>
     *   <li>EPSG CRS name: <b>Jamaica 1875</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6241</b></li>
     *   <li>EPSG Usage Extent: <b>Jamaica - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Jamaica 1875")
    public void EPSG_4241() throws FactoryException {
        code  = 4241;
        name  = "Jamaica 1875";
        datum = 6241;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “JGD2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4612</b></li>
     *   <li>EPSG CRS name: <b>JGD2000</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6612</b></li>
     *   <li>EPSG Usage Extent: <b>Japan</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("JGD2000")
    public void EPSG_4612() throws FactoryException {
        code  = 4612;
        name  = "JGD2000";
        datum = 6612;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “JGD2001” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4947</b></li>
     *   <li>EPSG CRS name: <b>JGD2001</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6612</b></li>
     *   <li>EPSG Usage Extent: <b>Japan</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("JGD2001")
    public void EPSG_4947() throws FactoryException {
        code  = 4947;
        name  = "JGD2001";
        datum = 6612;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “JGD2002” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4946</b></li>
     *   <li>EPSG CRS name: <b>JGD2002</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6612</b></li>
     *   <li>EPSG Usage Extent: <b>Japan</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("JGD2002")
    public void EPSG_4946() throws FactoryException {
        code  = 4946;
        name  = "JGD2002";
        isGeocentric = true;
        datum = 6612;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Johnston Island 1961” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4725</b></li>
     *   <li>EPSG CRS name: <b>Johnston Island 1961</b></li>
     *   <li>Alias(es) given by EPSG: <b>Johnston Atoll 1961</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6725</b></li>
     *   <li>EPSG Usage Extent: <b>Johnston Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Johnston Island 1961")
    public void EPSG_4725() throws FactoryException {
        code  = 4725;
        name  = "Johnston Island 1961";
        datum = 6725;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Jouik 1961” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4679</b></li>
     *   <li>EPSG CRS name: <b>Jouik 1961</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6679</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritania - north coast</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Jouik 1961")
    public void EPSG_4679() throws FactoryException {
        code  = 4679;
        name  = "Jouik 1961";
        datum = 6679;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Kalianpur 1880” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4243</b></li>
     *   <li>EPSG CRS name: <b>Kalianpur 1880</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6243</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Bangladesh; India; Myanmar; Pakistan - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1880")
    public void EPSG_4243() throws FactoryException {
        code  = 4243;
        name  = "Kalianpur 1880";
        datum = 6243;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Kalianpur 1937” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4144</b></li>
     *   <li>EPSG CRS name: <b>Kalianpur 1937</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6144</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Bangladesh; India; Myanmar; Pakistan - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1937")
    public void EPSG_4144() throws FactoryException {
        code  = 4144;
        name  = "Kalianpur 1937";
        datum = 6144;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Kalianpur 1962” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4145</b></li>
     *   <li>EPSG CRS name: <b>Kalianpur 1962</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6145</b></li>
     *   <li>EPSG Usage Extent: <b>Pakistan</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1962")
    public void EPSG_4145() throws FactoryException {
        code  = 4145;
        name  = "Kalianpur 1962";
        datum = 6145;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Kalianpur 1975” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4146</b></li>
     *   <li>EPSG CRS name: <b>Kalianpur 1975</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6146</b></li>
     *   <li>EPSG Usage Extent: <b>India - mainland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kalianpur 1975")
    public void EPSG_4146() throws FactoryException {
        code  = 4146;
        name  = "Kalianpur 1975";
        datum = 6146;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Kandawala” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4244</b></li>
     *   <li>EPSG CRS name: <b>Kandawala</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6244</b></li>
     *   <li>EPSG Usage Extent: <b>Sri Lanka - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kandawala")
    public void EPSG_4244() throws FactoryException {
        code  = 4244;
        name  = "Kandawala";
        datum = 6244;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Karbala 1979” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4743</b></li>
     *   <li>EPSG CRS name: <b>Karbala 1979</b></li>
     *   <li>Alias(es) given by EPSG: <b>Karbala 1979 (Polservice)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6743</b></li>
     *   <li>EPSG Usage Extent: <b>Iraq - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Karbala 1979")
    public void EPSG_4743() throws FactoryException {
        code  = 4743;
        name  = "Karbala 1979";
        datum = 6743;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Kasai 1953” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4696</b></li>
     *   <li>EPSG CRS name: <b>Kasai 1953</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6696</b></li>
     *   <li>EPSG Usage Extent: <b>Congo DR (Zaire) - Kasai - SE</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kasai 1953")
    public void EPSG_4696() throws FactoryException {
        code  = 4696;
        name  = "Kasai 1953";
        datum = 6696;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Katanga 1955” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4695</b></li>
     *   <li>EPSG CRS name: <b>Katanga 1955</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6695</b></li>
     *   <li>EPSG Usage Extent: <b>Congo DR (Zaire) - Katanga</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Katanga 1955")
    public void EPSG_4695() throws FactoryException {
        code  = 4695;
        name  = "Katanga 1955";
        datum = 6695;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Kertau (RSO)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4751</b></li>
     *   <li>EPSG CRS name: <b>Kertau (RSO)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6751</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Malaysia (west) and Singapore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kertau (RSO)")
    public void EPSG_4751() throws FactoryException {
        code  = 4751;
        name  = "Kertau (RSO)";
        datum = 6751;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Kertau 1968” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4245</b></li>
     *   <li>EPSG CRS name: <b>Kertau 1968</b></li>
     *   <li>Alias(es) given by EPSG: <b>MRT68</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6245</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Malaysia (west including SCS) and Singapore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kertau 1968")
    public void EPSG_4245() throws FactoryException {
        code  = 4245;
        name  = "Kertau 1968";
        datum = 6245;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “KKJ” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4123</b></li>
     *   <li>EPSG CRS name: <b>KKJ</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6123</b></li>
     *   <li>EPSG Usage Extent: <b>Finland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("KKJ")
    public void EPSG_4123() throws FactoryException {
        code  = 4123;
        name  = "KKJ";
        datum = 6123;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “KOC” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4246</b></li>
     *   <li>EPSG CRS name: <b>KOC</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6246</b></li>
     *   <li>EPSG Usage Extent: <b>Kuwait - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("KOC")
    public void EPSG_4246() throws FactoryException {
        code  = 4246;
        name  = "KOC";
        datum = 6246;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Korea 2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4737</b></li>
     *   <li>EPSG CRS name: <b>Korea 2000</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6737</b></li>
     *   <li>EPSG Usage Extent: <b>Korea; Republic of (South Korea)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Korea 2000")
    public void EPSG_4737() throws FactoryException {
        code  = 4737;
        name  = "Korea 2000";
        datum = 6737;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Korea 2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4927</b></li>
     *   <li>EPSG CRS name: <b>Korea 2000</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6737</b></li>
     *   <li>EPSG Usage Extent: <b>Korea; Republic of (South Korea)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Korea 2000")
    public void EPSG_4927() throws FactoryException {
        code  = 4927;
        name  = "Korea 2000";
        datum = 6737;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “Korea 2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4926</b></li>
     *   <li>EPSG CRS name: <b>Korea 2000</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6737</b></li>
     *   <li>EPSG Usage Extent: <b>Korea; Republic of (South Korea)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Korea 2000")
    public void EPSG_4926() throws FactoryException {
        code  = 4926;
        name  = "Korea 2000";
        isGeocentric = true;
        datum = 6737;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Korean 1985” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4162</b></li>
     *   <li>EPSG CRS name: <b>Korean 1985</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6162</b></li>
     *   <li>EPSG Usage Extent: <b>Korea; Republic of (South Korea) - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Korean 1985")
    public void EPSG_4162() throws FactoryException {
        code  = 4162;
        name  = "Korean 1985";
        datum = 6162;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Korean 1995” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4166</b></li>
     *   <li>EPSG CRS name: <b>Korean 1995</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6166</b></li>
     *   <li>EPSG Usage Extent: <b>Korea; Republic of (South Korea) - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Korean 1995")
    public void EPSG_4166() throws FactoryException {
        code  = 4166;
        name  = "Korean 1995";
        datum = 6166;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Kousseri” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4198</b></li>
     *   <li>EPSG CRS name: <b>Kousseri</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6198</b></li>
     *   <li>EPSG Usage Extent: <b>Cameroon - N'Djamena area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kousseri")
    public void EPSG_4198() throws FactoryException {
        code  = 4198;
        name  = "Kousseri";
        datum = 6198;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “KUDAMS” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4319</b></li>
     *   <li>EPSG CRS name: <b>KUDAMS</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6319</b></li>
     *   <li>EPSG Usage Extent: <b>Kuwait - Kuwait City</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("KUDAMS")
    public void EPSG_4319() throws FactoryException {
        code  = 4319;
        name  = "KUDAMS";
        datum = 6319;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Kusaie 1951” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4735</b></li>
     *   <li>EPSG CRS name: <b>Kusaie 1951</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6735</b></li>
     *   <li>EPSG Usage Extent: <b>Micronesia - Kosrae (Kusaie)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Kusaie 1951")
    public void EPSG_4735() throws FactoryException {
        code  = 4735;
        name  = "Kusaie 1951";
        datum = 6735;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “La Canoa” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4247</b></li>
     *   <li>EPSG CRS name: <b>La Canoa</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6247</b></li>
     *   <li>EPSG Usage Extent: <b>Venezuela - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("La Canoa")
    public void EPSG_4247() throws FactoryException {
        code  = 4247;
        name  = "La Canoa";
        datum = 6247;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Lake” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4249</b></li>
     *   <li>EPSG CRS name: <b>Lake</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6249</b></li>
     *   <li>EPSG Usage Extent: <b>Venezuela - Lake Maracaibo</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Lake")
    public void EPSG_4249() throws FactoryException {
        code  = 4249;
        name  = "Lake";
        datum = 6249;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Lao 1993” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4677</b></li>
     *   <li>EPSG CRS name: <b>Lao 1993</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6677</b></li>
     *   <li>EPSG Usage Extent: <b>Laos</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Lao 1993")
    public void EPSG_4677() throws FactoryException {
        code  = 4677;
        name  = "Lao 1993";
        datum = 6677;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Lao 1993” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4991</b></li>
     *   <li>EPSG CRS name: <b>Lao 1993</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6677</b></li>
     *   <li>EPSG Usage Extent: <b>Laos</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Lao 1993")
    public void EPSG_4991() throws FactoryException {
        code  = 4991;
        name  = "Lao 1993";
        datum = 6677;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “Lao 1993” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4990</b></li>
     *   <li>EPSG CRS name: <b>Lao 1993</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6677</b></li>
     *   <li>EPSG Usage Extent: <b>Laos</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Lao 1993")
    public void EPSG_4990() throws FactoryException {
        code  = 4990;
        name  = "Lao 1993";
        isGeocentric = true;
        datum = 6677;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Lao 1997” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4678</b></li>
     *   <li>EPSG CRS name: <b>Lao 1997</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6678</b></li>
     *   <li>EPSG Usage Extent: <b>Laos</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Lao 1997")
    public void EPSG_4678() throws FactoryException {
        code  = 4678;
        name  = "Lao 1997";
        datum = 6678;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Lao 1997” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4993</b></li>
     *   <li>EPSG CRS name: <b>Lao 1997</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6678</b></li>
     *   <li>EPSG Usage Extent: <b>Laos</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Lao 1997")
    public void EPSG_4993() throws FactoryException {
        code  = 4993;
        name  = "Lao 1997";
        datum = 6678;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “Lao 1997” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4992</b></li>
     *   <li>EPSG CRS name: <b>Lao 1997</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6678</b></li>
     *   <li>EPSG Usage Extent: <b>Laos</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Lao 1997")
    public void EPSG_4992() throws FactoryException {
        code  = 4992;
        name  = "Lao 1997";
        isGeocentric = true;
        datum = 6678;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Leigon” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4250</b></li>
     *   <li>EPSG CRS name: <b>Leigon</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6250</b></li>
     *   <li>EPSG Usage Extent: <b>Ghana</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Leigon")
    public void EPSG_4250() throws FactoryException {
        code  = 4250;
        name  = "Leigon";
        datum = 6250;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Le Pouce 1934” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4699</b></li>
     *   <li>EPSG CRS name: <b>Le Pouce 1934</b></li>
     *   <li>Alias(es) given by EPSG: <b>Le Pouce (Mauritius 94)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6699</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritius - mainland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Le Pouce 1934")
    public void EPSG_4699() throws FactoryException {
        code  = 4699;
        name  = "Le Pouce 1934";
        datum = 6699;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “LGD2006” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4754</b></li>
     *   <li>EPSG CRS name: <b>LGD2006</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6754</b></li>
     *   <li>EPSG Usage Extent: <b>Libya</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("LGD2006")
    public void EPSG_4754() throws FactoryException {
        code  = 4754;
        name  = "LGD2006";
        datum = 6754;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “LGD2006” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4900</b></li>
     *   <li>EPSG CRS name: <b>LGD2006</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6754</b></li>
     *   <li>EPSG Usage Extent: <b>Libya</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("LGD2006")
    public void EPSG_4900() throws FactoryException {
        code  = 4900;
        name  = "LGD2006";
        datum = 6754;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “LGD2006” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4899</b></li>
     *   <li>EPSG CRS name: <b>LGD2006</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6754</b></li>
     *   <li>EPSG Usage Extent: <b>Libya</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("LGD2006")
    public void EPSG_4899() throws FactoryException {
        code  = 4899;
        name  = "LGD2006";
        isGeocentric = true;
        datum = 6754;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Liberia 1964” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4251</b></li>
     *   <li>EPSG CRS name: <b>Liberia 1964</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6251</b></li>
     *   <li>EPSG Usage Extent: <b>Liberia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Liberia 1964")
    public void EPSG_4251() throws FactoryException {
        code  = 4251;
        name  = "Liberia 1964";
        datum = 6251;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Lisbon” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4207</b></li>
     *   <li>EPSG CRS name: <b>Lisbon</b></li>
     *   <li>Alias(es) given by EPSG: <b>Lisbon 1937</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6207</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - mainland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Lisbon")
    public void EPSG_4207() throws FactoryException {
        code  = 4207;
        name  = "Lisbon";
        datum = 6207;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Lisbon (Lisbon)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4803</b></li>
     *   <li>EPSG CRS name: <b>Lisbon (Lisbon)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Lisbon 1937 (Lisbon)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6803</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - mainland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Lisbon (Lisbon)")
    public void EPSG_4803() throws FactoryException {
        code  = 4803;
        name  = "Lisbon (Lisbon)";
        datum = 6803;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Lisbon 1890” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4666</b></li>
     *   <li>EPSG CRS name: <b>Lisbon 1890</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6666</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - mainland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Lisbon 1890")
    public void EPSG_4666() throws FactoryException {
        code  = 4666;
        name  = "Lisbon 1890";
        datum = 6666;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Lisbon 1890 (Lisbon)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4904</b></li>
     *   <li>EPSG CRS name: <b>Lisbon 1890 (Lisbon)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6904</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - mainland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Lisbon 1890 (Lisbon)")
    public void EPSG_4904() throws FactoryException {
        code  = 4904;
        name  = "Lisbon 1890 (Lisbon)";
        datum = 6904;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “LKS92” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4661</b></li>
     *   <li>EPSG CRS name: <b>LKS92</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6661</b></li>
     *   <li>EPSG Usage Extent: <b>Latvia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("LKS92")
    public void EPSG_4661() throws FactoryException {
        code  = 4661;
        name  = "LKS92";
        datum = 6661;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “LKS92” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4949</b></li>
     *   <li>EPSG CRS name: <b>LKS92</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6661</b></li>
     *   <li>EPSG Usage Extent: <b>Latvia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("LKS92")
    public void EPSG_4949() throws FactoryException {
        code  = 4949;
        name  = "LKS92";
        datum = 6661;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “LKS92” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4948</b></li>
     *   <li>EPSG CRS name: <b>LKS92</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6661</b></li>
     *   <li>EPSG Usage Extent: <b>Latvia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("LKS92")
    public void EPSG_4948() throws FactoryException {
        code  = 4948;
        name  = "LKS92";
        isGeocentric = true;
        datum = 6661;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “LKS94” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4669</b></li>
     *   <li>EPSG CRS name: <b>LKS94</b></li>
     *   <li>Alias(es) given by EPSG: <b>LKS94 (ETRS89)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6126</b></li>
     *   <li>EPSG Usage Extent: <b>Lithuania</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("LKS94")
    public void EPSG_4669() throws FactoryException {
        code  = 4669;
        name  = "LKS94";
        datum = 6126;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “LKS95” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4951</b></li>
     *   <li>EPSG CRS name: <b>LKS95</b></li>
     *   <li>Alias(es) given by EPSG: <b>LKS94 (ETRS89)</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6126</b></li>
     *   <li>EPSG Usage Extent: <b>Lithuania</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("LKS95")
    public void EPSG_4951() throws FactoryException {
        code  = 4951;
        name  = "LKS95";
        datum = 6126;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “LKS96” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4950</b></li>
     *   <li>EPSG CRS name: <b>LKS96</b></li>
     *   <li>Alias(es) given by EPSG: <b>LKS94 (ETRS89)</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6126</b></li>
     *   <li>EPSG Usage Extent: <b>Lithuania</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("LKS96")
    public void EPSG_4950() throws FactoryException {
        code  = 4950;
        name  = "LKS96";
        isGeocentric = true;
        datum = 6126;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Locodjo 1965” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4142</b></li>
     *   <li>EPSG CRS name: <b>Locodjo 1965</b></li>
     *   <li>Alias(es) given by EPSG: <b>Port Bouet</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6142</b></li>
     *   <li>EPSG Usage Extent: <b>Cote d'Ivoire (Ivory Coast)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Locodjo 1965")
    public void EPSG_4142() throws FactoryException {
        code  = 4142;
        name  = "Locodjo 1965";
        datum = 6142;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Loma Quintana” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4288</b></li>
     *   <li>EPSG CRS name: <b>Loma Quintana</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6288</b></li>
     *   <li>EPSG Usage Extent: <b>Venezuela - north of 7°45'N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Loma Quintana")
    public void EPSG_4288() throws FactoryException {
        code  = 4288;
        name  = "Loma Quintana";
        datum = 6288;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Lome” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4252</b></li>
     *   <li>EPSG CRS name: <b>Lome</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6252</b></li>
     *   <li>EPSG Usage Extent: <b>Togo</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Lome")
    public void EPSG_4252() throws FactoryException {
        code  = 4252;
        name  = "Lome";
        datum = 6252;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Luxembourg 1930” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4181</b></li>
     *   <li>EPSG CRS name: <b>Luxembourg 1930</b></li>
     *   <li>Alias(es) given by EPSG: <b>LUREF</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6181</b></li>
     *   <li>EPSG Usage Extent: <b>Luxembourg</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Luxembourg 1930")
    public void EPSG_4181() throws FactoryException {
        code  = 4181;
        name  = "Luxembourg 1930";
        datum = 6181;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Luzon 1911” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4253</b></li>
     *   <li>EPSG CRS name: <b>Luzon 1911</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6253</b></li>
     *   <li>EPSG Usage Extent: <b>Philippines - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Luzon 1911")
    public void EPSG_4253() throws FactoryException {
        code  = 4253;
        name  = "Luzon 1911";
        datum = 6253;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “M'poraloko” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4266</b></li>
     *   <li>EPSG CRS name: <b>M'poraloko</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6266</b></li>
     *   <li>EPSG Usage Extent: <b>Gabon</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("M'poraloko")
    public void EPSG_4266() throws FactoryException {
        code  = 4266;
        name  = "M'poraloko";
        datum = 6266;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Madrid 1870 (Madrid)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4903</b></li>
     *   <li>EPSG CRS name: <b>Madrid 1870 (Madrid)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Madrid</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6903</b></li>
     *   <li>EPSG Usage Extent: <b>Spain - mainland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Madrid 1870 (Madrid)")
    public void EPSG_4903() throws FactoryException {
        code  = 4903;
        name  = "Madrid 1870 (Madrid)";
        datum = 6903;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Madzansua” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4128</b></li>
     *   <li>EPSG CRS name: <b>Madzansua</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6128</b></li>
     *   <li>EPSG Usage Extent: <b>Mozambique - west - Tete province</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Madzansua")
    public void EPSG_4128() throws FactoryException {
        code  = 4128;
        name  = "Madzansua";
        datum = 6128;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “MAGNA-SIRGAS” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4686</b></li>
     *   <li>EPSG CRS name: <b>MAGNA-SIRGAS</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6686</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("MAGNA-SIRGAS")
    public void EPSG_4686() throws FactoryException {
        code  = 4686;
        name  = "MAGNA-SIRGAS";
        datum = 6686;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “MAGNA-SIRGAS” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4997</b></li>
     *   <li>EPSG CRS name: <b>MAGNA-SIRGAS</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6686</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("MAGNA-SIRGAS")
    public void EPSG_4997() throws FactoryException {
        code  = 4997;
        name  = "MAGNA-SIRGAS";
        datum = 6686;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “MAGNA-SIRGAS” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4996</b></li>
     *   <li>EPSG CRS name: <b>MAGNA-SIRGAS</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6686</b></li>
     *   <li>EPSG Usage Extent: <b>Colombia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("MAGNA-SIRGAS")
    public void EPSG_4996() throws FactoryException {
        code  = 4996;
        name  = "MAGNA-SIRGAS";
        isGeocentric = true;
        datum = 6686;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Mahe 1971” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4256</b></li>
     *   <li>EPSG CRS name: <b>Mahe 1971</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6256</b></li>
     *   <li>EPSG Usage Extent: <b>Seychelles - Mahe Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Mahe 1971")
    public void EPSG_4256() throws FactoryException {
        code  = 4256;
        name  = "Mahe 1971";
        datum = 6256;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Makassar” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4257</b></li>
     *   <li>EPSG CRS name: <b>Makassar</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6257</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Sulawesi SW</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Makassar")
    public void EPSG_4257() throws FactoryException {
        code  = 4257;
        name  = "Makassar";
        datum = 6257;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Makassar (Jakarta)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4804</b></li>
     *   <li>EPSG CRS name: <b>Makassar (Jakarta)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6804</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Sulawesi SW</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Makassar (Jakarta)")
    public void EPSG_4804() throws FactoryException {
        code  = 4804;
        name  = "Makassar (Jakarta)";
        datum = 6804;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Malongo 1987” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4259</b></li>
     *   <li>EPSG CRS name: <b>Malongo 1987</b></li>
     *   <li>Alias(es) given by EPSG: <b>Mhast</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6259</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Angola (Cabinda) and DR Congo (Zaire) - offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Malongo 1987")
    public void EPSG_4259() throws FactoryException {
        code  = 4259;
        name  = "Malongo 1987";
        datum = 6259;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Manoca 1962” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4193</b></li>
     *   <li>EPSG CRS name: <b>Manoca 1962</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6193</b></li>
     *   <li>EPSG Usage Extent: <b>Cameroon - coastal area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Manoca 1962")
    public void EPSG_4193() throws FactoryException {
        code  = 4193;
        name  = "Manoca 1962";
        datum = 6193;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Marcus Island 1952” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4711</b></li>
     *   <li>EPSG CRS name: <b>Marcus Island 1952</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6711</b></li>
     *   <li>EPSG Usage Extent: <b>Japan - Minamitori-shima (Marcus Island) - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Marcus Island 1952")
    public void EPSG_4711() throws FactoryException {
        code  = 4711;
        name  = "Marcus Island 1952";
        datum = 6711;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Marshall Islands 1960” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4732</b></li>
     *   <li>EPSG CRS name: <b>Marshall Islands 1960</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6732</b></li>
     *   <li>EPSG Usage Extent: <b>Pacific - Marshall Islands; Wake - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Marshall Islands 1960")
    public void EPSG_4732() throws FactoryException {
        code  = 4732;
        name  = "Marshall Islands 1960";
        datum = 6732;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Martinique 1938” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4625</b></li>
     *   <li>EPSG CRS name: <b>Martinique 1938</b></li>
     *   <li>Alias(es) given by EPSG: <b>Fort Desaix</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6625</b></li>
     *   <li>EPSG Usage Extent: <b>Martinique - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Martinique 1938")
    public void EPSG_4625() throws FactoryException {
        code  = 4625;
        name  = "Martinique 1938";
        datum = 6625;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Massawa” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4262</b></li>
     *   <li>EPSG CRS name: <b>Massawa</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6262</b></li>
     *   <li>EPSG Usage Extent: <b>Eritrea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Massawa")
    public void EPSG_4262() throws FactoryException {
        code  = 4262;
        name  = "Massawa";
        datum = 6262;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Maupiti 83” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4692</b></li>
     *   <li>EPSG CRS name: <b>Maupiti 83</b></li>
     *   <li>Alias(es) given by EPSG: <b>MOP 1983</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6692</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia - Society Islands - Maupiti</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Maupiti 83")
    public void EPSG_4692() throws FactoryException {
        code  = 4692;
        name  = "Maupiti 83";
        datum = 6692;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Mauritania 1999” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4702</b></li>
     *   <li>EPSG CRS name: <b>Mauritania 1999</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6702</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritania</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Mauritania 1999")
    public void EPSG_4702() throws FactoryException {
        code  = 4702;
        name  = "Mauritania 1999";
        datum = 6702;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Mauritania 1999” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4925</b></li>
     *   <li>EPSG CRS name: <b>Mauritania 1999</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6702</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritania</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Mauritania 1999")
    public void EPSG_4925() throws FactoryException {
        code  = 4925;
        name  = "Mauritania 1999";
        datum = 6702;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “Mauritania 1999” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4924</b></li>
     *   <li>EPSG CRS name: <b>Mauritania 1999</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6702</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritania</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Mauritania 1999")
    public void EPSG_4924() throws FactoryException {
        code  = 4924;
        name  = "Mauritania 1999";
        isGeocentric = true;
        datum = 6702;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Merchich” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4261</b></li>
     *   <li>EPSG CRS name: <b>Merchich</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6261</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Morocco and Western Sahara - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Merchich")
    public void EPSG_4261() throws FactoryException {
        code  = 4261;
        name  = "Merchich";
        datum = 6261;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “MGI” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4312</b></li>
     *   <li>EPSG CRS name: <b>MGI</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6312</b></li>
     *   <li>EPSG Usage Extent: <b>Austria</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("MGI")
    public void EPSG_4312() throws FactoryException {
        code  = 4312;
        name  = "MGI";
        datum = 6312;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “MGI (Ferro)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4805</b></li>
     *   <li>EPSG CRS name: <b>MGI (Ferro)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6805</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Austria and former Yugoslavia onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("MGI (Ferro)")
    public void EPSG_4805() throws FactoryException {
        code  = 4805;
        name  = "MGI (Ferro)";
        datum = 6805;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Mhast (offshore)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4705</b></li>
     *   <li>EPSG CRS name: <b>Mhast (offshore)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Mhast</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6705</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Angola (Cabinda) and DR Congo (Zaire) - offshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Mhast (offshore)")
    public void EPSG_4705() throws FactoryException {
        code  = 4705;
        name  = "Mhast (offshore)";
        datum = 6705;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Mhast (onshore)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4704</b></li>
     *   <li>EPSG CRS name: <b>Mhast (onshore)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Mhast</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6704</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - Angola (Cabinda) and DR Congo (Zaire) - coastal</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Mhast (onshore)")
    public void EPSG_4704() throws FactoryException {
        code  = 4704;
        name  = "Mhast (onshore)";
        datum = 6704;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Mhast 1951” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4703</b></li>
     *   <li>EPSG CRS name: <b>Mhast 1951</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6703</b></li>
     *   <li>EPSG Usage Extent: <b>Angola - Cabinda</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Mhast 1951")
    public void EPSG_4703() throws FactoryException {
        code  = 4703;
        name  = "Mhast 1951";
        datum = 6703;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Midway 1961” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4727</b></li>
     *   <li>EPSG CRS name: <b>Midway 1961</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6727</b></li>
     *   <li>EPSG Usage Extent: <b>Midway Islands - Sand and Eastern Islands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Midway 1961")
    public void EPSG_4727() throws FactoryException {
        code  = 4727;
        name  = "Midway 1961";
        datum = 6727;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Minna” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4263</b></li>
     *   <li>EPSG CRS name: <b>Minna</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6263</b></li>
     *   <li>EPSG Usage Extent: <b>Nigeria</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Minna")
    public void EPSG_4263() throws FactoryException {
        code  = 4263;
        name  = "Minna";
        datum = 6263;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Monte Mario” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4265</b></li>
     *   <li>EPSG CRS name: <b>Monte Mario</b></li>
     *   <li>Alias(es) given by EPSG: <b>Rome 1940</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6265</b></li>
     *   <li>EPSG Usage Extent: <b>Italy - including San Marino and Vatican</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Monte Mario")
    public void EPSG_4265() throws FactoryException {
        code  = 4265;
        name  = "Monte Mario";
        datum = 6265;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Monte Mario (Rome)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4806</b></li>
     *   <li>EPSG CRS name: <b>Monte Mario (Rome)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Rome 1940 (Rome)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6806</b></li>
     *   <li>EPSG Usage Extent: <b>Italy - including San Marino and Vatican</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Monte Mario (Rome)")
    public void EPSG_4806() throws FactoryException {
        code  = 4806;
        name  = "Monte Mario (Rome)";
        datum = 6806;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Montserrat 1958” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4604</b></li>
     *   <li>EPSG CRS name: <b>Montserrat 1958</b></li>
     *   <li>Alias(es) given by EPSG: <b>Montserrat 58</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6604</b></li>
     *   <li>EPSG Usage Extent: <b>Montserrat - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Montserrat 1958")
    public void EPSG_4604() throws FactoryException {
        code  = 4604;
        name  = "Montserrat 1958";
        datum = 6604;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Moorea 87” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4691</b></li>
     *   <li>EPSG CRS name: <b>Moorea 87</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6691</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia - Society Islands - Moorea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Moorea 87")
    public void EPSG_4691() throws FactoryException {
        code  = 4691;
        name  = "Moorea 87";
        datum = 6691;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “MOP78” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4639</b></li>
     *   <li>EPSG CRS name: <b>MOP78</b></li>
     *   <li>Alias(es) given by EPSG: <b>Uvea SHOM 1978</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6639</b></li>
     *   <li>EPSG Usage Extent: <b>Wallis and Futuna - Wallis</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("MOP78")
    public void EPSG_4639() throws FactoryException {
        code  = 4639;
        name  = "MOP78";
        datum = 6639;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Mount Dillon” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4157</b></li>
     *   <li>EPSG CRS name: <b>Mount Dillon</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6157</b></li>
     *   <li>EPSG Usage Extent: <b>Trinidad and Tobago - Tobago - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Mount Dillon")
    public void EPSG_4157() throws FactoryException {
        code  = 4157;
        name  = "Mount Dillon";
        datum = 6157;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Moznet” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4130</b></li>
     *   <li>EPSG CRS name: <b>Moznet</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6130</b></li>
     *   <li>EPSG Usage Extent: <b>Mozambique</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Moznet")
    public void EPSG_4130() throws FactoryException {
        code  = 4130;
        name  = "Moznet";
        datum = 6130;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Moznet” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4953</b></li>
     *   <li>EPSG CRS name: <b>Moznet</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6130</b></li>
     *   <li>EPSG Usage Extent: <b>Mozambique</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Moznet")
    public void EPSG_4953() throws FactoryException {
        code  = 4953;
        name  = "Moznet";
        datum = 6130;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “Moznet” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4952</b></li>
     *   <li>EPSG CRS name: <b>Moznet</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6130</b></li>
     *   <li>EPSG Usage Extent: <b>Mozambique</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Moznet")
    public void EPSG_4952() throws FactoryException {
        code  = 4952;
        name  = "Moznet";
        isGeocentric = true;
        datum = 6130;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “NAD27” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4267</b></li>
     *   <li>EPSG CRS name: <b>NAD27</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6267</b></li>
     *   <li>EPSG Usage Extent: <b>North America - NAD27</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD27")
    public void EPSG_4267() throws FactoryException {
        code  = 4267;
        name  = "NAD27";
        datum = 6267;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “NAD27(76)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4608</b></li>
     *   <li>EPSG CRS name: <b>NAD27(76)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6608</b></li>
     *   <li>EPSG Usage Extent: <b>Canada - Ontario</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD27(76)")
    public void EPSG_4608() throws FactoryException {
        code  = 4608;
        name  = "NAD27(76)";
        datum = 6608;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “NAD27(CGQ77)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4609</b></li>
     *   <li>EPSG CRS name: <b>NAD27(CGQ77)</b></li>
     *   <li>Alias(es) given by EPSG: <b>CGQ77</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6609</b></li>
     *   <li>EPSG Usage Extent: <b>Canada - Quebec</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD27(CGQ77)")
    public void EPSG_4609() throws FactoryException {
        code  = 4609;
        name  = "NAD27(CGQ77)";
        datum = 6609;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “NAD83” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4269</b></li>
     *   <li>EPSG CRS name: <b>NAD83</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83(1986)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6269</b></li>
     *   <li>EPSG Usage Extent: <b>North America - NAD83</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83")
    public void EPSG_4269() throws FactoryException {
        code  = 4269;
        name  = "NAD83";
        datum = 6269;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “NAD83(CSRS)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4617</b></li>
     *   <li>EPSG CRS name: <b>NAD83(CSRS)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6140</b></li>
     *   <li>EPSG Usage Extent: <b>Canada</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83(CSRS)")
    public void EPSG_4617() throws FactoryException {
        code  = 4617;
        name  = "NAD83(CSRS)";
        datum = 6140;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “NAD83(CSRS)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4955</b></li>
     *   <li>EPSG CRS name: <b>NAD83(CSRS)</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6140</b></li>
     *   <li>EPSG Usage Extent: <b>Canada</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83(CSRS)")
    public void EPSG_4955() throws FactoryException {
        code  = 4955;
        name  = "NAD83(CSRS)";
        datum = 6140;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “NAD83(CSRS)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4954</b></li>
     *   <li>EPSG CRS name: <b>NAD83(CSRS)</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6140</b></li>
     *   <li>EPSG Usage Extent: <b>Canada</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83(CSRS)")
    public void EPSG_4954() throws FactoryException {
        code  = 4954;
        name  = "NAD83(CSRS)";
        isGeocentric = true;
        datum = 6140;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “NAD83(HARN)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4152</b></li>
     *   <li>EPSG CRS name: <b>NAD83(HARN)</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83(HPGN)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6152</b></li>
     *   <li>EPSG Usage Extent: <b>USA - HARN</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83(HARN)")
    public void EPSG_4152() throws FactoryException {
        code  = 4152;
        name  = "NAD83(HARN)";
        datum = 6152;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “NAD83(HARN)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4957</b></li>
     *   <li>EPSG CRS name: <b>NAD83(HARN)</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83(HPGN)</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6152</b></li>
     *   <li>EPSG Usage Extent: <b>USA - HARN</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83(HARN)")
    public void EPSG_4957() throws FactoryException {
        code  = 4957;
        name  = "NAD83(HARN)";
        datum = 6152;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “NAD83(HARN)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4956</b></li>
     *   <li>EPSG CRS name: <b>NAD83(HARN)</b></li>
     *   <li>Alias(es) given by EPSG: <b>NAD83(HPGN)</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6152</b></li>
     *   <li>EPSG Usage Extent: <b>USA - HARN</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83(HARN)")
    public void EPSG_4956() throws FactoryException {
        code  = 4956;
        name  = "NAD83(HARN)";
        isGeocentric = true;
        datum = 6152;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “NAD83(NSRS2007)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4759</b></li>
     *   <li>EPSG CRS name: <b>NAD83(NSRS2007)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6759</b></li>
     *   <li>EPSG Usage Extent: <b>USA - CONUS and Alaska; PRVI</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83(NSRS2007)")
    public void EPSG_4759() throws FactoryException {
        code  = 4759;
        name  = "NAD83(NSRS2007)";
        datum = 6759;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “NAD83(NSRS2007)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4893</b></li>
     *   <li>EPSG CRS name: <b>NAD83(NSRS2007)</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6759</b></li>
     *   <li>EPSG Usage Extent: <b>USA - CONUS and Alaska; PRVI</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83(NSRS2007)")
    public void EPSG_4893() throws FactoryException {
        code  = 4893;
        name  = "NAD83(NSRS2007)";
        datum = 6759;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “NAD83(NSRS2007)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4892</b></li>
     *   <li>EPSG CRS name: <b>NAD83(NSRS2007)</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6759</b></li>
     *   <li>EPSG Usage Extent: <b>USA - CONUS and Alaska; PRVI</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NAD83(NSRS2007)")
    public void EPSG_4892() throws FactoryException {
        code  = 4892;
        name  = "NAD83(NSRS2007)";
        isGeocentric = true;
        datum = 6759;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Nahrwan 1934” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4744</b></li>
     *   <li>EPSG CRS name: <b>Nahrwan 1934</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6744</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Iraq and SW Iran</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Nahrwan 1934")
    public void EPSG_4744() throws FactoryException {
        code  = 4744;
        name  = "Nahrwan 1934";
        datum = 6744;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Nahrwan 1967” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4270</b></li>
     *   <li>EPSG CRS name: <b>Nahrwan 1967</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6270</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Qatar offshore and UAE</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Nahrwan 1967")
    public void EPSG_4270() throws FactoryException {
        code  = 4270;
        name  = "Nahrwan 1967";
        datum = 6270;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Nakhl-e Ghanem” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4693</b></li>
     *   <li>EPSG CRS name: <b>Nakhl-e Ghanem</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6693</b></li>
     *   <li>EPSG Usage Extent: <b>Iran - Kangan district</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Nakhl-e Ghanem")
    public void EPSG_4693() throws FactoryException {
        code  = 4693;
        name  = "Nakhl-e Ghanem";
        datum = 6693;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Naparima 1955” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4158</b></li>
     *   <li>EPSG CRS name: <b>Naparima 1955</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6158</b></li>
     *   <li>EPSG Usage Extent: <b>Trinidad and Tobago - Trinidad - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Naparima 1955")
    public void EPSG_4158() throws FactoryException {
        code  = 4158;
        name  = "Naparima 1955";
        datum = 6158;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Naparima 1972” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4271</b></li>
     *   <li>EPSG CRS name: <b>Naparima 1972</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6271</b></li>
     *   <li>EPSG Usage Extent: <b>Trinidad and Tobago - Tobago - onshore</b></li>
     * </ul>
     *
     * Remarks: Often confused with Naparima 1955.
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Naparima 1972")
    public void EPSG_4271() throws FactoryException {
        code  = 4271;
        name  = "Naparima 1972";
        datum = 6271;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “NEA74 Noumea” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4644</b></li>
     *   <li>EPSG CRS name: <b>NEA74 Noumea</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6644</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia - Grande Terre - Noumea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NEA74 Noumea")
    public void EPSG_4644() throws FactoryException {
        code  = 4644;
        name  = "NEA74 Noumea";
        datum = 6644;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “NGN” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4318</b></li>
     *   <li>EPSG CRS name: <b>NGN</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6318</b></li>
     *   <li>EPSG Usage Extent: <b>Kuwait - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NGN")
    public void EPSG_4318() throws FactoryException {
        code  = 4318;
        name  = "NGN";
        datum = 6318;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “NGO 1948” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4273</b></li>
     *   <li>EPSG CRS name: <b>NGO 1948</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6273</b></li>
     *   <li>EPSG Usage Extent: <b>Norway - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NGO 1948")
    public void EPSG_4273() throws FactoryException {
        code  = 4273;
        name  = "NGO 1948";
        datum = 6273;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “NGO 1948 (Oslo)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4817</b></li>
     *   <li>EPSG CRS name: <b>NGO 1948 (Oslo)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6817</b></li>
     *   <li>EPSG Usage Extent: <b>Norway - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NGO 1948 (Oslo)")
    public void EPSG_4817() throws FactoryException {
        code  = 4817;
        name  = "NGO 1948 (Oslo)";
        datum = 6817;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Nord Sahara 1959” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4307</b></li>
     *   <li>EPSG CRS name: <b>Nord Sahara 1959</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6307</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Nord Sahara 1959")
    public void EPSG_4307() throws FactoryException {
        code  = 4307;
        name  = "Nord Sahara 1959";
        datum = 6307;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Nouakchott 1965” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4680</b></li>
     *   <li>EPSG CRS name: <b>Nouakchott 1965</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6680</b></li>
     *   <li>EPSG Usage Extent: <b>Mauritania - central coast</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Nouakchott 1965")
    public void EPSG_4680() throws FactoryException {
        code  = 4680;
        name  = "Nouakchott 1965";
        datum = 6680;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “NSWC 9Z-2” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4276</b></li>
     *   <li>EPSG CRS name: <b>NSWC 9Z-2</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6276</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NSWC 9Z-2")
    public void EPSG_4276() throws FactoryException {
        code  = 4276;
        name  = "NSWC 9Z-2";
        datum = 6276;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “NTF” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4275</b></li>
     *   <li>EPSG CRS name: <b>NTF</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6275</b></li>
     *   <li>EPSG Usage Extent: <b>France - onshore - mainland and Corsica</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NTF")
    public void EPSG_4275() throws FactoryException {
        code  = 4275;
        name  = "NTF";
        datum = 6275;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “NTF (Paris)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4807</b></li>
     *   <li>EPSG CRS name: <b>NTF (Paris)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6807</b></li>
     *   <li>EPSG Usage Extent: <b>France - onshore - mainland and Corsica</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NTF (Paris)")
    public void EPSG_4807() throws FactoryException {
        code  = 4807;
        name  = "NTF (Paris)";
        datum = 6807;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “NZGD2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4167</b></li>
     *   <li>EPSG CRS name: <b>NZGD2000</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6167</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NZGD2000")
    public void EPSG_4167() throws FactoryException {
        code  = 4167;
        name  = "NZGD2000";
        datum = 6167;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “NZGD2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4959</b></li>
     *   <li>EPSG CRS name: <b>NZGD2000</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6167</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NZGD2000")
    public void EPSG_4959() throws FactoryException {
        code  = 4959;
        name  = "NZGD2000";
        datum = 6167;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “NZGD2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4958</b></li>
     *   <li>EPSG CRS name: <b>NZGD2000</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6167</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NZGD2000")
    public void EPSG_4958() throws FactoryException {
        code  = 4958;
        name  = "NZGD2000";
        isGeocentric = true;
        datum = 6167;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “NZGD49” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4272</b></li>
     *   <li>EPSG CRS name: <b>NZGD49</b></li>
     *   <li>Alias(es) given by EPSG: <b>GD49</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6272</b></li>
     *   <li>EPSG Usage Extent: <b>New Zealand - onshore and nearshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("NZGD49")
    public void EPSG_4272() throws FactoryException {
        code  = 4272;
        name  = "NZGD49";
        datum = 6272;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Observatario” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4129</b></li>
     *   <li>EPSG CRS name: <b>Observatario</b></li>
     *   <li>Alias(es) given by EPSG: <b>Observatario Campos Rodrigues 1907</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6129</b></li>
     *   <li>EPSG Usage Extent: <b>Mozambique - south</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Observatario")
    public void EPSG_4129() throws FactoryException {
        code  = 4129;
        name  = "Observatario";
        datum = 6129;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Old Hawaiian” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4135</b></li>
     *   <li>EPSG CRS name: <b>Old Hawaiian</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6135</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Hawaii - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Old Hawaiian")
    public void EPSG_4135() throws FactoryException {
        code  = 4135;
        name  = "Old Hawaiian";
        datum = 6135;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “OS(SN)80” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4279</b></li>
     *   <li>EPSG CRS name: <b>OS(SN)80</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6279</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - British Isles - UK and Ireland onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("OS(SN)80")
    public void EPSG_4279() throws FactoryException {
        code  = 4279;
        name  = "OS(SN)80";
        datum = 6279;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “OSGB36” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4277</b></li>
     *   <li>EPSG CRS name: <b>OSGB36</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6277</b></li>
     *   <li>EPSG Usage Extent: <b>UK - Britain and UKCS 49°45'N to 61°N; 9°W to 2°E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("OSGB36")
    public void EPSG_4277() throws FactoryException {
        code  = 4277;
        name  = "OSGB36";
        datum = 6277;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “OSGB70” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4278</b></li>
     *   <li>EPSG CRS name: <b>OSGB70</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6278</b></li>
     *   <li>EPSG Usage Extent: <b>UK - Great Britain; Isle of Man</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("OSGB70")
    public void EPSG_4278() throws FactoryException {
        code  = 4278;
        name  = "OSGB70";
        datum = 6278;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “OSNI 1952” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4188</b></li>
     *   <li>EPSG CRS name: <b>OSNI 1952</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6188</b></li>
     *   <li>EPSG Usage Extent: <b>UK - Northern Ireland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("OSNI 1952")
    public void EPSG_4188() throws FactoryException {
        code  = 4188;
        name  = "OSNI 1952";
        datum = 6188;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Palestine 1923” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4281</b></li>
     *   <li>EPSG CRS name: <b>Palestine 1923</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6281</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Middle East - Israel; Jordan and Palestine onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Palestine 1923")
    public void EPSG_4281() throws FactoryException {
        code  = 4281;
        name  = "Palestine 1923";
        datum = 6281;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Pampa del Castillo” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4161</b></li>
     *   <li>EPSG CRS name: <b>Pampa del Castillo</b></li>
     *   <li>Alias(es) given by EPSG: <b>Pampa Cas</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6161</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina - 42.5°S to 50.3°S</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Pampa del Castillo")
    public void EPSG_4161() throws FactoryException {
        code  = 4161;
        name  = "Pampa del Castillo";
        datum = 6161;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “PD/83” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4746</b></li>
     *   <li>EPSG CRS name: <b>PD/83</b></li>
     *   <li>Alias(es) given by EPSG: <b>DHDN</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6746</b></li>
     *   <li>EPSG Usage Extent: <b>Germany - Thuringen</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("PD/83")
    public void EPSG_4746() throws FactoryException {
        code  = 4746;
        name  = "PD/83";
        datum = 6746;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Perroud 1950” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4637</b></li>
     *   <li>EPSG CRS name: <b>Perroud 1950</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6637</b></li>
     *   <li>EPSG Usage Extent: <b>Antarctica - Adelie Land coastal area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Perroud 1950")
    public void EPSG_4637() throws FactoryException {
        code  = 4637;
        name  = "Perroud 1950";
        datum = 6637;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Petrels 1972” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4636</b></li>
     *   <li>EPSG CRS name: <b>Petrels 1972</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6636</b></li>
     *   <li>EPSG Usage Extent: <b>Antarctica - Adelie Land - Petrels island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Petrels 1972")
    public void EPSG_4636() throws FactoryException {
        code  = 4636;
        name  = "Petrels 1972";
        datum = 6636;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Phoenix Islands 1966” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4716</b></li>
     *   <li>EPSG CRS name: <b>Phoenix Islands 1966</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6716</b></li>
     *   <li>EPSG Usage Extent: <b>Kiribati - Phoenix Islands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Phoenix Islands 1966")
    public void EPSG_4716() throws FactoryException {
        code  = 4716;
        name  = "Phoenix Islands 1966";
        datum = 6716;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Pitcairn 1967” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4729</b></li>
     *   <li>EPSG CRS name: <b>Pitcairn 1967</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6729</b></li>
     *   <li>EPSG Usage Extent: <b>Pitcairn - Pitcairn Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Pitcairn 1967")
    public void EPSG_4729() throws FactoryException {
        code  = 4729;
        name  = "Pitcairn 1967";
        datum = 6729;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Pitcairn 2006” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4763</b></li>
     *   <li>EPSG CRS name: <b>Pitcairn 2006</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6763</b></li>
     *   <li>EPSG Usage Extent: <b>Pitcairn - Pitcairn Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Pitcairn 2006")
    public void EPSG_4763() throws FactoryException {
        code  = 4763;
        name  = "Pitcairn 2006";
        datum = 6763;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “PN84” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4728</b></li>
     *   <li>EPSG CRS name: <b>PN84</b></li>
     *   <li>Alias(es) given by EPSG: <b>Pico de las Nieves 1984</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6728</b></li>
     *   <li>EPSG Usage Extent: <b>Spain - Canary Islands western</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("PN84")
    public void EPSG_4728() throws FactoryException {
        code  = 4728;
        name  = "PN84";
        datum = 6728;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Point 58” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4620</b></li>
     *   <li>EPSG CRS name: <b>Point 58</b></li>
     *   <li>Alias(es) given by EPSG: <b>12th Parallel traverse</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6620</b></li>
     *   <li>EPSG Usage Extent: <b>Africa - 12th parallel N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Point 58")
    public void EPSG_4620() throws FactoryException {
        code  = 4620;
        name  = "Point 58";
        datum = 6620;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Pointe Noire” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4282</b></li>
     *   <li>EPSG CRS name: <b>Pointe Noire</b></li>
     *   <li>Alias(es) given by EPSG: <b>Congo 1960 Pointe Noire</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6282</b></li>
     *   <li>EPSG Usage Extent: <b>Congo</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Pointe Noire")
    public void EPSG_4282() throws FactoryException {
        code  = 4282;
        name  = "Pointe Noire";
        datum = 6282;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Porto Santo” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4615</b></li>
     *   <li>EPSG CRS name: <b>Porto Santo</b></li>
     *   <li>Alias(es) given by EPSG: <b>Porto Santo 1936</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6615</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - Madeira archipelago onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Porto Santo")
    public void EPSG_4615() throws FactoryException {
        code  = 4615;
        name  = "Porto Santo";
        datum = 6615;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Porto Santo 1995” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4663</b></li>
     *   <li>EPSG CRS name: <b>Porto Santo 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>Base SE</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6663</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - Madeira archipelago onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Porto Santo 1995")
    public void EPSG_4663() throws FactoryException {
        code  = 4663;
        name  = "Porto Santo 1995";
        datum = 6663;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “POSGAR 94” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4694</b></li>
     *   <li>EPSG CRS name: <b>POSGAR 94</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6694</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 94")
    public void EPSG_4694() throws FactoryException {
        code  = 4694;
        name  = "POSGAR 94";
        datum = 6694;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “POSGAR 94” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4929</b></li>
     *   <li>EPSG CRS name: <b>POSGAR 94</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6694</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 94")
    public void EPSG_4929() throws FactoryException {
        code  = 4929;
        name  = "POSGAR 94";
        datum = 6694;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “POSGAR 94” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4928</b></li>
     *   <li>EPSG CRS name: <b>POSGAR 94</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6694</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 94")
    public void EPSG_4928() throws FactoryException {
        code  = 4928;
        name  = "POSGAR 94";
        isGeocentric = true;
        datum = 6694;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “POSGAR 98” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4190</b></li>
     *   <li>EPSG CRS name: <b>POSGAR 98</b></li>
     *   <li>Alias(es) given by EPSG: <b>National Geodetic System [Argentina]</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6190</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 98")
    public void EPSG_4190() throws FactoryException {
        code  = 4190;
        name  = "POSGAR 98";
        datum = 6190;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “POSGAR 98” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4961</b></li>
     *   <li>EPSG CRS name: <b>POSGAR 98</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6190</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 98")
    public void EPSG_4961() throws FactoryException {
        code  = 4961;
        name  = "POSGAR 98";
        datum = 6190;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “POSGAR 98” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4960</b></li>
     *   <li>EPSG CRS name: <b>POSGAR 98</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6190</b></li>
     *   <li>EPSG Usage Extent: <b>Argentina</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("POSGAR 98")
    public void EPSG_4960() throws FactoryException {
        code  = 4960;
        name  = "POSGAR 98";
        isGeocentric = true;
        datum = 6190;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “PRS92” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4683</b></li>
     *   <li>EPSG CRS name: <b>PRS92</b></li>
     *   <li>Alias(es) given by EPSG: <b>New Luzon</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6683</b></li>
     *   <li>EPSG Usage Extent: <b>Philippines</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("PRS92")
    public void EPSG_4683() throws FactoryException {
        code  = 4683;
        name  = "PRS92";
        datum = 6683;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “PRS92” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4995</b></li>
     *   <li>EPSG CRS name: <b>PRS92</b></li>
     *   <li>Alias(es) given by EPSG: <b>New Luzon</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6683</b></li>
     *   <li>EPSG Usage Extent: <b>Philippines</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("PRS92")
    public void EPSG_4995() throws FactoryException {
        code  = 4995;
        name  = "PRS92";
        datum = 6683;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “PRS92” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4994</b></li>
     *   <li>EPSG CRS name: <b>PRS92</b></li>
     *   <li>Alias(es) given by EPSG: <b>New Luzon</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6683</b></li>
     *   <li>EPSG Usage Extent: <b>Philippines</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("PRS92")
    public void EPSG_4994() throws FactoryException {
        code  = 4994;
        name  = "PRS92";
        isGeocentric = true;
        datum = 6683;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “PSAD56” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4248</b></li>
     *   <li>EPSG CRS name: <b>PSAD56</b></li>
     *   <li>Alias(es) given by EPSG: <b>La Canoa</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6248</b></li>
     *   <li>EPSG Usage Extent: <b>South America - PSAD56 by country</b></li>
     * </ul>
     *
     * Remarks: Incorporates La Canoa (CRS code 4247) and within Venezuela (but not beyond) the names La Canoa and PSAD56 are synonymous.
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("PSAD56")
    public void EPSG_4248() throws FactoryException {
        code  = 4248;
        name  = "PSAD56";
        datum = 6248;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “PSD93” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4134</b></li>
     *   <li>EPSG CRS name: <b>PSD93</b></li>
     *   <li>Alias(es) given by EPSG: <b>PDO Survey Datum 1993</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6134</b></li>
     *   <li>EPSG Usage Extent: <b>Oman - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("PSD93")
    public void EPSG_4134() throws FactoryException {
        code  = 4134;
        name  = "PSD93";
        datum = 6134;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Puerto Rico” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4139</b></li>
     *   <li>EPSG CRS name: <b>Puerto Rico</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6139</b></li>
     *   <li>EPSG Usage Extent: <b>Caribbean - Puerto Rico and Virgin Islands - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Puerto Rico")
    public void EPSG_4139() throws FactoryException {
        code  = 4139;
        name  = "Puerto Rico";
        datum = 6139;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Pulkovo 1942” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4284</b></li>
     *   <li>EPSG CRS name: <b>Pulkovo 1942</b></li>
     *   <li>Alias(es) given by EPSG: <b>S-42</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6284</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - FSU onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Pulkovo 1942")
    public void EPSG_4284() throws FactoryException {
        code  = 4284;
        name  = "Pulkovo 1942";
        datum = 6284;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Pulkovo 1942(58)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4179</b></li>
     *   <li>EPSG CRS name: <b>Pulkovo 1942(58)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Pulkovo 1942(56)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6179</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - onshore - eastern - S-42(58)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Pulkovo 1942(58)")
    public void EPSG_4179() throws FactoryException {
        code  = 4179;
        name  = "Pulkovo 1942(58)";
        datum = 6179;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Pulkovo 1942(83)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4178</b></li>
     *   <li>EPSG CRS name: <b>Pulkovo 1942(83)</b></li>
     *   <li>Alias(es) given by EPSG: <b>42/83</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6178</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - onshore - eastern - S-42(83)</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Pulkovo 1942(83)")
    public void EPSG_4178() throws FactoryException {
        code  = 4178;
        name  = "Pulkovo 1942(83)";
        datum = 6178;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Pulkovo 1995” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4200</b></li>
     *   <li>EPSG CRS name: <b>Pulkovo 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>S-95</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6200</b></li>
     *   <li>EPSG Usage Extent: <b>Russia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Pulkovo 1995")
    public void EPSG_4200() throws FactoryException {
        code  = 4200;
        name  = "Pulkovo 1995";
        datum = 6200;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “PZ-90” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4740</b></li>
     *   <li>EPSG CRS name: <b>PZ-90</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6740</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("PZ-90")
    public void EPSG_4740() throws FactoryException {
        code  = 4740;
        name  = "PZ-90";
        datum = 6740;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “PZ-90” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4923</b></li>
     *   <li>EPSG CRS name: <b>PZ-90</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6740</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("PZ-90")
    public void EPSG_4923() throws FactoryException {
        code  = 4923;
        name  = "PZ-90";
        datum = 6740;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “PZ-90” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4922</b></li>
     *   <li>EPSG CRS name: <b>PZ-90</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6740</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("PZ-90")
    public void EPSG_4922() throws FactoryException {
        code  = 4922;
        name  = "PZ-90";
        isGeocentric = true;
        datum = 6740;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Qatar 1948” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4286</b></li>
     *   <li>EPSG CRS name: <b>Qatar 1948</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6286</b></li>
     *   <li>EPSG Usage Extent: <b>Qatar - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Qatar 1948")
    public void EPSG_4286() throws FactoryException {
        code  = 4286;
        name  = "Qatar 1948";
        datum = 6286;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Qatar 1974” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4285</b></li>
     *   <li>EPSG CRS name: <b>Qatar 1974</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6285</b></li>
     *   <li>EPSG Usage Extent: <b>Qatar</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Qatar 1974")
    public void EPSG_4285() throws FactoryException {
        code  = 4285;
        name  = "Qatar 1974";
        datum = 6285;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “QND95” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4614</b></li>
     *   <li>EPSG CRS name: <b>QND95</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6614</b></li>
     *   <li>EPSG Usage Extent: <b>Qatar - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("QND95")
    public void EPSG_4614() throws FactoryException {
        code  = 4614;
        name  = "QND95";
        datum = 6614;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Qornoq 1927” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4194</b></li>
     *   <li>EPSG CRS name: <b>Qornoq 1927</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6194</b></li>
     *   <li>EPSG Usage Extent: <b>Greenland - west coast</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Qornoq 1927")
    public void EPSG_4194() throws FactoryException {
        code  = 4194;
        name  = "Qornoq 1927";
        datum = 6194;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Rassadiran” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4153</b></li>
     *   <li>EPSG CRS name: <b>Rassadiran</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6153</b></li>
     *   <li>EPSG Usage Extent: <b>Iran - Taheri refinery</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Rassadiran")
    public void EPSG_4153() throws FactoryException {
        code  = 4153;
        name  = "Rassadiran";
        datum = 6153;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “RD/83” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4745</b></li>
     *   <li>EPSG CRS name: <b>RD/83</b></li>
     *   <li>Alias(es) given by EPSG: <b>DHDN</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6745</b></li>
     *   <li>EPSG Usage Extent: <b>Germany - Saxony</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RD/83")
    public void EPSG_4745() throws FactoryException {
        code  = 4745;
        name  = "RD/83";
        datum = 6745;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “REGVEN” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4189</b></li>
     *   <li>EPSG CRS name: <b>REGVEN</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6189</b></li>
     *   <li>EPSG Usage Extent: <b>Venezuela</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("REGVEN")
    public void EPSG_4189() throws FactoryException {
        code  = 4189;
        name  = "REGVEN";
        datum = 6189;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “REGVEN” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4963</b></li>
     *   <li>EPSG CRS name: <b>REGVEN</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6189</b></li>
     *   <li>EPSG Usage Extent: <b>Venezuela</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("REGVEN")
    public void EPSG_4963() throws FactoryException {
        code  = 4963;
        name  = "REGVEN";
        datum = 6189;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “REGVEN” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4962</b></li>
     *   <li>EPSG CRS name: <b>REGVEN</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6189</b></li>
     *   <li>EPSG Usage Extent: <b>Venezuela</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("REGVEN")
    public void EPSG_4962() throws FactoryException {
        code  = 4962;
        name  = "REGVEN";
        isGeocentric = true;
        datum = 6189;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Reunion 1947” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4626</b></li>
     *   <li>EPSG CRS name: <b>Reunion 1947</b></li>
     *   <li>Alias(es) given by EPSG: <b>Piton des Neiges</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6626</b></li>
     *   <li>EPSG Usage Extent: <b>Reunion - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Reunion 1947")
    public void EPSG_4626() throws FactoryException {
        code  = 4626;
        name  = "Reunion 1947";
        datum = 6626;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Reykjavik 1900” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4657</b></li>
     *   <li>EPSG CRS name: <b>Reykjavik 1900</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6657</b></li>
     *   <li>EPSG Usage Extent: <b>Iceland - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Reykjavik 1900")
    public void EPSG_4657() throws FactoryException {
        code  = 4657;
        name  = "Reykjavik 1900";
        datum = 6657;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “RGF93” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4171</b></li>
     *   <li>EPSG CRS name: <b>RGF93</b></li>
     *   <li>Alias(es) given by EPSG: <b>RGF93 (lat-lon)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6171</b></li>
     *   <li>EPSG Usage Extent: <b>France</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RGF93")
    public void EPSG_4171() throws FactoryException {
        code  = 4171;
        name  = "RGF93";
        datum = 6171;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “RGF94” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4965</b></li>
     *   <li>EPSG CRS name: <b>RGF94</b></li>
     *   <li>Alias(es) given by EPSG: <b>RGF93 (lat-lon)</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6171</b></li>
     *   <li>EPSG Usage Extent: <b>France</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RGF94")
    public void EPSG_4965() throws FactoryException {
        code  = 4965;
        name  = "RGF94";
        datum = 6171;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “RGF95” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4964</b></li>
     *   <li>EPSG CRS name: <b>RGF95</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6171</b></li>
     *   <li>EPSG Usage Extent: <b>France</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RGF95")
    public void EPSG_4964() throws FactoryException {
        code  = 4964;
        name  = "RGF95";
        isGeocentric = true;
        datum = 6171;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “RGFG95” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4624</b></li>
     *   <li>EPSG CRS name: <b>RGFG95</b></li>
     *   <li>Alias(es) given by EPSG: <b>RGFG95 (lat-lon)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6624</b></li>
     *   <li>EPSG Usage Extent: <b>French Guiana</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RGFG95")
    public void EPSG_4624() throws FactoryException {
        code  = 4624;
        name  = "RGFG95";
        datum = 6624;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “RGFG95” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4967</b></li>
     *   <li>EPSG CRS name: <b>RGFG95</b></li>
     *   <li>Alias(es) given by EPSG: <b>RGFG95 (lat-lon)</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6624</b></li>
     *   <li>EPSG Usage Extent: <b>French Guiana</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RGFG95")
    public void EPSG_4967() throws FactoryException {
        code  = 4967;
        name  = "RGFG95";
        datum = 6624;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “RGFG95” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4966</b></li>
     *   <li>EPSG CRS name: <b>RGFG95</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6624</b></li>
     *   <li>EPSG Usage Extent: <b>French Guiana</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RGFG95")
    public void EPSG_4966() throws FactoryException {
        code  = 4966;
        name  = "RGFG95";
        isGeocentric = true;
        datum = 6624;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “RGNC91-93” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4749</b></li>
     *   <li>EPSG CRS name: <b>RGNC91-93</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6749</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RGNC91-93")
    public void EPSG_4749() throws FactoryException {
        code  = 4749;
        name  = "RGNC91-93";
        datum = 6749;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “RGNC91-93” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4907</b></li>
     *   <li>EPSG CRS name: <b>RGNC91-93</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6749</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RGNC91-93")
    public void EPSG_4907() throws FactoryException {
        code  = 4907;
        name  = "RGNC91-93";
        datum = 6749;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “RGNC91-93” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4906</b></li>
     *   <li>EPSG CRS name: <b>RGNC91-93</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6749</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RGNC91-93")
    public void EPSG_4906() throws FactoryException {
        code  = 4906;
        name  = "RGNC91-93";
        isGeocentric = true;
        datum = 6749;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “RGPF” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4687</b></li>
     *   <li>EPSG CRS name: <b>RGPF</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6687</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RGPF")
    public void EPSG_4687() throws FactoryException {
        code  = 4687;
        name  = "RGPF";
        datum = 6687;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “RGPF” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4999</b></li>
     *   <li>EPSG CRS name: <b>RGPF</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6687</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RGPF")
    public void EPSG_4999() throws FactoryException {
        code  = 4999;
        name  = "RGPF";
        datum = 6687;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “RGPF” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4998</b></li>
     *   <li>EPSG CRS name: <b>RGPF</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6687</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RGPF")
    public void EPSG_4998() throws FactoryException {
        code  = 4998;
        name  = "RGPF";
        isGeocentric = true;
        datum = 6687;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “RGR92” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4627</b></li>
     *   <li>EPSG CRS name: <b>RGR92</b></li>
     *   <li>Alias(es) given by EPSG: <b>RGR92 (lat-lon)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6627</b></li>
     *   <li>EPSG Usage Extent: <b>Reunion</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RGR92")
    public void EPSG_4627() throws FactoryException {
        code  = 4627;
        name  = "RGR92";
        datum = 6627;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “RGR92” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4971</b></li>
     *   <li>EPSG CRS name: <b>RGR92</b></li>
     *   <li>Alias(es) given by EPSG: <b>RGR92 (lat-lon)</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6627</b></li>
     *   <li>EPSG Usage Extent: <b>Reunion</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RGR92")
    public void EPSG_4971() throws FactoryException {
        code  = 4971;
        name  = "RGR92";
        datum = 6627;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “RGR92” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4970</b></li>
     *   <li>EPSG CRS name: <b>RGR92</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6627</b></li>
     *   <li>EPSG Usage Extent: <b>Reunion</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RGR92")
    public void EPSG_4970() throws FactoryException {
        code  = 4970;
        name  = "RGR92";
        isGeocentric = true;
        datum = 6627;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “RRAF 1991” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4558</b></li>
     *   <li>EPSG CRS name: <b>RRAF 1991</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>1047</b></li>
     *   <li>EPSG Usage Extent: <b>Caribbean - French Antilles</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RRAF 1991")
    public void EPSG_4558() throws FactoryException {
        code  = 4558;
        name  = "RRAF 1991";
        datum = 1047;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “RSRGD2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4764</b></li>
     *   <li>EPSG CRS name: <b>RSRGD2000</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6764</b></li>
     *   <li>EPSG Usage Extent: <b>Antarctica - Ross Sea Region</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RSRGD2000")
    public void EPSG_4764() throws FactoryException {
        code  = 4764;
        name  = "RSRGD2000";
        datum = 6764;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “RSRGD2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4885</b></li>
     *   <li>EPSG CRS name: <b>RSRGD2000</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6764</b></li>
     *   <li>EPSG Usage Extent: <b>Antarctica - Ross Sea Region</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RSRGD2000")
    public void EPSG_4885() throws FactoryException {
        code  = 4885;
        name  = "RSRGD2000";
        datum = 6764;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “RSRGD2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4884</b></li>
     *   <li>EPSG CRS name: <b>RSRGD2000</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6764</b></li>
     *   <li>EPSG Usage Extent: <b>Antarctica - Ross Sea Region</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RSRGD2000")
    public void EPSG_4884() throws FactoryException {
        code  = 4884;
        name  = "RSRGD2000";
        isGeocentric = true;
        datum = 6764;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “RT38” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4308</b></li>
     *   <li>EPSG CRS name: <b>RT38</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6308</b></li>
     *   <li>EPSG Usage Extent: <b>Sweden - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RT38")
    public void EPSG_4308() throws FactoryException {
        code  = 4308;
        name  = "RT38";
        datum = 6308;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “RT38 (Stockholm)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4814</b></li>
     *   <li>EPSG CRS name: <b>RT38 (Stockholm)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6814</b></li>
     *   <li>EPSG Usage Extent: <b>Sweden - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RT38 (Stockholm)")
    public void EPSG_4814() throws FactoryException {
        code  = 4814;
        name  = "RT38 (Stockholm)";
        datum = 6814;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “RT90” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4124</b></li>
     *   <li>EPSG CRS name: <b>RT90</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6124</b></li>
     *   <li>EPSG Usage Extent: <b>Sweden</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("RT90")
    public void EPSG_4124() throws FactoryException {
        code  = 4124;
        name  = "RT90";
        datum = 6124;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “S-JTSK” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4156</b></li>
     *   <li>EPSG CRS name: <b>S-JTSK</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6156</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Czechoslovakia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("S-JTSK")
    public void EPSG_4156() throws FactoryException {
        code  = 4156;
        name  = "S-JTSK";
        datum = 6156;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “S-JTSK (Ferro)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4818</b></li>
     *   <li>EPSG CRS name: <b>S-JTSK (Ferro)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6818</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Czechoslovakia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("S-JTSK (Ferro)")
    public void EPSG_4818() throws FactoryException {
        code  = 4818;
        name  = "S-JTSK (Ferro)";
        datum = 6818;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “SAD69” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4618</b></li>
     *   <li>EPSG CRS name: <b>SAD69</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6618</b></li>
     *   <li>EPSG Usage Extent: <b>South America - SAD69 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("SAD69")
    public void EPSG_4618() throws FactoryException {
        code  = 4618;
        name  = "SAD69";
        datum = 6618;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Saint Pierre et Miquelon 1950” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4638</b></li>
     *   <li>EPSG CRS name: <b>Saint Pierre et Miquelon 1950</b></li>
     *   <li>Alias(es) given by EPSG: <b>St Pierre Miquelon 1950</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6638</b></li>
     *   <li>EPSG Usage Extent: <b>St Pierre and Miquelon - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Saint Pierre et Miquelon 1950")
    public void EPSG_4638() throws FactoryException {
        code  = 4638;
        name  = "Saint Pierre et Miquelon 1950";
        datum = 6638;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Santo 1965” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4730</b></li>
     *   <li>EPSG CRS name: <b>Santo 1965</b></li>
     *   <li>Alias(es) given by EPSG: <b>Santo (DOS)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6730</b></li>
     *   <li>EPSG Usage Extent: <b>Vanuatu - northern islands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Santo 1965")
    public void EPSG_4730() throws FactoryException {
        code  = 4730;
        name  = "Santo 1965";
        datum = 6730;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Sapper Hill 1943” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4292</b></li>
     *   <li>EPSG CRS name: <b>Sapper Hill 1943</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6292</b></li>
     *   <li>EPSG Usage Extent: <b>Falkland Islands - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Sapper Hill 1943")
    public void EPSG_4292() throws FactoryException {
        code  = 4292;
        name  = "Sapper Hill 1943";
        datum = 6292;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Schwarzeck” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4293</b></li>
     *   <li>EPSG CRS name: <b>Schwarzeck</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6293</b></li>
     *   <li>EPSG Usage Extent: <b>Namibia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Schwarzeck")
    public void EPSG_4293() throws FactoryException {
        code  = 4293;
        name  = "Schwarzeck";
        datum = 6293;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Scoresbysund 1952” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4195</b></li>
     *   <li>EPSG CRS name: <b>Scoresbysund 1952</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6195</b></li>
     *   <li>EPSG Usage Extent: <b>Greenland - Scoresbysund area</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Scoresbysund 1952")
    public void EPSG_4195() throws FactoryException {
        code  = 4195;
        name  = "Scoresbysund 1952";
        datum = 6195;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Segara” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4613</b></li>
     *   <li>EPSG CRS name: <b>Segara</b></li>
     *   <li>Alias(es) given by EPSG: <b>Samboja</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6613</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Kalimantan E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Segara")
    public void EPSG_4613() throws FactoryException {
        code  = 4613;
        name  = "Segara";
        datum = 6613;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Segara (Jakarta)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4820</b></li>
     *   <li>EPSG CRS name: <b>Segara (Jakarta)</b></li>
     *   <li>Alias(es) given by EPSG: <b>Samboja (Jakarta)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6820</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Kalimantan E</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Segara (Jakarta)")
    public void EPSG_4820() throws FactoryException {
        code  = 4820;
        name  = "Segara (Jakarta)";
        datum = 6820;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Selvagem Grande” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4616</b></li>
     *   <li>EPSG CRS name: <b>Selvagem Grande</b></li>
     *   <li>Alias(es) given by EPSG: <b>Marco Astro</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6616</b></li>
     *   <li>EPSG Usage Extent: <b>Portugal - Selvagens onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Selvagem Grande")
    public void EPSG_4616() throws FactoryException {
        code  = 4616;
        name  = "Selvagem Grande";
        datum = 6616;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Serindung” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4295</b></li>
     *   <li>EPSG CRS name: <b>Serindung</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6295</b></li>
     *   <li>EPSG Usage Extent: <b>Indonesia - Kalimantan W - coastal</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Serindung")
    public void EPSG_4295() throws FactoryException {
        code  = 4295;
        name  = "Serindung";
        datum = 6295;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Sierra Leone 1924” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4174</b></li>
     *   <li>EPSG CRS name: <b>Sierra Leone 1924</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6174</b></li>
     *   <li>EPSG Usage Extent: <b>Sierra Leone - Freetown Peninsula</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Sierra Leone 1924")
    public void EPSG_4174() throws FactoryException {
        code  = 4174;
        name  = "Sierra Leone 1924";
        datum = 6174;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Sierra Leone 1968” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4175</b></li>
     *   <li>EPSG CRS name: <b>Sierra Leone 1968</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6175</b></li>
     *   <li>EPSG Usage Extent: <b>Sierra Leone - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Sierra Leone 1968")
    public void EPSG_4175() throws FactoryException {
        code  = 4175;
        name  = "Sierra Leone 1968";
        datum = 6175;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “SIGD61” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4726</b></li>
     *   <li>EPSG CRS name: <b>SIGD61</b></li>
     *   <li>Alias(es) given by EPSG: <b>Little Cayman 1961</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6726</b></li>
     *   <li>EPSG Usage Extent: <b>Cayman Islands - Little Cayman and Cayman Brac</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("SIGD61")
    public void EPSG_4726() throws FactoryException {
        code  = 4726;
        name  = "SIGD61";
        datum = 6726;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “SIRGAS 1995” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4170</b></li>
     *   <li>EPSG CRS name: <b>SIRGAS 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>SIRGAS</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6170</b></li>
     *   <li>EPSG Usage Extent: <b>South America - SIRGAS 1995 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("SIRGAS 1995")
    public void EPSG_4170() throws FactoryException {
        code  = 4170;
        name  = "SIRGAS 1995";
        datum = 6170;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “SIRGAS 1995” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4975</b></li>
     *   <li>EPSG CRS name: <b>SIRGAS 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>SIRGAS</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6170</b></li>
     *   <li>EPSG Usage Extent: <b>South America - SIRGAS 1995 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("SIRGAS 1995")
    public void EPSG_4975() throws FactoryException {
        code  = 4975;
        name  = "SIRGAS 1995";
        datum = 6170;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “SIRGAS 1995” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4974</b></li>
     *   <li>EPSG CRS name: <b>SIRGAS 1995</b></li>
     *   <li>Alias(es) given by EPSG: <b>SIRGAS</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6170</b></li>
     *   <li>EPSG Usage Extent: <b>South America - SIRGAS 1995 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("SIRGAS 1995")
    public void EPSG_4974() throws FactoryException {
        code  = 4974;
        name  = "SIRGAS 1995";
        isGeocentric = true;
        datum = 6170;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “SIRGAS 2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4674</b></li>
     *   <li>EPSG CRS name: <b>SIRGAS 2000</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6674</b></li>
     *   <li>EPSG Usage Extent: <b>Latin America - SIRGAS 2000 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("SIRGAS 2000")
    public void EPSG_4674() throws FactoryException {
        code  = 4674;
        name  = "SIRGAS 2000";
        datum = 6674;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “SIRGAS 2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4989</b></li>
     *   <li>EPSG CRS name: <b>SIRGAS 2000</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6674</b></li>
     *   <li>EPSG Usage Extent: <b>Latin America - SIRGAS 2000 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("SIRGAS 2000")
    public void EPSG_4989() throws FactoryException {
        code  = 4989;
        name  = "SIRGAS 2000";
        datum = 6674;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “SIRGAS 2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4988</b></li>
     *   <li>EPSG CRS name: <b>SIRGAS 2000</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6674</b></li>
     *   <li>EPSG Usage Extent: <b>Latin America - SIRGAS 2000 by country</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("SIRGAS 2000")
    public void EPSG_4988() throws FactoryException {
        code  = 4988;
        name  = "SIRGAS 2000";
        isGeocentric = true;
        datum = 6674;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Slovenia 1996” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4765</b></li>
     *   <li>EPSG CRS name: <b>Slovenia 1996</b></li>
     *   <li>Alias(es) given by EPSG: <b>D96</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6765</b></li>
     *   <li>EPSG Usage Extent: <b>Slovenia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Slovenia 1996")
    public void EPSG_4765() throws FactoryException {
        code  = 4765;
        name  = "Slovenia 1996";
        datum = 6765;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Slovenia 1996” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4883</b></li>
     *   <li>EPSG CRS name: <b>Slovenia 1996</b></li>
     *   <li>Alias(es) given by EPSG: <b>D96</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6765</b></li>
     *   <li>EPSG Usage Extent: <b>Slovenia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Slovenia 1996")
    public void EPSG_4883() throws FactoryException {
        code  = 4883;
        name  = "Slovenia 1996";
        datum = 6765;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “Slovenia 1996” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4882</b></li>
     *   <li>EPSG CRS name: <b>Slovenia 1996</b></li>
     *   <li>Alias(es) given by EPSG: <b>D96</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6765</b></li>
     *   <li>EPSG Usage Extent: <b>Slovenia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Slovenia 1996")
    public void EPSG_4882() throws FactoryException {
        code  = 4882;
        name  = "Slovenia 1996";
        isGeocentric = true;
        datum = 6765;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Solomon 1968” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4718</b></li>
     *   <li>EPSG CRS name: <b>Solomon 1968</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6718</b></li>
     *   <li>EPSG Usage Extent: <b>Solomon Islands - onshore main islands</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Solomon 1968")
    public void EPSG_4718() throws FactoryException {
        code  = 4718;
        name  = "Solomon 1968";
        datum = 6718;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “South Georgia 1968” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4722</b></li>
     *   <li>EPSG CRS name: <b>South Georgia 1968</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6722</b></li>
     *   <li>EPSG Usage Extent: <b>South Georgia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("South Georgia 1968")
    public void EPSG_4722() throws FactoryException {
        code  = 4722;
        name  = "South Georgia 1968";
        datum = 6722;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “South Yemen” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4164</b></li>
     *   <li>EPSG CRS name: <b>South Yemen</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6164</b></li>
     *   <li>EPSG Usage Extent: <b>Yemen - South Yemen - mainland</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("South Yemen")
    public void EPSG_4164() throws FactoryException {
        code  = 4164;
        name  = "South Yemen";
        datum = 6164;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “St. George Island” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4138</b></li>
     *   <li>EPSG CRS name: <b>St. George Island</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6138</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - St. George Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("St. George Island")
    public void EPSG_4138() throws FactoryException {
        code  = 4138;
        name  = "St. George Island";
        datum = 6138;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “St. Kitts 1955” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4605</b></li>
     *   <li>EPSG CRS name: <b>St. Kitts 1955</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6605</b></li>
     *   <li>EPSG Usage Extent: <b>St Kitts and Nevis - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("St. Kitts 1955")
    public void EPSG_4605() throws FactoryException {
        code  = 4605;
        name  = "St. Kitts 1955";
        datum = 6605;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “St. Lawrence Island” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4136</b></li>
     *   <li>EPSG CRS name: <b>St. Lawrence Island</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6136</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - St. Lawrence Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("St. Lawrence Island")
    public void EPSG_4136() throws FactoryException {
        code  = 4136;
        name  = "St. Lawrence Island";
        datum = 6136;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “St. Lucia 1955” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4606</b></li>
     *   <li>EPSG CRS name: <b>St. Lucia 1955</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6606</b></li>
     *   <li>EPSG Usage Extent: <b>St Lucia - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("St. Lucia 1955")
    public void EPSG_4606() throws FactoryException {
        code  = 4606;
        name  = "St. Lucia 1955";
        datum = 6606;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “St. Paul Island” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4137</b></li>
     *   <li>EPSG CRS name: <b>St. Paul Island</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6137</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Alaska - St. Paul Island</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("St. Paul Island")
    public void EPSG_4137() throws FactoryException {
        code  = 4137;
        name  = "St. Paul Island";
        datum = 6137;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “St. Vincent 1945” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4607</b></li>
     *   <li>EPSG CRS name: <b>St. Vincent 1945</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6607</b></li>
     *   <li>EPSG Usage Extent: <b>St Vincent and the Grenadines - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("St. Vincent 1945")
    public void EPSG_4607() throws FactoryException {
        code  = 4607;
        name  = "St. Vincent 1945";
        datum = 6607;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “ST71 Belep” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4643</b></li>
     *   <li>EPSG CRS name: <b>ST71 Belep</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6643</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia - Belep</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ST71 Belep")
    public void EPSG_4643() throws FactoryException {
        code  = 4643;
        name  = "ST71 Belep";
        datum = 6643;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “ST84 Ile des Pins” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4642</b></li>
     *   <li>EPSG CRS name: <b>ST84 Ile des Pins</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6642</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia - Ile des Pins</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ST84 Ile des Pins")
    public void EPSG_4642() throws FactoryException {
        code  = 4642;
        name  = "ST84 Ile des Pins";
        datum = 6642;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “ST87 Ouvea” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4750</b></li>
     *   <li>EPSG CRS name: <b>ST87 Ouvea</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6750</b></li>
     *   <li>EPSG Usage Extent: <b>New Caledonia - Ouvea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("ST87 Ouvea")
    public void EPSG_4750() throws FactoryException {
        code  = 4750;
        name  = "ST87 Ouvea";
        datum = 6750;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “SVY21” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4757</b></li>
     *   <li>EPSG CRS name: <b>SVY21</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6757</b></li>
     *   <li>EPSG Usage Extent: <b>Singapore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("SVY21")
    public void EPSG_4757() throws FactoryException {
        code  = 4757;
        name  = "SVY21";
        datum = 6757;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “SWEREF99” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4619</b></li>
     *   <li>EPSG CRS name: <b>SWEREF99</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6619</b></li>
     *   <li>EPSG Usage Extent: <b>Sweden</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("SWEREF99")
    public void EPSG_4619() throws FactoryException {
        code  = 4619;
        name  = "SWEREF99";
        datum = 6619;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “SWEREF99” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4977</b></li>
     *   <li>EPSG CRS name: <b>SWEREF99</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6619</b></li>
     *   <li>EPSG Usage Extent: <b>Sweden</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("SWEREF99")
    public void EPSG_4977() throws FactoryException {
        code  = 4977;
        name  = "SWEREF99";
        datum = 6619;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “SWEREF99” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4976</b></li>
     *   <li>EPSG CRS name: <b>SWEREF99</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6619</b></li>
     *   <li>EPSG Usage Extent: <b>Sweden</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("SWEREF99")
    public void EPSG_4976() throws FactoryException {
        code  = 4976;
        name  = "SWEREF99";
        isGeocentric = true;
        datum = 6619;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Tahaa 54” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4629</b></li>
     *   <li>EPSG CRS name: <b>Tahaa 54</b></li>
     *   <li>Alias(es) given by EPSG: <b>Tahaa</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6629</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia - Society Islands - Bora Bora; Huahine; Raiatea; Tahaa</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Tahaa 54")
    public void EPSG_4629() throws FactoryException {
        code  = 4629;
        name  = "Tahaa 54";
        datum = 6629;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Tahiti 52” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4628</b></li>
     *   <li>EPSG CRS name: <b>Tahiti 52</b></li>
     *   <li>Alias(es) given by EPSG: <b>Tahiti</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6628</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia - Society Islands - Moorea and Tahiti</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Tahiti 52")
    public void EPSG_4628() throws FactoryException {
        code  = 4628;
        name  = "Tahiti 52";
        datum = 6628;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Tahiti 79” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4690</b></li>
     *   <li>EPSG CRS name: <b>Tahiti 79</b></li>
     *   <li>Alias(es) given by EPSG: <b>IGN79 Tahiti</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6690</b></li>
     *   <li>EPSG Usage Extent: <b>French Polynesia - Society Islands - Tahiti</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Tahiti 79")
    public void EPSG_4690() throws FactoryException {
        code  = 4690;
        name  = "Tahiti 79";
        datum = 6690;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Tananarive” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4297</b></li>
     *   <li>EPSG CRS name: <b>Tananarive</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6297</b></li>
     *   <li>EPSG Usage Extent: <b>Madagascar - onshore and nearshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Tananarive")
    public void EPSG_4297() throws FactoryException {
        code  = 4297;
        name  = "Tananarive";
        datum = 6297;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Tananarive (Paris)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4810</b></li>
     *   <li>EPSG CRS name: <b>Tananarive (Paris)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6810</b></li>
     *   <li>EPSG Usage Extent: <b>Madagascar - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Tananarive (Paris)")
    public void EPSG_4810() throws FactoryException {
        code  = 4810;
        name  = "Tananarive (Paris)";
        datum = 6810;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “TC(1948)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4303</b></li>
     *   <li>EPSG CRS name: <b>TC(1948)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6303</b></li>
     *   <li>EPSG Usage Extent: <b>UAE - Abu Dhabi and Dubai - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("TC(1948)")
    public void EPSG_4303() throws FactoryException {
        code  = 4303;
        name  = "TC(1948)";
        datum = 6303;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Tern Island 1961” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4707</b></li>
     *   <li>EPSG CRS name: <b>Tern Island 1961</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6707</b></li>
     *   <li>EPSG Usage Extent: <b>USA - Hawaii - Tern Island and Sorel Atoll</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Tern Island 1961")
    public void EPSG_4707() throws FactoryException {
        code  = 4707;
        name  = "Tern Island 1961";
        datum = 6707;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Tete” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4127</b></li>
     *   <li>EPSG CRS name: <b>Tete</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6127</b></li>
     *   <li>EPSG Usage Extent: <b>Mozambique - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Tete")
    public void EPSG_4127() throws FactoryException {
        code  = 4127;
        name  = "Tete";
        datum = 6127;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Timbalai 1948” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4298</b></li>
     *   <li>EPSG CRS name: <b>Timbalai 1948</b></li>
     *   <li>Alias(es) given by EPSG: <b>Timbalai 1968</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6298</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Brunei and East Malaysia</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Timbalai 1948")
    public void EPSG_4298() throws FactoryException {
        code  = 4298;
        name  = "Timbalai 1948";
        datum = 6298;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “TM65” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4299</b></li>
     *   <li>EPSG CRS name: <b>TM65</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6299</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Ireland (Republic and Ulster) - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("TM65")
    public void EPSG_4299() throws FactoryException {
        code  = 4299;
        name  = "TM65";
        datum = 6299;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “TM75” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4300</b></li>
     *   <li>EPSG CRS name: <b>TM75</b></li>
     *   <li>Alias(es) given by EPSG: <b>1975 Mapping Adjustment</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6300</b></li>
     *   <li>EPSG Usage Extent: <b>Europe - Ireland (Republic and Ulster) - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("TM75")
    public void EPSG_4300() throws FactoryException {
        code  = 4300;
        name  = "TM75";
        datum = 6300;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Tokyo” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4301</b></li>
     *   <li>EPSG CRS name: <b>Tokyo</b></li>
     *   <li>Alias(es) given by EPSG: <b>Tokyo 1918</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6301</b></li>
     *   <li>EPSG Usage Extent: <b>Asia - Japan and Korea</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Tokyo")
    public void EPSG_4301() throws FactoryException {
        code  = 4301;
        name  = "Tokyo";
        datum = 6301;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Trinidad 1903” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4302</b></li>
     *   <li>EPSG CRS name: <b>Trinidad 1903</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6302</b></li>
     *   <li>EPSG Usage Extent: <b>Trinidad and Tobago - Trinidad</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Trinidad 1903")
    public void EPSG_4302() throws FactoryException {
        code  = 4302;
        name  = "Trinidad 1903";
        datum = 6302;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Tristan 1968” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4734</b></li>
     *   <li>EPSG CRS name: <b>Tristan 1968</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6734</b></li>
     *   <li>EPSG Usage Extent: <b>St Helena - Tristan da Cunha</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Tristan 1968")
    public void EPSG_4734() throws FactoryException {
        code  = 4734;
        name  = "Tristan 1968";
        datum = 6734;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “TWD67” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>3821</b></li>
     *   <li>EPSG CRS name: <b>TWD67</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>1025</b></li>
     *   <li>EPSG Usage Extent: <b>Taiwan - onshore - mainland and Penghu</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("TWD67")
    public void EPSG_3821() throws FactoryException {
        code  = 3821;
        name  = "TWD67";
        datum = 1025;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “TWD97” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>3824</b></li>
     *   <li>EPSG CRS name: <b>TWD97</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>1026</b></li>
     *   <li>EPSG Usage Extent: <b>Taiwan</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("TWD97")
    public void EPSG_3824() throws FactoryException {
        code  = 3824;
        name  = "TWD97";
        datum = 1026;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “TWD97” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>3823</b></li>
     *   <li>EPSG CRS name: <b>TWD97</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>1026</b></li>
     *   <li>EPSG Usage Extent: <b>Taiwan</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("TWD97")
    public void EPSG_3823() throws FactoryException {
        code  = 3823;
        name  = "TWD97";
        datum = 1026;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “TWD97” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>3822</b></li>
     *   <li>EPSG CRS name: <b>TWD97</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>1026</b></li>
     *   <li>EPSG Usage Extent: <b>Taiwan</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("TWD97")
    public void EPSG_3822() throws FactoryException {
        code  = 3822;
        name  = "TWD97";
        isGeocentric = true;
        datum = 1026;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Vanua Levu 1915” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4748</b></li>
     *   <li>EPSG CRS name: <b>Vanua Levu 1915</b></li>
     *   <li>Alias(es) given by EPSG: <b>Vanua Levu 1917</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6748</b></li>
     *   <li>EPSG Usage Extent: <b>Fiji - Vanua Levu and Taveuni</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Vanua Levu 1915")
    public void EPSG_4748() throws FactoryException {
        code  = 4748;
        name  = "Vanua Levu 1915";
        datum = 6748;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Vientiane 1982” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4676</b></li>
     *   <li>EPSG CRS name: <b>Vientiane 1982</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6676</b></li>
     *   <li>EPSG Usage Extent: <b>Laos</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Vientiane 1982")
    public void EPSG_4676() throws FactoryException {
        code  = 4676;
        name  = "Vientiane 1982";
        datum = 6676;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Viti Levu 1912” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4752</b></li>
     *   <li>EPSG CRS name: <b>Viti Levu 1912</b></li>
     *   <li>Alias(es) given by EPSG: <b>Viti Levu 1916</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6752</b></li>
     *   <li>EPSG Usage Extent: <b>Fiji - Viti Levu</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Viti Levu 1912")
    public void EPSG_4752() throws FactoryException {
        code  = 4752;
        name  = "Viti Levu 1912";
        datum = 6752;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “VN-2000” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4756</b></li>
     *   <li>EPSG CRS name: <b>VN-2000</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6756</b></li>
     *   <li>EPSG Usage Extent: <b>Vietnam - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("VN-2000")
    public void EPSG_4756() throws FactoryException {
        code  = 4756;
        name  = "VN-2000";
        datum = 6756;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Voirol 1875” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4304</b></li>
     *   <li>EPSG CRS name: <b>Voirol 1875</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6304</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria - north of 32°N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Voirol 1875")
    public void EPSG_4304() throws FactoryException {
        code  = 4304;
        name  = "Voirol 1875";
        datum = 6304;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Voirol 1875 (Paris)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4811</b></li>
     *   <li>EPSG CRS name: <b>Voirol 1875 (Paris)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6811</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria - north of 32°N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Voirol 1875 (Paris)")
    public void EPSG_4811() throws FactoryException {
        code  = 4811;
        name  = "Voirol 1875 (Paris)";
        datum = 6811;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Voirol 1879” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4671</b></li>
     *   <li>EPSG CRS name: <b>Voirol 1879</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6671</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria - north of 32°N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Voirol 1879")
    public void EPSG_4671() throws FactoryException {
        code  = 4671;
        name  = "Voirol 1879";
        datum = 6671;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Voirol 1879 (Paris)” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4821</b></li>
     *   <li>EPSG CRS name: <b>Voirol 1879 (Paris)</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6821</b></li>
     *   <li>EPSG Usage Extent: <b>Algeria - north of 32°N</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Voirol 1879 (Paris)")
    public void EPSG_4821() throws FactoryException {
        code  = 4821;
        name  = "Voirol 1879 (Paris)";
        datum = 6821;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Wake Island 1952” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4733</b></li>
     *   <li>EPSG CRS name: <b>Wake Island 1952</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6733</b></li>
     *   <li>EPSG Usage Extent: <b>Wake - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Wake Island 1952")
    public void EPSG_4733() throws FactoryException {
        code  = 4733;
        name  = "Wake Island 1952";
        datum = 6733;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “WGS 66” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4760</b></li>
     *   <li>EPSG CRS name: <b>WGS 66</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6760</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("WGS 66")
    public void EPSG_4760() throws FactoryException {
        code  = 4760;
        name  = "WGS 66";
        datum = 6760;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “WGS 66” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4891</b></li>
     *   <li>EPSG CRS name: <b>WGS 66</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6760</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("WGS 66")
    public void EPSG_4891() throws FactoryException {
        code  = 4891;
        name  = "WGS 66";
        datum = 6760;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “WGS 66” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4890</b></li>
     *   <li>EPSG CRS name: <b>WGS 66</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6760</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("WGS 66")
    public void EPSG_4890() throws FactoryException {
        code  = 4890;
        name  = "WGS 66";
        isGeocentric = true;
        datum = 6760;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “WGS 72” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4322</b></li>
     *   <li>EPSG CRS name: <b>WGS 72</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6322</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * Remarks: Used by GPS before 1987.
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("WGS 72")
    public void EPSG_4322() throws FactoryException {
        code  = 4322;
        name  = "WGS 72";
        datum = 6322;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “WGS 72” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4985</b></li>
     *   <li>EPSG CRS name: <b>WGS 72</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6322</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("WGS 72")
    public void EPSG_4985() throws FactoryException {
        code  = 4985;
        name  = "WGS 72";
        datum = 6322;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “WGS 72” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4984</b></li>
     *   <li>EPSG CRS name: <b>WGS 72</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6322</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("WGS 72")
    public void EPSG_4984() throws FactoryException {
        code  = 4984;
        name  = "WGS 72";
        isGeocentric = true;
        datum = 6322;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “WGS 72BE” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4324</b></li>
     *   <li>EPSG CRS name: <b>WGS 72BE</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6324</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("WGS 72BE")
    public void EPSG_4324() throws FactoryException {
        code  = 4324;
        name  = "WGS 72BE";
        datum = 6324;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “WGS 72BE” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4987</b></li>
     *   <li>EPSG CRS name: <b>WGS 72BE</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6324</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("WGS 72BE")
    public void EPSG_4987() throws FactoryException {
        code  = 4987;
        name  = "WGS 72BE";
        datum = 6324;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “WGS 72BE” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4986</b></li>
     *   <li>EPSG CRS name: <b>WGS 72BE</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6324</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("WGS 72BE")
    public void EPSG_4986() throws FactoryException {
        code  = 4986;
        name  = "WGS 72BE";
        isGeocentric = true;
        datum = 6324;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “WGS 84” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4326</b></li>
     *   <li>EPSG CRS name: <b>WGS 84</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6326</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("WGS 84")
    public void EPSG_4326() throws FactoryException {
        code  = 4326;
        name  = "WGS 84";
        datum = 6326;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “WGS 84” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4979</b></li>
     *   <li>EPSG CRS name: <b>WGS 84</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6326</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("WGS 84")
    public void EPSG_4979() throws FactoryException {
        code  = 4979;
        name  = "WGS 84";
        datum = 6326;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “WGS 84” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4978</b></li>
     *   <li>EPSG CRS name: <b>WGS 84</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6326</b></li>
     *   <li>EPSG Usage Extent: <b>World</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("WGS 84")
    public void EPSG_4978() throws FactoryException {
        code  = 4978;
        name  = "WGS 84";
        isGeocentric = true;
        datum = 6326;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Xian 1980” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4610</b></li>
     *   <li>EPSG CRS name: <b>Xian 1980</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6610</b></li>
     *   <li>EPSG Usage Extent: <b>China - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Xian 1980")
    public void EPSG_4610() throws FactoryException {
        code  = 4610;
        name  = "Xian 1980";
        datum = 6610;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Yacare” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4309</b></li>
     *   <li>EPSG CRS name: <b>Yacare</b></li>
     *   <li>Alias(es) given by EPSG: <b>ROU-USAMS</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6309</b></li>
     *   <li>EPSG Usage Extent: <b>Uruguay - onshore</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Yacare")
    public void EPSG_4309() throws FactoryException {
        code  = 4309;
        name  = "Yacare";
        datum = 6309;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Yemen NGN96” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4163</b></li>
     *   <li>EPSG CRS name: <b>Yemen NGN96</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6163</b></li>
     *   <li>EPSG Usage Extent: <b>Yemen</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Yemen NGN96")
    public void EPSG_4163() throws FactoryException {
        code  = 4163;
        name  = "Yemen NGN96";
        datum = 6163;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Yemen NGN96” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4981</b></li>
     *   <li>EPSG CRS name: <b>Yemen NGN96</b></li>
     *   <li>CRS type: <b>Geographic 3D</b></li>
     *   <li>EPSG datum code: <b>6163</b></li>
     *   <li>EPSG Usage Extent: <b>Yemen</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Yemen NGN96")
    public void EPSG_4981() throws FactoryException {
        code  = 4981;
        name  = "Yemen NGN96";
        datum = 6163;
        verifyGeodeticCRS(GEOGRAPHIC_3D);
    }

    /**
     * Tests “Yemen NGN96” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4980</b></li>
     *   <li>EPSG CRS name: <b>Yemen NGN96</b></li>
     *   <li>CRS type: <b>Geocentric</b></li>
     *   <li>EPSG datum code: <b>6163</b></li>
     *   <li>EPSG Usage Extent: <b>Yemen</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Yemen NGN96")
    public void EPSG_4980() throws FactoryException {
        code  = 4980;
        name  = "Yemen NGN96";
        isGeocentric = true;
        datum = 6163;
        verifyGeodeticCRS(GEOCENTRIC);
    }

    /**
     * Tests “Yoff” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4310</b></li>
     *   <li>EPSG CRS name: <b>Yoff</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6310</b></li>
     *   <li>EPSG Usage Extent: <b>Senegal</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Yoff")
    public void EPSG_4310() throws FactoryException {
        code  = 4310;
        name  = "Yoff";
        datum = 6310;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }

    /**
     * Tests “Zanderij” geodetic CRS creation from the factory.
     *
     * <ul>
     *   <li>EPSG CRS code: <b>4311</b></li>
     *   <li>EPSG CRS name: <b>Zanderij</b></li>
     *   <li>CRS type: <b>Geographic 2D</b></li>
     *   <li>EPSG datum code: <b>6311</b></li>
     *   <li>EPSG Usage Extent: <b>Suriname</b></li>
     * </ul>
     *
     * @throws FactoryException if an error occurred while creating the CRS from the EPSG code.
     */
    @Test
    @DisplayName("Zanderij")
    public void EPSG_4311() throws FactoryException {
        code  = 4311;
        name  = "Zanderij";
        datum = 6311;
        verifyGeodeticCRS(GEOGRAPHIC_2D);
    }
}